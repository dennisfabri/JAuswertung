/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.StartkartenPrintable;
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
class StartkartenPrinter implements Printer {

    IPluginManager controller;

    private JPanel panel;
    private JComboBox<String> pages;
    private JCheckBox etime;

    public StartkartenPrinter(IPluginManager window) {
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        JButton empty = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        empty.addActionListener(new EmptyActionListener());
        JButton preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        etime = new JCheckBox(I18n.get("ElektronischeZeitnahme"));

        pages = new JComboBox<>(new String[] { "1", "2", "4" });
        pages.setSelectedIndex(2);

        panel.add(etime, CC.xy(2, 2));
        panel.add(pages, CC.xy(4, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(6, 2));
        panel.add(preview, CC.xy(8, 2));
        panel.add(empty, CC.xy(10, 2));
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
        return I18n.get("Startkarten");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        // Nothing to do
    }

    Printable getPrintable() {
        StartkartenPrintable sp = new StartkartenPrintable((AWettkampf[]) null, getMode(), false, true, 0, 0);
        sp.setEtime(etime.isSelected());
        return sp;
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
            PrintExecutor.print(getPrintable(), I18n.get("Startkarten"), true, controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = StartkartenPrinter.this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Startkarten"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}