/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_FILTERS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_FILTER_SELECTION;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Printable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.UnterschriftPrintable;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.MultiplePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ResultsPrinter implements Printer {

    CorePlugin                core          = null;
    IPluginManager            controller    = null;
    WarningPlugin             warn          = null;

    private JPanel            panel         = null;
    private JButton           print         = null;
    private JButton           preview       = null;
    private JComboBox<String> diszipline    = null;
    private JLabel            warning       = null;
    private JLabel            filter        = null;
    private JCheckBox         unterschrift  = null;

    private JSelectionDialog  printDialog   = null;
    private JSelectionDialog  previewDialog = null;

    private void initDialogs() {
        if (printDialog == null) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"), JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"), JSelectionDialog.MODE_AK_SELECTION);
        }
    }

    public ResultsPrinter(IPluginManager window, CorePlugin plugin, WarningPlugin warn) {
        core = plugin;
        controller = window;
        this.warn = warn;
        initGUI();
    }

    private void initGUI() {
        panel = new JPanel(
                new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        "4dlu,fill:default,4dlu"));

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

        diszipline = new JComboBox<String>();
        diszipline.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (isUpdating) {
                    return;
                }
                checkWarning(core.getFilteredWettkampf());
            }
        });

        unterschrift = new JCheckBox(I18n.get("Unterschrift"));
        unterschrift.setSelected(Utils.getPreferences().getBoolean("ResultsWithSignature", false));
        unterschrift.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Utils.getPreferences().putBoolean("ResultsWithSignature", unterschrift.isSelected());
            }
        });
        unterschrift.setEnabled(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(unterschrift, CC.xy(6, 2));
        panel.add(diszipline, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
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
        return I18n.get("Results");
    }

    private boolean isUpdating = false;

    @Override
    public <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk) {
        try {
            isUpdating = true;

            boolean result = false;
            if (wk.isHeatBased()) {
                result = wk.hasSchwimmer() && !wk.getLauflisteOW().isEmpty();
                filteredwk = wk;
            } else {
                if (filteredwk == null) {
                    filteredwk = wk;
                } else {
                    if (filteredwk.hasSchwimmer()) {
                        result = true;
                    }
                }
            }

            if ((due.getChangeReason() & (REASON_AKS_CHANGED | REASON_FILTER_SELECTION | REASON_FILTERS_CHANGED)) > 0) {
                Regelwerk aks = filteredwk.getRegelwerk();
                int length = aks.getMaxDisciplineCount();
                if (length != diszipline.getItemCount() - 1) {
                    boolean max = (diszipline.getSelectedIndex() == diszipline.getItemCount() - 1) && (diszipline.getItemCount() != 0);
                    diszipline.removeAllItems();

                    diszipline.addItem(I18n.get("Automatic"));
                    while (diszipline.getItemCount() - 1 < length) {
                        diszipline.addItem(I18n.get("Discipline1toN", diszipline.getItemCount()));
                    }
                    while (length < diszipline.getItemCount()) {
                        diszipline.removeItemAt(diszipline.getItemCount() - 1);
                    }

                    diszipline.addItem(I18n.get("FinalResult"));
                    if (max) {
                        diszipline.setSelectedIndex(length);
                    } else {
                        diszipline.setSelectedIndex(0);
                    }
                }
            }

            diszipline.setEnabled(result);
            print.setEnabled(result);
            preview.setEnabled(result);
            unterschrift.setEnabled(result);
            checkWarning(filteredwk);
            filter.setVisible(result && wk.isFilterActive());
        } finally {
            isUpdating = false;
        }
    }

    @SuppressWarnings("rawtypes")
    void checkWarning(AWettkampf wk) {
        boolean check = false;
        int index = diszipline.getSelectedIndex();
        if (index >= 0) {
            if (index == 0) {
                Regelwerk aks = wk.getRegelwerk();
                check = true;
                for (int x = 0; x < aks.size(); x++) {
                    if (wk.isOneDisciplineComplete(x)) {
                        check = false;
                    }
                }
                // check = (wk.getToDisciplineComplete() == 0);
            } else {
                if (index < diszipline.getItemCount() - 1) {
                    check = !wk.isToDisciplineComplete(index - 1);
                } else {
                    check = !wk.isCompetitionComplete();
                }
            }
        }
        warning.setVisible(preview.isEnabled() && check);
    }

    /**
     * @param selected
     */
    <T extends ASchwimmer> Printable getPrintable(boolean[][] selected) {
        AWettkampf<T> wk = getWettkampf();

        LinkedList<Printable> ps = null;
        if (diszipline.getSelectedIndex() == diszipline.getItemCount() - 1) {
            ps = PrintUtils.getFullResultsPrintable(selected, wk, true, true, 0);
        } else {
            if (diszipline.getSelectedIndex() == 0) {
                boolean selectedcomplete = true;
                for (int x = 0; x < selected.length; x++) {
                    for (int y = 0; y < 2; y++) {
                        if (selected[x].length <= y) {
                            continue;
                        }
                        if (selected[x][y]) {
                            if (!wk.isAgegroupComplete(y, x == 1)) {
                                selectedcomplete = false;
                                break;
                            }
                            if (!wk.isHlwComplete(y, x == 1)) {
                                selectedcomplete = false;
                                break;
                            }
                        }
                    }
                }
                if (selectedcomplete) {
                    ps = PrintUtils.getFullResultsPrintable(selected, wk, true, true, 0);
                } else {
                    ps = PrintUtils.getIntermediateResults(selected, wk, 0, true, true, 0);
                }
            } else {
                ps = PrintUtils.getIntermediateResults(selected, wk, diszipline.getSelectedIndex(), true, true, 0);
            }
        }
        Printable p = null;
        if (ps.size() == 1) {
            p = ps.getFirst();
        } else {
            p = new MultiplePrintable(ps);
        }
        if (unterschrift.isSelected()) {
            p = new UnterschriftPrintable(p);
        }
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), true, I18n.get("Results"));
    }

    void printResults(boolean[][] selection) {
        PrintExecutor.print(getPrintable(selection), I18n.get("Results"), true, controller.getWindow());
    }

    private class PPrintableCreator implements PrintableCreator {

        private boolean[][] selection;

        public PPrintableCreator(boolean[][] selection) {
            this.selection = selection;
        }

        @Override
        public Printable create() {
            return getPrintable(selection);
        }
    }

    void previewResults(boolean[][] selection) {
        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("Results"), IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        showSelectionDialog(previewDialog);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        showSelectionDialog(printDialog);
    }

    private void showSelectionDialog(JSelectionDialog dialog) {
        if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
            return;
        }

        if (diszipline.getSelectedIndex() == 0) {
            warn.information(controller.getWindow(), I18n.get("AutomaticResultsPrinting.Title"), I18n.get("AutomaticResultsPrinting.Text"),
                    I18n.get("AutomaticResultsPrinting.Note"), "AutomaticResultsPrinting");
        }

        int disz = diszipline.getSelectedIndex();
        if (disz + 1 == diszipline.getItemCount()) {
            disz = JSelectionDialog.ALL_DISCIPLINES;
        } else {
            if (disz == 0) {
                disz = JSelectionDialog.AUTO_DISCIPLINES;
            } else {
                disz -= 1;
            }
        }
        @SuppressWarnings("rawtypes")
        AWettkampf wk = getWettkampf();
        dialog.setCompetition(wk);
        dialog.setDiscipline(disz);
        dialog.setVisible(true);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
        AWettkampf wk = Utils.copy(core.getFilteredWettkampf());
        CompetitionUtils.minimizeCompetition(wk);
        for (Wertungsgruppe wg : wk.getRegelwerk().getWertungsgruppen()) {
            if (wg.isProtokollMitMehrkampfwertung()) {
                Hashtable<Integer, Altersklasse> aks = new Hashtable<Integer, Altersklasse>();
                int index = -1;
                for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                    Altersklasse ak = wk.getRegelwerk().getAk(x);
                    if ((ak.getWertungsgruppe() != null) && (ak.getWertungsgruppe().equals(wg.getName()))) {
                        aks.put(x, ak);
                        index = x;
                    }
                }
                LinkedList<ASchwimmer> swimmers = Utils.copy(wk).getSchwimmer();
                ListIterator<ASchwimmer> li = swimmers.listIterator();
                LinkedList<ASchwimmer> result = new LinkedList<ASchwimmer>();
                while (li.hasNext()) {
                    ASchwimmer s = li.next();
                    if (aks.containsKey(s.getAKNummer())) {
                        result.addLast(s);
                    }
                }
                if (!result.isEmpty()) {
                    Altersklasse ak = Utils.copy(wk.getRegelwerk().getAk(index));
                    ak.setStrafeIstDisqualifikation(wg.isStrafeIstDisqualifikation());
                    ak.setName(wg.getName());

                    index = wk.getRegelwerk().size();

                    wk.getRegelwerk().setSize(index + 1);
                    wk.getRegelwerk().setAk(index, ak);
                    for (ASchwimmer s : result) {
                        wk.addSchwimmer(s);
                        s.setAKNummer(index, false);
                    }
                }
            }
        }
        return wk;
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPrintSelectionDialog();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPreviewSelectionDialog();
        }
    }

    final class PrintCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            printResults(selected);
        }
    }

    final class PreviewCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            previewResults(selected);
        }

    }
}