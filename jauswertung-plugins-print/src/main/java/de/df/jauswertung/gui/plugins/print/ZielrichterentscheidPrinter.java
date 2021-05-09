package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.ZielrichterentscheidUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 13.07.2005
 */
public class ZielrichterentscheidPrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JButton preview;
    private JLabel  filter;

    public ZielrichterentscheidPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
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
        return I18n.get("Zielrichterentscheide");
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        LinkedList<Zielrichterentscheid<ASchwimmer>>[] zes = ZielrichterentscheidUtils.checkZielrichterentscheide(filteredwk);
        boolean b = !zes[0].isEmpty();
        print.setEnabled(b);
        preview.setEnabled(b);

        filter.setVisible(b && wk.isFilterActive());
    }

    @SuppressWarnings({})
    <T extends ASchwimmer> Printable getPrintable() {
        return PrintManager.getFinalPrintable(de.df.jauswertung.print.PrintableCreator.createZielrichterentscheidPrintable(core.getWettkampf()), core.getLastChangedDate(), true, I18n.get("Zielrichterentscheide"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static boolean checkWettkampf(JFrame parent, AWettkampf wk) {
        LinkedList<Zielrichterentscheid<ASchwimmer>>[] zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk);
        if (zes[1].isEmpty()) {
            return true;
        }
        return DialogUtils.askAndWarn(parent, I18n.get("Print"), I18n.get("ZielrichterentscheideFehlerhaftDrucken"),
                I18n.get("ZielrichterentscheideFehlerhaftDrucken.Note"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!checkWettkampf(controller.getWindow(), core.getFilteredWettkampf())) {
                return;
            }

            PrintExecutor.print(getPrintable(), I18n.get("Zielrichterentscheide"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!checkWettkampf(controller.getWindow(), core.getFilteredWettkampf())) {
                return;
            }

            PrintableCreator pc = new PrintableCreator() {
                @Override
                public Printable create() {
                    return getPrintable();
                }
            };
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Zielrichterentscheide"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}