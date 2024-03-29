/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.TeamnamesRegistrationPrintable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class RegisteredTeamnamesPrinter implements Printer {

    private CorePlugin core;
    private IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JLabel filter;
    private JComboBox<String> organisation;
    private DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

    public RegisteredTeamnamesPrinter(IPluginManager window, CorePlugin plugin) {
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

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        // order = new JComboBox(new String[] { I18n.get("Startnumber"),
        // I18n.get("Organisation"), I18n.get("AgeGroup"),
        // I18n.get("Name") });
        // order.setSelectedIndex(2);
        organisation = new JComboBox<>(model);

        FormLayout layout = new FormLayout(
                "0dlu,fill:default,4dlu:grow,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(filter, CC.xy(4, 2));
        panel.add(new JLabel(I18n.get("Organisation")), CC.xy(6, 2));
        panel.add(organisation, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    @SuppressWarnings("rawtypes")
    void checkMeldungen(AWettkampf wk) {
        boolean b = wk instanceof MannschaftWettkampf && wk.hasSchwimmer();
        if (b) {
            boolean found = false;
            MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
            for (Mannschaft m : mwk.getSchwimmer()) {
                if (m.hasMannschaftsmitglieder()) {
                    found = true;
                    break;
                }
            }
            b = found;
        }
        print.setEnabled(b);
        preview.setEnabled(b);
        filter.setVisible(b && core.getWettkampf().isFilterActive());
        organisation.setEnabled(b);
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
        return I18n.get("RegisteredTeamnames");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk);

        if (filteredwk instanceof MannschaftWettkampf) {
            if (due.isReason(UpdateEventConstants.REASON_GLIEDERUNG_CHANGED
                    | UpdateEventConstants.REASON_FILTER_SELECTION | UpdateEventConstants.REASON_FILTERS_CHANGED
                    | UpdateEventConstants.REASON_LOAD_WK | UpdateEventConstants.REASON_NEW_TN
                    | UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_SWIMMER_CHANGED)) {
                MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
                int index = organisation.getSelectedIndex();
                model.removeAllElements();
                for (String s : mwk.getGliederungenMitQGliederung().toArray(new String[0])) {
                    model.addElement(s);
                }
                model.addElement(I18n.get("All"));
                if (index >= 0 && index < model.getSize()) {
                    organisation.setSelectedIndex(index);
                }
            }
        } else {
            model.removeAllElements();
        }

    }

    Printable getPrintable() {
        MannschaftWettkampf wk = (MannschaftWettkampf) (AWettkampf<?>) core.getFilteredWettkampf();
        String gliederung = organisation.getSelectedIndex() + 1 == model.getSize() ? null
                : organisation.getSelectedItem().toString();
        return new TeamnamesRegistrationPrintable(wk, gliederung);
    }

    void print() {
        PrintExecutor.print(getPrintable(), I18n.get("RegisteredTeamnames"), true, controller.getWindow());
    }

    void preview() {
        PrintableCreator pc = new MeldelistenPC();
        PrintExecutor.preview(controller.getWindow(), pc, I18n.get("RegisteredTeamnames"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    private final class MeldelistenPC implements PrintableCreator {

        public MeldelistenPC() {
        }

        @Override
        public Printable create() {
            return getPrintable();
        }
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