/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.ComponentListPrintable;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri @date 17.10.2004
 */
class LaufeinteilungPrinter implements Printer {

    private CorePlugin        core;
    private IPluginManager    controller;

    private JPanel            panel;
    private JButton           print;
    private JButton           preview;
    private JComboBox<String> selection;
    private JCheckBox         hlw;

    public LaufeinteilungPrinter(IPluginManager window, CorePlugin plugin) {
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

        selection = new JComboBox<String>(new String[] { I18n.get("Heats"), I18n.get("AgeGroups"), I18n.get("Organization"), I18n.get("Compact") });

        hlw = new JCheckBox(I18n.get("PrintLaufeinteilungIncludeZW"));

        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(new JLabel(I18n.get("GroupedBy")), CC.xy(2, 2));
        panel.add(selection, CC.xy(4, 2));
        panel.add(hlw, CC.xy(6, 2));
        panel.add(preview, CC.xy(8, 2));
        panel.add(print, CC.xy(10, 2));
    }

    @SuppressWarnings("rawtypes")
    void checkLaufliste(AWettkampf wk) {
        boolean b = wk.hasSchwimmer() && wk.hasLaufliste() && !wk.isHeatBased();

        print.setEnabled(b);
        preview.setEnabled(b);
        selection.setEnabled(b);

        hlw.setEnabled(b && wk.hasHLW() && wk.hasHLWListe());
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
        return I18n.get("Heatarrangement");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste(wk);
    }

    Printable getPrintable() {
        boolean withhlw = hlw.isSelected() && hlw.isEnabled();
        Printable internal = null;
        switch (selection.getSelectedIndex()) {
        case 1: {
            // Altersklasse
            AWettkampf<?> wk = core.getWettkampf();
            JTable[] tables = TableHeatUtils.getLaufeinteilungTabellenJeAK(wk, withhlw);
            Component[] titles = new Component[tables.length];
            int ak = 0;
            int male = -1;
            for (int x = 0; x < tables.length; x++) {
                boolean first = true;
                while (first || SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(ak), male == 1).isEmpty()) {
                    first = false;
                    male++;
                    if (male > 1) {
                        male = 0;
                        ak++;
                    }
                }
                TableModel tm = tables[x].getModel();
                if (tm instanceof ExtendedTableModel) {
                    titles[x] = PrintUtils.createHeaderPanel(wk.getRegelwerk(), wk.getRegelwerk().getAk(ak), male == 1, false, true);
                } else {
                    titles[x] = null;
                }
            }
            internal = PrintManager.getPrintable(tables, titles, JTablePrintable.OPT_ALL, true, true);
            break;
        }
        case 2: {
            // Gliederung
            AWettkampf<?> wk = core.getWettkampf();
            JTable[] tables = TableHeatUtils.getLaufeinteilungTabellenJeGliederung(wk, withhlw);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                ExtendedTableModel etm = (ExtendedTableModel) tables[x].getModel();
                titles[x] = etm.getName();
            }
            internal = PrintManager.getPrintable(tables, titles, JTablePrintable.OPT_ALL, true, true);
            break;
        }
        case 3: {
            // Kompakt
            AWettkampf<?> wk = core.getWettkampf();
            JTable table = TableHeatUtils.getLaufeinteilungTabelle(wk, withhlw);
            // SimpleTableModel etm = (SimpleTableModel) table.getModel();
            internal = PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true);
            break;
        }
        default: {
            // Läufe
            JTable[] tables = TableHeatUtils.getLaufeinteilungTabellen(core.getWettkampf(), withhlw);
            Component[] components = new Component[tables.length];
            for (int x = 0; x < tables.length; x++) {
                if (PrintManager.getFont() != null) {
                    tables[x].setFont(PrintManager.getFont());
                    tables[x].getTableHeader().setFont(PrintManager.getFont());
                }
                JPrintTable.initPrintableJTable(tables[x]);
                FormLayout layout = new FormLayout("fill:default:grow", "fill:default,0dlu,fill:default");
                JPanel p = new JPanel(layout);

                p.add(tables[x].getTableHeader(), CC.xy(1, 1));
                p.add(tables[x], CC.xy(1, 3));
                components[x] = p;
            }
            internal = new ComponentListPrintable(components);
            break;
        }
        }
        return PrintManager.getFinalPrintable(internal, core.getLastChangedDate(), I18n.get("Heatarrangement"), I18n.get("Heatarrangement"));
    }

    /**
     * 
     */
    void print() {
        PrintManager.print(getPrintable(), I18n.get("Heatarrangement"), controller.getWindow());
    }

    void preview() {
        PrintableCreator pc = new PrintableCreator() {
            @Override
            public Printable create() {
                return getPrintable();
            }
        };
        PrintManager.preview(controller.getWindow(), pc, I18n.get("Heatarrangement"), IconManager.getIconBundle(), IconManager.getTitleImages());
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