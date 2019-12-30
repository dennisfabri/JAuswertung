/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.print.PropertiesTableCreator;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class PropertiesPrinter implements Printer {

    CorePlugin     core       = null;
    IPluginManager controller = null;

    private JLabel filter;
    private JPanel panel      = null;

    public PropertiesPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {

        panel = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu"));

        JButton print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());

        JButton preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
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
        return I18n.get("CompetitionInformation");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        filter.setVisible(wk.isFilterActive());
    }

    @SuppressWarnings({})
    Printable getPrintable() {
        return PrintManager.getFinalPrintable(PropertiesTableCreator.getPrintable(core.getWettkampf()), core.getLastChangedDate(),
                I18n.get("CompetitionInformation"), I18n.get("CompetitionInformation"));
    }

    @SuppressWarnings({ "unchecked" })
    static Printable getPrintable(@SuppressWarnings("rawtypes") AWettkampf wk, Date date) {
        return PrintManager.getFinalPrintable(PrintManager.getHeaderPrintable(PropertiesTableCreator.getPrintable(wk), I18n.get("CompetitionInformation")),
                date, true, I18n.get("CompetitionInformation"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintManager.print(getPrintable(), I18n.get("CompetitionInformation"), true, controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = new PrintableCreator() {
                @Override
                public Printable create() {
                    return getPrintable();
                }
            };
            PrintManager.preview(controller.getWindow(), pc, I18n.get("CompetitionInformation"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}