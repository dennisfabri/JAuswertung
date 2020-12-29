/*
 * BugReportViewer.java Created on 15. Oktober 2001, 22:45
 */

package de.df.jauswertung.misc;

import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.*;
import de.df.jutils.gui.util.*;

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

        if (o instanceof LinkedList) {
            w.println("LinkedList discovered:");
            ListIterator li = ((LinkedList) o).listIterator();
            while (li.hasNext()) {
                w.println("Element " + li.nextIndex() + ":");
                analysiere(li.next(), w);
            }
            return;
        }

        if (o instanceof AWettkampf) {
            if (o instanceof EinzelWettkampf) {
                w.print("Einzel: ");
                OutputManager.speichereWettkampf("../../bug.wk", (EinzelWettkampf) o);

            }
            if (o instanceof MannschaftWettkampf) {
                w.print("Mannschaft: ");
                OutputManager.speichereWettkampf("../../bug.wk", (MannschaftWettkampf) o);
            }
            try {
                if (((AWettkampf) o).hasSchwimmer()) {
                    ExportManager.export(ExportManager.RESULTS, "../../results.html", "HTML", (AWettkampf) o, null);
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
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        try {
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
                    DialogUtils.showException(null, null, I18n.get("AnExceptionOcurred"), I18n.get("AnExceptionOcurred.Note"), e);
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