/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.MultiplePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class DisciplineresultsOverAllPrinter implements Printer {

    CorePlugin       core          = null;
    IPluginManager   controller    = null;

    private JPanel   panel         = null;
    private JButton  print         = null;
    private JButton  preview       = null;
    private JLabel   warning       = null;
    private JLabel   filter        = null;

    JSelectionDialog printDialog   = null;
    JSelectionDialog previewDialog = null;

    @SuppressWarnings("rawtypes")
    private void initDialogs() {
        if (printDialog == null) {
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"), JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"), JSelectionDialog.MODE_AK_SELECTION);
        }
    }

    public DisciplineresultsOverAllPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        panel = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu"));

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */

    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("DisciplineOverAllAgeGroups");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {

        boolean result = false;
        if (wk.isHeatBased()) {
            result = false;
        } else {
            if (filteredwk != null) {
                if (filteredwk.hasSchwimmer()) {
                    result = true;
                }
            }
        }

        print.setEnabled(result);
        preview.setEnabled(result);
        checkWarning(filteredwk);
        filter.setVisible(result && wk.isFilterActive());
    }

    @SuppressWarnings("rawtypes")
    void checkWarning(AWettkampf wk) {
        boolean check = !wk.isCompetitionComplete();
        warning.setVisible(preview.isEnabled() && check);
    }

    /**
     * @param selected
     */
    @SuppressWarnings("rawtypes")
    Printable getPrintable(boolean[][] selected) {
        AWettkampf wk = core.getFilteredWettkampf();

        @SuppressWarnings("unchecked")
        LinkedList<Printable> ps = PrintUtils.getBestOfDisciplineResultsPrintable(selected, wk, true, true);
        return PrintManager.getFinalPrintable(new MultiplePrintable(ps), wk.getLastChangedDate(), true, I18n.get("DisciplineOverAllAgeGroups"));
    }

    void printResults(boolean[][] selection) {
        PrintManager.print(getPrintable(selection), I18n.get("DisciplineOverAllAgeGroups"), true, controller.getWindow());
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
        PrintManager.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("DisciplineOverAllAgeGroups"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        int disz = JSelectionDialog.ALL_DISCIPLINES;
        previewDialog.setCompetition(core.getFilteredWettkampf());
        previewDialog.setDiscipline(disz);
        previewDialog.setVisible(true);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        int disz = JSelectionDialog.ALL_DISCIPLINES;
        printDialog.setCompetition(core.getFilteredWettkampf());
        printDialog.setDiscipline(disz);
        printDialog.setVisible(true);
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