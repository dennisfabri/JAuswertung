package de.df.jauswertung.util.valueobjects;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jutils.util.StringTools;

/**
 * Writes and parses the codes used for team-member registration. {@code JA00} codes carry no export id, {@code JA01}
 * codes do. Both are Huffman-compressed and hex-encoded.
 */
public final class TeamCode {

    private static final String[] FORBIDDEN = new String[] { "<", ">", ";", "#" };

    private TeamCode() {
    }

    public static String to(Team team) {
        if (team.isExportIdEmpty()) {
            return to00(team);
        }
        return to01(team);
    }

    private static String to00(Team team) {
        StringBuilder sb = header(team);

        StringBuilder sb2 = new StringBuilder();
        int amount = members(team, sb2);

        sb.append(amount);
        sb.append("#");
        sb.append(sb2.toString());
        String fulltext = sb.toString();
        String crc = StringTools.crc(fulltext);
        sb.append(crc);

        String data = sb.toString();
        return "JA00" + StringTools.bytesToHex(compress(data));
    }

    private static String to01(Team team) {
        StringBuilder sb = header(team);

        StringBuilder sb2 = new StringBuilder();
        int amount = members(team, sb2);

        sb.append(amount);
        sb.append(";");
        sb.append(toId(team.getExportId()));
        sb.append("#");
        sb.append(sb2.toString());
        String fulltext = sb.toString();
        String crc = StringTools.crc(fulltext);
        sb.append(crc);

        String data = sb.toString();
        return "JA01" + StringTools.bytesToHex(compress(data));
    }

    private static StringBuilder header(Team team) {
        StringBuilder sb = new StringBuilder();
        sb.append(team.getId());
        sb.append(";");
        sb.append(team.getAk());
        sb.append(";");
        sb.append(team.isMale() ? 1 : 0);
        sb.append(";");
        return sb;
    }

    private static int members(Team team, StringBuilder sb) {
        int amount = 0;
        for (int x = 0; x < team.getMemberCount(); x++) {
            Teammember member = team.getMember(x);
            if (!member.isEmpty()) {
                sb.append(to(member));
                sb.append("#");
                amount++;
            }
        }
        return amount;
    }

    private static String to(Teammember member) {
        return String.join("<", List.of("" + member.getIndex(),
                                        cleanup(member.getLastname()),
                                        cleanup(member.getFirstname()),
                                        geschlechtToIndex(member.getGeschlecht()),
                                        "" + member.getJahrgang(),
                                        cleanup(member.getImportId())));
    }

    private static String geschlechtToIndex(Geschlecht geschlecht) {
        return switch (geschlecht) {
            case weiblich -> "0";
            case maennlich -> "1";
            default -> "2";
        };
    }

    private static String cleanup(String text) {
        for (String s : FORBIDDEN) {
            text = text.replace(s, "");
        }
        return text;
    }

    private static String toId(String id) {
        if (id == null || id.equals("")) {
            return "-";
        }
        return id;
    }

    public static Team from(String code) throws IOException {
        if (code.startsWith("JA00")) {
            return from00(code);
        }
        if (code.startsWith("JA01")) {
            return from01(code);
        }
        System.err.println("Expected 'JA??' but was " + code.substring(0, Math.min(4, code.length())));
        throw new IOException("Expected 'JA??' but was " + code.substring(0, Math.min(4, code.length())));
    }

    private static Team from00(String code) throws IOException {
        String[] parts = checkedParts(code);

        String[] header = parts[0].split(";");
        if (header.length != 4) {
            System.err.println("Expected an header with 4 entries but found " + header.length);
            throw new IOException("Expected an header with 4 entries but found " + header.length);
        }
        int id = Integer.parseInt(header[0]);
        int ak = Integer.parseInt(header[1]);
        int sex = Integer.parseInt(header[2]);
        int amount = Integer.parseInt(header[3]);
        Team team = new Team(id, ak, sex == 1, null, members(parts, amount));
        return checkRoundTrip(code, team);
    }

