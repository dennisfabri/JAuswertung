/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.PseudoTable;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.ComponentListPrintable2;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ListOfMedalsPrinter implements Printer {

    private CorePlugin core;
    private IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JLabel filter;
    private JCheckBox bigPrint;

    private static final int ORGANISATION = 2;

    private float fontscale = 1.8f;
    private int gapscale = 50;

    public ListOfMedalsPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        bigPrint = new JCheckBox("Schrift vergößern");
        bigPrint.setEnabled(false);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(bigPrint, CC.xy(2, 2));
        panel.add(filter, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    @SuppressWarnings("rawtypes")
    void checkMeldungen(AWettkampf wk) {
        boolean b = wk.hasSchwimmer() && !wk.isHeatBased();
        bigPrint.setEnabled(b);
        print.setEnabled(b);
        preview.setEnabled(b);
        filter.setVisible(b && core.getWettkampf().isFilterActive());
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
        return I18n.get("ListOfMedals");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk);
    }

    Printable getPrintable() {
        return getPrintable(bigPrint.isSelected());
    }

    Printable getPrintable(boolean big) {
        LinkedList<ExtendedTableModel> results = new LinkedList<>();

        AWettkampf<?> wk = Utils.copy(core.getFilteredWettkampf());
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                @SuppressWarnings("rawtypes")
                SchwimmerResult[] result = ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(x), y == 1);

                int max = -1;
                for (int z = 0; z < result.length; z++) {
                    int place = result[z].getPlace();
                    if (result[z].getPoints() > 0.001) {
                        if ((place > 0) && (place <= 3)) {
                            max = z;
                        }
                    }
                }
                if (max >= 0) {
                    max++;
                    Object[][] data = new Object[max][5];
                    for (int z = 0; z < max; z++) {
                        data[z][0] = result[z].getPlace() + " ";
                        data[z][1] = " " + result[z].getSchwimmer().getName() + " ";
                        data[z][2] = " " + result[z].getSchwimmer().getGliederung() + " ";
                        data[z][3] = " " + result[z].getSchwimmer().getQualifikationsebene() + " ";
                        data[z][4] = StringTools.punkteString(result[z].getPoints()) + "  ";
                    }
                    ExtendedTableModel etm = new ExtendedTableModel(data,
                            new String[] { I18n.get("Rank"), I18n.get("Name"), I18n.get("Organisation"), " ",
                                    I18n.get("Points") });
                    etm.setColumnAlignments(
                            new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT,
                                    SwingConstants.LEFT, SwingConstants.RIGHT });
                    etm.setName(I18n.getAgeGroupAsString(wk.getRegelwerk(), wk.getRegelwerk().getAk(x), y == 1));
                    results.addLast(etm);
                }
            }
        }

        int versteckteSpalte = PrintUtils.isOrganisationOmitted(wk) ? ORGANISATION : PseudoTable.NO_HIDDEN_COLUMN;
        JComponent[] parts = getResults(results.toArray(new ExtendedTableModel[results.size()]), versteckteSpalte,
                big);

        return PrintManager.getFinalPrintable(
                new ComponentListPrintable2(big ? gapscale : 0, false, parts),
                core.getLastChangedDate(),
                getName(), getName());
    }

    private JComponent[] getResults(ExtendedTableModel[] etm, int versteckteSpalte, boolean big) {

        JComponent[] result = new JComponent[etm.length];
        for (int x = 0; x < etm.length; x++) {

            JLabel title = new JLabel(etm[x].getName());
            title.setForeground(Color.BLACK);
            title.setBackground(Color.WHITE);
            Font f = PrintManager.getFont();
            Font font = f.deriveFont(f.getSize2D() * (big ? fontscale : 1.0f));
            title.setFont(font);

            FormLayout layout = new FormLayout(FormLayoutUtils.createGrowingLayoutString(1),
                    "0dlu,fill:default,1dlu,fill:default,2dlu");
            JComponent c = new JPanel(layout);
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);

            c.add(title, CC.xy(2, 2, "center,center"));
            c.add(PseudoTable.create(etm[x], font, versteckteSpalte), CC.xy(2, 4));

            result[x] = c;
        }
        return result;
    }

    void print() {
        final boolean big = bigPrint.isSelected();
        PrintExecutor.print(() -> getPrintable(big), getName(), true, controller.getWindow());
    }

    void preview() {
        PrintExecutor.preview(controller.getWindow(), new ListOfMedalsPC(), getName(), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    private final class ListOfMedalsPC implements PrintableCreator {

        public ListOfMedalsPC() {
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
