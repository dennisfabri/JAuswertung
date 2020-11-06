/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.GraphUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class DocumentsPrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JButton preview;

    private JLabel  warning = null;
    private JLabel  filter  = null;

    public DocumentsPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu," + "fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        print.setEnabled(false);
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
        return I18n.get("Documents");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        Object o = filteredwk.getProperty(PropertyConstants.URKUNDE);
        boolean b = (o != null) && filteredwk.hasSchwimmer() && !wk.isHeatBased();
        /*
         * if (b) { b = false; Altersklassen aks = wk.getAks(); for (int x = 0;
         * x < aks.size(); x++) { for (int y = 0; y < 2; y++) { JResultTable
         * result = JResultTable.getResultTable( filteredwk, aks.getAk(x), y ==
         * 1); if (result.getRowCount() > 0) { double points =
         * result.getPunkte(0); if (points > 0.005) { b = true; break; } } } if
         * (b) { break; } } }
         */

        warning.setVisible(!filteredwk.isCompetitionComplete());
        filter.setVisible(filteredwk.isFilterActive());

        print.setEnabled(b);
        preview.setEnabled(b);
    }

    void previewResults(boolean[][] selection) {
        PrintManager.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("Documents"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    void printResults(boolean[][] selection) {
        PrintManager.print(GraphUtils.getPrintable(core.getWettkampf(), selection, false), I18n.get("Documents"), true, controller.getWindow());
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        previewDialog.setCompetition(core.getFilteredWettkampf());
        previewDialog.setDiscipline(JSelectionDialog.ALL_DISCIPLINES);
        previewDialog.setMinPoints(0.005);
        previewDialog.setVisible(true);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        printDialog.setCompetition(core.getFilteredWettkampf());
        printDialog.setDiscipline(JSelectionDialog.ALL_DISCIPLINES);
        printDialog.setMinPoints(0.005);
        printDialog.setVisible(true);
    }

    private class PPrintableCreator implements PrintableCreator {

        private boolean[][] selection;

        public PPrintableCreator(boolean[][] selection) {
            this.selection = selection;
        }

        @Override
        public Printable create() {
            return GraphUtils.getPrintable(core.getWettkampf(), selection, false);
        }

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

    JSelectionDialog printDialog   = null;
    JSelectionDialog previewDialog = null;

    private void initDialogs() {
        if (printDialog == null) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"), JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"), JSelectionDialog.MODE_AK_SELECTION);
        }
    }
}