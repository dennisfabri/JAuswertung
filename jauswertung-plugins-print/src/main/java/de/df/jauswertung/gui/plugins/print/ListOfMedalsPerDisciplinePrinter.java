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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.ComponentListPrintable2;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ListOfMedalsPerDisciplinePrinter implements Printer {

    CorePlugin core = null;
    IPluginManager controller = null;

    private JPanel panel = null;
    private JButton print = null;
    private JButton preview = null;
    private JComboBox<String> agegroup = null;
    private JLabel warning = null;
    private JLabel filter = null;
    private JCheckBox bigPrint;

    private int[] indices = new int[0];
    private float fontscale = 1.8f;
    private int gapscale = 50;

    public ListOfMedalsPerDisciplinePrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        bigPrint = new JCheckBox("Schrift vergößern");
        bigPrint.setEnabled(false);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        agegroup = new JComboBox<>();
        agegroup.addItemListener(arg0 -> {
            if (isUpdating) {
                return;
            }
            checkWarning(core.getFilteredWettkampf());
        });

        panel = new JPanel(
                new FormLayout(
                        "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                                + "4dlu,fill:default,4dlu,fill:default,4dlu",
                        "4dlu,fill:default,4dlu"));

        panel.add(bigPrint, CC.xy(2, 2));
        panel.add(filter, CC.xy(4, 2));
        panel.add(warning, CC.xy(6, 2));
        panel.add(agegroup, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    private boolean isUpdating = false;

    boolean checkMeldungen(AWettkampf<?> wk, long reason) {
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
                    LinkedList<Integer> indexlist = new LinkedList<>();
                    LinkedList<String> names = new LinkedList<>();
                    agegroup.removeAllItems();
                    Regelwerk aks = wk.getRegelwerk();
                    for (int x = 0; x < aks.size(); x++) {
                        if (aks.getAk(x).hasEinzelwertung()) {
                            if (ResultUtils.hasEinzelwertungswettkampf(wk, x, false)) {
                                result = true;
                                indexlist.addLast(x);
                                names.addLast(aks.getAk(x).getName());
                            }
                        }
                    }
                    for (Wertungsgruppe wg : aks.getWertungsgruppen()) {
                        if (wg.isProtokollMitEinzelwertung()) {
                            result = true;
                            indexlist.addLast(-1);
                            names.addLast(wg.getName());
                        }
                    }
                    agegroup.setModel(new DefaultComboBoxModel<>(names.toArray(new String[names.size()])));
                    if (names.isEmpty()) {
                        agegroup.addItem(I18n.get("Empty"));
                    }
                    indices = new int[indexlist.size()];
                    for (int x = 0; x < indices.length; x++) {
                        indices[x] = indexlist.get(x);
                    }
                    if (agegroup.getItemCount() <= index) {
                        index = 0;
                    }
                    agegroup.setSelectedIndex(index);
                } else {
                    result = false;
                }
            }

            agegroup.setEnabled(result && (agegroup.getItemCount() > 1));
            bigPrint.setEnabled(result);
            print.setEnabled(result);
            preview.setEnabled(result);
            checkWarning(wk);
            return result;
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    void checkWarning(AWettkampf wk) {
        boolean check = false;
        int index = 0;
        switch (indices.length) {
        case 0:
            index = 0;
            break;
        case 1:
            index = indices[0];
            break;
        default:
            if (agegroup.getSelectedIndex() >= 0) {
                index = indices[agegroup.getSelectedIndex()];
            }
            break;
        }
        if (index < 0) {
            String wgname = null;
            if (agegroup.getSelectedItem() == null) {
                if (agegroup.getItemCount() != 0) {
                    wgname = agegroup.getItemAt(0).toString();
                }
            } else {
                wgname = agegroup.getSelectedItem().toString();
            }
            if (wgname != null) {
                for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                    Altersklasse ak = wk.getRegelwerk().getAk(x);
                    if (wgname.equals(ak.getWertungsgruppe())) {
                        if (!wk.isAgegroupComplete(x)) {
                            check = true;
                            break;
                        }
                    }
                }
            }
        } else if (index < wk.getRegelwerk().getMaxDisciplineCount()) {
            check = !wk.isAgegroupComplete(index);
        } else {
            check = !wk.isCompetitionComplete();
        }
        warning.setVisible(preview.isEnabled() && check);
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
        return I18n.get("ListOfMedalsPerDiscipline");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        boolean result = checkMeldungen(filteredwk, due.getChangeReason());
        filter.setVisible(result && (wk != null) && wk.isFilterActive());
    }

    @SuppressWarnings("rawtypes")
    private AWettkampf getWettkampf() {
        if (indices[agegroup.getSelectedIndex()] < 0) {
            return getWettkampf(core.getFilteredWettkampf(), agegroup.getSelectedItem().toString());
        }
        return getWettkampf(core.getFilteredWettkampf(), indices[agegroup.getSelectedIndex()]);
    }

    private AWettkampf<?> getWettkampf(AWettkampf<?> wk, int x) {
        return ResultUtils.generateEinzelwertungswettkampf(wk, x, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private AWettkampf getWettkampf(AWettkampf wk, String wg) {
        return ResultUtils.generateEinzelwertungswettkampf(wk, wg, false);
    }

    @SuppressWarnings("rawtypes")
    Printable getPrintable() {
        AWettkampf wk = getWettkampf();
        Printable p = getPrintable(wk);
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), getName(), getName());
    }

    Printable getPrintable(AWettkampf<?> wk) {
        LinkedList<ExtendedTableModel> results = new LinkedList<>();

        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                @SuppressWarnings("rawtypes")
                SchwimmerResult[] result = ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(x), y == 1);

                int max = -1;
                for (int z = 0; z < result.length; z++) {
                    int place = result[z].getPlace();
                    if ((place > 0) && (place <= 3) && (result[z].getPoints() > 0.001))
                        max = z;
                }
                if (max >= -1) {
                    max++;
                    Object[][] data = new Object[max][5];
                    for (int z = 0; z < max; z++) {
                        data[z][0] = result[z].getPlace() + " ";
                        data[z][1] = " " + result[z].getSchwimmer().getName() + " ";
                        data[z][2] = " " + result[z].getSchwimmer().getGliederung() + " ";
                        data[z][3] = " " + result[z].getSchwimmer().getQualifikationsebene() + " ";
                        data[z][4] = StringTools.zeitString(result[z].getSchwimmer().getZeit(0)) + "  ";
                    }
                    ExtendedTableModel etm = new ExtendedTableModel(data,
                            new String[] { I18n.get("Rank"), I18n.get("Name"), I18n.get("Organisation"), " ",
                                    I18n.get("Time") });
                    etm.setColumnAlignments(
                            new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT,
                                    SwingConstants.LEFT, SwingConstants.RIGHT });
                    etm.setName(I18n.getAgeGroupAsString(wk.getRegelwerk(), wk.getRegelwerk().getAk(x), y == 1) + " - "
                            + wk.getRegelwerk().getAk(x).getDisziplin(0, y == 1).getName());
                    results.addLast(etm);
                }
            }
        }

        JComponent[] parts = getResults(results.toArray(new ExtendedTableModel[results.size()]),
                PrintUtils.printOmitOrganisationForTeams && (((AWettkampf) wk) instanceof MannschaftWettkampf));
        return new ComponentListPrintable2(bigPrint.isSelected() ? gapscale : 0, false, parts);
    }

    private JComponent getPseudoTable(ExtendedTableModel etm, boolean showOrganisation) {
        FormLayout layout = null;
        if (!showOrganisation) {
            layout = new FormLayout(
                    FormLayoutUtils.createGrowingLayoutString(2, 0) + ",0dlu,"
                            + FormLayoutUtils.createGrowingLayoutString(etm.getColumnCount() - 3, 0),
                    FormLayoutUtils.createLayoutString(etm.getRowCount() + 1, 1, 0));
        } else {
            layout = new FormLayout(FormLayoutUtils.createGrowingLayoutString(etm.getColumnCount(), 0),
                    FormLayoutUtils.createLayoutString(etm.getRowCount() + 1, 1, 0));
        }

        JPanel p = new JPanel(layout) {
            @Override
            public void setFont(Font font) {
                super.setFont(font);
                if (font != null) {
                    for (Component c : getComponents()) {
                        c.setFont(font);
                    }
                }
            }

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                if (bg != null) {
                    for (Component c : getComponents()) {
                        c.setBackground(bg);
                    }
                }
            }

            @Override
            public void setForeground(Color bg) {
                super.setForeground(bg);
                if (bg != null) {
                    for (Component c : getComponents()) {
                        c.setForeground(bg);
                    }
                }
            }
        };
        p.setBackground(Color.WHITE);

        Color marker = JPrintTable.getRowMarkerColor();

        for (int y = 0; y < etm.getColumnCount(); y++) {
            JLabel l1 = new JLabel(etm.getColumnName(y));
            l1.setHorizontalAlignment(SwingConstants.CENTER);
            l1.setOpaque(true);
            l1.setBackground(marker);
            l1.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
            p.add(l1, CC.xy(2 + 2 * y, 2));
        }

        for (int x = 0; x < etm.getRowCount(); x++) {
            for (int y = 0; y < etm.getColumnCount(); y++) {
                JLabel l1 = new JLabel(etm.getValueAt(x, y).toString());
                l1.setHorizontalAlignment(etm.getColumnAlignment(y));
                l1.setOpaque(true);
                if (x % 2 != 0) {
                    l1.setBackground(marker);
                } else {
                    l1.setBackground(Color.WHITE);
                }
                p.add(l1, CC.xy(2 + 2 * y, 4 + 2 * x));

                JLabel l2 = new JLabel(" ");
                l2.setOpaque(true);
                if (x % 2 != 0) {
                    l2.setBackground(marker);
                } else {
                    l2.setBackground(Color.WHITE);
                }
                p.add(l2, CC.xy(2 + 2 * y, 4 + 2 * x - 1));
            }
        }

        Font f = PrintManager.getFont();
        p.setFont(f.deriveFont(f.getSize2D() * (bigPrint.isSelected() ? fontscale : 1.0f)));
        p.setForeground(Color.BLACK);

        p.setBorder(new LineBorder(Color.BLACK, 1));
        return p;
    }

    private JComponent[] getResults(ExtendedTableModel[] etm, boolean showOrganisation) {

        JComponent[] result = new JComponent[etm.length];
        for (int x = 0; x < etm.length; x++) {

            JLabel title = new JLabel(etm[x].getName());
            Font f = PrintManager.getFont();
            title.setFont(f.deriveFont(f.getSize2D() * (bigPrint.isSelected() ? fontscale : 1.0f)));

            FormLayout layout = new FormLayout(FormLayoutUtils.createGrowingLayoutString(1),
                    "0dlu,fill:default,1dlu,fill:default,2dlu");
            JComponent c = new JPanel(layout);
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);

            c.add(title, CC.xy(2, 2, "center,center"));
            c.add(getPseudoTable(etm[x], showOrganisation), CC.xy(2, 4));

            result[x] = c;
        }
        return result;
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
}