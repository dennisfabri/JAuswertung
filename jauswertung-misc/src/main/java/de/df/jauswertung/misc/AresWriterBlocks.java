package de.df.jauswertung.misc;

import java.io.*;
import java.util.*;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.InputManager;
import de.dm.ares.data.Heat;
import de.dm.ares.data.LaneStatus;
import de.dm.collector.AlphaHttpServer;

public class AresWriterBlocks {

    private static final String CHARSET = "ISO-8859-1";
    private static final String CHARSET2 = "Cp850";

    public static void main(String[] args) throws IOException {
        main2("DM2016_einzel - Block 1.wk", "DM-Einzel1");
        main2("DM2016_einzel - Block 2.wk", "DM-Einzel2");
        main2("DM2016_mannschaft - Block 1.wk", "DM-Mannschaft1");
        main2("DM2016_mannschaft - Block 2.wk", "DM-Mannschaft2");
        main2("DM2016_mannschaft - Block 3.wk", "DM-Mannschaft3");
        main2("DM2016_mannschaft - Block 4.wk", "DM-Mannschaft4");
    }

    @SuppressWarnings("unchecked")
    public static void main2(String name1, String name2) throws IOException {
        @SuppressWarnings("rawtypes")
        AWettkampf wk1 = InputManager.ladeWettkampf("../../" + name1);
        writeAres(new AWettkampf[] { wk1 }, "../../Ares/" + name2);
        writeAnzeigetafel(new AWettkampf[] { wk1 }, "../../Ares/" + name2 + "/Anzeige");
    }

