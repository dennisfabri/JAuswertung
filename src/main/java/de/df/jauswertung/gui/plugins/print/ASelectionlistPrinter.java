/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.aselection.ISelection;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.SelectionConstructor;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
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
abstract class ASelectionlistPrinter implements Printer {

    private CorePlugin        core;
    private IPluginManager    controller;

    private String            title;
    private ISelection        selector;

    private JPanel            panel;
    private JButton           print;
    private JButton           preview;
    private JLabel            filter;
    private JComboBox<String> order;

    private JSelectionDialog  einzel     = null;
    private JSelectionDialog  mannschaft = null;
    private JSelectionDialog  selection  = null;

    public ASelectionlistPrinter(IPluginManager window, CorePlugin plugin, String title, ISelection selector) {
        core = plugin;
        controller = window;

        this.title = title;
        this.selector = selector;

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

        order = new JComboBox<String>(
                new String[] { I18n.get("Startnumber"), I18n.get("Organisation"), I18n.get("AgeGroup"), I18n.get("Name"), I18n.get("Heat") });
        order.setSelectedIndex(2);

        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(5, 4, "4dlu:grow", "4dlu"), FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        panel.add(filter, CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("SortBy")), CC.xy(4, 2));
        panel.add(order, CC.xy(6, 2));
        panel.add(preview, CC.xy(8, 2));
        panel.add(print, CC.xy(10, 2));
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
        return title;
    }

    @Override
    public <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk) {
        LinkedList<T> schwimmer = filteredwk.getSchwimmer();
        ListIterator<T> li = schwimmer.listIterator();
        boolean b = false;
        while (li.hasNext()) {
            if (selector.getValue(li.next())) {
                b = true;
                break;
            }
        }
        print.setEnabled(b);
        preview.setEnabled(b);
        order.setEnabled(b);
        filter.setVisible(b && wk.isFilterActive());
    }

    @SuppressWarnings({ "unchecked" })
    private ExtendedTableModel getTableModel(boolean[] selected) {
        AWettkampf<ASchwimmer> wk = core.getFilteredWettkampf();
        LinkedList<ASchwimmer> schwimmer = wk.getSchwimmer();
        if (schwimmer == null) {
            return null;
        }
        ListIterator<ASchwimmer> li = schwimmer.listIterator();
        while (li.hasNext()) {
            if (!selector.getValue(li.next())) {
                li.remove();
            }
        }
        switch (order.getSelectedIndex()) {
        default:
        case 0:
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_STARTNUMMER);
            break;
        case 1:
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_NAME);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            break;
        case 2:
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_NAME);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            break;
        case 3:
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_NAME);
            break;
        }
        return DataTableUtils.registration(wk, schwimmer, DataTableUtils.RegistrationDetails.SHORT_WITH_TEAMMEMBERS, selected, false, null);
    }

    private JTable getTable() {
        return getTable(core.getWettkampf(), selector);
    }

    private static <T extends ASchwimmer> JTable getTable(AWettkampf<T> wk, ISelection selector) {
        wk = Utils.copy(wk);
        LinkedList<T> swimmers = wk.getSchwimmer();
        for (T s : swimmers) {
            if (!selector.getValue(s)) {
                wk.removeSchwimmer(s);
            }
        }
        return TableHeatUtils.getLaufeinteilungTabelle(wk, false);
    }

    private JTable getTable(boolean[] selected) {
        ExtendedTableModel etm = getTableModel(selected);
        JTable table = new JTable(etm);
        JTableUtils.setAlignmentRenderer(table, etm.getColumnAlignments(), SwingConstants.LEFT);
        return table;
    }

    private Printable getPrintable() {
        Printable p = PrintManager.getPrintable(getTable(), (String) null, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), title, title);
    }

    Printable getPrintable(boolean[] selected) {
        Printable p = PrintManager.getPrintable(getTable(selected), (String) null, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), title, title);
    }

    void print() {
        if (order.getSelectedIndex() == 4) {
            PrintableCreator pc = new LaufeinteilungPC();
            PrintManager.preview(controller.getWindow(), pc, title, IconManager.getIconBundle(), IconManager.getTitleImages());
        } else {
            checkDialog();
            selection.setVisible(true);
            if (selection.isAccepted()) {
                PrintManager.print(getPrintable(selection.getSelection()), title, true, controller.getWindow());
            }
        }
    }

    private void checkDialog() {
        if (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf) {
            if (einzel == null) {
                SelectionConstructor sc = new SelectionConstructor();
                sc.add(I18n.get("Startnumber"), true);
                sc.add(I18n.get("FamilyName"), true);
                sc.add(I18n.get("FirstName"), true);
                sc.add(I18n.get("YearOfBirth"), true);
                sc.add(I18n.get("Organisation"), true);
                sc.add(I18n.get("Qualifikationsebene"), false);
                sc.add(I18n.get("AgeGroup"), true);
                sc.add(I18n.get("Sex"), true);
                sc.add(I18n.get("ReportedPoints"), false);
                sc.add(I18n.get("Protocol"), false);
                sc.add(I18n.get("AusserKonkurrenz"), false);
                sc.add(I18n.get("Comment"), false);
                sc.add(I18n.get("Startunterlagenkontrolle"), false);
                sc.add(I18n.get("Qualification"), false);
                einzel = new JSelectionDialog(controller.getWindow(), title, sc.getTexts(), sc.getValues(), false, IconManager.getIconBundle());
            }
            selection = einzel;
        } else {
            if (mannschaft == null) {
                SelectionConstructor sc = new SelectionConstructor();
                sc.add(I18n.get("Startnumber"), true);
                sc.add(I18n.get("Name"), true);
                sc.add(I18n.get("Organisation"), true);
                sc.add(I18n.get("Qualifikationsebene"), false);
                sc.add(I18n.get("AgeGroup"), true);
                sc.add(I18n.get("Sex"), true);
                sc.add(I18n.get("ReportedPoints"), false);
                sc.add(I18n.get("Protocol"), false);
                sc.add(I18n.get("AusserKonkurrenz"), false);
                sc.add(I18n.get("Comment"), false);
                sc.add(I18n.get("Startunterlagenkontrolle"), false);
                sc.add(I18n.get("Qualification"), false);
                sc.add(I18n.get("Teammembers"), false);
                mannschaft = new JSelectionDialog(controller.getWindow(), title, sc.getTexts(), sc.getValues(), false, IconManager.getIconBundle());
            }
            selection = mannschaft;
        }
    }

    void preview() {
        if (order.getSelectedIndex() == 4) {
            PrintableCreator pc = new LaufeinteilungPC();
            PrintManager.preview(controller.getWindow(), pc, title, IconManager.getIconBundle(), IconManager.getTitleImages());
        } else {
            checkDialog();
            selection.setVisible(true);
            if (selection.isAccepted()) {
                PrintableCreator pc = new MeldelistenPC(selection.getSelection());
                PrintManager.preview(controller.getWindow(), pc, title, IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }

    private final class MeldelistenPC implements PrintableCreator {

        private boolean[] selected;

        public MeldelistenPC(boolean[] selection) {
            this.selected = selection;
        }

        @Override
        public Printable create() {
            return getPrintable(selected);
        }
    }

    private final class LaufeinteilungPC implements PrintableCreator {

        public LaufeinteilungPC() {
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