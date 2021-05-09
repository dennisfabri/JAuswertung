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

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JDetailsDialog;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.ZWStartkartenPrintable;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class FilledZWStartkartenPrinter implements Printer {

    CorePlugin        core;
    IPluginManager    controller;

    private JPanel    panel;
    private JButton   print;
    private JButton   preview;
    private JComboBox pages;
    private JCheckBox fillnames;
    private JComboBox sorting;

    boolean           allheats = true;
    int               minheat  = 0;
    int               maxheat  = 0;

    public FilledZWStartkartenPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu,"
                + "fill:default,4dlu,fill:default,4dlu", FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        pages = new JComboBox(new String[] { "1", "2", "4" });
        pages.setSelectedIndex(2);

        sorting = new JComboBox(new String[] { I18n.get("Lane"), I18n.get("Heat") });
        sorting.setSelectedIndex(1);

        fillnames = new JCheckBox(I18n.get("PrintNamesOfTeammembersShort"));
        fillnames.setToolTipText(I18n.getToolTip("PrintNamesOfTeammembersShort"));
        fillnames.setSelected(true);
        fillnames.setVisible(false);

        fillnames.setEnabled(false);
        sorting.setEnabled(false);
        pages.setEnabled(false);
        preview.setEnabled(false);
        print.setEnabled(false);

        panel.add(fillnames, CC.xy(2, 2));
        panel.add(pages, CC.xy(4, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(6, 2));
        panel.add(new JLabel(I18n.get("SortBy")), CC.xy(8, 2));
        panel.add(sorting, CC.xy(10, 2));
        panel.add(preview, CC.xy(12, 2));
        panel.add(print, CC.xy(14, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = false;
        if (wk.hasSchwimmer()) {
            if (!wk.getHLWListe().isEmpty()) {
                b = true;
            }
        }

        fillnames.setVisible(wk instanceof MannschaftWettkampf);

        fillnames.setEnabled(b && (wk instanceof MannschaftWettkampf));
        sorting.setEnabled(b);
        pages.setEnabled(b);
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
        return I18n.get("FilledZWStartkarten");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    private boolean isSortedByLane() {
        return sorting.getSelectedIndex() == 0;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    Printable getPrintable() {
        return new ZWStartkartenPrintable(core.getWettkampf(), getMode(), allheats, minheat, maxheat, fillnames.isVisible() && fillnames.isSelected(),
                isSortedByLane(), PrintUtils.barcodeType);
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

    @SuppressWarnings({ "unchecked" })
    <T extends ASchwimmer> boolean askForDetails() {
        JDetailsDialog<T> details = new JDetailsDialog<T>(controller.getWindow(), (AWettkampf<T>) core.getWettkampf(), I18n.get("ZWStartkarten"), true);
        EDTUtils.setVisible(details, true);
        if (details.isOk()) {
            allheats = details.printAllHeats();
            minheat = details.getMinHeat();
            maxheat = details.getMaxHeat();
            return true;
        }
        return false;
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (askForDetails()) {
                PrintExecutor.print(getPrintable(), I18n.get("FilledZWStartkarten"), true, controller.getWindow());
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (askForDetails()) {
                PrintableCreator pc = new PrintableCreator() {
                    @Override
                    public Printable create() {
                        return getPrintable();
                    }
                };
                PrintExecutor.preview(controller.getWindow(), pc, I18n.get("FilledZWStartkarten"), IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}