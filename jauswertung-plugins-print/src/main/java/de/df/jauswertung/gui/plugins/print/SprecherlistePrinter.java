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

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.SprecherlistePrintable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class SprecherlistePrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;

    private JComboBox<String> columns;

    public SprecherlistePrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu," + "fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        columns = new JComboBox<>(
                new String[] { I18n.get("None"), I18n.get("Disciplines"), I18n.get("Meldezeiten"),
                        I18n.get("DisciplinesAndMeldezeiten") });
        columns.setSelectedIndex(3);

        print.setEnabled(false);
        preview.setEnabled(false);
        columns.setEnabled(false);

        panel.add(new JLabel(I18n.get("AdditionalColumns")), CC.xy(2, 2));
        panel.add(columns, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = false;
        if (wk.hasSchwimmer()) {
            if ((wk.getLaufliste() != null) && (wk.getLaufliste().getLaufliste() != null)) {
                b = true;
            }
        }
        print.setEnabled(b);
        preview.setEnabled(b);
        columns.setEnabled(b);
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
        return I18n.get("SpeakerHeatList");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    private static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, boolean discipline, boolean times) {
        boolean quali = false;
        for (T t : wk.getSchwimmer()) {
            if (t.getQualifikationsebene().length() > 0) {
                quali = true;
                break;
            }
        }
        return new SprecherlistePrintable<T>(wk, discipline, times, quali, false,
                !PrintUtils.printOmitOrganisationForTeams, PrintUtils.printYearOfBirth);
    }

    @SuppressWarnings({})
    Printable getPrintable() {
        boolean discipline = (columns.getSelectedIndex() % 2 != 0);
        boolean times = (columns.getSelectedIndex() > 1);
        return PrintManager.getFinalPrintable(
                PrintManager.getHeaderPrintable(getPrintable(core.getWettkampf(), discipline, times),
                        I18n.get("SpeakerHeatList")),
                core.getLastChangedDate(),
                true, I18n.get("SpeakerHeatList"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("SpeakerHeatList"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = SprecherlistePrinter.this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("SpeakerHeatList"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}