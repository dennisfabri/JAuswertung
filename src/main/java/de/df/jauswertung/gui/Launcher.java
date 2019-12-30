package de.df.jauswertung.gui;

import de.df.jauswertung.gui.akeditor.JAKsEditor;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.veranstaltung.Veranstaltungswertung;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.*;
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
        DesignInit.init(Utils.getPreferences().getBoolean("MayUseSystemLaF", true), Utils.getUIPerformanceMode());
        if ((args != null) && (args.length > 0) && (args[0] != null)) {
            String name = args[0].trim();
            if (name.startsWith("-")) {
                if ((name.toLowerCase().equals("-einzelak")) || (name.toLowerCase().equals("-ake")) || (name.toLowerCase().equals("-rwe"))) {
                    launchAKsEditor(true);
                    return;
                }
                if (name.toLowerCase().equals("-ak") || name.toLowerCase().equals("-rw")) {
                    launchAKsEditor((args.length > 1 ? args[1] : null));
                    return;
                }
                if ((name.toLowerCase().equals("-mannschaftak")) || (name.toLowerCase().equals("-akm")) || (name.toLowerCase().equals("-rwm"))) {
                    launchAKsEditor(false);
                    return;
                }
                if ((name.toLowerCase().equals("-strafenkatalog"))) {
                    launchStrafenkatalog();
                    return;
                }
                if ((name.toLowerCase().equals("-veranstaltungswertung"))) {
                    launchVeranstaltungswertung();
                    return;
                }
                if ((name.toLowerCase().equals("-alphaserver")) || (name.toLowerCase().equals("-as"))) {
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
        StringBuffer sb = new StringBuffer();
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
        EDTUtils.executeOnEDT(new JAKsEditorRunnable1(name));
    }

    private static void launchAKsEditor(boolean einzel) {
        EDTUtils.executeOnEDT(new JAKsEditorRunnable2(einzel));
    }

    private static void launchStrafenkatalog() {
        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                PenaltyUtils.showPenalties(InputManager.ladeStrafen(null, true));
            }
        });
    }

    private static void launchAlphaServer() {
        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                JCollector.run();
            }
        });
    }

    private static void launchVeranstaltungswertung() {
        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                Veranstaltungswertung.start();
            }
        });
    }

    private static void launchVeranstaltungswertung(String name) {
        EDTUtils.executeOnEDT(new VeranstaltungswertungRunnable(name));
    }

    static final class VeranstaltungswertungRunnable implements Runnable {

        private String name;

        public VeranstaltungswertungRunnable(String n) {
            name = n;
        }

        @Override
        public void run() {
            Veranstaltungswertung.start(name);
        }
    }

    static final class JAKsEditorRunnable1 implements Runnable {

        private String name;

        public JAKsEditorRunnable1(String n) {
            name = n;
        }

        @Override
        public void run() {
            JAKsEditor editor;
            if (name == null) {
                editor = new JAKsEditor();

            } else {
                editor = new JAKsEditor(name);
            }
            editor.setVisible(true);
        }
    }

    static final class JAKsEditorRunnable2 implements Runnable {

        private boolean einzel;

        public JAKsEditorRunnable2(boolean n) {
            einzel = n;
        }

        @Override
        public void run() {
            JAKsEditor editor = new JAKsEditor(einzel);
            editor.setVisible(true);
        }
    }
}