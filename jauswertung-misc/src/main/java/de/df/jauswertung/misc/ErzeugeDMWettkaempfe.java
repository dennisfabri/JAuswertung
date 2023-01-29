package de.df.jauswertung.misc;

import java.awt.Font;
import java.io.IOException;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.io.TableEntryException;
import de.df.jauswertung.io.TableException;
import de.df.jauswertung.io.TableFormatException;
import de.df.jutils.print.PrintManager;

public class ErzeugeDMWettkaempfe {

    private static final String[] FILES = new String[] { "ak12", "ak13", "ak15", "ak17", "akOffen" };

    private static final String[] OLD_FILES = new String[] { "dm1999", "dm00", "dm01", "dm02", "dm03", "dm04" };

    private static final String[] NEW_FILES = new String[] { "dm05", "dm06", "dm07", "dm08" };

    /**
     * @param args
     */
    public static void main(String[] args)
            throws TableFormatException, IOException, TableEntryException, TableException {

        try {
            PrintManager.setFont(new Font("Arial", Font.PLAIN, 8));
        } catch (RuntimeException re) {
            // Nothing to do
        }

        for (int x = 0; x < OLD_FILES.length; x++) {
            MiscImportUtils.createFiles("src/test/resources/competitions/" + OLD_FILES[x] + "/");
            writeWK(OLD_FILES[x], true, 1999 + x);
            writeWK(OLD_FILES[x], false, 1999 + x);
        }

        for (int x = 0; x < NEW_FILES.length; x++) {
            writeWK(NEW_FILES[x], true, 2005 + x);
            writeWK(NEW_FILES[x], false, 2005 + x);
        }

        System.out.println("Fertig");
    }

    @SuppressWarnings({ "unchecked" })
    private static void writeWK(String name, boolean einzel, int jahr)
            throws TableFormatException, IOException, TableEntryException, TableException {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = null;
        String aks = null;
        String str = null;
        if (jahr < 2007) {
            aks = "src/test/resources/rulebooks/DLRG 1999";
            str = "src/test/resources/rulebooks/DLRG 1999";
        } else if (jahr <= 2015) {
            aks = "src/test/resources/rulebooks/DLRG " + jahr;
            str = "src/test/resources/rulebooks/DLRG " + jahr;
        } else {
            aks = "DLRG " + jahr;
            str = "DLRG " + jahr;
        }
        if (einzel) {
            wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/main/files/aks/" + aks + ".rwe"),
                    InputManager.ladeStrafen(str, true));
        } else {
            wk = new MannschaftWettkampf(AgeGroupIOUtils.ladeAKs("src/main/files/aks/" + aks + ".rwm"),
                    InputManager.ladeStrafen(str, false));
        }
        for (int x = 0; x < FILES.length; x++) {
            for (int y = 0; y < 2; y++) {
                String file = "src/test/resources/competitions/" + name + "/" + (einzel ? "einzel-" : "mannschaft-")
                        + FILES[x] + (y == 1 ? "m" : "w")
                        + (jahr >= 2005 ? ".csv" : ".txt");
                System.out.println("Reading file: " + file);
                if (jahr >= 2005) {
                    MiscImportUtils.importFile(wk, file, jahr);
                } else {
                    MiscImportUtils.importFile(wk, x, y == 1, file, jahr);
                }
            }
        }
        String filename = "src/test/resources/competitions/results/dm" + jahr + (einzel ? "-einzel" : "-mannschaft");
        System.out.println("Schreibe: " + filename);
        OutputManager.speichereWettkampf(filename + ".wk", wk);

        /*
         * FileOutputStream fos = new FileOutputStream(filename + ".pdf");
         * ExportManager.export(new PdfExporter(), fos, ExportManager.RESULTS, wk,
         * null); fos.close();
         */
    }
}
