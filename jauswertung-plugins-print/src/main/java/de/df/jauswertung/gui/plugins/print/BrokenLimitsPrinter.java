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
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
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
class BrokenLimitsPrinter implements Printer {

    CorePlugin core;
    private IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JLabel filter;
    private JLabel warning;
    private JComboBox<String> agegroup;

    public BrokenLimitsPrinter(IPluginManager window, CorePlugin plugin) {
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

        agegroup = new JComboBox<>();
        agegroup.addActionListener(e -> {
            if (isUpdating) {
                return;
            }
            checkWarning(core.getFilteredWettkampf());
        });

        panel = new JPanel(new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu"));

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(agegroup, CC.xy(6, 2));
        panel.add(preview, CC.xy(8, 2));
        panel.add(print, CC.xy(10, 2));
    }

    private boolean isUpdating = false;

    void checkMeldungen(@SuppressWarnings("rawtypes") AWettkampf wk, long reason) {
        try {
            isUpdating = true;

            boolean result = false;
            if ((wk != null) && (wk.hasSchwimmer())) {
                if ((reason & (REASON_AKS_CHANGED | REASON_NEW_TN | REASON_FILTER_SELECTION | REASON_POINTS_CHANGED
                        | REASON_FILTERS_CHANGED | REASON_LOAD_WK
                        | REASON_NEW_WK)) > 0) {
                    result = false;
                    int index = Math.max(0, agegroup.getSelectedIndex());
                    LinkedList<String> names = new LinkedList<>();
                    agegroup.removeAllItems();
                    Regelwerk aks = wk.getRegelwerk();
                    for (int x = 0; x < aks.size(); x++) {
                        result = true;
                        names.addLast(aks.getAk(x).getName());
                    }
                    names.addLast(I18n.get("All"));
                    agegroup.setModel(new DefaultComboBoxModel<>(names.toArray(new String[names.size()])));
                    if (names.isEmpty()) {
                        agegroup.addItem(I18n.get("Empty"));
                    }
                    if (agegroup.getSelectedIndex() < 0) {
                        index = agegroup.getItemCount() - 1;
                    }
                    if (agegroup.getItemCount() <= index) {
                        index = agegroup.getItemCount() - 1;
                    }
                    agegroup.setSelectedIndex(index);
                }
            } else {
                result = false;
                agegroup.removeAllItems();
            }

            agegroup.setEnabled(result && (agegroup.getItemCount() > 1));
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
        return I18n.get("Timelimits");
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

    static boolean checkRecords(AWettkampf<ASchwimmer> wk, int index) {
        TimelimitsContainer tc = wk.getTimelimits();

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
                for (ASchwimmer s : swimmer) {
                    if (wk.isHeatBased()) {
                        OWDisziplin<ASchwimmer>[] disziplinen = wk.getLauflisteOW().getDisziplinen();
                        for (OWDisziplin<ASchwimmer> disziplin : disziplinen) {
                            if (s.hasInput(disziplin.Id)) {
                                if (tc.isBrokenBy(s.getZeit(disziplin.Id), s.getAkkumulierteStrafe(disziplin.Id),
                                        ak.getDisziplin(disziplin.disziplin, s.isMaennlich()).getName(), s,
                                        wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION),
                                        disziplin.round)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                            Disziplin d = ak.getDisziplin(z, y == 1);
                            if (s.hasInput(z)) {
                                if (tc.isBrokenBy(s.getZeit(z), s.getAkkumulierteStrafe(z), d.getName(), s,
                                        wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), 0)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    JTable getData(AWettkampf<ASchwimmer> wk, int index) {
        TimelimitsContainer tlc = wk.getTimelimits();

        LinkedList<Object[]> result = new LinkedList<>();

        if (tlc != null && !tlc.isEmpty()) {
            int akmin = 0;
            int akmax = wk.getRegelwerk().size();
            if ((index >= 0) && (index < wk.getRegelwerk().size())) {
                akmin = index;
                akmax = index + 1;
            }
            for (Timelimits tl : tlc.getTimelimits()) {
                for (int x = akmin; x < akmax; x++) {
                    Altersklasse ak = wk.getRegelwerk().getAk(x);
                    for (int y = 0; y < 2; y++) {
                        LinkedList<ASchwimmer> swimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                        if (wk.isHeatBased()) {
                            OWDisziplin<ASchwimmer>[] disziplinen = wk.getLauflisteOW().getDisziplinen();
                            for (OWDisziplin<ASchwimmer> disziplin : disziplinen) {
                                for (ASchwimmer s : swimmer) {
                                    Disziplin d = ak.getDisziplin(disziplin.disziplin, y == 1);
                                    if (s.hasInput(disziplin.Id) && tl.isBrokenBy(s.getZeit(disziplin.Id),
                                            s.getAkkumulierteStrafe(disziplin.Id), d.getName(),
                                            s, wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION),
                                            disziplin.round)) {
                                        Strafe str = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                                        Object[] data = new Object[] { s.getName()
                                                + (str.isStrafe() ? " (" + I18n.getPenaltyShort(str) + ")" : ""),
                                                s.getGliederung(), s.getQualifikationsebene(),
                                                I18n.getAgeGroupAsString(s),
                                                StringTools.zeitString(s.getZeit(disziplin.Id)),
                                                PenaltyUtils.getPenaltyMediumText(s.getAkkumulierteStrafe(disziplin.Id),
                                                        ak),
                                                d.getName(), tl.getShortname() };
                                        result.addLast(data);
                                    }
                                }
                            }
                        } else {
                            for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                                Collections.sort(swimmer, new ZeitenVergleicher(z));
                                for (ASchwimmer s : swimmer) {
                                    Disziplin d = ak.getDisziplin(z, y == 1);
                                    if (s.hasInput(z)
                                            && tl.isBrokenBy(s.getZeit(z), s.getAkkumulierteStrafe(z), d.getName(), s,
                                                    wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), 0)) {
                                        Strafe str = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                                        Object[] data = new Object[] { s.getName()
                                                + (str.isStrafe() ? " (" + I18n.getPenaltyShort(str) + ")" : ""),
                                                s.getGliederung(), s.getQualifikationsebene(),
                                                I18n.getAgeGroupAsString(s),
                                                StringTools.zeitString(s.getZeit(z)),
                                                PenaltyUtils.getPenaltyMediumText(s.getAkkumulierteStrafe(z), ak),
                                                d.getName(), tl.getShortname() };
                                        result.addLast(data);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        TableModel tm = new DefaultTableModel(result.toArray(new Object[result.size()][0]),
                new Object[] { I18n.get("Name"), I18n.get("Organisation"), "",
                        I18n.get("AgeGroup"), I18n.get("Time"), "", I18n.get("Discipline"), I18n.get("Type") });
        JTable table = new JTable(tm);
        if (PrintUtils.printOmitOrganisationForTeams && (((AWettkampf) wk) instanceof MannschaftWettkampf)) {
            JTableUtils.hideColumnAndRemoveData(table, 1);
        }
        return table;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    Printable getPrintable(AWettkampf wk, int index) {
        return new JTablePrintable(getData(wk, index), JTablePrintable.OPT_ALL, true, PrintMode.FIT_WIDTH, true,
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
        boolean check = true;
        int index = agegroup.getSelectedIndex();
        if ((index < wk.getRegelwerk().size()) && (index >= 0)) {
            check = !wk.isAgegroupComplete(index);
        } else {
            check = !wk.isCompetitionComplete();
        }
        warning.setVisible(check);

        @SuppressWarnings("unchecked")
        boolean b = checkRecords(wk, index);
        print.setEnabled(b);
        preview.setEnabled(b);
    }
}