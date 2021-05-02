package de.df.jauswertung.misc;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jutils.util.StringTools;

// $rec = array(
// array("AK 12", -1, array( 31.90, 35.70, 24.30), array( 31.10, 35.10, 23.60), array("50m Hindernis", "50m k. Schwimmen", "50m Flossen")),
// array("AK 13/14", -1, array( 66.20, 41.70, 30.40), array( 59.20, 35.90, 26.90), array("100m Hindernis", "50m Retten", "50m Retten mit Flossen")),
// array("AK 15/16", -1, array( 65.30, 41.80, 64.70), array( 57.10, 35.40, 57.90), array("100m Hindernis", "50m Retten", "100m Retten mit Flossen")),
// array("AK 17/18", -1, array(142.00, 39.10, 61.60), array(129.70, 33.30, 54.00), array("200m Hindernis", "50m Retten", "100m Retten mit Flossen")),
// array("AK offen", 3, array(129.98, 35.26, 56.93, 72.78, 62.19, 148.59), array(115.31, 30.69, 50.48, 63.64, 53.75, 129.29), array("200m Hindernis", "50m
// Retten", "100m Retten mit Flossen", "100m k. Rettungs&uuml;bung", "100m Lifesaver", "200m Super Lifesaver")),
// array("AK 25, AK 30", -1, array( 67.00, 40.00, 65.80), array( 59.00, 33.00, 57.60), array("100m Hindernis", "50m Retten", "100m Retten mit Flossen")),
// array("AK 35", -1, array( 67.00, 40.00, 31.70), array( 59.00, 33.00, 26.10), array("100m Hindernis", "50m Retten", "50m Retten mit Flossen")),
// array("AK 40, AK 45", -1, array( 32.02, 40.00, 31.70), array( 27.50, 33.00, 26.10), array("50m Hindernis", "50m Retten", "50m Retten mit Flossen")),
// array("AK 50+", -1, array( 36.80, 44.90, 27.90), array( 28.50, 35.20, 23.60), array("50m Freistil", "50m k. Schwimmen", "25m Schleppen einer Puppe")),
// );

public class PunkteTabelleExport {

    private static String[] idsE1   = new String[] {};
    private static String[] namesE1 = new String[] {};

    private static String[] idsE2   = new String[] { "AK 12", "AK 13/14", "AK 15/16", "AK 17/18", "AK Offen" };
    private static String[] namesE2 = new String[] { "AK 12", "AK 13/14", "AK 15/16", "AK 17/18", "AK Offen" };

    private static String[] idsM1   = new String[] { "AK 12", "AK 13/14" };
    private static String[] namesM1 = new String[] { "AK 12", "AK 13/14" };

    private static String[] idsM2   = new String[] { "AK 15/16", "AK 17/18", "AK Offen" };
    private static String[] namesM2 = new String[] { "AK 15/16", "AK 17/18", "AK Offen" };

    public static void main(String[] args) {
        Regelwerk einzel = AltersklassenUtils.getDefaultAKs(true);
        Regelwerk mannschaft = AltersklassenUtils.getDefaultAKs(false);

        System.out.println("-");
        writeByAgegroup(einzel, idsE1, namesE1);
        System.out.println("Einzel");
        writeByAgegroup(einzel, idsE2, namesE2);
        System.out.println("Mannschaft AK 12 & 13/14");
        writeBySex(mannschaft, idsM1, namesM1);
        System.out.println("Mannschaft AK 15/16 - Offen");
        writeBySex(mannschaft, idsM2, namesM2);
    }

    private static final String[][] replacements = new String[][] {
            new String[] { "50m Kombiniertes Schwimmen", "50m komb. Schwimmen" },
            new String[] { "50m Hindernisschwimmen", "50m Hindernis-schwimmen" },
            new String[] { "100m Hindernisschwimmen", "100m Hindernis-schwimmen" },
            new String[] { "200m Hindernisschwimmen", "200m Hindernis-schwimmen" },
            new String[] { "50m Flossenschwimmen", "50m Flossen" },
            new String[] { "100m Kombinierte Rettungsübung", "100m komb. Rettungs-übung" },
            new String[] { "100m Lifesaver", "100m Retten m. Fl. u. GR. (Lifesaver)" },
            new String[] { "4*25m Rückenlage ohne Armtätigkeit", "4*25m Rückenlage ohne Arme" },
            new String[] { "50m Flossenschwimmen", "50m Flossen" }, };

    private static String disziplinReplacement(String disziplin) {
        for (String[] r : replacements) {
            if (disziplin.equalsIgnoreCase(r[0])) {
                return r[1];
            }
        }
        return disziplin;
    }

    private static void writeByAgegroup(Regelwerk aks, String[] idx, String[] namex) {
        if (idx == null || idx.length == 0) {
            return;
        }

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int x = 0; x < idx.length; x++) {
            int a = aks.getIndex(idx[x]);
            Altersklasse ak = aks.getAk(a);
            for (int i = 0; i < 2; i++) {
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    Disziplin d = ak.getDisziplin(y, i == 1);
                    sb1.append(String.format("\"%s\";", disziplinReplacement(d.getName())));
                    sb2.append(String.format("\"%s\";", StringTools.zeitString(d.getRec())));
                }
            }
        }
        String t1 = sb1.toString();
        String t2 = sb2.toString();
        t1 = t1.substring(0, t1.length() - 1);
        t2 = t2.substring(0, t2.length() - 1);
        String text = String.format("%s\n%s", t1, t2);
        System.out.println(text);
    }

    private static void writeBySex(Regelwerk aks, String[] idx, String[] namex) {
        if (idx == null || idx.length == 0) {
            return;
        }

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            for (int x = 0; x < idx.length; x++) {
                int a = aks.getIndex(idx[x]);
                Altersklasse ak = aks.getAk(a);
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    Disziplin d = ak.getDisziplin(y, i == 1);
                    sb1.append(String.format("\"%s\";", disziplinReplacement(d.getName())));
                    sb2.append(String.format("\"%s\";", StringTools.zeitString(d.getRec())));
                }
            }
        }
        String t1 = sb1.toString();
        String t2 = sb2.toString();
        t1 = t1.substring(0, t1.length() - 1);
        t2 = t2.substring(0, t2.length() - 1);
        String text = String.format("%s\n%s", t1, t2);
        System.out.println(text);
    }
}
