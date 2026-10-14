package de.df.jauswertung.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.util.PrinterInit;
import de.df.jauswertung.util.DefaultInit;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.awt.ProgressSplashWindow;
import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.plugin.PluginManager;
import de.df.jutils.plugin.RemoteAction;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.TimeMeasurement;
import lombok.extern.slf4j.Slf4j;

/**
 * Starts an application built from plugins (JAuswertung, JTeams): splash window, command line, plugin manager and main
 * window.
 */
@Slf4j
public final class PluginApplicationLauncher {

    private static final Color BANNER_TEXT = new Color(30, 20, 190);

    private PluginApplicationLauncher() {
    }

    /**
     * @param windowKey key under which the size and position of the main window are remembered
     */
    public static void launch(String name, String pluginDirectory, String windowKey, boolean singleDisplayMode,
            String[] args) {
        DefaultInit.init();

        TimeMeasurement tm = new TimeMeasurement(System.out, 5);

        tm.start("Loading " + name);

        PrinterInit.init();
        DesignInit.init(Utils.getPreferences().getBoolean("MayUseSystemLaF", true), Utils.getUIPerformanceMode());

        tm.start("Loading Splashscreen");
        ProgressSplashWindow splash = new ProgressSplashWindow(getBanner(), 4, false);
        splash.setBackground(Color.WHITE);
        splash.setForeground(BANNER_TEXT);
        splash.setVisible(true);
        splash.setStatus(I18n.get("Init"));
        tm.quit("Splashscreen loaded");

        tm.start("Processing commandline");
        RemoteAction[] actions = processCommandline(args);
        tm.quit("Processing finished");

        UpdateEvent.installReasonChecker(new JAuswertungReasonChecker());
        Preferences prefs = Utils.getPreferences();
        SwingUtilities.invokeLater(() -> {
            PluginManager japm = new PluginManager(name, IconManager.getTitleImages(), tm, splash, singleDisplayMode,
                    prefs.getBoolean("ForceSingleInstance", false), pluginDirectory);
            japm.setVerbose(Utils.isInDevelopmentModeFor("PluginManager"));
            japm.sendDataUpdateEvent("Init",
                    UpdateEventConstants.REASON_EVERYTHING_CHANGED | UpdateEventConstants.REASON_STARTUP, null);

            japm.getWindow().setExtendedState(Frame.MAXIMIZED_BOTH);

            UIStateUtils.uistatemanage(japm.getWindow(), windowKey);

            japm.start(actions);
            EDTUtils.setVisible(splash, false);

            tm.quit(name + " loaded");
        });
    }

    /**
     * Turns pairs of {@code -verb value} into remote actions; a pair whose verb does not start with "-" is skipped.
     */
    static RemoteAction[] processCommandline(String[] args) {
        if (args == null) {
            return new RemoteAction[0];
        }
        List<RemoteAction> actions = new ArrayList<>();
        for (int x = 0; x + 1 < args.length; x += 2) {
            if ((args[x] != null) && (args[x + 1] != null)) {
                String verb = args[x].trim();
                if (verb.startsWith("-")) {
                    actions.add(new RemoteAction(verb.substring(1).trim(), args[x + 1].trim()));
                }
            }
        }
        return actions.toArray(new RemoteAction[0]);
    }

    private static Image getBanner() {
        try {
            BufferedImage bix = ImageIO.read(new File(Utils.getUserDir() + "images/banner.png"));
            BufferedImage bi = new BufferedImage(bix.getWidth(), bix.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            Font f = g.getFont();
            if (f == null) {
                f = (Font) UIManager.get("Label.font");
            }
            if (f != null) {
                f = f.deriveFont(12.0f);
            }
            g.setFont(f);
            g.drawImage(bix, 0, 0, null);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setBackground(Color.WHITE);
            g.setColor(BANNER_TEXT);

            FontMetrics fm = g.getFontMetrics(f);

            int width = fm.stringWidth(I18n.get("Version"));

            g.drawString(I18n.get("Version"), bi.getWidth() - width - 35, bi.getHeight() - fm.getDescent());
            g.dispose();
            return bi;
        } catch (IOException e) {
            log.warn("Banner could not be loaded", e);
            return null;
        }
    }
}
