/*
 * defaultInit.java Created on 16. August 2001, 19:45
 */

package de.df.jauswertung.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.jdesktop.swinghelper.debug.CheckThreadViolationRepaintManager;
import org.jdesktop.swinghelper.debug.EventDispatchThreadHangMonitor;
import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.jfx.FileChooserJFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.print.PrintManager;
import skt.swing.scroll.ScrollGestureRecognizer;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

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

        if (Utils.isInDevelopmentMode()) {
            // RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
            // EventDispatchThreadHangMonitor.initMonitoring();
        }
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

        UIStateUtils.initUIState();
        initFileChooser();

        SwingUtilities.invokeLater(() -> {
            try {
                ScrollGestureRecognizer.getInstance();
                initPrintManager();
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        });
    }

    private static void initFileChooser() {
        FileChooserUtils.initialize(new FileChooserJFX());
        SwingUtilities.invokeLater(() -> {
            try {
                new JFileChooser();
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