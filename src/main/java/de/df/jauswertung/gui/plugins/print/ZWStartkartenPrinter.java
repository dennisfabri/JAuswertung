/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.ZWStartkartenPrintable;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ZWStartkartenPrinter implements Printer {

    IPluginManager    controller;

    private JPanel    panel;
    private JComboBox pages;

    /**
     * 
     */
    public ZWStartkartenPrinter(IPluginManager window) {
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        JButton empty = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        empty.addActionListener(new PrintActionListener());
        JButton preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        pages = new JComboBox(new String[] { "1", "2", "4" });
        pages.setSelectedIndex(2);

        panel.add(pages, CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(empty, CC.xy(8, 2));
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
        return I18n.get("ZWStartkarten");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        // Nothing to do
    }

    @SuppressWarnings("rawtypes")
    Printable getPrintable() {
        return new ZWStartkartenPrintable(getMode());
    }

    private PageMode getMode() {
        switch (pages.getSelectedIndex()) {
        case 0:
            return PageMode.ONE_PER_PAGE;
        case 1:
            return PageMode.TWO_PER_PAGE;
        default:
            return PageMode.FOUR_PER_PAGE;
        }
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintManager.print(getPrintable(), I18n.get("ZWStartkarten"), true, controller.getWindow());
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
            PrintManager.preview(controller.getWindow(), pc, I18n.get("ZWStartkarten"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}