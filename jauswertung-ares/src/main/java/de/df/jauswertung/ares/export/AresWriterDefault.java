package de.df.jauswertung.ares.export;

import static java.util.Arrays.stream;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;

class AresWriterDefault {

    private static final String CHARSET = "ISO-8859-1";
    private static final String CHARSET2 = "Cp850";

    public static <T extends ASchwimmer> void writeAres(AWettkampf<T>[] wks, String dir) throws IOException {
        ensureUniqueStartnumbers(wks);

        AresCompetition competition = new AresCompetition();

        writeGenders(dir);
        determineLaengen(wks, competition);
        writeLaengen(competition, wks, dir);
        writeStyles(competition, dir);
        writeNames(wks, dir);
        writeRecs(dir);
        writeRaceList(competition, wks, dir);
        writeNrList(wks, dir);
        writeRoundList(dir);
        writeHeatList(wks, dir);
        writeCompetitionInfo(wks, dir);
    }

    private static <T extends ASchwimmer> void ensureUniqueStartnumbers(AWettkampf<T>[] wks) {
        int max = stream(wks)
                .map(wk -> wk.getSchwimmer().stream().map(ASchwimmer::getStartnummer).max(Integer::compare))
                .map(m -> m.orElse(0)).mapToInt(i -> i).sum();
        for (AWettkampf<T> wk : wks) {
            for (T t : wk.getSchwimmer()) {
                max++;
                t.setStartnummer(max);
            }
        }

        int sn = 1;
        for (AWettkampf<T> wk : wks) {
            for (T t : wk.getSchwimmer()) {
                t.setStartnummer(sn);
                sn++;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    private static void writeAnzeigetafel(AWettkampf[] wks, String dir) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(dir + File.separator + "steuer.txt");
        writeSteuerText(wks, fos);
        fos.close();

        writeNAMs(wks, dir);
    }

    private static void writeGenders(String dir) throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream(Path.of(dir, "LSTCAT.TXT").toFile()), true,
                CHARSET)) {
            ps.println("\"Category\";\"AbrevCat\"");
            stream(AresGender.values()).map(AresGender::toLine).forEach(ps::println);
        }
    }

    @SuppressWarnings("unused")
    private static int writeAKs(Regelwerk aks, PrintStream ps, int offset) {
        for (int x = 0; x < aks.size(); x++) {
            String ak = aks.getAk(x).getName().replace("\"", "");
            for (int y = 0; y < 2; y++) {
                ps.println("\"" + ak + " " + (y == 1 ? "m" : "w") + "\";\"" + (x * 2 + y + offset) + "\"");
            }
        }
        return aks.size() * 2 + offset;
    }

    private static void determineLaengen(AWettkampf<?>[] wks, AresCompetition competition) {
        for (AWettkampf<?> wk : wks) {
            determineLaengen(wk, competition);
        }
    }

    private static void determineLaengen(AWettkampf<?> wk, AresCompetition competition) {
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                Disziplin d = ak.getDisziplin(y, false);
                Discipline discipline = LengthUtil.guessLength(d.getName());
                competition.addDiscipline(discipline);
            }
        }
    }

    private static void writeLaengen(AresCompetition competition, AWettkampf<?>[] wks, String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTLONG.TXT")) {
            determineLaengen(wks, competition);
            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("\"idLength\";\"Longueur\";\"Mlongueur\";\"Relais\"");
            competition.calculateLengths();

            for (Laenge length : competition.lengths()) {
                ps.println(
                        competition.getLengthId(length.distance()) + ";\"" + length.distance() + "\";" + length.laenge()
                                + ";\"1\"");
            }
        }
    }

    private static final String[][] laengen = new String[][] { { "200m", "200m" }, { "200 m", "200m" },
            { "100m", "100m" }, { "100 m", "100m" }, { "50m", "50m" }, { "50 m", "50m" }, { "25m", "25m" },
            { "25 m", "25m" }, { "4*25m", "4*25m" }, { "4*25 m", "4*25m" }, { "4*50m", "4*50m" },
            { "4*50 m", "4*50m" } };

    private static void writeStyles(AresCompetition competition, String dir)
            throws IOException {
        // idStyle;Style;StyleAbrév
        // 0; "Freistil " ;"FR"
        // 1; "Hindernis " ;"HI"
        // 2; "Rückenlage o.A. " ;"RU"
        // 3; "Schleppen e Puppe" ;"SP"
        // 4; "Vermischt " ;"ME"

        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTSTYLE.TXT")) {
            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("idStyle;Style;StyleAbrév");

            Map<Integer, String> reverse = new HashMap<>();

            LinkedList<Integer> ids = new LinkedList<>();
            int x = 0;
            for (Discipline discipline : competition.getDisciplines()) {
                String d = discipline.name();
                int id = x;
                ids.add(id);
                reverse.put(id, d);
                x++;
            }

            for (Integer id : ids) {
                String d = reverse.get(id).replace("\"", "");
                for (String[] kv : laengen) {
                    if (d.startsWith(kv[0])) {
                        d = d.replace(kv[0], kv[1]);
                    }
                }
                d = d.replace(" - Preheat", "");
                String key = "";
                if (d.equalsIgnoreCase("4*25m Rettungsstaffel")) {
                    key = "RE";
                } else if (d.equalsIgnoreCase("4*25m Rückenlage ohne Armtätigkeit")) {
                    key = "RA";
                } else if (d.equalsIgnoreCase("4*25m Gurtretterstaffel")) {
                    key = "GR";
                } else if (d.equalsIgnoreCase("4*25m Hindernisstaffel")) {
                    key = "HI";
                } else if (d.equalsIgnoreCase("4*50m Rettungsstaffel")) {
                    key = "RE";
                } else if (d.equalsIgnoreCase("4*25m Puppenstaffel")) {
                    key = "PU";
                } else if (d.equalsIgnoreCase("4*50m Gurtretterstaffel")) {
                    key = "GR";
                } else if (d.equalsIgnoreCase("4*50m Hindernisstaffel")) {
                    key = "HI";
                } else if (d.equalsIgnoreCase("50m Hindernisschwimmen")) {
                    key = "HI";
                } else if (d.equalsIgnoreCase("50m Kombiniertes Schwimmen")) {
                    key = "KS";
                } else if (d.equalsIgnoreCase("50m Flossenschwimmen")) {
                    key = "FL";
                } else if (d.equalsIgnoreCase("100m Hindernisschwimmen")) {
                    key = "HI";
                } else if (d.equalsIgnoreCase("50m Retten")) {
                    key = "RE";
                } else if (d.equalsIgnoreCase("50m Retten mit Flossen")) {
                    key = "RF";
                } else if (d.equalsIgnoreCase("100m Retten mit Flossen")) {
                    key = "RF";
                } else if (d.equalsIgnoreCase("200m Hindernisschwimmen")) {
                    key = "HI";
                } else if (d.equalsIgnoreCase("100m Lifesaver")) {
                    key = "LS";
                } else if (d.equalsIgnoreCase("100m Retten mit Flossen und Gurtretter")) {
                    key = "FG";
                } else if (d.equalsIgnoreCase("100m Kombinierte Rettungsübung")) {
                    key = "KR";
                } else if (d.equalsIgnoreCase("200m Super-Lifesaver")) {
                    key = "SL";
                } else if (d.equalsIgnoreCase("Obstacle Swim")) {
                    key = "OS";
                } else if (d.equalsIgnoreCase("Manikin Tow with Fins")) {
                    key = "MT";
                } else if (d.equalsIgnoreCase("Manikin Carry")) {
                    key = "MC";
                } else if (d.equalsIgnoreCase("Super Lifesaver")) {
                    key = "SL";
                } else if (d.equalsIgnoreCase("Manikin Carry with Fins")) {
                    key = "MC";
                } else if (d.equalsIgnoreCase("Rescue Medley")) {
                    key = "RM";
                }

                if (key.isEmpty()) {
                    System.out.println("Kürzel nicht gefunden: "+ d);
                }

                if (d.startsWith("4*25m ")) {
                    d = d.substring("4*25m ".length());
                } else if (d.startsWith("4*50m ")) {
                    d = d.substring("4*50m ".length());
                } else if (d.startsWith("25m ")) {
                    d = d.substring("25m ".length());
                } else if (d.startsWith("50m ")) {
                    d = d.substring("50m ".length());
                } else if (d.startsWith("100m ")) {
                    d = d.substring("100m ".length());
                } else if (d.startsWith("200m ")) {
                    d = d.substring("200m ".length());
                }
                ps.println(id + ";\"" + d + "\";\"" + key + "\";");
            }
        }
    }

    private static <T extends ASchwimmer> void writeRaceList(AresCompetition competition, AWettkampf<T>[] wks,
            String dir)
            throws UnsupportedEncodingException, IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTRACE.TXT")) {
            // event;round;nbHeat;idLen;idStyle;abCat;date;time
            // 1 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 2 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 3 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 4 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 5 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 6 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 7 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 8 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
            // 9 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;

            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("event;round;nbHeat;idLen;idStyle;abCat;date;time");
            LinkedList<Lauf<T>> ll = new LinkedList<>();
            for (AWettkampf<T> wk : wks) {
                ll.addAll(wk.getLaufliste().getLaufliste());
            }

            ll.sort((l1, l2) -> {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            });

            for (Lauf<T> lauf : ll) {
                int id1 = lauf.getLaufnummer();
                int id2 = lauf.getLaufbuchstabe() + 1;

                boolean male = (lauf.isEmpty() || (!lauf.isOnlyOneSex()) || lauf.getSchwimmer().isMaennlich());
                String gender = (male ? "M" : "W");

                Integer value = competition.getDisciplineId(lauf.getDisziplin());
                if (value == null) {
                    System.out.println("Disziplin \"" + lauf.getDisziplin() + "\" wurde nicht gefunden.");
                    value = 0;
                }
                int idLen = competition.getLengthIdByDiscipline(lauf.getDisziplin());
                int idStyle = value;
                String date = "00/00/00";
                String time = "00:00";

                ps.println(id1 + ";0;" + id2 + ";" + idLen + ";" + idStyle + ";\"" + gender + "\";\"" + date + "\";\""
                        + time + "\";");
            }
        }
    }

    private static <T extends ASchwimmer> void writeHeatList(AWettkampf<T>[] wks, String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTSTART.TXT")) {
            // event;round;heat;lane;relais;idBib
            // 4 ;1 ;0 ;1 ;0 ;3 ;
            // 4 ;1 ;0 ;2 ;0 ;106 ;
            // 4 ;1 ;0 ;3 ;0 ;7 ;
            // 4 ;1 ;0 ;4 ;0 ;108 ;
            // 4 ;1 ;0 ;5 ;0 ;109 ;
            // 4 ;1 ;0 ;6 ;0 ;105 ;

            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("event;round;heat;lane;relais;idBib");

            LinkedList<Lauf<T>> ll = new LinkedList<>();
            for (AWettkampf<T> wk : wks) {
                ll.addAll(wk.getLaufliste().getLaufliste());
            }

            ll.sort((l1, l2) -> {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            });

            for (Lauf<T> lauf : ll) {
                int id1 = lauf.getLaufnummer();
                int id2 = lauf.getLaufbuchstabe();

                for (int x = 0; x < lauf.getBahnen(); x++) {
                    T t = lauf.getSchwimmer(x);
                    if (t != null) {
                        int relay = 0;
                        int lane = x + 1;
                        ps.println(id1 + " ;0 ;" + id2 + " ;" + lane + " ;" + relay + " ;" + t.getStartnummer());
                    }
                }
            }
        }
    }

    private static <T extends ASchwimmer> void writeNrList(AWettkampf<T>[] wks, String dir) throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "lstrnum.txt")) {
            // id;event;round ;heat
            // 1 ;1 ;0 ;0 ;
            // 2 ;1 ;0 ;0 ;

            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("id;event;round ;heat");

            LinkedList<Lauf<T>> ll = new LinkedList<>();
            for (AWettkampf<T> wk : wks) {
                ll.addAll(wk.getLaufliste().getLaufliste());
            }

            ll.sort((l1, l2) -> {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            });

            int x = 1;
            for (Lauf<T> lauf : ll) {
                int id1 = lauf.getLaufnummer();
                int id2 = lauf.getLaufbuchstabe();

                ps.println(x + " ;" + id1 + " ;0 ;" + id2 + " ;");
                x++;
            }
        }
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T>[] wks, String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "Lstconc.TXT")) {
            // id;bib;lastname;firstname;birthyear;abNat;abCat
            // 1 ;"257" ;"POPOV" ;"Alexander" ;"1971" ;"RUS" ;"M" ;
            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("id;bib;lastname;firstname;birthyear;abNat;abCat");
            for (AWettkampf<T> wk : wks) {
                writeNames(wk, ps);
            }
        }
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T> wk, PrintStream ps) {
        for (T t : wk.getSchwimmer()) {
            ps.print(t.getStartnummer() + ";\"" + t.getStartnummer() + "\";");
            if (t instanceof Teilnehmer tn) {
                int jg = tn.getJahrgang();
                String vn = tn.getVorname().replace("\"", "");
                String nn = tn.getNachname().replace("\"", "");
                ps.print("\"" + nn + "\";\"" + vn + "\";\"" + (jg > 0 ? "" + jg : "") + "\"");
            } else {
                ps.print("\"" + fixName(t.getName()) + "\";\"\";\"\"");
            }
            ps.print(";\"" + t.getGliederungMitQGliederung() + "\"");
            ps.println(";\"" + (t.isMaennlich() ? "M" : "W") + "\";");
        }
    }

    private static String fixName(String name) {
        return name.trim().replace("e.V.", "").replace("DLRG LV", "").replace("DLRG", "").trim();
    }

    private static void writeRecs(String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTREC.TXT")) {
            // idLen;idStyle;idRec;abCat;time;name;date;place
            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("idLen;idStyle;idRec;abCat;time;name;date;place");
        }
    }

    private static <T extends ASchwimmer> void writeCompetitionInfo(AWettkampf<T>[] wks, String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "lsttitpr.txt")) {
            // event;round;text
            // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

            PrintStream ps = new PrintStream(os, true, CHARSET);
            ps.println("event;round;text");

            LinkedList<Lauf<T>> ll = new LinkedList<>();
            for (AWettkampf<T> wk : wks) {
                ll.addAll(wk.getLaufliste().getLaufliste());
            }

            ll.sort((l1, l2) -> {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            });

            String title = wks[0].getStringProperty(PropertyConstants.NAME);

            for (Lauf<T> lauf : ll) {
                ps.println(lauf.getLaufnummer() + " ;0 ;\"" + title + "\" ;\"" + title + "\" ;\"\" ;");
            }
        }
    }

    private static void writeRoundList(String dir)
            throws IOException {
        try (OutputStream os = new FileOutputStream(dir + File.separator + "LSTROUND.TXT")) {
            // idLen;idStyle;idRec;abCat;time;name;date;place
            PrintStream ps = new PrintStream(os, true, CHARSET);

            ps.println("idRound;TITLE;RoundAbrev;roundtext;sequence");
            ps.println(
                    "0; \"Entscheidung\"; \"TIM\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
            ps.println(
                    "1; \"Vorlauf\"; \"VOR\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
            ps.println(
                    "2; \"Zwischenlauf\"; \"ZWI\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
            ps.println(
                    "3; \"Finale\"; \"FIN\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
            ps.println(
                    "4; \"Stechen\"; \"SwimOff\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
            ps.println(
                    "5; \"Nachschwimmen\"; \"NACH\"; \"Lauf\";  \"1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99\"");
        }
    }

    private static void writeSteuerText(AWettkampf<?>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET2);
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Lauf<?>> ll = new LinkedList<>();
        for (AWettkampf<?> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        ll.sort(new Comparator<Lauf<?>>() {

            @Override
            public int compare(Lauf<?> l1, Lauf<?> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        // String title = wks[0].getStringProperty(PropertyConstants.NAME);

        for (Lauf<?> lauf : ll) {
            int id1 = lauf.getLaufnummer();
            // int id2 = lauf.getLaufbuchstabe() + 1;

            StringBuilder sb = new StringBuilder();
            sb.append(id1);
            resize(sb, 4, ' ');

            String disziplin = lauf.getDisziplin();
            Discipline l = LengthUtil.guessLength(disziplin);
            String amount = "" + l.amount();
            String length = l.length() + "m";
            disziplin = l.aresDiscipline();

            resize(sb, 7 - amount.length(), ' ');
            sb.append(amount);
            sb.append(" x ");
            resize(sb, 15 - length.length(), ' ');
            sb.append(length);
            sb.append(" ");
            sb.append(disziplin);
            resize(sb, 37, ' ');
            if (lauf.isEmpty()) {
                sb.append("-");
            } else if (lauf.isOnlyOneSex()) {
                ASchwimmer t = lauf.getSchwimmer();
                sb.append(t.isMaennlich() ? "männlich" : "weiblich");
            } else {
                sb.append("mixed");
            }

            ps.println(sb.toString());
        }
    }

    private static void writeNAMs(AWettkampf<?>[] wks, String dir) throws IOException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        LinkedList<Lauf<?>> ll = new LinkedList<>();
        for (AWettkampf<?> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        ll.sort((l1, l2) -> {
            int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
            int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
            return id1 - id2;
        });

        for (Lauf<?> lauf : ll) {
            String ln = "" + lauf.getLaufnummer();
            String lb = "" + (lauf.getLaufbuchstabe() + 1);

            StringBuilder name = new StringBuilder();
            resize(name, 5 - ln.length(), '0');
            name.append(ln);
            resize(name, 8 - lb.length(), '0');
            name.append(lb);

            try (FileOutputStream os = new FileOutputStream(dir + File.separator + name + ".NAM")) {
                PrintStream ps = new PrintStream(os, true, CHARSET2);

                for (int x = 0; x < lauf.getBahnen(); x++) {
                    ASchwimmer t = lauf.getSchwimmer(x);
                    if (t != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(" ");
                        sb.append(x + 1);
                        if (t instanceof Mannschaft m) {
                            sb.append(m.getName());
                            resize(sb, 33, ' ');
                            sb.append("-");
                        } else {
                            Teilnehmer te = (Teilnehmer) t;
                            sb.append(te.getNachname());
                            resize(sb, 33, ' ');
                            sb.append(te.getVorname());
                        }
                        resize(sb, 62, ' ');
                        sb.append(t.getGliederung());
                        resize(sb, 82, ' ');
                        ps.println(sb);
                    }
                }
            }
        }
    }

    private static void resize(StringBuilder sb, int length, char filler) {
        if (sb.length() > length) {
            sb.setLength(length);
        }
        while (sb.length() < length) {
            sb.append(filler);
        }
    }
}