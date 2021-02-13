package de.df.jauswertung.util.valueobjects;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.oxbow.swingbits.util.Strings;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jutils.util.StringTools;

public class Team {

    private final int          id;
    private final boolean      male;
    private final int          ak;

    private final String       teamname;
    private final String       gliederung;
    private final String       qualifikationsgliederung;
    private final String       agegroup;

    private final String       exportId;

    private final String       code;

    private final Teammember[] members;

    public Team(Mannschaft m, String exportId) {
        this.id = m.getStartnummer();
        this.teamname = m.getName();
        this.gliederung = m.getGliederung();
        this.qualifikationsgliederung = m.getQualifikationsebene();
        this.male = m.isMaennlich();
        this.agegroup = m.getAK().getName();
        this.ak = m.getAKNummer();
        if (exportId == null || Strings.isEmpty(exportId) || exportId.equals("-")) {
            this.exportId = null;
        } else {
            this.exportId = exportId;
        }

        int count = m.getMaxMembers();
        members = new Teammember[count];
        for (int x = 0; x < count; x++) {
            members[x] = new Teammember(m, x);
        }

        code = createCode();
    }

    private Team(int id, int ak, boolean male, String exportId, Teammember[] members) {
        this.id = id;
        this.ak = ak;
        this.male = male;
        this.members = members;
        this.exportId = exportId == null ? "" : exportId;

        teamname = "";
        gliederung = "";
        qualifikationsgliederung = "";
        agegroup = "";

        code = createCode();

        // try {
        // Team.FromCode(code);
        // } catch (IOException e) {
        // throw new
        // IllegalStateException("Codes do not match. Internal computation error.");
        // }
    }

    public String getExportId() {
        return exportId;
    }

    public boolean isExportIdEmpty() {
        return exportId == null || "".equals(exportId) || "-".equals(exportId);
    }

    private String createCode() {
        if (isExportIdEmpty()) {
            return createCode00();
        }
        return createCode01();
    }

    private String createCode00() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(";");
        sb.append(ak);
        sb.append(";");
        sb.append(male ? 1 : 0);
        sb.append(";");

        StringBuilder sb2 = new StringBuilder();
        int amount = 0;
        for (int x = 0; x < members.length; x++) {
            if (!members[x].isEmpty()) {
                sb2.append(members[x].getCode());
                sb2.append("#");
                amount++;
            }
        }

        sb.append(amount);
        sb.append("#");
        sb.append(sb2.toString());
        String fulltext = sb.toString();
        String crc = StringTools.CRC(fulltext);
        sb.append(crc);

