package de.df.jauswertung.gui;

import de.df.jauswertung.gui.akeditor.JAKsEditor;
import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.veranstaltung.Veranstaltungswertung;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.util.PrinterInit;
import de.df.jauswertung.util.DefaultInit;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.dm.collector.JCollector;

/**
 * @author Dennis Fabri @date 23.04.2017
 */
public final class Launcher {

    private Launcher() {
        // Hide constructor
    }

    public static void main(String[] args) {

        DefaultInit.init();
        PrinterInit.init();
        DesignInit.init(Utils.getPreferences().getBoolean("MayUseSystemLaF", true), Utils.getUIPerformanceMode());

        if ((args != null) && (args.length > 0) && (args[0] != null)) {
            String name = args[0].trim();
            if (name.startsWith("-")) {
                if (name.equalsIgnoreCase("-einzelak") || name.equalsIgnoreCase("-ake")
                        || name.equalsIgnoreCase("-rwe")) {
                    launchAKsEditor(true);
                    return;
                }
                if (name.equalsIgnoreCase("-ak") || name.equalsIgnoreCase("-rw")) {
                    launchAKsEditor((args.length > 1 ? args[1] : null));
                    return;
                }
                if (name.equalsIgnoreCase("-mannschaftak") || name.equalsIgnoreCase("-akm")
                        || name.equalsIgnoreCase("-rwm")) {
                    launchAKsEditor(false);
                    return;
                }
                if ((name.equalsIgnoreCase("-strafenkatalog"))) {
                    launchStrafenkatalog();
                    return;
                }
                if ((name.equalsIgnoreCase("-veranstaltungswertung"))) {
                    launchVeranstaltungswertung();
                    return;
                }
                if (name.equalsIgnoreCase("-alphaserver") || name.equalsIgnoreCase("-as")) {
                    launchAlphaServer();
                    return;
                }
            }
            if (name.toLowerCase().endsWith(".wk")) {
                JAuswertungLauncher.main(new String[] { "-open", name });
                return;
            }
            if (name.toLowerCase().endsWith(".wkmm")) {
                JAuswertungTeammembersLauncher.main(new String[] { "-open", name });
                return;
            }
            if (name.toLowerCase().endsWith(".rwe")) {
                launchAKsEditor(name);
                return;
            }
            if (name.toLowerCase().endsWith(".rwm")) {
                launchAKsEditor(name);
                return;
            }
            if (name.toLowerCase().endsWith(".vs")) {
                launchVeranstaltungswertung(name);
                return;
            }
        } else {
            launchAKsEditor(null);
            return;
        }
        DialogUtils.error(null, I18n.get("Error"), I18n.get("Error.Programstart.WrongParameter"),
                I18n.get("Error.Programstart.WrongParameter.Note", argsToString(args)));
    }

    private static Object argsToString(String[] args) {
        if ((args == null) || (args.length == 0)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            boolean quote = arg.contains(" ");
            if (quote) {
                sb.append("\"");
            }
            sb.append(arg);
            if (quote) {
                sb.append("\"");
            }
        }
        return sb.toString();
    }

    private static void launchAKsEditor(String name) {
        EDTUtils.executeOnEDT(() -> new JAKsEditor(name).setVisible(true));
    }

    private static void launchAKsEditor(boolean einzel) {
        EDTUtils.executeOnEDT(() -> new JAKsEditor(einzel).setVisible(true));
    }

    private static void launchStrafenkatalog() {
        EDTUtils.executeOnEDT(() -> PenaltyUIUtils.showPenalties(InputManager.ladeStrafen(null, true)));
    }

    private static void launchAlphaServer() {
        EDTUtils.executeOnEDT(JCollector::run);
    }

    private static void launchVeranstaltungswertung() {
        EDTUtils.executeOnEDT(Veranstaltungswertung::start);
    }

    private static void launchVeranstaltungswertung(String filename) {
        EDTUtils.executeOnEDT(() -> Veranstaltungswertung.start(filename));
    }
}