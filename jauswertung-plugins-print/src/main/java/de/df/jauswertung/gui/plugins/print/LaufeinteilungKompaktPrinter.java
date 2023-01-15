/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.ComponentListPrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class LaufeinteilungKompaktPrinter implements Printer {

    private CorePlugin core;
    private IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;

    public LaufeinteilungKompaktPrinter(IPluginManager window, CorePlugin plugin) {
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

        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(preview, CC.xy(2, 2));
        panel.add(print, CC.xy(4, 2));
    }

    void checkLaufliste() {
        AWettkampf<?> wk = core.getWettkampf();
        boolean b = wk.hasSchwimmer() && wk.hasLaufliste() && !wk.isHeatBased();
        if (b) {
            b = TableHeatUtils.checkLaufeinteilungKompaktTabellen(wk);
        }
        print.setEnabled(b);
        preview.setEnabled(b);
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
        return I18n.get("HeatarrangementCompact");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    Printable getPrintable() {
        try {
            JTable[] tables = TableHeatUtils.getLaufeinteilungKompaktTabellen(core.getWettkampf());
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
            Printable internal = new ComponentListPrintable(components);
            return PrintManager.getFinalPrintable(internal, core.getLastChangedDate(), I18n.get("Heatarrangement"),
                    I18n.get("Heatarrangement"));
        } catch (Exception t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 
     */
    void print() {
        PrintExecutor.print(getPrintable(), I18n.get("HeatarrangementCompact"), controller.getWindow());
    }

    void preview() {
        PrintableCreator pc = this::getPrintable;
        PrintExecutor.preview(controller.getWindow(), pc, I18n.get("HeatarrangementCompact"),
                IconManager.getIconBundle(), IconManager.getTitleImages());
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