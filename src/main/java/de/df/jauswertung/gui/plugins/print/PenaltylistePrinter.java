/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.ComponentListPrintable2;
import de.df.jutils.print.ComponentPackingPrintable;
import de.df.jutils.print.EmptyPrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class PenaltylistePrinter implements Printer {

    private JSelectionDialog printDialog   = null;
    private JSelectionDialog previewDialog = null;

    @SuppressWarnings("rawtypes")
    private void initDialogs() {
        if (printDialog == null) {
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"), JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"), JSelectionDialog.MODE_AK_SELECTION);
        }
    }

    final class PrintCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            PrintManager.print(getPrintable(selected), I18n.get("Penaltylist"), controller.getWindow());
        }

    }

    final class PreviewCB implements JSelectionDialog.PrintCallBack {

        private final class PenaltyPC implements PrintableCreator {

            private final boolean[][] selected;

            public PenaltyPC(boolean[][] s) {
                selected = s;
            }

            @Override
            public Printable create() {
                return getPrintable(selected);
            }
        }

        @Override
        public void print(boolean[][] selected) {
            PrintManager.preview(controller.getWindow(), new PenaltyPC(selected), I18n.get("Penaltylist"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }

    }

    private CorePlugin        core;
    private IPluginManager    controller;

    private JPanel            panel;
    private JButton           print;
    private JButton           preview;
    private JLabel            warning;
    private JLabel            filter;
    private JCheckBox         kurz;

    private JComboBox<String> disziplin;

    public PenaltylistePrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        disziplin = new JComboBox(new Object[] { I18n.get("All") });
        disziplin.setEnabled(false);

        warning = new JLabel(" ", IconManager.getSmallIcon("warn"), 0);
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        kurz = new JCheckBox(I18n.get("Shortform"));

        print.setEnabled(false);
        preview.setEnabled(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(kurz, CC.xy(6, 2));
        panel.add(disziplin, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    @SuppressWarnings({ "unchecked" })
    private void checkPenalties(@SuppressWarnings("rawtypes") AWettkampf wk) {
        boolean b = PenaltyUtils.hasPenalties(wk) && !wk.isHeatBased();
        print.setEnabled(b);
        preview.setEnabled(b);
        disziplin.setEnabled(b);
        kurz.setEnabled(b);
        warning.setVisible(b && !wk.isCompetitionComplete());
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
        return I18n.get("Penaltylist");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        if ((due.getChangeReason() & UpdateEventConstants.REASON_AKS_CHANGED) > 0) {
            updateDisciplines();
        }
        if (core.getWettkampf().hasSchwimmer()) {
            checkPenalties(filteredwk);
        }
    }

    private void updateDisciplines() {
        int max = core.getWettkampf().getRegelwerk().getMaxDisciplineCount();

        if (max + 1 == disziplin.getItemCount()) {
            return;
        }

        int index = Math.min(max, Math.max(0, disziplin.getSelectedIndex()));
        if (index == disziplin.getItemCount() - 1) {
            index = max;
        }

        disziplin.removeAllItems();
        String[] daten = new String[max + 1];
        for (int x = 0; x < max; x++) {
            daten[x] = I18n.get("DisciplineNumber", x + 1);
        }
        daten[max] = I18n.get("All");
        disziplin.setModel(new DefaultComboBoxModel<String>(daten));
        disziplin.setSelectedIndex(index);
    }

    static <T extends ASchwimmer> Printable getPrintable(CorePlugin core, int index, boolean[][] selected, boolean kurz) {
        JPanel[] panels = PenaltyUtils.getPenalties(core.getFilteredWettkampf(), index, selected, !PrintUtils.printDidNotStart, kurz);
        if (panels == null) {
            return EmptyPrintable.Instance;
        }
        Component[] cs = new Component[panels.length];
        for (int x = 0; x < panels.length; x++) {
            panels[x].setOpaque(false);
            panels[x].setBackground(Color.WHITE);
            panels[x].setBorder(new LineBorder(Color.BLACK, 1));
            // panels[x].setBorder(new EmptyBorder(0, 0, 0, 0));
            cs[x] = panels[x];
        }
        Printable p = null;
        if (kurz) {
            p = new ComponentPackingPrintable(3, 3, false, cs);
        } else {
            p = new ComponentListPrintable2(3, false, cs);
        }
        return PrintManager.getHeaderPrintable(p, I18n.get("Penaltylist")
                + (core.getWettkampf().getCurrentFilterIndex() > 0 ? " (" + core.getWettkampf().getCurrentFilter().getName() + ")" : ""));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T extends ASchwimmer> Printable getPrintable(AWettkampf wk, boolean kurz) {
        JPanel[] panels = null;
        if (kurz) {
            panels = PenaltyUtils.getPenaltiesShort(wk);
        } else {
            panels = PenaltyUtils.getPenalties(wk);
        }
        if (panels == null) {
            return EmptyPrintable.Instance;
        }

        Component[] cs = new Component[panels.length];
        for (int x = 0; x < panels.length; x++) {
            panels[x].setOpaque(false);
            panels[x].setBackground(Color.WHITE);
            panels[x].setBorder(new LineBorder(Color.BLACK, 1));
            // panels[x].setBorder(new EmptyBorder(0, 0, 0, 0));
            cs[x] = panels[x];
        }
        Printable p = null;
        if (kurz) {
            p = new ComponentPackingPrintable(3, 3, false, cs);
        } else {
            p = new ComponentListPrintable2(3, false, cs);
        }
        return PrintManager.getHeaderPrintable(p, I18n.get("Penaltylist"));
    }

    Printable getPrintable(boolean[][] selected) {
        return PrintManager.getFinalPrintable(getPrintable(core, disziplin.getSelectedIndex(), selected, kurz.isSelected()), core.getLastChangedDate(), true,
                I18n.get("Penaltylist"));
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        int disz = disziplin.getSelectedIndex();
        if (disz + 1 == disziplin.getItemCount()) {
            disz = JSelectionDialog.ALL_DISCIPLINES;
        }
        previewDialog.setCompetition(core.getFilteredWettkampf());
        previewDialog.setDiscipline(disz);
        previewDialog.setVisible(true);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        int disz = disziplin.getSelectedIndex();
        if (disz + 1 == disziplin.getItemCount()) {
            disz = JSelectionDialog.ALL_DISCIPLINES;
        }
        printDialog.setCompetition(core.getFilteredWettkampf());
        printDialog.setDiscipline(disz);
        printDialog.setVisible(true);
    }

    class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPrintSelectionDialog();
        }
    }

    class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPreviewSelectionDialog();
        }
    }
}