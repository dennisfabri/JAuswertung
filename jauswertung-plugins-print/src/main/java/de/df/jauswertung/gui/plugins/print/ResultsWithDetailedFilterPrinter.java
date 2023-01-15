/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.plugins.core.AgegroupResultSelection;
import de.df.jauswertung.gui.plugins.core.JResultsSelectionButton;
import de.df.jauswertung.gui.plugins.core.ResultSelectionUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.UnterschriftPrintable;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.MultiplePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ResultsWithDetailedFilterPrinter implements Printer {

    CorePlugin core = null;
    IPluginManager controller = null;
    WarningPlugin warn = null;

    private JPanel panel = null;
    private JButton print = null;
    private JButton preview = null;
    private JResultsSelectionButton diszipline = null;
    private JLabel warning = null;
    private JLabel filter = null;
    private JCheckBox unterschrift = null;

    JSelectionDialog printDialog = null;
    JSelectionDialog previewDialog = null;

    private void initDialogs() {
        if (printDialog == null) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"),
                    JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"),
                    JSelectionDialog.MODE_AK_SELECTION);
        }
    }

    public ResultsWithDetailedFilterPrinter(IPluginManager window, CorePlugin plugin, WarningPlugin warn) {
        core = plugin;
        controller = window;
        this.warn = warn;
        initGUI();
    }

    private void initGUI() {
        panel = new JPanel(
                new FormLayout(
                        "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,"
                                + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        "4dlu,fill:default,4dlu"));

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotCompleteOrSelected"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        diszipline = new JResultsSelectionButton(() -> ResultSelectionUtils.getResultWettkampf(core.getFilteredWettkampf()));
        diszipline.addItemListener(arg0 -> {
            if (arg0.getStateChange() == ItemEvent.DESELECTED) {
                checkWarning(ResultSelectionUtils.getResultWettkampf(core.getFilteredWettkampf()));
            }
        });

        unterschrift = new JCheckBox(I18n.get("Unterschrift"));
        unterschrift.setSelected(Utils.getPreferences().getBoolean("ResultsWithSignature", false));
        unterschrift.addActionListener(arg0 -> {
            Utils.getPreferences().putBoolean("ResultsWithSignature", unterschrift.isSelected());
        });
        unterschrift.setEnabled(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(unterschrift, CC.xy(6, 2));
        panel.add(diszipline, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */
    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("ResultsWithDetailedFilter");
    }

    @Override
    public <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk) {
        boolean result = false;
        if (wk.isHeatBased()) {
            result = false;
        } else {
            if (filteredwk == null) {
                filteredwk = wk;
            } else {
                if (filteredwk.hasSchwimmer()) {
                    result = true;
                }
            }
        }

        diszipline.setEnabled(result);
        print.setEnabled(result);
        preview.setEnabled(result);
        unterschrift.setEnabled(result);
        checkWarning(filteredwk);
        filter.setVisible(result && wk.isFilterActive());
    }

    @SuppressWarnings("rawtypes")
    void checkWarning(AWettkampf wk) {
        @SuppressWarnings("unchecked")
        boolean check = !ResultSelectionUtils.IsCompleteSelection(wk, diszipline.getSelection(wk));
        warning.setVisible(preview.isEnabled() && check);
    }

    private <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
        AWettkampf<T> wk = ResultSelectionUtils.getResultWettkampf(core.getFilteredWettkampf());
        return ResultSelectionUtils.getResultWettkampf(wk, diszipline.getSelection(wk));
    }

    /**
     * @param selected
     */
    <T extends ASchwimmer> Printable getPrintable(boolean[][] selected) {
        AWettkampf<T> wk = getWettkampf();

        LinkedList<T> remove = new LinkedList<>();
        for (int x = 0; x < selected.length; x++) {
            for (int y = 0; y < selected[x].length; y++) {
                if (!selected[x][y]) {
                    remove.addAll(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(y), x == 1));
                }
            }
        }
        wk.removeSchwimmer(remove);

        AgegroupResultSelection[] selection = diszipline.getSelection(wk);

        boolean complete = ResultSelectionUtils.IsCompleteSelection(wk, selection) && wk.isCompetitionComplete();
        LinkedList<Printable> ps = null;
        if (complete) {
            ps = PrintUtils.getFullResultsPrintable(selected, wk, true, true, 0);
        } else {
            ps = PrintUtils.getIntermediateResults(selected, wk, wk.getRegelwerk().getMaxDisciplineCount(), true, true,
                    0);
        }
        Printable p = null;
        if (ps.size() == 1) {
            p = ps.getFirst();
        } else {
            p = new MultiplePrintable(ps);
        }
        if (unterschrift.isSelected()) {
            p = new UnterschriftPrintable(p);
        }
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), true, I18n.get("Results"));
    }

    void printResults(boolean[][] selection) {
        PrintExecutor.print(getPrintable(selection), I18n.get("Results"), true, controller.getWindow());
    }

    private class PPrintableCreator implements PrintableCreator {

        private boolean[][] selection;

        public PPrintableCreator(boolean[][] selection) {
            this.selection = selection;
        }

        @Override
        public Printable create() {
            return getPrintable(selection);
        }
    }

    void previewResults(boolean[][] selection) {
        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("Results"),
                IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        showSelectionDialog(previewDialog);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        showSelectionDialog(printDialog);
    }

    private void showSelectionDialog(JSelectionDialog dialog) {
        if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
            return;
        }

        @SuppressWarnings("rawtypes")
        AWettkampf wk = getWettkampf();
        dialog.setCompetition(wk);
        dialog.setDiscipline(JSelectionDialog.ALL_DISCIPLINES);
        dialog.setVisible(true);
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPrintSelectionDialog();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPreviewSelectionDialog();
        }
    }

    final class PrintCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            printResults(selected);
        }
    }

    final class PreviewCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            previewResults(selected);
        }

    }
}