/*
 * Created on 17.06.2006
 */
package de.df.jauswertung.misc;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.*;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.io.FileUtils;
import de.df.jutils.io.csv.CsvManager;
import de.df.jutils.io.csv.CsvTableModel;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.StringTools;

public final class MiscImportUtils {

    private MiscImportUtils() {
        // Hide constructor
    }

    private final static String[] NAMES = { "ak12", "ak13", "ak15", "ak17", "akoffen" };

    private static CsvImporter importer = new CsvImporter();

    public static <T extends ASchwimmer> LinkedList<T> importMeldeliste(String name, AWettkampf<T> wk)
            throws TableFormatException, TableEntryException, TableException, IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(name);

            LinkedList<T> results = importer.registration(fis, wk, new NullFeedback(), null, null);
            fis.close();
            fis = null;
            return results;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }

    public static void createFiles(String dir) throws FileNotFoundException, UnsupportedEncodingException {
        createFiles(dir, "einzel");
        createFiles(dir, "mannschaft");
    }

    private static void createFiles(String dir, String prefix)
            throws FileNotFoundException, UnsupportedEncodingException {
        String[] daten = FileUtils.readTextFile(dir + prefix + ".txt");
        int y = 0;
        boolean male = false;
        PrintStream ps = null;
        try {
            for (String aDaten : daten) {
                if (ps != null) {
                    if (aDaten.indexOf("CHAOS-SOFTWARE") >= 0) {
                        ps.close();
                        ps = null;
                    } else {
                        if (aDaten.trim().indexOf(" ") >= 0) {
                            ps.println(aDaten);
                        }
                    }
                } else {
                    if (aDaten.trim().indexOf("Zeit-1") >= 0) {
                        String name = dir + prefix + "-" + NAMES[y] + (male ? "m" : "w") + ".txt";
                        ps = new PrintStream(name, "Cp1252");
                        if (male) {
                            y++;
                        }
                        male = !male;

                        // new File(name).deleteOnExit();
                    }
                }
            }
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public static <T extends ASchwimmer> Object[][] importFile(AWettkampf<T> wk, int ak, boolean male, String file)
            throws TableFormatException, IOException, TableEntryException, TableException {
        return importFile(wk, ak, male, file, 2001);
    }

    public static <T extends ASchwimmer> Object[][] importFile(AWettkampf<T> wk, int ak, boolean male, String file,
            int jahr) throws TableException, TableFormatException, IOException, TableEntryException {
        String[] datei = FileUtils.readTextFile(file);
        int disz = wk.getRegelwerk().getAk(ak).getDiszAnzahl();
        LinkedList<String[]> result = new LinkedList<String[]>();
        if (wk instanceof EinzelWettkampf) {
            if (disz > 3) {
                for (String aDatei : datei) {
                    if (aDatei.trim().length() > 0) {
                        try {
                            LinkedList<String> l = new LinkedList<String>();
                            String sn = aDatei.substring(0, 5).trim();
                            String nachname = aDatei.substring(5, 21).trim();
                            String vorname = aDatei.substring(21, 37).trim();
                            String gld = aDatei.substring(37, 39).trim();
                            String jg = aDatei.substring(39, 43).trim();
                            String punkte = aDatei.substring(43, 52).trim();
                            String diff = aDatei.substring(52, 58).trim();
                            String z1 = aDatei.substring(58, 66).trim();
                            String p1 = aDatei.substring(66, 71).trim();
                            String s1 = aDatei.substring(71, 77).trim();
                            String z2 = aDatei.substring(77, 83).trim();
                            String p2 = aDatei.substring(83, 88).trim();
                            String s2 = aDatei.substring(88, 94).trim();
                            String z3 = aDatei.substring(94, 100).trim();
                            String p3 = aDatei.substring(100, 105).trim();
                            String s3 = aDatei.substring(105, 111).trim();
                            String z4 = aDatei.substring(111, 117).trim();
                            String p4 = aDatei.substring(117, 122).trim();
                            String s4 = aDatei.substring(122, 127).trim();
                            String zw = aDatei.substring(127).trim();

                            l.addLast(sn);
                            l.addLast(nachname);
                            l.addLast(vorname);
                            l.addLast(gld);
                            l.addLast(jg);
                            l.addLast(punkte);
                            l.addLast(diff);
                            switch (jahr) {
                            case 1999:
                                l.addLast(z1);
                                l.addLast(p1);
                                l.addLast(s1);
                                l.addLast(z2);
                                l.addLast(p2);
                                l.addLast(s2);
                                l.addLast(z3);
                                l.addLast(p3);
                                l.addLast(s3);
                                l.addLast(z4);
                                l.addLast(p4);
                                l.addLast(s4);
                                break;
                            case 2000:
                                l.addLast(z4);
                                l.addLast(p4);
                                l.addLast(s4);
                                l.addLast(z3);
                                l.addLast(p3);
                                l.addLast(s3);
                                l.addLast(z2);
                                l.addLast(p2);
                                l.addLast(s2);
                                l.addLast(z1);
                                l.addLast(p1);
                                l.addLast(s1);
                                break;
                            default:
                                l.addLast(z1);
                                l.addLast(p1);
                                l.addLast(s1);
                                l.addLast(z2);
                                l.addLast(p2);
                                l.addLast(s2);
                                l.addLast(z3);
                                l.addLast(p3);
                                l.addLast(s3);
                                l.addLast(z4);
                                l.addLast(p4);
                                l.addLast(s4);
                                break;
                            }
                            l.addLast(zw);

                            result.addLast(l.toArray(new String[0]));
                        } catch (RuntimeException re) {
                            System.err.println(aDatei);
                            re.printStackTrace();
                            System.exit(0);
                        }
                    }
                }
            } else {
                for (String aDatei : datei) {
                    if (aDatei.trim().length() > 0) {
                        LinkedList<String> l = new LinkedList<String>();
                        String sn = aDatei.substring(0, 6).trim();
                        String nachname = aDatei.substring(6, 24).trim();
                        String vorname = aDatei.substring(24, 41).trim();
                        String gld = aDatei.substring(41, 43).trim();
                        String jg = aDatei.substring(43, 52).trim();
                        String punkte = aDatei.substring(52, 61).trim();
                        String diff = aDatei.substring(61, 67).trim();
                        String z1 = aDatei.substring(67, 77).trim();
                        String p1 = aDatei.substring(77, 82).trim();
                        String s1 = aDatei.substring(82, 90).trim();
                        String z2 = aDatei.substring(90, 96).trim();
                        String p2 = aDatei.substring(96, 101).trim();
                        String s2 = aDatei.substring(101, 109).trim();
                        String z3 = aDatei.substring(109, 115).trim();
                        String p3 = aDatei.substring(115, 120).trim();
                        String s3 = "";
                        String hlw = "";
                        if (wk.getRegelwerk().getAk(ak).hasHLW()) {
                            s3 = aDatei.substring(120, 127).trim();
                            hlw = aDatei.substring(127).trim();
                        } else {
                            s3 = aDatei.substring(120).trim();
                        }
                        l.addLast(sn);
                        l.addLast(nachname);
                        l.addLast(vorname);
                        l.addLast(gld);
                        l.addLast(jg);
                        l.addLast(punkte);
                        l.addLast(diff);
                        switch (jahr) {
                        case 1999:
                            l.addLast(z1);
                            l.addLast(p1);
                            l.addLast(s1);
                            l.addLast(z2);
                            l.addLast(p2);
                            l.addLast(s2);
                            l.addLast(z3);
                            l.addLast(p3);
                            l.addLast(s3);
                            break;
                        case 2000:
                            l.addLast(z3);
                            l.addLast(p3);
                            l.addLast(s3);
                            l.addLast(z2);
                            l.addLast(p2);
                            l.addLast(s2);
                            l.addLast(z1);
                            l.addLast(p1);
                            l.addLast(s1);
                            break;
                        default:
                            l.addLast(z1);
                            l.addLast(p1);
                            l.addLast(s1);
                            l.addLast(z2);
                            l.addLast(p2);
                            l.addLast(s2);
                            l.addLast(z3);
                            l.addLast(p3);
                            l.addLast(s3);
                            break;
                        }
                        l.addLast(hlw);

                        result.addLast(l.toArray(new String[l.size()]));
                    }
                }
            }
        } else {
            for (String aDatei : datei) {
                if (aDatei.trim().length() > 0) {
                    LinkedList<String> l = new LinkedList<String>();
                    String sn = aDatei.substring(0, 8).trim();
                    String nachname = aDatei.substring(8, 30).trim();
                    String vorname = "";
                    String gld = aDatei.substring(30, 33).trim();
                    String jg = "";
                    String punkte = aDatei.substring(33, 42).trim();
                    String diff = aDatei.substring(42, 48).trim();
                    String z1 = aDatei.substring(48, 58).trim();
                    String p1 = aDatei.substring(58, 63).trim();
                    String s1 = aDatei.substring(63, 71).trim();
                    String z2 = aDatei.substring(71, 77).trim();
                    String p2 = aDatei.substring(77, 82).trim();
                    String s2 = aDatei.substring(82, 90).trim();
                    String z3 = aDatei.substring(90, 96).trim();
                    String p3 = aDatei.substring(96, 101).trim();
                    String s3 = aDatei.substring(101, 109).trim();
                    String z4 = aDatei.substring(109, 115).trim();
                    String p4 = aDatei.substring(115, 120).trim();
                    String s4 = "";
                    String hlw = "";
                    if (wk.getRegelwerk().getAk(ak).hasHLW()) {
                        s4 = aDatei.substring(120, 127).trim();
                        hlw = aDatei.substring(127).trim();
                    } else {
                        s4 = aDatei.substring(120).trim();
                    }
                    l.addLast(sn);
                    l.addLast(nachname);
                    l.addLast(vorname);
                    l.addLast(gld);
                    l.addLast(jg);
                    l.addLast(punkte);
                    l.addLast(diff);
                    switch (jahr) {
                    case 1999:
                        l.addLast(z1);
                        l.addLast(p1);
                        l.addLast(s1);
                        l.addLast(z2);
                        l.addLast(p2);
                        l.addLast(s2);
                        l.addLast(z3);
                        l.addLast(p3);
                        l.addLast(s3);
                        l.addLast(z4);
                        l.addLast(p4);
                        l.addLast(s4);
                        break;
                    case 2000:
                        l.addLast(z4);
                        l.addLast(p4);
                        l.addLast(s4);
                        l.addLast(z3);
                        l.addLast(p3);
                        l.addLast(s3);
                        l.addLast(z2);
                        l.addLast(p2);
                        l.addLast(s2);
                        l.addLast(z1);
                        l.addLast(p1);
                        l.addLast(s1);
                        break;
                    default:
                        l.addLast(z1);
                        l.addLast(p1);
                        l.addLast(s1);
                        l.addLast(z3);
                        l.addLast(p3);
                        l.addLast(s3);
                        l.addLast(z4);
                        l.addLast(p4);
                        l.addLast(s4);
                        l.addLast(z2);
                        l.addLast(p2);
                        l.addLast(s2);
                        break;
                    }
                    l.addLast(hlw);

                    result.addLast(l.toArray(new String[l.size()]));
                }
            }
        }

        String akname = wk.getRegelwerk().getAk(ak).getName();
        String geschlecht = (male ? "m" : "w");
        boolean hlw = wk.getRegelwerk().getAk(ak).hasHLW();

        String dateiname = file + ".csv";
        PrintStream ps = new PrintStream(dateiname);
        if (wk instanceof EinzelWettkampf) {
            ps.print("Altersklasse;Geschlecht;Platz;Nachname;Vorname;Gliederung;Jahrgang;Punkte;Differenz");
        } else {
            ps.print("Altersklasse;Geschlecht;Platz;Name;Leer;Gliederung;Leer;Punkte;Differenz");
        }
        for (int x = 0; x < disz; x++) {
            ps.print(";Zeit " + (x + 1) + ";Punkte " + (x + 1) + ";Strafe " + (x + 1));
        }
        if (hlw) {
            ps.print(";hlw");
        }
        ps.println();

        ListIterator<String[]> li = result.listIterator();
        while (li.hasNext()) {
            String[] daten = li.next();

            ps.print(akname);
            ps.print(";");
            ps.print(geschlecht);
            for (String aDaten : daten) {
                ps.print(";");
                ps.print(aDaten);
            }
            ps.println();
        }
        ps.close();
        Object[][] data = importFile(wk, dateiname, jahr);
        // new File(dateiname).deleteOnExit();
        return data;
    }

    public static <T extends ASchwimmer> Object[][] importFile(AWettkampf<T> wk, String FILE)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return importFile(wk, FILE, -1);
    }

    public static <T extends ASchwimmer> Object[][] importFile(AWettkampf<T> wk, String FILE, int jahr)
            throws TableFormatException, TableEntryException, TableException, IOException {
        int[] order = new int[] { 0, 1, 2, 3, 4, 5 };
        if ((jahr >= 2007) && (wk instanceof MannschaftWettkampf)) {
            if (FILE.contains("-ak12")) {
                order[1] = 2;
                order[2] = 3;
                order[3] = 1;
            } else {
                order[2] = 3;
                order[3] = 2;
            }
        }
        return importFile(wk, FILE, order);
    }

    public static <T extends ASchwimmer> Object[][] importFile(AWettkampf<T> wk, String FILE, int[] order)
            throws TableFormatException, TableEntryException, TableException, IOException {
        int offset = wk.getSchwimmeranzahl();

        LinkedList<T> ll = MiscImportUtils.importMeldeliste(FILE, wk);
        ListIterator<T> li = ll.listIterator();
        while (li.hasNext()) {
            T s = li.next();
            String gliederung = s.getGliederung().trim();
            if (gliederung.indexOf(" ") > 0) {
                String quali = gliederung.substring(0, gliederung.indexOf(" "));
                gliederung = gliederung.substring(gliederung.indexOf(" ") + 1);
                s.setGliederung(gliederung);
                s.setQualifikationsebene(quali);
            }
            wk.addSchwimmer(s);
        }
        InputStream is = new FileInputStream(FILE);
        CsvManager.setSeparator(';');
        CsvTableModel tm = CsvManager.getReaderInstance().read(is);
        Object[][] data = tm.getData();
        Object[] title = tm.getTitles();
        for (int y = 0; y < data[0].length; y++) {
            T t = SearchUtils.getSchwimmer(wk, y + 1 + offset);
            if (data[7][y].toString().startsWith("Ausschlu") || data[7][y].toString().startsWith("disq")
                    || data[7][y].toString().startsWith("ausg.")
                    || (wk.getStrafen().getStrafe(data[7][y].toString()) != null)) {
                if ((wk.getStrafen().getStrafe(data[7][y].toString()) != null)) {
                    t.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF, wk.getStrafen().getStrafe(data[7][y].toString()));
                } else {
                    t.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);
                }
            } else {
                for (int x = 0; x < (data.length - 8) / 3; x++) {
                    int index = 9 + (order[x] * 3);
                    String indexstring = data[index][y].toString().trim();
                    String index1string = data[index + 1][y].toString().trim();
                    String index2string = data[index + 2][y].toString().trim();

                    String[] text = new String[] { " 800", " 600", " 400", " 300", " 200", " 100", " 50", " n.a.",
                            " disq." };
                    for (String aText : text) {
                        if (index2string.endsWith(aText)) {
                            index2string = index2string.substring(0, index2string.length() - aText.length()).trim();
                        }
                    }
                    if (indexstring.equals("n. a.") || (index2string.equals("n.a."))
                            || (index2string.equalsIgnoreCase("V1")) || (index2string.equalsIgnoreCase("V1 n.a."))) {
                        t.addStrafe(x, Strafe.NICHT_ANGETRETEN);
                    } else if ((indexstring.length() > 0) && (!indexstring.equals("-"))) {
                        if (indexstring.equals("aufg.")) {
                            t.addStrafe(x, Strafe.DISQUALIFIKATION);
                        } else {
                            t.setZeit(x, getChaosZeit(indexstring));
                            if (index2string.indexOf(":") >= 0) {
                                String code = index2string.substring(0, index2string.indexOf(":"));
                                Strafe s = wk.getStrafen().getStrafe(code);
                                if (s != null) {
                                    t.addStrafe(x, s);
                                }
                            }
                            if (index2string.endsWith("disq.")) {
                                t.addStrafe(x, Strafe.DISQUALIFIKATION);
                            } else if (index1string.endsWith("disq.")) {
                                t.addStrafe(x, Strafe.DISQUALIFIKATION);
                            } else if (index2string.equals("VaP")) {
                                t.addStrafe(x, Strafe.DISQUALIFIKATION);
                            } else if (index1string.equals("VaP")) {
                                t.addStrafe(x, Strafe.DISQUALIFIKATION);
                            } else if (StringTools.isInteger(index2string)) {
                                t.addStrafe(x, new Strafe(Integer.parseInt(index2string)));
                            } else if (index2string.indexOf(":") >= 0) {
                                String[] p = index2string.substring(0, index2string.indexOf(":")).split(",");
                                for (int i = 0; i < p.length; i++) {
                                    p[i] = p[i].trim();
                                    Strafe s = wk.getStrafen().getStrafe(p[i]);
                                    if (s == null) {
                                        throw new RuntimeException("Illegal value " + p[i] + " in " + index2string);
                                    }
                                    t.addStrafe(x, s);
                                }
                            } else if ((index2string.length() % 2 == 0) && (index2string.length() > 2)) {
                                for (int i = 0; i < index2string.length() / 2; i++) {
                                    Strafe s = wk.getStrafen().getStrafe(index2string.substring(2 * i, 2 * i + 2));
                                    if (s == null) {
                                        // System.err.println("Illegal value "+index2string +" (from "+(2*i)+" to " +
                                        // (2*i+2)+")");
                                        throw new RuntimeException("Illegal value "
                                                + index2string.substring(2 * i, 2 * i + 2) + " in " + index2string);
                                    }
                                    t.addStrafe(x, s);
                                }
                            } else if (wk.getStrafen().getStrafe(index2string) != null) {
                                t.addStrafe(x, wk.getStrafen().getStrafe(index2string));
                            }

                        }
                    }
                    if ((t.getZeit(x) == 0) && (t.getStrafen(x).size() == 0)) {
                        t.addStrafe(x, Strafe.NICHT_ANGETRETEN);
                    }
                }
                if (title[title.length - 1].toString().toLowerCase().equals("hlw")) {
                    String hlw = data[data.length - 1][y].toString();
                    if (StringTools.isInteger(hlw)) {
                        t.setHLWPunkte(0, Integer.parseInt(hlw));
                    }
                }
            }
        }
        return data;
    }

    private static int getChaosZeit(Object o) {
        if (o.toString().length() == 0) {
            return 0;
        }
        String[] zeit = StringTools.split(o.toString(), ":");
        if (zeit.length != 3) {
            if (zeit.length != 2) {
                throw new IllegalArgumentException("1: " + o.toString());
            }
            String[] s = StringTools.split(zeit[1], ",");
            if (s.length != 2) {
                throw new IllegalArgumentException("2: " + o.toString());
            }
            String[] temp = new String[3];
            temp[0] = zeit[0];
            temp[1] = s[0];
            temp[2] = s[1].trim();

            zeit = temp;
        }
        int result = 0;
        int m = Integer.parseInt(zeit[0]);
        int s = Integer.parseInt(zeit[1]);
        int z = Integer.parseInt(zeit[2]);
        result = m * 6000 + s * 100 + z * (zeit[2].length() == 1 ? 10 : 1);
        return result;
    }
}
