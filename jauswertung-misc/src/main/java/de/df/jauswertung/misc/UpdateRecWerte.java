package de.df.jauswertung.misc;

import java.io.IOException;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.*;

public class UpdateRecWerte {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        recwerte("..\\..\\..\\src\\test\\resources\\rec-werte\\Rec-Werte %s Einzel.csv", "aks\\DLRG %s.rwe", "Regelwerk %s", 2021);
        recwerte("..\\..\\..\\src\\test\\resources\\rec-werte\\Rec-Werte %s Mannschaft.csv", "aks\\DLRG %s.rwm", "Regelwerk %s",
                2021);
    }

    private static void recwerte(String werte, String regelwerk, String beschreibung, int jahr) throws IOException {
        recwerte(String.format(werte, jahr), String.format(regelwerk, jahr), String.format(beschreibung, jahr));
    }

    private static void recwerte(String werte, String regelwerk, String beschreibung) throws IOException {
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
                int index = rw.getIndex(row[1].toString());
                // System.out.println(row[2].toString() + " -> "+index);
                Altersklasse ak = rw.getAk(index);
                boolean maennlich = de.df.jauswertung.io.ImportUtils.getMaennlich(row[2]);
                String disziplin = row[3].toString();
                int zeit = de.df.jauswertung.io.ImportUtils.getTime(row[4]);
                int dindex = de.df.jauswertung.io.ImportUtils.getDisciplineIndex(ak, disziplin);
                Disziplin dz = ak.getDisziplin(dindex, maennlich);
                System.out.println(row[1].toString() + " " + row[2] + " - " + disziplin + " -> " + zeit);
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
}
