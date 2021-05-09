package de.df.jauswertung.misc;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.AgeGroupIOUtils;

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

public class XamarinExport {

    public static void main(String[] args) {
        Regelwerk einzel = AgeGroupIOUtils.getDefaultAKs(true);
        Regelwerk mannschaft = AgeGroupIOUtils.getDefaultAKs(false);

        // System.out.println("Einzel");
        write("2020", einzel, "Single", true, true);
        // write("2017", einzel, "Single", false);
        // System.out.println("Mannschaft");
        write("2020", mannschaft, "Team", true, true);
        // write("2017", mannschaft, "Team", false);
    }

    private static void write(String jahr, Regelwerk aks, String ext, boolean junioren, boolean senioren) {
        if (!junioren && !senioren) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        int min = junioren ? 0 : 5;
        int max = !senioren ? 5 : aks.size();

        String ext2 = junioren && senioren ? "" : (junioren ? "Juniors" : "Seniors");

        sb.append("      public static readonly Agegroup[] " + ext + ext2 + " = new Agegroup[] {\n");
        for (int x = min; x < max; x++) {
            // int a = aks.getIndex(ids[x]);
            Altersklasse ak = aks.getAk(x);

            for (int y = 0; y < 2; y++) {
                String name = I18n.getAgeGroupAsString(aks, ak, y == 1);
                int amount1 = ak.getDiszAnzahl();
                int amount2 = ak.getUsedDisciplines();
                sb.append("        new Agegroup() { Name = \"" + name + "\", Description = \"\", AmountOfDisciplines = " + amount1
                        + ", CalculatedDisciplines = " + amount2 + ",\n");
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    int i = z + 1;
                    Disziplin d = ak.getDisziplin(z, y == 1);
                    String dname = d.getName();
                    int rec = d.getRec();
                    sb.append("          Discipline" + i + " = \"" + dname + "\", Record" + i + " = " + rec + ",\n");
                }
                sb.append("        },\n");
            }

        }
        sb.append("      };\n");
        String text = sb.toString();
        System.out.println(text);
    }
}
