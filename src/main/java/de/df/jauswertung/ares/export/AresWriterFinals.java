package de.df.jauswertung.ares.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jutils.util.Tripel;
import de.df.jutils.util.Tupel;

public class AresWriterFinals {

    private static final String CHARSET  = "ISO-8859-1";
    private static final String CHARSET2 = "Cp850";

    public static <T extends ASchwimmer> void writeAres(AWettkampf<T>[] wks, String dir) throws IOException {
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

    public static <T extends ASchwimmer> void writeAnzeigetafel(AWettkampf<T>[] wks, String dir) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(dir + File.separator + "steuer.txt");
        writeSteuerText(wks, fos);
        fos.close();

        writeNAMs(wks, dir);
    }

    private static void writeAKs(AWettkampf<?>[] wks, OutputStream os) throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"Kategorie\";\"Abr�vCat\"");
        int offset = 0;
        for (AWettkampf<?> wk : wks) {
            offset = writeAKs(wk.getRegelwerk(), ps, offset);
        }
    }

    private static int writeAKs(Regelwerk aks, PrintStream ps, int offset) throws UnsupportedEncodingException {
        for (int x = 0; x < aks.size(); x++) {
            String ak = aks.getAk(x).getName().toUpperCase().replace("\"", "");
            for (int y = 0; y < 2; y++) {
                ps.println("\"" + ak + " " + (y == 1 ? aks.getTranslation("maleShort", "m") : aks.getTranslation("femaleShort", "w")) + "\";\""
                        + (x * 2 + y + offset) + "\"");
            }
        }
        return aks.size() * 2 + offset;
    }

    private static int anschlaegeJe100m = 1;

    private static <T extends ASchwimmer> Hashtable<String, Integer> writeLaengen(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"idLength\";\"Longueur\";\"Mlongueur\";\"Relais\"");
        // 0; "25 m" ; 25 ;1
        Hashtable<String, Integer> disziplinen = new Hashtable<String, Integer>();
        for (AWettkampf<T> wk : wks) {
            writeLaengen(wk.getRegelwerk(), ps, disziplinen);
        }
        return disziplinen;
    }

    private static void writeLaengen(Regelwerk aks, PrintStream ps, Hashtable<String, Integer> disziplinen) throws UnsupportedEncodingException {
        // 0; "25 m" ; 25 ;1
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                Disziplin d = ak.getDisziplin(y, false);
                Integer i = disziplinen.get(d.getName());
                if (i == null) {
                    int id = disziplinen.size();
                    // String laenge1 = getLaenge(d);
                    int laenge2 = d.getLaenge(); // getLaenge(laenge1);
                    String laenge1 = "" + laenge2 + "m";
                    int anschlaege = 1; // Math.max(1, laenge2 / 100 / anschlaegeJe100m);
                    ps.println(id + ";\"" + laenge1 + "\";" + laenge2 + ";" + anschlaege);
                    disziplinen.put(d.getName(), id);
                    System.out.println(id + " -> " + d.getName().toUpperCase() + " / " + disziplinen.get(d.getName()));
                }
            }
        }
    }

    private static String[][] laengen = new String[][] { { "200m", "200m" }, { "200 m", "200m" }, { "100m", "100m" }, { "100 m", "100m" }, { "50m", "50m" },
            { "50 m", "50m" }, { "25m", "25m" }, { "25 m", "25m" }, { "4*25m", "4*25m" }, { "4*25 m", "4*25m" }, { "4*50m", "4*50m" }, { "4*50 m", "4*50m" } };

    private static String getLaenge(Disziplin d) {
        String name = d.getName().trim().toLowerCase();
        name = name.replace('x', '*').replace(" *", "*").replace("* ", "*");
        for (String[] kv : laengen) {
            if (name.startsWith(kv[0])) {
                return kv[1];
            }
        }
        return "25m";
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
        if (laenge.equals("25m")) {
            return 25;
        }
        if (laenge.equals("4*25m")) {
            return 100;
        }
        if (laenge.equals("4*50m")) {
            return 200;
        }
        return 0;
    }

    private static String[][] shortnames = new String[][] { new String[] { "200m Obstacle Swim", "OS" }, new String[] { "50m Manikin Carry", "MC" },
            new String[] { "100m Manikin Carry with Fins", "MCF" }, new String[] { "100m Rescue Medley", "RM" },
            new String[] { "100m Manikin Tow with Fins", "MTF" }, new String[] { "200m Super Lifesaver", "SL" }, new String[] { "Line Throw", "LT" },
            new String[] { "4 x 50m Obstacle Relay", "OR" }, new String[] { "4 x 25m Manikin Relay", "MR" }, new String[] { "4 x 50m Medley Relay", "MR" },
            new String[] { "4 x 50m Mixed Pool Lifesaver Relay", "MLR" }, };

    private static void writeStyles(Hashtable<String, Integer> disziplinen, OutputStream os) throws UnsupportedEncodingException {
        // idStyle;Style;StyleAbr�v
        // 0; "Freistil " ;"FR"
        // 1; "Hindernis " ;"HI"
        // 2; "R�ckenlage o.A. " ;"RU"
        // 3; "Schleppen e Puppe" ;"SP"
        // 4; "Vermischt " ;"ME"

        Hashtable<String, String> shorts = new Hashtable<String, String>();
        for (String[] sn : shortnames) {
            shorts.put(sn[0].toLowerCase(), sn[1]);
        }

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idStyle;Style;StyleAbr�v");
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
            String dlower = d.toLowerCase();
            String shortname = "";
            if (shorts.containsKey(dlower)) {
                shortname = shorts.get(dlower);
            }
            d = d.replace("\"", "");
            ps.println("" + id + ";\"" + d + "\";\"" + shortname + "\"");
        }
    }

    private static <T extends ASchwimmer> void writeRaceList(AWettkampf<T>[] wks, Hashtable<String, Integer> disziplinen, OutputStream os)
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

        LinkedList<Tupel<AWettkampf<T>, OWDisziplin<T>>> ll = getRounds(wks);
        for (Tupel<AWettkampf<T>, OWDisziplin<T>> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = owd.getLaeufe().size();

            boolean male = owd.maennlich;
            int ak = owd.akNummer;
            Disziplin d = wk.getRegelwerk().getAk(owd.akNummer).getDisziplin(owd.disziplin, owd.maennlich);

            int akmw = ak * 2 + (male ? 1 : 0);

            Integer value = disziplinen.get(d.getName());

            if (value == null) {
                System.out.println("Disziplin \"" + d.getName() + "\" wurde nicht gefunden.");
                value = 0;
            }
            int idLen = value;
            int idStyle = idLen;
            String date = "10/18/09";
            String time = "00:00";
            ps.println("" + id1 + " ;0 ;" + id2 + " ;" + idLen + " ;" + idStyle + " ;\"" + akmw + "\" ;\"" + date + "\" ;\"" + time + "\" ;");
        }
    }

    private static <T extends ASchwimmer> LinkedList<Tupel<AWettkampf<T>, OWDisziplin<T>>> getRounds(AWettkampf<T>[] wks) {
        LinkedList<Tupel<AWettkampf<T>, OWDisziplin<T>>> ll = new LinkedList<Tupel<AWettkampf<T>, OWDisziplin<T>>>();
        for (AWettkampf<T> wk : wks) {
            OWLaufliste<T> low = wk.getLauflisteOW();
            for (OWDisziplin<T> dis : low.getDisziplinen()) {
                ll.add(new Tupel<AWettkampf<T>, OWDisziplin<T>>(wk, dis));
            }
        }

        Collections.sort(ll, new Comparator<Tupel<AWettkampf<T>, OWDisziplin<T>>>() {

            @Override
            public int compare(Tupel<AWettkampf<T>, OWDisziplin<T>> lx1, Tupel<AWettkampf<T>, OWDisziplin<T>> lx2) {
                AWettkampf<T> wk1 = lx1.getFirst();
                AWettkampf<T> wk2 = lx2.getFirst();
                OWDisziplin<T> l1 = lx1.getSecond();
                OWDisziplin<T> l2 = lx2.getSecond();
                int id1 = wk1.getRegelwerk().getRundenId(l1);
                int id2 = wk2.getRegelwerk().getRundenId(l2);
                return id1 - id2;
            }
        });
        return ll;
    }

    private static <T extends ASchwimmer> LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> getHeats(AWettkampf<T>[] wks) {
        LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> ll = new LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>>();
        for (AWettkampf<T> wk : wks) {
            OWLaufliste<T> low = wk.getLauflisteOW();
            for (OWDisziplin<T> dis : low.getDisziplinen()) {
                for (OWLauf<T> l : dis.getLaeufe()) {
                    ll.add(new Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>(wk, dis, l));
                }
            }
        }

        Collections.sort(ll, new Comparator<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>>() {

            @Override
            public int compare(Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx1, Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx2) {
                AWettkampf<T> wk1 = lx1.getFirst();
                AWettkampf<T> wk2 = lx2.getFirst();
                OWDisziplin<T> d1 = lx1.getSecond();
                OWDisziplin<T> d2 = lx2.getSecond();
                OWLauf<T> l1 = lx1.getThird();
                OWLauf<T> l2 = lx2.getThird();
                int id1 = wk1.getRegelwerk().getRundenId(d1) * 1000 + getLaufnummer(l1);
                int id2 = wk2.getRegelwerk().getRundenId(d2) * 1000 + getLaufnummer(l2);
                return id1 - id2;
            }
        });
        return ll;
    }

    private static <T extends ASchwimmer> int getLaufnummer(OWLauf<T> l1) {
        int l = l1.getLaufnummer();
        switch (l) {
        case 51:
            return 61;
        case 31:
            return 41;
        case 131:
            return 141;
        case 271:
            return 281;
        case 311:
            return 321;
        case 331:
            return 341;
        default:
            return l;
        }
    }

    private static <T extends ASchwimmer> void writeHeatList(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;heat;lane;relais;idBib
        // 4 ;1 ;0 ;1 ;0 ;3 ;
        // 4 ;1 ;0 ;2 ;0 ;106 ;
        // 4 ;1 ;0 ;3 ;0 ;7 ;
        // 4 ;1 ;0 ;4 ;0 ;108 ;
        // 4 ;1 ;0 ;5 ;0 ;109 ;
        // 4 ;1 ;0 ;6 ;0 ;105 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;nbHeat;idLen;idStyle;abCat;date;time");

        LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> ll = getHeats(wks);

        for (Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = lauf.getLaufnummer() - 1;

            for (int x = 0; x < lauf.getBahnen(); x++) {
                T t = lauf.getSchwimmer(x);
                if (t != null) {
                    int relay = 0;// (t instanceof Teilnehmer ? 0 : 1);

                    int lane = x + 1;
                    String sn = "" + getId(t); // StartnumberFormatManager.format(t);
                    String date = "10/18/09";
                    String time = "00:00";
                    ps.println("" + id1 + " ;0 ;" + id2 + " ;" + lane + " ;" + relay + " ;" + sn + ";\"" + date + "\" ; \"" + time + "\" ;");
                }
            }
        }
    }

    private static <T extends ASchwimmer> void writeNrList(AWettkampf<T>[] wks, Hashtable<String, Integer> disziplinen, OutputStream os)
            throws UnsupportedEncodingException {
        // id;event;round ;heat
        // 1 ;1 ;0 ;0 ;
        // 2 ;1 ;0 ;0 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;event;round ;heat");

        LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> ll = getHeats(wks);

        int x = 1;
        for (Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = lauf.getLaufnummer() - 1;

            ps.println("" + x + " ;" + id1 + " ;0 ;" + id2 + " ;");
            x++;
        }
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // id;bib;lastname;firstname;birthyear;abNat;abCat
        // 1 ;"257" ;"POPOV" ;"Alexander" ;"1971" ;"RUS" ;"M" ;
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;bib;lastname;firstname;birthyear;abNat;abCat");
        int offset = 0;
        for (AWettkampf<T> wk : wks) {
            offset = writeNames(wk, ps, offset);
        }
    }

    private static String getKey(ASchwimmer t) {
        return "" + t.getStartnummer() + "" + t.getName() + "+" + t.getAKNummer() + "+" + (t.isMaennlich() ? 1 : 0);
    }

    private static int getId(ASchwimmer t) {
        String key = getKey(t);
        int sn = 0;
        if (!snMapping.containsKey(key)) {
            sn = nextSN;
            snMapping.put(key, sn);
            nextSN++;
        } else {
            sn = snMapping.get(key);
        }
        return sn;
    }

    private static Hashtable<String, Integer> snMapping = new Hashtable<String, Integer>();
    private static int                        nextSN    = 1;

    private static <T extends ASchwimmer> int writeNames(AWettkampf<T> wk, PrintStream ps, int offset) throws UnsupportedEncodingException {
        for (T t : wk.getSchwimmer()) {
            int sn = getId(t);
            // ps.print("" + StartnumberFormatManager.format(t) + ";\"" + StartnumberFormatManager.format(t) + "\";");
            ps.print("" + sn + ";\"" + sn + "\";");
            if (t instanceof Teilnehmer) {
                Teilnehmer tn = (Teilnehmer) t;
                int jg = tn.getJahrgang();
                String vn = tn.getVorname().replace("\"", "");
                String nn = tn.getNachname().replace("\"", "");
                ps.print("\"" + nn + "\";\"" + vn + "\";\"" + (jg > 0 ? "" + jg : "") + "\"");
            } else {
                ps.print("\"" + t.getName() + "\";\"\";\"\"");
            }
            ps.println(";\"" + t.getGliederung() + "\";\"" + (offset + (t.getAKNummer() * 2 + (t.isMaennlich() ? 1 : 0))) + "\"");
        }
        return wk.getRegelwerk().size() * 2 + offset;
    }

    private static <T extends ASchwimmer> void writeRecs(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idLen;idStyle;idRec;abCat;time;name;date;place");
    }

    private static <T extends ASchwimmer> void writeCompetitionInfo(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;text");
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Tupel<AWettkampf<T>, OWDisziplin<T>>> ll = getRounds(wks);

        for (Tupel<AWettkampf<T>, OWDisziplin<T>> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();

            String title = wk.getStringProperty(PropertyConstants.NAME);

            int id1 = wk.getRegelwerk().getRundenId(owd);

            ps.println("" + id1 + " ;0 ;\"" + title + "\" ;\"" + title + "\" ;\"\" ;");
        }
    }

    private static <T extends ASchwimmer> void writeRoundList(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idRound;TITLE;RoundAbrev;roundtext; sequence");
        ps.println("0; \"Lauf\"; \"Lauf\"; \"Lauf\";  \"1\"");
    }

    private static <T extends ASchwimmer> void writeSteuerText(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET2);
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> ll = getHeats(wks);

        for (Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            // String title = wk.getStringProperty(PropertyConstants.NAME);

            int id1 = lauf.getLaufnummer();
            // int id2 = lauf.getLaufbuchstabe() + 1;

            StringBuilder sb = new StringBuilder();
            sb.append(id1);
            resize(sb, 4, ' ');

            Disziplin d = wk.getRegelwerk().getAk(owd.akNummer).getDisziplin(owd.disziplin, owd.maennlich);

            String disziplin = d.getName();
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
            } else if (disziplin.startsWith("25m")) {
                disziplin = disziplin.substring(4);
                amount = "1";
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
            } else {
                T t = lauf.getSchwimmer();
                sb.append(t.isMaennlich() ? "m�nnlich" : "weiblich");
            }

            ps.println(sb.toString());
        }
    }

    private static <T extends ASchwimmer> void writeNAMs(AWettkampf<T>[] wks, String dir) throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>>> ll = getHeats(wks);

        for (Tripel<AWettkampf<T>, OWDisziplin<T>, OWLauf<T>> lx : ll) {
            // AWettkampf<T> wk = lx.getFirst();
            // OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            // String title = wk.getStringProperty(PropertyConstants.NAME);

            try {
                String ln = "" + lauf.getLaufnummer();
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