    public static <T extends Teilnehmer> void writeEZ(AWettkampf<T>[] wks, String file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        LinkedList<Heat> heats = new LinkedList<Heat>();
        int pos = 1;
        for (AWettkampf<T> wk : wks) {
            for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
                int eventn = lauf.getLaufnummer() % 100;
                int heatn = lauf.getLaufnummer() / 100;
                Heat h = new Heat("" + pos, eventn, heatn);
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    h.store(x, 100, LaneStatus.RaceTimes);
                }
                heats.add(h);
                pos++;
            }
        }
        Heat[] ha = heats.toArray(new Heat[heats.size()]);
        fos.write(AlphaHttpServer.toData(ha));
        fos.close();
    }

    public static <T extends Teilnehmer> void writeAres(AWettkampf<T>[] wks, String dir) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(dir + File.separator + "LSTCAT.TXT");
        writeAKs(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTLONG.TXT");
        Hashtable<String, Integer> disziplinen = writeLaengen(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTSTYLE.TXT");
        writeStyles(disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "Lstconc.TXT");
        writeNames(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTREC.TXT");
        writeRecs(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTRACE.TXT");
        writeRaceList(wks, disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "lstrnum.txt");
        writeNrList(wks, disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTROUND.TXT");
        writeRoundList(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTSTART.TXT");
        writeHeatList(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "lsttitpr.txt");
        writeCompetitionInfo(wks, fos);
        fos.close();
    }

    public static <T extends Teilnehmer> void writeAnzeigetafel(AWettkampf<T>[] wks, String dir) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(dir + File.separator + "steuer.txt");
        writeSteuerText(wks, fos);
        fos.close();

        writeNAMs(wks, dir);
    }

    private static void writeAKs(AWettkampf<?>[] wks, OutputStream os) throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"Kategorie\";\"AbrèvCat\"");
        int offset = 0;
        for (AWettkampf<?> wk : wks) {
            offset = writeAKs(wk.getRegelwerk(), ps, offset);
        }
    }

    private static int writeAKs(Regelwerk aks, PrintStream ps, int offset) throws UnsupportedEncodingException {
        for (int x = 0; x < aks.size(); x++) {
            String ak = aks.getAk(x).getName().replace("\"", "");
            for (int y = 0; y < 2; y++) {
                ps.println("\"" + ak + " " + (y == 1 ? "m" : "w") + "\";\"" + (x * 2 + y + offset) + "\"");
            }
        }
        return aks.size() * 2 + offset;
    }

    private static int anschlaegeJe100m = 1;

    private static <T extends ASchwimmer> Hashtable<String, Integer> writeLaengen(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"idLength\";\"Longueur\";\"Mlongueur\";\"Relais\"");
        // 0; "25 m" ; 25 ;1
        Hashtable<String, Integer> disziplinen = new Hashtable<String, Integer>();
        for (AWettkampf<T> wk : wks) {
            writeLaengen(wk.getRegelwerk(), ps, disziplinen);
        }
        return disziplinen;
    }

    private static void writeLaengen(Regelwerk aks, PrintStream ps, Hashtable<String, Integer> disziplinen)
            throws UnsupportedEncodingException {
        // 0; "25 m" ; 25 ;1
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                Disziplin d = ak.getDisziplin(y, false);
                Integer i = disziplinen.get(d.getName());
                if (i == null) {
                    int id = disziplinen.size();
                    String laenge1 = getLaenge(d);
                    int laenge2 = getLaenge(laenge1);
                    int anschlaege = Math.max(1, laenge2 / 100 / anschlaegeJe100m);
                    ps.println(id + ";\"" + laenge1 + "\";" + laenge2 + ";" + anschlaege);
                    disziplinen.put(d.getName(), id);
                    System.out.println(id + " -> " + d.getName() + " / " + disziplinen.get(d.getName()));
                }
            }
        }
    }

    private static String[][] laengen = new String[][] { { "200m", "200m" }, { "200 m", "200m" }, { "100m", "100m" },
            { "100 m", "100m" }, { "50m", "50m" },
            { "50 m", "50m" }, { "25m", "25m" }, { "25 m", "25m" }, { "4*25m", "4*25m" }, { "4*25 m", "4*25m" },
            { "4*50m", "4*50m" }, { "4*50 m", "4*50m" } };

    private static String getLaenge(Disziplin d) {
        String name = d.getName().trim().toLowerCase();
        name = name.replace('x', '*').replace(" *", "*").replace("* ", "*");
        for (String[] kv : laengen) {
            if (name.startsWith(kv[0])) {
                return kv[1];
            }
        }
        return "";
    }

    private static int getLaenge(String laenge) {
        if (laenge.equals("200m")) {
            return 200;
        }
        if (laenge.equals("100m")) {
            return 100;
        }
        if (laenge.equals("50m")) {
            return 50;
        }
        if (laenge.equals("4*25m")) {
            return 100;
        }
        if (laenge.equals("4*50m")) {
            return 200;
        }
        return 0;
    }

    private static void writeStyles(Hashtable<String, Integer> disziplinen, OutputStream os)
            throws UnsupportedEncodingException {
        // idStyle;Style;StyleAbrév
        // 0; "Freistil " ;"FR"
        // 1; "Hindernis " ;"HI"
        // 2; "Rückenlage o.A. " ;"RU"
        // 3; "Schleppen e Puppe" ;"SP"
        // 4; "Vermischt " ;"ME"

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idStyle;Style;StyleAbrév");
        Enumeration<String> dis = disziplinen.keys();

        Hashtable<Integer, String> reverse = new Hashtable<Integer, String>();

        LinkedList<Integer> ids = new LinkedList<Integer>();
        while (dis.hasMoreElements()) {
            String d = dis.nextElement();
            int id = disziplinen.get(d);
            System.out.println(id + " => " + d);
            ids.add(id);
            reverse.put(id, d);
        }
        Collections.sort(ids);

        for (Integer id : ids) {
            String d = reverse.get(id);
            d = d.replace("\"", "");
            ps.println("" + id + ";\"" + d + "\";\"\"");
        }
    }

    private static <T extends ASchwimmer> void writeRaceList(AWettkampf<T>[] wks,
            Hashtable<String, Integer> disziplinen, OutputStream os)
            throws UnsupportedEncodingException {
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
        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        for (Lauf<T> lauf : ll) {
            int id1 = lauf.getLaufnummer() % 100;
            int id2 = lauf.getLaufbuchstabe();

            boolean male = (lauf.isEmpty() || (!lauf.isOnlyOneSex()) || lauf.getSchwimmer().isMaennlich());
            int ak = (lauf.isEmpty() ? 0 : lauf.getSchwimmer().getAKNummer());
            int akmw = ak * 2 + (male ? 1 : 0);

            Integer value = disziplinen.get(lauf.getDisziplin());
            if (value == null) {
                System.out.println("Disziplin \"" + lauf.getDisziplin() + "\" wurde nicht gefunden.");
                value = 0;
            }
            int idLen = value;
            int idStyle = idLen;
            String date = "10/18/09";
            String time = "00:00";

            ps.println("" + id1 + " ;0 ;" + id2 + " ;" + idLen + " ;" + idStyle + " ;\"" + akmw + "\" ;\"" + date
                    + "\" ;\"" + time + "\" ;");
        }
    }

    private static <T extends ASchwimmer> void writeHeatList(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // event;round;heat;lane;relais;idBib
        // 4 ;1 ;0 ;1 ;0 ;3 ;
        // 4 ;1 ;0 ;2 ;0 ;106 ;
        // 4 ;1 ;0 ;3 ;0 ;7 ;
        // 4 ;1 ;0 ;4 ;0 ;108 ;
        // 4 ;1 ;0 ;5 ;0 ;109 ;
        // 4 ;1 ;0 ;6 ;0 ;105 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;nbHeat;idLen;idStyle;abCat;date;time");

        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        for (Lauf<T> lauf : ll) {
            int id1 = lauf.getLaufnummer() % 100;
            int id2 = lauf.getLaufbuchstabe();

            for (int x = 0; x < lauf.getBahnen(); x++) {
                T t = lauf.getSchwimmer(x);
                if (t != null) {
                    int relay = (t instanceof Teilnehmer ? 0 : 1);

                    int lane = x + 1;
                    String sn = "" + t.getStartnummer(); // StartnumberFormatManager.format(t);
                    String date = "10/18/09";
                    String time = "00:00";
                    ps.println("" + id1 + " ;0 ;" + id2 + " ;" + lane + " ;" + relay + " ;" + sn + ";\"" + date
                            + "\" ; \"" + time + "\" ;");
                }
            }
        }
    }

    private static <T extends ASchwimmer> void writeNrList(AWettkampf<T>[] wks, Hashtable<String, Integer> disziplinen,
            OutputStream os)
            throws UnsupportedEncodingException {
        // id;event;round ;heat
        // 1 ;1 ;0 ;0 ;
        // 2 ;1 ;0 ;0 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;event;round ;heat");

        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        // Laufliste<T> liste = wk.getLaufliste();

        int x = 1;
        for (Lauf<T> lauf : ll) {
            int id1 = lauf.getLaufnummer() % 100;
            int id2 = lauf.getLaufbuchstabe();

            ps.println("" + x + " ;" + id1 + " ;0 ;" + id2 + " ;");
            x++;
        }
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // id;bib;lastname;firstname;birthyear;abNat;abCat
        // 1 ;"257" ;"POPOV" ;"Alexander" ;"1971" ;"RUS" ;"M" ;
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;bib;lastname;firstname;birthyear;abNat;abCat");
        for (AWettkampf<T> wk : wks) {
            writeNames(wk, ps);
        }
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T> wk, PrintStream ps)
            throws UnsupportedEncodingException {
        for (T t : wk.getSchwimmer()) {
            // ps.print("" + StartnumberFormatManager.format(t) + ";\"" +
            // StartnumberFormatManager.format(t) + "\";");
            ps.print("" + t.getStartnummer() + ";\"" + t.getStartnummer() + "\";");
            if (t instanceof Teilnehmer) {
                Teilnehmer tn = (Teilnehmer) t;
                int jg = tn.getJahrgang();
                String vn = tn.getVorname().replace("\"", "");
                String nn = tn.getNachname().replace("\"", "");
                ps.print("\"" + nn + "\";\"" + vn + "\";\"" + (jg > 0 ? "" + jg : "") + "\"");
            } else {
                ps.print("\"" + t.getName() + "\";\"\";\"\"");
            }
            ps.print(";\"" + t.getGliederungMitQGliederung() + "\"");
            ps.println(";\"" + (t.getAKNummer() * 2 + (t.isMaennlich() ? 1 : 0)) + "\"");
        }
    }

    private static <T extends ASchwimmer> void writeRecs(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idLen;idStyle;idRec;abCat;time;name;date;place");
    }

    private static <T extends ASchwimmer> void writeCompetitionInfo(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;text");
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        String title = wks[0].getStringProperty(PropertyConstants.NAME);

        for (Lauf<T> lauf : ll) {
            int id1 = lauf.getLaufnummer() % 100;
            // int id2 = lauf.getLaufbuchstabe() + 1;

            ps.println("" + id1 + " ;0 ;\"" + title + "\" ;\"" + title + "\" ;\"\" ;");
        }
    }

    private static <T extends ASchwimmer> void writeRoundList(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idRound;TITLE;RoundAbrev;roundtext; sequence");
        ps.println("0; \"Lauf\"; \"Lauf\"; \"Lauf\";  \"1\"");
    }

    private static <T extends ASchwimmer> void writeSteuerText(AWettkampf<T>[] wks, OutputStream os)
            throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET2);
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        // String title = wks[0].getStringProperty(PropertyConstants.NAME);

        for (Lauf<T> lauf : ll) {
            int id1 = lauf.getLaufnummer() % 100;
            // int id2 = lauf.getLaufbuchstabe() + 1;

            StringBuilder sb = new StringBuilder();
            sb.append(id1);
            resize(sb, 4, ' ');

            String disziplin = lauf.getDisziplin();
            String amount = "";
            String length = "";
            if (disziplin.startsWith("4x50m") || disziplin.startsWith("4*50m")) {
                disziplin = disziplin.substring(6);
                amount = "4";
                length = "50m";
            } else if (disziplin.startsWith("4 x 50m") || disziplin.startsWith("4 * 50m")) {
                disziplin = disziplin.substring(8);
                amount = "4";
                length = "50m";
            } else if (disziplin.startsWith("4x25m") || disziplin.startsWith("4*25m")) {
                disziplin = disziplin.substring(6);
                amount = "4";
                length = "25m";
            } else if (disziplin.startsWith("4 x 25m") || disziplin.startsWith("4 * 25m")) {
                disziplin = disziplin.substring(8);
                amount = "4";
                length = "25m";
            } else if (disziplin.startsWith("50m")) {
                disziplin = disziplin.substring(4);
                amount = "1";
                length = "50m";
            } else if (disziplin.startsWith("100m")) {
                disziplin = disziplin.substring(5);
                amount = "1";
                length = "100m";
            } else if (disziplin.startsWith("200m")) {
                disziplin = disziplin.substring(5);
                amount = "1";
                length = "200m";
            } else if (disziplin.equals("Line Throw")) {
                // disziplin = disziplin.substring(4);
                amount = "1";
                length = "25m";
            } else {
                System.err.println(disziplin);
            }

            if (disziplin.equals("Manikin Tow with Fins")) {
                disziplin = "Lifesaver";
            } else if (disziplin.equals("Manikin Carry with Fins")) {
                disziplin = "Manikin Carry w Fins";
            }
            if (disziplin.length() > 15) {
                disziplin = disziplin.substring(0, 15);
            }

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
                T t = lauf.getSchwimmer();
                sb.append(t.isMaennlich() ? "männlich" : "weiblich");
            } else {
                sb.append("mixed");
            }

            ps.println(sb.toString());
        }
    }

    private static <T extends ASchwimmer> void writeNAMs(AWettkampf<T>[] wks, String dir)
            throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Lauf<T>> ll = new LinkedList<Lauf<T>>();
        for (AWettkampf<T> wk : wks) {
            ll.addAll(wk.getLaufliste().getLaufliste());
        }

        Collections.sort(ll, new Comparator<Lauf<T>>() {

            @Override
            public int compare(Lauf<T> l1, Lauf<T> l2) {
                int id1 = l1.getLaufnummer() * 100 + l1.getLaufbuchstabe();
                int id2 = l2.getLaufnummer() * 100 + l2.getLaufbuchstabe();
                return id1 - id2;
            }
        });

        // String title = wks[0].getStringProperty(PropertyConstants.NAME);

        for (Lauf<T> lauf : ll) {
            try {
                String ln = "" + lauf.getLaufnummer() % 100;
                String lb = "" + (lauf.getLaufbuchstabe() + 1);

                StringBuilder name = new StringBuilder();
                resize(name, 5 - ln.length(), '0');
                name.append(ln);
                resize(name, 8 - lb.length(), '0');
                name.append(lb);

                FileOutputStream os = new FileOutputStream(dir + File.separator + name.toString() + ".NAM");
                PrintStream ps = new PrintStream(os, true, CHARSET2);

                for (int x = 0; x < lauf.getBahnen(); x++) {
                    T t = lauf.getSchwimmer(x);
                    if (t != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(" ");
                        sb.append(x + 1);
                        if (t instanceof Mannschaft) {
                            Mannschaft te = (Mannschaft) t;
                            sb.append(te.getName());
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
                        ps.println(sb.toString());
                    }
                }

                os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
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
