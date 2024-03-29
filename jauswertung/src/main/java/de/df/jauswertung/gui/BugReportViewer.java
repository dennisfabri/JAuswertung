/*
 * BugReportViewer.java Created on 15. Oktober 2001, 22:45
 */

package de.df.jauswertung.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.DefaultInit;
import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.DialogUtils;

/**
 * @author Dennis Fabri
 * @version
 */
public final class BugReportViewer {

    private BugReportViewer() {
        // Hide constructor
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void analysiere(Object o, PrintWriter w) {
        if (o == null) {
            w.println("No Data!");
            return;
        }

        if (o instanceof List list) {
            w.println("List discovered:");
            ListIterator li = list.listIterator();
            while (li.hasNext()) {
                w.println("Element " + li.nextIndex() + ":");
                analysiere(li.next(), w);
            }
            return;
        }

        if (o instanceof AWettkampf wk) {
            if (wk instanceof EinzelWettkampf ewk) {
                w.print("Einzel: ");
                OutputManager.speichereWettkampf("../../../../bug.wk", ewk);

            }
            if (wk instanceof MannschaftWettkampf mwk) {
                w.print("Mannschaft: ");
                OutputManager.speichereWettkampf("../../../../bug.wk", mwk);
            }
            try {
                if (wk.hasSchwimmer()) {
                    ExportManager.export(ImportExportTypes.RESULTS, "../../../../results.html", "HTML", wk, null);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            w.println(o);
            return;
        }
        if (o instanceof Altersklasse) {
            w.println(o);
            return;
        }

        if (o instanceof String) {
            w.println(o);
            return;
        }

        w.println("Unknown Class: " + o.getClass().getName());
        w.println(o);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            DefaultInit.init();
            DesignInit.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String name = "bug.bug";
            if ((args != null) && (args.length > 0)) {
                name = args[0];
            }

            try {
                FileChooserUtils.setBaseDir(new File(System.getProperty("user.dir")).getParentFile().getParent());
            } catch (Exception ex) {
                // Nothing to do
            }

            String file = FileChooserUtils.openFile(null, "BugReport laden", new SimpleFileFilter("Bug", "bug"));
            if (file != null) {
                name = file;
            }
            BugReport br = InputManager.ladeBugReport(name);
            if (br != null) {
                try {
                    PrintWriter pw = new PrintWriter(System.out);
                    pw.print(br.toString());
                    analysiere(br.getDaten(), pw);
                    pw.close();
                } catch (Exception e) {
                    DialogUtils.showException(null, null, I18n.get("AnExceptionOcurred"),
                            I18n.get("AnExceptionOcurred.Note"), e);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Couldn't load BugReport!");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}