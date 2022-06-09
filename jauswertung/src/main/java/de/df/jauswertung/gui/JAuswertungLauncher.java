package de.df.jauswertung.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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
import de.df.jutils.plugin.JPanelContainer;
import de.df.jutils.plugin.PluginManager;
import de.df.jutils.plugin.RemoteAction;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.TimeMeasurement;

/**
 * @since 27.09.2005
 */
public final class JAuswertungLauncher {

    private JAuswertungLauncher() {
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
            BufferedImage bix = ImageIO.read(new File(Utils.getUserDir() + "images/logo.png"));
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
            g.setColor(new Color(30, 20, 190));

            FontMetrics fm = g.getFontMetrics(f);

            int width = fm.stringWidth(I18n.get("Version"));

            g.drawString(I18n.get("Version"), bi.getWidth() - width - 35, bi.getHeight() - fm.getDescent());
            g.dispose();
            return bi;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean mode = JPanelContainer.DOUBLE;

    /**
     * @param args
     */
    public static void main(String[] args) {
        DefaultInit.init();
        
        TimeMeasurement tm = new TimeMeasurement(System.out, 5);

        tm.start("Loading JAuswertung");

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
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        mode = JPanelContainer.DOUBLE;
        if (height < 1000) {
            mode = JPanelContainer.SINGLE;
        }
        Preferences prefs = Utils.getPreferences();
        SwingUtilities.invokeLater(() -> {
            PluginManager japm = new PluginManager("JAuswertung", IconManager.getTitleImages(), tm, splash,
                    prefs.getBoolean("WindowMode", mode), prefs.getBoolean("ForceSingleInstance", false), "plugins");
            japm.setVerbose(Utils.isInDevelopmentModeFor("PluginManager"));
            japm.sendDataUpdateEvent("Init",
                    UpdateEventConstants.REASON_EVERYTHING_CHANGED | UpdateEventConstants.REASON_STARTUP, null);

            japm.getWindow().setExtendedState(Frame.MAXIMIZED_BOTH);

            UIStateUtils.uistatemanage(japm.getWindow(), "JAuswertungMainWindow");

            japm.start(actions);
            EDTUtils.setVisible(splash, false);
        });

        tm.quit("JAuswertung loaded");
    }
}