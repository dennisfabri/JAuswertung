/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.*;
import de.df.jauswertung.util.vergleicher.*;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.window.JSelectionDialog;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class RegisteredTimesPrinter implements Printer {

    private CorePlugin       core;
    private IPluginManager   controller;

    private JPanel           panel;
    private JButton          print;
    private JButton          preview;
    private JLabel           filter;

    private JSelectionDialog einzel     = null;
    private JSelectionDialog mannschaft = null;
    private JSelectionDialog selection  = null;

    public RegisteredTimesPrinter(IPluginManager window, CorePlugin plugin) {
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

        FormLayout layout = new FormLayout("4dlu:grow,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(filter, CC.xy(2, 2));
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
    }

    @SuppressWarnings("rawtypes")
    void checkMeldungen(AWettkampf wk) {
        boolean b = wk.hasSchwimmer();
        print.setEnabled(b);
        preview.setEnabled(b);
        filter.setVisible(b && core.getWettkampf().isFilterActive());
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
        return I18n.get("Meldezeiten");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk);
    }

    private ExtendedTableModel[] getTableModels(boolean[] selected) {
        AWettkampf<ASchwimmer> wk = core.getFilteredWettkampf();
        if (!wk.hasSchwimmer()) {
            return null;
        }
        LinkedList<ExtendedTableModel> result = new LinkedList<ExtendedTableModel>();

        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<ASchwimmer> schwimmer = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);
                if ((schwimmer != null) && (schwimmer.size() > 0)) {
                    Collections.sort(schwimmer, new SchwimmerGliederungVergleicher<ASchwimmer>());
                    Collections.sort(schwimmer, new SchwimmerMeldepunkteVergleicher<ASchwimmer>());
                    ExtendedTableModel etm = DataTableUtils.registration(wk, schwimmer, DataTableUtils.RegistrationDetails.EVERYTHING, selected, false, null);
                    etm.setName(aks.getAk(x).getName() + " " + I18n.geschlechtToString(aks, y == 1));
                    result.addLast(etm);
                }
            }
        }
        return result.toArray(new ExtendedTableModel[result.size()]);
    }

    private JTable[] getTables(boolean[] selected) {
        ExtendedTableModel[] etm = getTableModels(selected);
        JTable[] tables = new JTable[etm.length];
        for (int x = 0; x < etm.length; x++) {
            JTable table = new JTable(etm[x]);
            JTableUtils.setAlignmentRenderer(table, etm[x].getColumnAlignments(), SwingConstants.LEFT);
            table.setName(etm[x].getName());
            tables[x] = table;
        }
        return tables;
    }

    Printable getPrintable(boolean[] selected) {
        JTable[] tables = getTables(selected);
        String[] names = new String[tables.length];
        for (int x = 0; x < tables.length; x++) {
            names[x] = tables[x].getName();
        }
        Printable p = PrintManager.getPrintable(tables, names, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), I18n.get("Meldezeiten"), I18n.get("Meldezeiten"));
    }

    void print() {
        checkDialog();
        selection.setVisible(true);
        if (selection.isAccepted()) {
            PrintManager.print(getPrintable(selection.getSelection()), I18n.get("Meldezeiten"), true, controller.getWindow());
        }
    }

    private void checkDialog() {
        if (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf) {
            if (einzel == null) {
                SelectionConstructor sc = new SelectionConstructor();
                sc.add(I18n.get("Startnumber"), false);
                sc.add(I18n.get("FamilyName"), true);
                sc.add(I18n.get("FirstName"), true);
                sc.add(I18n.get("YearOfBirth"), true);
                sc.add(I18n.get("Organisation"), true);
                sc.add(I18n.get("Qualifikationsebene"), true);
                sc.add(I18n.get("AgeGroup"), false);
                sc.add(I18n.get("Sex"), false);
                sc.add(I18n.get("ReportedPoints"), false);
                sc.add(I18n.get("Protocol"), false);
                sc.add(I18n.get("AusserKonkurrenz"), false);
                sc.add(I18n.get("Comment"), false);
                sc.add(I18n.get("Startunterlagenkontrolle"), false);
                sc.add(I18n.get("Qualification"), true);
                einzel = new JSelectionDialog(controller.getWindow(), I18n.get("Registrations"), sc.getTexts(), sc.getValues(), false,
                        IconManager.getIconBundle());
            }
            selection = einzel;
        } else {
            if (mannschaft == null) {
                SelectionConstructor sc = new SelectionConstructor();
                sc.add(I18n.get("Startnumber"), false);
                sc.add(I18n.get("Name"), true);
                // sc.add(I18n.get("Members"), false);
                sc.add(I18n.get("Organisation"), true);
                sc.add(I18n.get("Qualifikationsebene"), true);
                sc.add(I18n.get("AgeGroup"), false);
                sc.add(I18n.get("Sex"), false);
                sc.add(I18n.get("ReportedPoints"), false);
                sc.add(I18n.get("Protocol"), false);
                sc.add(I18n.get("AusserKonkurrenz"), false);
                sc.add(I18n.get("Comment"), false);
                sc.add(I18n.get("Startunterlagenkontrolle"), false);
                sc.add(I18n.get("Qualification"), true);
                mannschaft = new JSelectionDialog(controller.getWindow(), I18n.get("Registrations"), sc.getTexts(), sc.getValues(), false,
                        IconManager.getIconBundle());
            }
            selection = mannschaft;
        }
    }

    void preview() {
        checkDialog();
        selection.setVisible(true);
        if (selection.isAccepted()) {
            PrintableCreator pc = new MeldezeitenPC(selection.getSelection());
            PrintManager.preview(controller.getWindow(), pc, I18n.get("Meldezeiten"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }

    private final class MeldezeitenPC implements PrintableCreator {

        private boolean[] selected;

        public MeldezeitenPC(boolean[] s) {
            selected = s;
        }

        @Override
        public Printable create() {
            return getPrintable(selected);
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