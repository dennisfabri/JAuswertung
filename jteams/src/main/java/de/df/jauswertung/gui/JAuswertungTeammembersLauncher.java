package de.df.jauswertung.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.util.PrinterInit;
import de.df.jauswertung.util.DefaultInit;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.awt.ProgressSplashWindow;
import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.io.NullOutputStream;
import de.df.jutils.plugin.PluginManager;
import de.df.jutils.plugin.RemoteAction;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.TimeMeasurement;

/**
 * @since 27.09.2005
 */
public final class JAuswertungTeammembersLauncher {

    private JAuswertungTeammembersLauncher() {
        // Hide constructor
    }

    private static RemoteAction[] processCommandline(String[] args) {
        if (args == null) {
            return new RemoteAction[0];
        }
        LinkedList<RemoteAction> actions = new LinkedList<RemoteAction>();
        int x = 0;
        while (x + 1 < args.length) {
            if ((args[x] != null) && (args[x + 1] != null)) {
                String verb = args[x].trim();
                String command = args[x + 1].trim();
                if (verb.startsWith("-")) {
                    actions.addLast(new RemoteAction(args[x].substring(1).trim(), command));
                }
            }
            x += 2;
        }
        return actions.toArray(new RemoteAction[actions.size()]);
    }

    private static Image getImage() {
        try {
            return ImageIO.read(new File(Utils.getUserDir() + "images/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DefaultInit.init();

        TimeMeasurement tm = new TimeMeasurement(System.out, 5);

        tm.start("Loading JTeams");

        PrinterInit.init();
        DesignInit.init(Utils.getPreferences().getBoolean("MayUseSystemLaF", true), Utils.getUIPerformanceMode());

        tm.start("Loading Splashscreen");
        ProgressSplashWindow splash = new ProgressSplashWindow(getImage(), 4, false);
        splash.setBackground(Color.WHITE);
        splash.setForeground(new Color(30, 20, 190));
        splash.setVisible(true);
        splash.setStatus(I18n.get("Init"));
        tm.quit("Splashscreen loaded");

        tm.start("Processing commandline");
        RemoteAction[] actions = processCommandline(args);
        tm.quit("Processing finished");

        UpdateEvent.installReasonChecker(new JAuswertungReasonChecker());
        Preferences prefs = Utils.getPreferences();
        PluginManager japm = new PluginManager("JTeams", IconManager.getTitleImages(), tm, splash, true, prefs.getBoolean("ForceSingleInstance", false),
                "plugins-mmm");
        japm.setVerbose(Utils.isInDevelopmentModeFor("PluginManager"));
        japm.sendDataUpdateEvent("Init", UpdateEventConstants.REASON_EVERYTHING_CHANGED | UpdateEventConstants.REASON_STARTUP, null);

        japm.getWindow().setExtendedState(Frame.MAXIMIZED_BOTH);
        UIStateUtils.uistatemanage(japm.getWindow(), "JAuswertungTeammembersWindow");

        japm.start(actions);
        EDTUtils.setVisible(splash, false);

        tm.quit("JAuswertung loaded");
    }
}