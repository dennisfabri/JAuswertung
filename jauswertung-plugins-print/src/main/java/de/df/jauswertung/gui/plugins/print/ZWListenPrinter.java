/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

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
import de.df.jauswertung.gui.util.TableZWUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.JTablePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ZWListenPrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JButton preview;

    public ZWListenPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        print.setEnabled(false);
        preview.setEnabled(false);

        panel.add(preview, CC.xy(2, 2));
        panel.add(print, CC.xy(4, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkHlwliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = false;
        if (wk.hasSchwimmer()) {
            if (!wk.getHLWListe().isEmpty()) {
                b = true;
            }
        }
        print.setEnabled(b);
        preview.setEnabled(b);
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
        return I18n.get("ZWList");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkHlwliste();
    }

    Printable getPrintable() {
        JTable table = TableZWUtils.getZWUebersicht(core.getWettkampf());
        Printable p = PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_HEADER, false, true);
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), I18n.get("ZWList"), I18n.get("ZWList"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("ZWList"), true, controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        class PPrintableCreator implements PrintableCreator {
            @Override
            public Printable create() {
                return getPrintable();
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(), I18n.get("ZWList"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}