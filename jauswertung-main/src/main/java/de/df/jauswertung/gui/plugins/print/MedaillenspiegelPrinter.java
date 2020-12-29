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
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class MedaillenspiegelPrinter implements Printer {

    private CorePlugin     core;
    private IPluginManager controller;

    private JPanel         panel;
    private JButton        print;
    private JButton        preview;
    private JLabel         filter;

    public MedaillenspiegelPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        FormLayout layout = new FormLayout("4dlu:grow,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(filter, CC.xy(2, 2));
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
    }

    @SuppressWarnings("rawtypes")
    void checkMeldungen(AWettkampf wk) {
        boolean b = wk.hasSchwimmer() && !wk.isHeatBased();
        print.setEnabled(b);
        preview.setEnabled(b);
        filter.setVisible(b && core.getWettkampf().isFilterActive());
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
        return I18n.get("OverviewOfMedals");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk);
    }

    Printable getPrintable() {
        JTable result = new JTable(CompetitionUtils.generateMedaillenspiegel(core.getFilteredWettkampf()));
        JTableUtils.setAlignmentRenderer(result, new int[] { SwingConstants.RIGHT, SwingConstants.LEFT }, SwingConstants.CENTER);
        JTableUtils.setPreferredCellWidths(result);
        return PrintManager.getFinalPrintable(PrintManager.getPrintable(result, (String) null, JTablePrintable.OPT_ALL, true, true), core.getLastChangedDate(),
                getName(), getName());
    }

    void print() {
        PrintManager.print(getPrintable(), getName(), true, controller.getWindow());
    }

    void preview() {
        PrintManager.preview(controller.getWindow(), new MeldelistenPC(), getName(), IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    private final class MeldelistenPC implements PrintableCreator {

        public MeldelistenPC() {
            // Nothing to do
        }

        @Override
        public Printable create() {
            return getPrintable();
        }
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            print();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            preview();
        }
    }
}