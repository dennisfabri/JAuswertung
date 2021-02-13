/*
 * defaultInit.java Created on 16. August 2001, 19:45
 */

package de.df.jauswertung.util;

/**
 * @author Dennis Fabri
 * @version 0.1
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.print.PageFormat;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.jdesktop.swinghelper.debug.CheckThreadViolationRepaintManager;
import org.jdesktop.swinghelper.debug.EventDispatchThreadHangMonitor;
import org.lisasp.legacy.uistate.UIStateHandler;
import org.lisasp.legacy.uistate.UIStateManager;
import org.lisasp.legacy.uistate.handlers.JListStateHandler;
import org.lisasp.legacy.uistate.handlers.JTabbedPaneStateHandler;
import org.lisasp.legacy.uistate.handlers.JTableStateHandler;
import org.lisasp.legacy.uistate.handlers.JTreeStateHandler;
import org.lisasp.legacy.uistate.handlers.JViewportStateHandler;
import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.jfx.FileChooserJFX;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.io.NullOutputStream;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.PrintManager;
import net.java.swingfx.waitwithstyle.InfiniteProgressPanel;
import skt.swing.scroll.ScrollGestureRecognizer;

/**
 * Enth\u00e4lt eine init-Methode, die jedes Auswertungsprogramm beim
 * Programmstart ausf\u00fchren muss.
 */
public final class DefaultInit {

    private DefaultInit() {
        // Hide
    }

    private static boolean initialized = false;

    /**
     * Allgemeine Initialisierungsmethode.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (Utils.isInDevelopmentMode()) {
            RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
            EventDispatchThreadHangMonitor.initMonitoring();
        } else {
            System.setOut(new PrintStream(new NullOutputStream()));
            System.setErr(new PrintStream(new NullOutputStream()));
        }

        initUIState();
        initFileChooser();

        SwingUtilities.invokeLater(() -> {
            try {
                ScrollGestureRecognizer.getInstance();
                new JFileChooser();

                initPrintSetup();
                initPrintManager();
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        });

        // Set the focus color to our blue
        InfiniteProgressPanel.setColorFocus(new Color(4, 22, 188));
    }

    private static void initFileChooser() {
        FileChooserUtils.initialize(new FileChooserJFX());
    }

    private static void initUIState() {
        try {
            UIStateManager.setPreferences(Preferences.userRoot().node("jauswertung/uistate"));
            List<UIStateHandler> l = UIStateManager.getDefaultHandlers();
            ListIterator<UIStateHandler> li = l.listIterator();
            while (li.hasNext()) {
                Object o = li.next();
                if (o instanceof JTreeStateHandler || o instanceof JListStateHandler || o instanceof JTableStateHandler
                        || o instanceof JViewportStateHandler || o instanceof JTabbedPaneStateHandler) {
                    li.remove();
                }
            }
            UIStateManager.setDefaultHandlers(l);
            l = UIStateManager.getDefaultHandlers();
            PrintStream err = System.err;
            if (l.size() != 5) {
                err.println("Changes in UIStateManager:");
                li = l.listIterator();
                while (li.hasNext()) {
                    err.println("  " + li.next());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings({ "unchecked" })
    static void initPrintSetup() {
        Object o = Utils.readFromPreferences("PrintSettings");
        Hashtable<String, PageFormat> prasTable = (Hashtable<String, PageFormat>) o;
        if (prasTable == null) {
            prasTable = new Hashtable<>();
            prasTable.put(I18n.get("Results"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("GroupEvaluation"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Meldezeiten"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Laufliste"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("ZWList"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Disciplines"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("BrokenRecords"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
        }
        PageSetup.setPRASTable(prasTable);
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
        PrintManager.setFont(font);
    }
}