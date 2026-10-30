/*
 * defaultInit.java Created on 16. August 2001, 19:45
 */

package de.df.jauswertung.util;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.print.PrintManager;
import org.jdesktop.swinghelper.debug.CheckThreadViolationRepaintManager;
import org.lisasp.swing.filechooser.platform.PlatformFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skt.swing.scroll.ScrollGestureRecognizer;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * Enth\u00e4lt eine init-Methode, die jedes Auswertungsprogramm beim
 * Programmstart ausf\u00fchren muss.
 */
public final class DefaultInit {

    static final Logger logger = LoggerFactory.getLogger(DefaultInit.class);

    private DefaultInit() {
        // Hide
    }

    private static boolean initialized = false;

    /**
     * Allgemeine Initialisierungsmethode.
     */
    public static synchronized void init() {
        logger.debug("init()");

        if (initialized) {
            return;
        }
        initialized = true;

        if (BetaFeature.EDT_CHECKS.isEnabled()) {
            CheckThreadViolationRepaintManager.init();
            // EventDispatchThreadHangMonitor.initMonitoring();
        }
        SystemStreamsToLog.install();

        UIStateUtils.initUIState();
        PlatformFileChooser.initialize();

        SwingUtilities.invokeLater(() -> {
            try {
                ScrollGestureRecognizer.getInstance();
                initPrintManager();
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        });
    }

    static void initPrintManager() {
        // Init headers and footers
        MessageFormat leftFooter = new MessageFormat(I18n.get("StateAtTime"));
        MessageFormat centerFooter = new MessageFormat(I18n.get("PrintFooter"));
        MessageFormat rightFooter = new MessageFormat(I18n.get("ProgrammerShortInfo"));

        MessageFormat rightHeader = new MessageFormat(I18n.get("HeaderRight"));
        MessageFormat leftHeader = new MessageFormat(I18n.get("HeaderLeft"));

        PrintManager.setHeaderMessages(leftHeader, rightHeader);
        PrintManager.setFooterMessages(leftFooter, centerFooter, rightFooter);

        // Init font
        Font font = null;
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font1 : fonts) {
            String fontname = font1.getName();
            if (fontname.equals("DLRG Univers 55 Roman")) {
                font = font1;
                break;
            }
            if (fontname.equals("DLRG-Jugend Sans")) {
                font = font1;
            }
        }
        if (font != null) {
            font = font.deriveFont(Font.PLAIN, 10);
        }
        if (font != null) {
            PrintManager.setFont(font);
        }
    }
}
