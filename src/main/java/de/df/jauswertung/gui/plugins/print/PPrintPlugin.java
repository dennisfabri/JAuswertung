/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.daten.PropertyConstants.SHORTNAME;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.plugins.print.JSelectionDialog.PrintCallBack;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.UnterschriftPrintable;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ActionInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.JPFrame;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.MultiplePrintable;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintQueue;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
public class PPrintPlugin extends ANullPlugin {

    JPanel               panel     = null;

    private Printer[]    printers;

    private JComponent[] panels;
    private MenuInfo[]   menu;

    private CorePlugin   core      = null;
    WarningPlugin        warner;

    PanelInfo            panelInfo = null;

    PrintCallback        printcallback;

    public PPrintPlugin() {
        panelInfo = new PanelInfo(I18n.get("Print"), IconManager.getBigIcon("print"), false, true, 2000) {
            @Override
            public JPanel getPanelI() {
                if (panel == null) {
                    createPanel();
                    dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                }
                return panel;
            }
        };

        printcallback = new PrintCallback();
        PrintQueue.getInstance().addPrintCallback(printcallback);
    }

    void createPanel() {
        panel = new JPanel();
        panel.setName(I18n.get("Print"));

        initPrinters();
        initGUI();
        dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
    }

    void createMenu() {
        if (menu != null) {
            return;
        }

        JMenuItem m = new JMenuItem(I18n.get("Print"), IconManager.getSmallIcon("print"));
        m.setToolTipText(I18n.getToolTip("Printpanel"));
        m.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showPrintPanel();
            }
        });

        menu = new MenuInfo[1];
        menu[0] = new MenuInfo(I18n.get("File"), 100, m, 300);
    }

    void showPrintPanel() {
        warner.information(getController().getWindow(), I18n.get("Information"), I18n.get("PrintButton.Information"), I18n.get("PrintButton.Note"),
                "MenuPrint");
        getController().showPanel(I18n.get("Print"));
    }

    private void initPrinters() {
        printers = PrinterUtils.createPrinters(getController(), core, warner);

        panels = new JComponent[printers.length];
        String[] names = new String[printers.length];

        for (int x = 0; x < printers.length; x++) {
            names[x] = printers[x].getName();
            JPanel p = printers[x].getPanel();
            JTaskPaneGroup jt = new JTaskPaneGroup();
            jt.setUI(new GradientTaskPaneGroupUI());
            jt.setTitle(names[x]);
            // jt.setBorder(new ShadowLabeledBorder(names[x]));
            jt.setBackground(p.getBackground());
            if (x != 0) {
                jt.setExpanded(false);
            }
            panels[x] = jt;
            panels[x].add(p);
            panels[x].setOpaque(false);
        }
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(panels.length));
        panel.setLayout(layout);

        for (int x = 0; x < panels.length; x++) {
            panel.add(panels[x], CC.xy(2, 2 + 2 * x));
        }
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);

        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
        warner = (WarningPlugin) controller.getFeature("de.df.jauswertung.warning", pluginuid);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { panelInfo };
    }

    @Override
    public ActionInfo[] getActions() {
        return new ActionInfo[] { new ActionInfo(KeyEvent.VK_F11, 0, new Runnable() {
            @Override
            public void run() {
                printLastCompleted(false);
            }
        }), new ActionInfo(KeyEvent.VK_F12, 0, new Runnable() {
            @Override
            public void run() {
                printResults(false);
            }
        }), new ActionInfo(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK, new Runnable() {
            @Override
            public void run() {
                printLastCompleted(true);
            }
        }), new ActionInfo(KeyEvent.VK_F12, InputEvent.CTRL_DOWN_MASK, new Runnable() {
            @Override
            public void run() {
                printResults(true);
            }
        }) };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void dataUpdated(UpdateEvent due) {
        AWettkampf filteredwk = core.getFilteredWettkampf();
        if (panel == null) {
            return;
        }
        AWettkampf wk = core.getWettkampf();
        PrintManager.setName(wk.getStringProperty(NAME), wk.getStringProperty(SHORTNAME));
        PrintManager.setOrt(wk.getStringProperty(LOCATION));
        PrintManager.setDatum(wk.getStringProperty(DATE));
        for (Printer printer : printers) {
            try {
                long time = System.currentTimeMillis();
                printer.dataUpdated(due, wk, filteredwk);
                time = System.currentTimeMillis() - time;
                if (time > 0) {
                    System.out.println("    " + printer.getName() + ": " + time);
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                // Protect execution of normal code from uncatched exceptions
            }
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        createMenu();
        return menu;
    }

    @Override
    public void shutDown() {
        Utils.writeToPreferences("PrintSettings", PageSetup.getPRASTable());

        PrintQueue.getInstance().removePrintCallback(printcallback);
    }

    void printLastCompleted(boolean view) {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getFilteredWettkampf();

        int[] lastcomplete = wk.getLastComplete();
        if (lastcomplete[0] == -1) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        boolean[][] selected = new boolean[2][wk.getRegelwerk().size()];
        selected[lastcomplete[1]][lastcomplete[0]] = true;

        if (view) {
            PrintCallBack pcb = new PrintCallBack() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void print(boolean[][] select) {
                    printResults(core.getFilteredWettkampf(), select, true);
                }
            };
            JSelectionDialog dialog = new JSelectionDialog(getController().getWindow(), wk, pcb, I18n.get("Print"), false);
            dialog.setSelected(selected);
            dialog.setDiscipline(JSelectionDialog.AUTO_DISCIPLINES);
            dialog.setVisible(true);
            return;
        }
        printResults(wk, selected, view);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void printResults(boolean view) {
        AWettkampf wk = core.getFilteredWettkampf();

        boolean found = false;
        boolean[][] selected = new boolean[2][wk.getRegelwerk().size()];
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < selected[x].length; y++) {
                selected[x][y] = SearchUtils.hasSchwimmer(wk, wk.getRegelwerk().getAk(y), x == 1);
                if (selected[x][y]) {
                    found = true;
                }
            }
        }
        if (!found) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (view) {
            PrintCallBack pcb = new PrintCallBack() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void print(boolean[][] select) {
                    printResults(core.getFilteredWettkampf(), select, true);
                }
            };
            JSelectionDialog dialog = new JSelectionDialog(getController().getWindow(), wk, pcb, I18n.get("Print"), false);
            dialog.setDiscipline(JSelectionDialog.AUTO_DISCIPLINES);
            dialog.setVisible(true);
            return;
        }

        printResults(wk, selected, view);
    }

    @SuppressWarnings("unchecked")
    void printResults(@SuppressWarnings("rawtypes") AWettkampf wk, boolean[][] selected, boolean view) {
        LinkedList<Printable> ps = null;
        if (wk.isCompetitionComplete()) {
            ps = PrintUtils.getFullResultsPrintable(selected, wk, true, true, 0);
        } else {
            // int[] lastcomplete = wk.getLastComplete();
            ps = PrintUtils.getIntermediateResults(selected, wk, wk.getRegelwerk().getMaxDisciplineCount(), true, true, 0);
        }
        Printable p = PrintManager.getFinalPrintable(new UnterschriftPrintable(new MultiplePrintable(ps)), wk.getLastChangedDate(), true, I18n.get("Results"));
        PrintManager.print(p, I18n.get("Results"), true, getController().getWindow());
    }

    void showError(String title, String message, String note) {
        new ErrorScheduler(title, message, note).start();
    }

    boolean showErrorI(String title, String message, String note) {
        JFrame parent = getController().getWindow();
        if (parent.isEnabled()) {
            DialogUtils.warn(parent, title, message, note);
            return true;
        }
        return false;
    }

    private final class PrintCallback implements PrintQueue.IPrintCallback {
        @Override
        @SuppressWarnings("synthetic-access")
        public void jobFinished(String job, int jobs) {
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), ""));
        }

        @Override
        public void jobAdded(String job, int jobs) {
            jobStarted(job, jobs);
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void jobStarted(String job, int jobs) {
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), I18n.get("PrintingJobOfJobs", job, jobs)));
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void jobError(String job, int jobs, String title, String message, String note) {
            showError(title, message, note);
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), ""));
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void queueEmpty() {
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), null));
        }
    }

    private final class StatusTextChanger implements Runnable {

        private final JPFrame frame;
        private final String  text;

        public StatusTextChanger(JPFrame f, String t) {
            frame = f;
            text = t;
        }

        @Override
        public void run() {
            frame.setStatusBarText(text);
        }
    }

    private class ErrorScheduler extends Thread {

        private final String title;
        private final String message;
        private final String note;

        public ErrorScheduler(String title, String message, String note) {
            this.title = title;
            this.message = message;
            this.note = note;
        }

        @Override
        public void run() {
            boolean ok = false;
            while (!ok) {
                ok = EDTUtils.executeOnEDTwithReturn(() -> PPrintPlugin.this.showErrorI(title, message, note));
            }
        }
    }
}