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
import de.df.jauswertung.print.FehlermeldekartenPrintable;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class FehlermeldekartenPrinter implements Printer {

    IPluginManager controller;

    private JPanel panel;
    private JComboBox pages;

    public FehlermeldekartenPrinter(IPluginManager parent) {
        controller = parent;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        JButton empty = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        empty.addActionListener(new EmptyActionListener());
        JButton preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        pages = new JComboBox(new String[]{"1", "2", "4"});
        pages.setSelectedIndex(2);

        panel.add(pages, CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(empty, CC.xy(8, 2));
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
        return I18n.get("Fehlermeldekarten");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        // Nothing to do
    }

    Printable getPrintable() {
        return new FehlermeldekartenPrintable(getMode());
    }

    private PageMode getMode() {
        return switch (pages.getSelectedIndex()) {
            case 0 -> PageMode.ONE_PER_PAGE;
            case 1 -> PageMode.TWO_PER_PAGE;
            default -> PageMode.FOUR_PER_PAGE;
        };
    }

    final class EmptyActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("Fehlermeldekarten"), true, controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = FehlermeldekartenPrinter.this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Fehlermeldekarten"),
                                  IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}
