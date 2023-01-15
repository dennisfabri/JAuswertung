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
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Printable;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.FormelILSOutdoor;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.ISimpleCallback;
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
abstract class APerDisciplinePrinter implements Printer {

    CorePlugin core = null;
    IPluginManager controller = null;

    private JPanel panel = null;
    private JButton print = null;
    private JButton export = null;
    private JButton preview = null;
    private JComboBox<String> agegroup = null;
    private JLabel warning = null;
    private JLabel filter = null;

    private final boolean hasHeader;

    private int[] indices = new int[0];

    private PreviewCB previewcb = new PreviewCB();
    private PrintCB printcb = new PrintCB();

    private JSelectionDialog previewDialog = null;
    private JSelectionDialog printDialog = null;

    public APerDisciplinePrinter(IPluginManager window, CorePlugin plugin, boolean hasHeader) {
        core = plugin;
        controller = window;
        this.hasHeader = hasHeader;
        initGUI();
    }

    private void initGUI() {
        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        export = new JButton(I18n.get("Export"));
        export.addActionListener(new ExportActionListener());
        export.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        agegroup = new JComboBox<String>();
        agegroup.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (isUpdating) {
                    return;
                }
                checkWarning(core.getFilteredWettkampf());
            }
        });

        panel = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,"
                + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu"));

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(agegroup, CC.xy(6, 2));
        panel.add(export, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
    }

    private boolean isUpdating = false;

    boolean checkMeldungen(AWettkampf<?> wk, long reason) {
        try {
            isUpdating = true;

            boolean result = false;
            if (wk != null) {
                result = print.isEnabled();
                if (wk.isHeatBased()) {
                    result = wk.getLauflisteOW() != null && !wk.getLauflisteOW().isEmpty();
                    agegroup.removeAllItems();
                } else if (wk.hasSchwimmer()) {
                    if ((reason & (REASON_AKS_CHANGED | REASON_NEW_TN | REASON_SWIMMER_CHANGED | REASON_SWIMMER_DELETED
                            | REASON_FILTER_SELECTION | REASON_POINTS_CHANGED | REASON_FILTERS_CHANGED | REASON_LOAD_WK
                            | REASON_NEW_WK)) > 0) {
                        result = false;
                        int index = Math.max(0, agegroup.getSelectedIndex());
                        LinkedList<Integer> indexlist = new LinkedList<Integer>();
                        LinkedList<String> names = new LinkedList<String>();
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
                        agegroup.setModel(new DefaultComboBoxModel<String>(names.toArray(new String[names.size()])));
                        if (names.size() == 0) {
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
            }

            agegroup.setEnabled(result && (agegroup.getItemCount() > 1));
            print.setEnabled(result);
            export.setEnabled(result && hasExport());
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

    @Override
    @SuppressWarnings({ "rawtypes" })
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

    private Printable getPrintable(boolean[][] selected) {
        return getPrintable(getWettkampf(), 0, 0, selected);
    }

    @SuppressWarnings("rawtypes")
    protected abstract LinkedList<Printable> getPrintable(boolean[][] selected, AWettkampf wk, int qualification);

    @SuppressWarnings("rawtypes")
    protected void export(AWettkampf wk, OutputStream os) {
    }

    protected boolean hasExport() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private void export(OWSelection t) {
        String filename = FileChooserUtils.saveFile(controller.getWindow(), I18n.get("Export"),
                new SimpleFileFilter("Microsoft Excel", "xls"));
        if (filename == null || filename.trim().length() == 0) {
            return;
        }
        @SuppressWarnings("rawtypes")
        AWettkampf wk = createCompetitionFor(t);
        try {
            FileOutputStream os = new FileOutputStream(filename);
            if (!ExportManager.export("Microsoft Excel", os, ImportExportTypes.RESULTS, wk, null)) {
                DialogUtils.inform(controller.getWindow(), "Fehler", "Fehler beim Speichern", "...");
            }
            os.close();
        } catch (Exception ex) {
            DialogUtils.showException(controller.getWindow(), "Fehler", "Fehler beim Speichern", "...", ex);
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private Printable getPrintable(AWettkampf wk, int round, int qualification, boolean[][] selected) {
        LinkedList<Printable> ps = getPrintable(selected, wk, qualification);
        MessageFormat header = new MessageFormat(I18n.getRound(round, qualification == 0));
        if (qualification <= 0) {
            header = new MessageFormat("");
        }
        Printable p = new MultiplePrintable(ps);
        if (hasHeader) {
            p = PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), header, I18n.get("Einzelwertung"));

        }
        return p;
    }

    <T extends ASchwimmer> Printable getPrintable(OWSelection[] tx) {
        MultiplePrintable mp = new MultiplePrintable();
        for (OWSelection t : tx) {
            mp.add(getPrintable(t));
        }
        return mp;
    }

    <T extends ASchwimmer> Printable getPrintable(OWSelection t) {
        AWettkampf<T> wk = createCompetitionFor(t);
        boolean[][] selection = new boolean[2][wk.getRegelwerk().size()];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < selection[y].length; x++) {
                selection[y][x] = (x == t.akNummer) && (y == (t.male ? 1 : 0));
            }
        }
        int qualification = 0;
        int[] runden = wk.getRegelwerk().getAk(t.akNummer).getDisziplin(0, t.male).getRunden();
        if (runden.length > t.round) {
            qualification = runden[t.round];
        }
        return getPrintable(wk, t.round, qualification, selection);
    }

    void printResults(boolean[][] selection) {
        PrintExecutor.print(getPrintable(selection), I18n.get("Einzelwertung"), true, controller.getWindow());
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
        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("Einzelwertung"),
                IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    @SuppressWarnings("rawtypes")
    void showPreviewSelectionDialog() {
        AWettkampf w = getWettkampf();
        if (w == null) {
            DialogUtils.warn(controller.getWindow(), I18n.get("Title.NoParticipantsForResultgroup"),
                    I18n.get("Information.NoParticipantsForResultgroup"),
                    I18n.get("Note.NoParticipantsForResultgroup"));
            return;
        }
        if (previewDialog == null) {
            previewDialog = createPreviewDialog(w);
        }
        previewDialog.setCompetition(w);
        previewDialog.setVisible(true);
    }

    @SuppressWarnings("rawtypes")
    void showPrintSelectionDialog() {
        AWettkampf w = getWettkampf();
        if (w == null) {
            DialogUtils.warn(controller.getWindow(), I18n.get("Title.NoParticipantsForResultgroup"),
                    I18n.get("Information.NoParticipantsForResultgroup"),
                    I18n.get("Note.NoParticipantsForResultgroup"));
            return;
        }
        if (printDialog == null) {
            printDialog = createPrintDialog(w);
        }
        printDialog.setCompetition(w);
        printDialog.setVisible(true);
    }

    <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        AWettkampf<T> wkx = ResultUtils.createCompetitionFor(cwk, t);
        if (!t.isFinal) {
            wkx.getRegelwerk().setFormelID(cwk.getDataType() == DataType.RANK ? FormelILSOutdoor.ID : FormelILS.ID);
        }
        return wkx;
    }

    final class PrintActionListener implements ActionListener {

        <T extends ASchwimmer> void printResult(OWSelection[] t) {
            PrintExecutor.print(getPrintable(t), I18n.get("Einzelwertung"), true, controller.getWindow());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = new ISimpleCallback<OWSelection[]>() {
                    @Override
                    public void callback(OWSelection[] t) {
                        if (t != null) {
                            printResult(t);
                        }
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Ergebnis auswählen",
                        "Ergebnis zum Drucken auswählen", OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                showPrintSelectionDialog();
            }
        }
    }

    final class ExportActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection> cb = new ISimpleCallback<OWSelection>() {
                    @Override
                    public void callback(OWSelection t) {
                        if (t != null) {
                            export(t);
                        }
                    }
                };
                OWUtils.ShowRoundSelector(controller.getWindow(), wk, "Ergebnis auswählen",
                        "Ergebnis zum Drucken auswählen", OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                // export();
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        class PPrintableCreator implements PrintableCreator {

            private OWSelection[] tx;

            public PPrintableCreator(AWettkampf<?> wk, OWSelection[] tx) {
                this.tx = tx;
            }

            @Override
            public Printable create() {
                return getPrintable(tx);
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = new ISimpleCallback<OWSelection[]>() {
                    @Override
                    public void callback(OWSelection[] t) {
                        if (t != null) {
                            PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(wk, t),
                                    I18n.get("Einzelwertung"), IconManager.getIconBundle(),
                                    IconManager.getTitleImages());
                        }
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Ergebnis auswählen",
                        "Ergebnis zum Drucken auswählen", OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                showPreviewSelectionDialog();
            }
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

    @SuppressWarnings("rawtypes")
    private JSelectionDialog createPrintDialog(AWettkampf wk) {
        return new JSelectionDialog(controller.getWindow(), wk, printcb, I18n.get("Print"),
                JSelectionDialog.MODE_DISCIPLINE_SELECTION);
    }

    @SuppressWarnings("rawtypes")
    private JSelectionDialog createPreviewDialog(AWettkampf wk) {
        return new JSelectionDialog(controller.getWindow(), wk, previewcb, I18n.get("Preview"),
                JSelectionDialog.MODE_DISCIPLINE_SELECTION);
    }
}