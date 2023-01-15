/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_FILTERS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_FILTER_SELECTION;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.ZeitenVergleicher;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.JTablePrintable;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri @date 17.10.2004
 */
class BrokenRecordsPrinter implements Printer {

    CorePlugin core;
    private IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JLabel filter;
    private JLabel warning;
    private JComboBox<String> agegroup;
    private JComboBox<String> allRecords;

    public BrokenRecordsPrinter(IPluginManager window, CorePlugin plugin) {
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

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        agegroup = new JComboBox<String>();
        agegroup.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isUpdating) {
                    return;
                }
                checkWarning(core.getFilteredWettkampf());
            }
        });

        allRecords = new JComboBox<String>(
                new String[] { I18n.get("BrokenRecords.All"), I18n.get("BrokenRecords.ValidOnly") });
        allRecords.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkWarning(core.getFilteredWettkampf());
            }
        });
        allRecords.setSelectedIndex(1);

        panel = new JPanel(
                new FormLayout(
                        "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,"
                                + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        "4dlu,fill:default,4dlu"));

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(allRecords, CC.xy(6, 2));
        panel.add(agegroup, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    private boolean isUpdating = false;

    void checkMeldungen(@SuppressWarnings("rawtypes") AWettkampf wk, long reason) {
        try {
            isUpdating = true;

            boolean result = false;
            if (wk != null && wk.isHeatBased()) {
                agegroup.removeAllItems();
                result = false;
            } else if ((wk != null) && (wk.hasSchwimmer())) {
                if ((reason & (REASON_AKS_CHANGED | REASON_NEW_TN | REASON_FILTER_SELECTION | REASON_POINTS_CHANGED
                        | REASON_FILTERS_CHANGED | REASON_LOAD_WK
                        | REASON_NEW_WK)) > 0) {
                    result = false;
                    int index = Math.max(0, agegroup.getSelectedIndex());
                    LinkedList<String> names = new LinkedList<String>();
                    agegroup.removeAllItems();
                    Regelwerk aks = wk.getRegelwerk();
                    for (int x = 0; x < aks.size(); x++) {
                        result = true;
                        names.addLast(aks.getAk(x).getName());
                    }
                    names.addLast(I18n.get("All"));
                    agegroup.setModel(new DefaultComboBoxModel<String>(names.toArray(new String[names.size()])));
                    if (names.size() == 0) {
                        agegroup.addItem(I18n.get("Empty"));
                    }
                    if (agegroup.getSelectedIndex() < 0) {
                        index = agegroup.getItemCount() - 1;
                    }
                    if (agegroup.getItemCount() <= index) {
                        index = agegroup.getItemCount() - 1;
                    }
                    agegroup.setSelectedIndex(index);
                } else {
                    result = false;
                    agegroup.removeAllItems();
                }
            }

            agegroup.setEnabled(result && (agegroup.getItemCount() > 1));
            allRecords.setEnabled(result);
            // print.setEnabled(result);
            // preview.setEnabled(result);
            filter.setVisible(result && (wk != null) && wk.isFilterActive());
            checkWarning(wk);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
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
        return I18n.get("BrokenRecords");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk, due.getChangeReason());
    }

    Printable getPrintable() {
        Printable p = getPrintable(core.getFilteredWettkampf(), agegroup.getSelectedIndex());
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), getName(), getName());

    }

    static boolean checkRecords(AWettkampf<ASchwimmer> wk, int index, boolean onlyValid) {
        int akmin = 0;
        int akmax = wk.getRegelwerk().size();
        if ((index >= 0) && (index < wk.getRegelwerk().size())) {
            akmin = index;
            akmax = index + 1;
        }
        for (int x = akmin; x < akmax; x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<ASchwimmer> swimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    for (ASchwimmer s : swimmer) {
                        Disziplin d = ak.getDisziplin(z, y == 1);
                        if ((s.getZeit(z) < d.getRec()) && (s.getZeit(z) > 0)) {
                            boolean ok = !onlyValid || !s.getAkkumulierteStrafe(z).isStrafe();
                            if (ok) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    JTable getData(AWettkampf<ASchwimmer> wk, int index, boolean onlyValid) {
        int akmin = 0;
        int akmax = wk.getRegelwerk().size();
        if ((index >= 0) && (index < wk.getRegelwerk().size())) {
            akmin = index;
            akmax = index + 1;
        }
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (int x = akmin; x < akmax; x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<ASchwimmer> swimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    Collections.sort(swimmer, new ZeitenVergleicher(z));
                    for (ASchwimmer s : swimmer) {
                        if (onlyValid && s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
                            continue;
                        }
                        Disziplin d = ak.getDisziplin(z, y == 1);
                        if ((s.getZeit(z) < d.getRec()) && (s.getZeit(z) > 0)
                                && (!onlyValid || !s.getAkkumulierteStrafe(z).isStrafe())) {
                            Object[] data = new Object[8];
                            data[0] = s.getName();
                            Strafe str = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                            if (str.isStrafe()) {
                                data[0] = data[0] + " (" + I18n.getPenaltyShort(str) + ")";
                            }
                            data[1] = s.getGliederung();
                            data[2] = s.getQualifikationsebene();
                            data[3] = I18n.getAgeGroupAsString(s);
                            data[4] = StringTools.zeitString(s.getZeit(z));
                            data[5] = PenaltyUtils.getPenaltyMediumText(s.getAkkumulierteStrafe(z), ak);
                            data[6] = d.getName();
                            data[7] = StringTools.zeitString(d.getRec());
                            result.addLast(data);
                            if (onlyValid) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        TableModel tm = new DefaultTableModel(result.toArray(new Object[result.size()][0]),
                new Object[] { I18n.get("Name"), I18n.get("Organisation"), "",
                        I18n.get("AgeGroup"), I18n.get("Time"), "", I18n.get("Discipline"), I18n.get("Rec-Value") });
        JTable table = new JTable(tm);
        if (PrintUtils.printOmitOrganisationForTeams && (((AWettkampf) wk) instanceof MannschaftWettkampf)) {
            JTableUtils.hideColumnAndRemoveData(table, 1);
        }
        return table;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    Printable getPrintable(AWettkampf wk, int index) {
        return new JTablePrintable(getData(wk, index, allRecords.getSelectedIndex() == 1), JTablePrintable.OPT_ALL,
                true, PrintMode.FIT_WIDTH, true,
                PrintManager.getFont());
    }

    void print() {
        PrintExecutor.print(getPrintable(), getName(), true, controller.getWindow());
    }

    void preview() {
        PrintExecutor.preview(controller.getWindow(), new ListOfMedalsPC(), getName(), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    private final class ListOfMedalsPC implements PrintableCreator {

        public ListOfMedalsPC() {
            // Nothing to do
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

    @SuppressWarnings("rawtypes")
    void checkWarning(AWettkampf wk) {
        boolean check = false;
        int index = agegroup.getSelectedIndex();
        if ((index < wk.getRegelwerk().size()) && (index >= 0)) {
            check = !wk.isAgegroupComplete(index);
        } else {
            check = !wk.isCompetitionComplete();
        }
        warning.setVisible(check);

        @SuppressWarnings("unchecked")
        boolean b = checkRecords(wk, index, allRecords.getSelectedIndex() == 1);
        print.setEnabled(b);
        preview.setEnabled(b);
    }
}