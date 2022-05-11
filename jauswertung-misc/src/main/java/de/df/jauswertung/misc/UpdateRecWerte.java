package de.df.jauswertung.misc;

import java.io.IOException;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.CsvUtils;
import de.df.jauswertung.io.IOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;

public class UpdateRecWerte {

    private static final int YEAR = 2022;

    private static final String PathToCsv = "src\\test\\resources\\rec-werte\\";
    private static final String PathToRulebook = "..\\jauswertung-files\\src\\main\\resources\\aks\\";

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        recwerte(null, PathToCsv + "Rec-Werte %s Einzel.csv", PathToRulebook + "DLRG %s.rwe", "Regelwerk %s", YEAR);
        recwerte(null, PathToCsv + "Rec-Werte %s Mannschaft.csv", PathToRulebook + "DLRG %s.rwm", "Regelwerk %s", YEAR);
    }

    private static void recwerte(AWettkampf wk, String werte, String regelwerk, String beschreibung, int jahr) throws IOException {
        recwerte(wk, String.format(werte, jahr), String.format(regelwerk, jahr), String.format(beschreibung, jahr));
    }

    private static void recwerte(AWettkampf wk, String werte, String regelwerk, String beschreibung) {
        System.out.println("Importiere \"" + werte + "\"");
        Regelwerk rw = (Regelwerk) InputManager.ladeObject(regelwerk);

        for (int x = 0; x < rw.size(); x++) {
            Altersklasse ak = rw.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                for (int z = 0; z < 2; z++) {
                    Disziplin d = ak.getDisziplin(y, z == 1);
                    d.setRec(0);
                }
            }
        }

        Object[][] table = CsvUtils.read(werte);
        for (int x = 1; x < table.length; x++) {
            try {
                Object[] row = table[x];
                String agegroup = fixAgegroupName(row[1]);
                String discipline = fixDisciplineName(row[2]);

                int index = rw.getIndex(agegroup);
                // System.out.println(agegroup + "/" + discipline + " -> " + index);
                Altersklasse ak = rw.getAk(index);
                boolean maennlich = de.df.jauswertung.io.ImportUtils.getMaennlich(wk, discipline, 2, x, "CSV", werte);
                String disziplin = row[3].toString();
                int zeit = de.df.jauswertung.io.ImportUtils.getTime(row[4]);
                int dindex = de.df.jauswertung.io.ImportUtils.getDisciplineIndex(ak, disziplin);
                Disziplin dz = ak.getDisziplin(dindex, maennlich);
                System.out.println(agegroup + " " + discipline + " - " + disziplin + " -> " + zeit);
                dz.setRec(zeit);
            } catch (Exception ex) {
                System.out.println("Konnte Zeile " + x + " nicht verarbeiten.");
                ex.printStackTrace();
                return;
            }
        }

        for (int x = 0; x < rw.size(); x++) {
            Altersklasse ak = rw.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                for (int z = 0; z < 2; z++) {
                    Disziplin d = ak.getDisziplin(y, z == 1);
                    if (d.getRec() == 0) {
                        System.out.println("Ein Rekord wurde nicht gesetzt: " + I18n.getAgeGroupAsString(rw, ak, z == 1)
                                + " - " + d.getName());
                        return;
                    }
                }
            }
        }

        rw.setBeschreibung(beschreibung);

        System.out.println(regelwerk);

        OutputManager.speichereObject(regelwerk, rw);

        System.out.println("Fertig");
    }

    private static String fixDisciplineName(Object disciplineName) {
        return disciplineName.toString().replace("0 m", "0m").replace("5 m", "5m");
    }

    private static String fixAgegroupName(Object agegroupName) {
        return agegroupName.toString().replace("0 m", "0m").replace("5 m", "5m");
    }
}