        String data = sb.toString();
        return "JA00" + StringTools.bytesToHex(compress(data));
    }

    private String createCode01() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(";");
        sb.append(ak);
        sb.append(";");
        sb.append(male ? 1 : 0);
        sb.append(";");

        StringBuilder sb2 = new StringBuilder();
        int amount = 0;
        for (int x = 0; x < members.length; x++) {
            if (!members[x].isEmpty()) {
                sb2.append(members[x].getCode());
                sb2.append("#");
                amount++;
            }
        }

        sb.append(amount);
        sb.append(";");
        sb.append(ToId(exportId));
        sb.append("#");
        sb.append(sb2.toString());
        String fulltext = sb.toString();
        String crc = StringTools.CRC(fulltext);
        sb.append(crc);

        String data = sb.toString();
        return "JA01" + StringTools.bytesToHex(compress(data));
    }

    private static String ToId(String id) {
        if (id == null || id.equals("")) {
            return "-";
        }
        return id;
    }

    public static Team FromCode(String code) throws IOException {
        if (code.startsWith("JA00")) {
            return FromCode00(code);
        }
        if (code.startsWith("JA01")) {
            return FromCode01(code);
        }
        System.err.println("Expected 'JA??' but was " + code.substring(0, Math.min(4, code.length())));
        throw new IOException("Expected 'JA??' but was " + code.substring(0, Math.min(4, code.length())));
    }

    private static Team FromCode00(String code) throws IOException {
        if (!code.startsWith("JA00")) {
            System.err.println("Expected 'JA00' but was " + code.substring(0, Math.min(4, code.length())));
            throw new IOException("Expected 'JA00' but was " + code.substring(0, Math.min(4, code.length())));
        }
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
        String crc = StringTools.CRC(fulltext);
        String crcdata = parts[parts.length - 1].trim();
        if (!crc.equals(crcdata)) {
            System.err.println("Checksums do not match: Excepted " + crc + " but was " + crcdata);
            throw new IOException("Checksums do not match: Excepted " + crc + " but was " + crcdata);
        }

        String[] header = parts[0].split(";");
        if (header.length != 4) {
            System.err.println("Expected an header with 4 entries but found " + header.length);
            throw new IOException("Expected an header with 4 entries but found " + header.length);
        }
        int id = Integer.parseInt(header[0]);
        int ak = Integer.parseInt(header[1]);
        int sex = Integer.parseInt(header[2]);
        int amount = Integer.parseInt(header[3]);
        if (parts.length != 2 + amount) {
            System.err.println("Expected " + (2 + amount) + " rows of data but found " + parts.length);
            throw new IOException("Expected " + (2 + amount) + " rows of data but found " + parts.length);
        }

        Teammember[] members = new Teammember[amount];
        for (int x = 0; x < amount; x++) {
            members[x] = Teammember.FromCode(parts[x + 1]);
        }
        Team team = new Team(id, ak, sex == 1, null, members);

        if (!code.equals(team.getCode())) {
            System.err.println("Codes do not match. Expected " + code + " but was " + team.getCode());
            throw new IllegalStateException("Codes do not match.");
        }

        return team;
    }

    private static Team FromCode01(String code) throws IOException {
        if (!code.startsWith("JA01")) {
            System.err.println("Expected 'JA01' but was " + code.substring(0, Math.min(4, code.length())));
            throw new IOException("Expected 'JA01' but was " + code.substring(0, Math.min(4, code.length())));
        }
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
        String crc = StringTools.CRC(fulltext);
        String crcdata = parts[parts.length - 1].trim();
        if (!crc.equals(crcdata)) {
            System.err.println("Checksums do not match: Excepted " + crc + " but was " + crcdata);
            throw new IOException("Checksums do not match: Excepted " + crc + " but was " + crcdata);
        }

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
        if (parts.length != 2 + amount) {
            System.err.println("Expected " + (2 + amount) + " rows of data but found " + parts.length);
            throw new IOException("Expected " + (2 + amount) + " rows of data but found " + parts.length);
        }
        if ("-".equals(exportId)) {
            exportId = null;
        }

        Teammember[] members = new Teammember[amount];
        for (int x = 0; x < amount; x++) {
            members[x] = Teammember.FromCode(parts[x + 1]);
        }
        Team team = new Team(id, ak, sex == 1, exportId, members);

        if (!code.equals(team.getCode())) {
            System.err.println("Codes do not match. Expected " + code + " but was " + team.getCode());
            throw new IllegalStateException("Codes do not match.");
        }

        return team;
    }

    private static byte[] compress(String data) {
        if (data == null || data.length() == 0) {
            return new byte[0];
        }
        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compresser = new Deflater(Deflater.HUFFMAN_ONLY);
        // DeflaterOutputStream dos = new DeflaterOutputStream(bos, compresser);
        byte[] output = new byte[1024];
        try {
            compresser.setInput(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        return Arrays.copyOf(output, compressedDataLength);
    }

    private static String decompress(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(data);
        byte[] result = new byte[1000];

        try {
            decompresser.inflate(result);
        } catch (DataFormatException e) {
            return null;
        }
        decompresser.end();

        // Decode the bytes into a String
        ByteArrayInputStream bis = new ByteArrayInputStream(result);
        Scanner sc = new Scanner(bis, "UTF-8");
        // String outputString = new String(result, 0, resultLength);
        String outputString = sc.nextLine();
        sc.close();
        return outputString;
    }

    public boolean isMale() {
        return male;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public int getId() {
        return id;
    }

    public String getTeamname() {
        return teamname;
    }

    public String getGliederung() {
        return gliederung;
    }

    public String getQualifikationsgliederung() {
        return qualifikationsgliederung;
    }

    public int getMemberCount() {
        return members.length;
    }

    public Teammember getMember(int x) {
        return members[x];
    }

    public String getCode() {
        return code;
    }

    public int getAk() {
        return ak;
    }
}
