package de.df.jauswertung.misc;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.util.AltersklassenUtils;

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

public class PunkteRechnerExport {

    public static void main(String[] args) {
        Regelwerk einzel = AgeGroupIOUtils.getDefaultAKs(true);
        Regelwerk mannschaft = AgeGroupIOUtils.getDefaultAKs(false);

        int jahr = 2022;

        // System.out.println("Einzel");
        System.out.println("  new Year(" + jahr + ", [");

        write(einzel, jahr, "Types.individual");
        // System.out.println("Mannschaft");
        write(mannschaft, jahr, "Types.team");

        System.out.println("  ]),");
        // System.out.println(String.format("$recs[%s] = array(\"e\" => $rec%se, \"m\" => $rec%sm);", jahr, jahr, jahr));
    }

    private static void write(Regelwerk aks, int year, String suffix) {

        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);

            sb.append("    new Agegroup(" + suffix + ", \"");
            sb.append(ak.getName());
            sb.append("\", ");
            if (ak.isDisciplineChoiceAllowed()) {
                sb.append(ak.getUsedDisciplines());
            } else {
                sb.append(ak.getDiszAnzahl());
            }
            sb.append(", [\n");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                sb.append("      new Discipline(\"");
                sb.append(ak.getDisziplin(y, false).getName());
                sb.append("\", [\n");
                sb.append("        new Record(Sexes.female, ");
                sb.append(((double) ak.getDisziplin(y, false).getRec()) / 100);
                sb.append("),\n");
                sb.append("        new Record(Sexes.male, ");
                sb.append(((double) ak.getDisziplin(y, true).getRec()) / 100);
                sb.append("),\n");
                sb.append("      ]),\n");
            }
            sb.append("    ]),\n");
        }
        String text = sb.toString();
        //text = text.replace("ä", "&auml;");
        //text = text.replace("ö", "&ouml;");
        //text = text.replace("ü", "&uuml;");
        //text = text.replace("Ä", "&Auml;");
        //text = text.replace("Ö", "&Ouml;");
        //text = text.replace("Ü", "&Uuml;");
        //text = text.replace("ß", "&szlig;");
        System.out.print(text);
    }
}