    private static Team from01(String code) throws IOException {
        String[] parts = checkedParts(code);

        String[] header = parts[0].split(";");
        if (header.length != 5) {
            System.err.println("Expected an header with 5 entries but found " + header.length);
            throw new IOException("Expected an header with 5 entries but found " + header.length);
        }
        int id = Integer.parseInt(header[0]);
        int ak = Integer.parseInt(header[1]);
        int sex = Integer.parseInt(header[2]);
        int amount = Integer.parseInt(header[3]);
        String exportId = header[4];
        if ("-".equals(exportId)) {
            exportId = null;
        }
        Team team = new Team(id, ak, sex == 1, exportId, members(parts, amount));
        return checkRoundTrip(code, team);
    }

    private static String[] checkedParts(String code) throws IOException {
        String value = decompress(StringTools.hexToBytes(code.trim().substring(4))).trim();
        String[] parts = value.split("#");
        if (parts.length < 2) {
            System.err.println("Expected at least 2 rows of data but found " + parts.length);
            throw new IOException("Expected at least 2 rows of data but found " + parts.length);
        }

        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < parts.length - 1; x++) {
            sb.append(parts[x]);
            sb.append("#");
        }
        String fulltext = sb.toString();
        String crc = StringTools.crc(fulltext);
        String crcdata = parts[parts.length - 1].trim();
        if (!crc.equals(crcdata)) {
            System.err.println("Checksums do not match: Excepted " + crc + " but was " + crcdata);
            throw new IOException("Checksums do not match: Excepted " + crc + " but was " + crcdata);
        }
        return parts;
    }

    private static Teammember[] members(String[] parts, int amount) throws IOException {
        if (parts.length != 2 + amount) {
            System.err.println("Expected " + (2 + amount) + " rows of data but found " + parts.length);
            throw new IOException("Expected " + (2 + amount) + " rows of data but found " + parts.length);
        }
        Teammember[] members = new Teammember[amount];
        for (int x = 0; x < amount; x++) {
            members[x] = member(parts[x + 1]);
        }
        return members;
    }

    private static Teammember member(String code) throws IOException {
        String[] parts = code.split("<", -1);
        if (parts.length != 6) {
            throw new IOException("Expected 6 entries but found " + parts.length);
        }
        int index = Integer.parseInt(parts[0]);
        String lastname = parts[1];
        String firstname = parts[2];
        int sex = Integer.parseInt(parts[3]);
        int jg = Integer.parseInt(parts[4]);
        Geschlecht s = switch (sex) {
            case 0 -> Geschlecht.weiblich;
            case 1 -> Geschlecht.maennlich;
            default -> Geschlecht.unbekannt;
        };
        String importId = parts[5];
        return new Teammember(index, firstname, lastname, s, jg, importId);
    }

    private static Team checkRoundTrip(String code, Team team) {
        String created = to(team);
        if (!code.equals(created)) {
            System.err.println("Codes do not match. Expected " + code + " but was " + created);
            throw new IllegalStateException("Codes do not match.");
        }
        return team;
    }

    private static byte[] compress(String data) {
        if (data == null || data.length() == 0) {
            return new byte[0];
        }
        Deflater compresser = new Deflater(Deflater.HUFFMAN_ONLY);
        byte[] output = new byte[1024];
        compresser.setInput(data.getBytes(StandardCharsets.UTF_8));
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        return Arrays.copyOf(output, compressedDataLength);
    }

    private static String decompress(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        Inflater decompresser = new Inflater();
        decompresser.setInput(data);
        byte[] result = new byte[1000];

        try {
            decompresser.inflate(result);
        } catch (DataFormatException e) {
            return null;
        }
        decompresser.end();

        ByteArrayInputStream bis = new ByteArrayInputStream(result);
        Scanner sc = new Scanner(bis, StandardCharsets.UTF_8);
        String outputString = sc.nextLine();
        sc.close();
        return outputString;
    }
}
