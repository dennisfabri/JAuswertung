package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.gui.util.JStartsViewer;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JGradientLabel;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.ListLayout;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.MultiplePrintable;

/**
 * @author Dennis Fabri
 * @since 26.03.2004
 */
public class PResulttablePlugin extends ANullPlugin {

    JGlassPanel<JScrollPane> glas;
    JPanel panel;
    JResultTable result;
    private JScrollPane scroller;
    private JComboBox<String> altersklasse;
    private JComboBox<String> geschlecht;
    private JComboBox<String> disziplin;
    private JComboBox<String> runde;
    private JButton update;
    private JButton print;

    private LinkedList<ASchwimmer> elements;

    private InternalMouseListener mouseListener;

    IPluginManager controller = null;
    CorePlugin core = null;
    FEditorPlugin editor = null;
    @SuppressWarnings("rawtypes")
    AWettkampf wk = null;

    private static final long BITMASK1 = REASON_AKS_CHANGED | REASON_LOAD_WK | REASON_NEW_WK | REASON_LAUF_LIST_CHANGED;
    private static final long BITMASK2 = REASON_LOAD_WK | REASON_GLIEDERUNG_CHANGED | REASON_SWIMMER_DELETED
            | REASON_SWIMMER_CHANGED
            | REASON_POINTS_CHANGED | REASON_NEW_TN | REASON_NEW_WK | REASON_PENALTY | REASON_PROPERTIES_CHANGED;

    public PResulttablePlugin() {
        super();
    }

    private ASchwimmer getSelectedSwimmer() {
        ASchwimmer s = result.getSelectedSchwimmer();
        if (isHeatBased()) {
            if (disziplin.getSelectedIndex() + 1 == disziplin.getItemCount()) {
                s = SearchUtils.getSchwimmer(wk, s);
            } else {
                s = SearchUtils.getSchwimmer(wk, Integer.parseInt(s.getBemerkung()));
            }
        } else {
            s = SearchUtils.getSchwimmer(wk, s);
        }
        return s;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (panel == null) {
            return;
        }
        if ((due.getChangeReason() & BITMASK1) > 0) {
            updateWk();
            updateAks();
            updateSexes();
        }
        if ((due.getChangeReason() & BITMASK2) > 0) {
            updateResult(false);
        }
    }

    private void updateSexes() {
        String[] items = null;
        if (wk == null) {
            items = new String[] { I18n.get("female"), I18n.get("male") };
        } else {
            Regelwerk rw = wk.getRegelwerk();
            items = new String[] { I18n.geschlechtToStringSubject(rw, false),
                    I18n.geschlechtToStringSubject(rw, true) };
        }
        boolean changed = false;
        if (geschlecht.getModel().getSize() != items.length) {
            changed = true;
        } else {
            for (int x = 0; x < geschlecht.getModel().getSize(); x++) {
                if (!geschlecht.getModel().getElementAt(x).equals(items[x])) {
                    changed = true;
                }
            }
        }
        if (changed) {
            int index = geschlecht.getSelectedIndex();
            geschlecht.setModel(new DefaultComboBoxModel<>(items));
            if (index >= 0) {
                geschlecht.setSelectedIndex(index);
            }
        }
    }

    void print() {
        updateResult(true, true);
    }

    void updateResult(boolean force) {
        updateResult(force, false);
    }

    private void disableDisplay() {
        elements.clear();
        glas.setEnabled(false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void updateResult(boolean force, boolean doPrint) {
        try {
            glas.setEnabled(!isHeatBased() || force);
            int index = Math.max(0, altersklasse.getSelectedIndex());
            boolean maennlich = geschlecht.getSelectedIndex() == 1;
            int discipline = disziplin.getSelectedIndex();
            AWettkampf wkx = wk;
            Altersklasse ak = wkx.getRegelwerk().getAk(index);
            int round = 0;
            int qualification = 0;
            boolean isFinal = false;
            boolean isPartOnly = false;
            boolean isCompleteDiscipline = false;
            if (isHeatBased()) {
                if (!force && !doPrint) {
                    disableDisplay();
                    return;
                }
                isPartOnly = (discipline + 1 < disziplin.getItemCount()) && (disziplin.getItemCount() > 0)
                        && (runde.getItemCount() > 0);
                if (!isPartOnly) {
                    ak = wkx.getRegelwerk().getAk(index);
                    if (!ak.hasMehrkampfwertung()) {
                        wkx = Utils.copy(wk);
                        wkx.getRegelwerk().setFormelID(FormelILS.ID);
                        wkx.removeSchwimmer(wkx.getSchwimmer());
                        ak = wkx.getRegelwerk().getAk(index);
                    }
                } else {
                    round = runde.getSelectedIndex();
                    isCompleteDiscipline = round + 1 == runde.getItemCount();
                    if (round < 0) {
                        // Todo
                    } else if (isCompleteDiscipline) {
                        wkx = ResultUtils.generateEinzelwertungswettkampf(wkx, index, maennlich, false);
                        if (wkx == null) {
                            disableDisplay();
                            return;
                        }
                        index = discipline;
                        ak = wkx.getRegelwerk().getAk(discipline);
                        discipline = 0;
                    } else {
                        isFinal = round == ak.getDisziplin(discipline, maennlich).getRunden().length;
                        OWSelection t = new OWSelection(ak, index, maennlich, discipline, round, isFinal);
                        wkx = ResultUtils.createCompetitionFor(wkx, t);
                        if (wkx == null) {
                            disableDisplay();
                            DialogUtils.inform(getController().getWindow(),
                                    I18n.get("RoundNotYetGenerated.Information"),
                                    I18n.get("RoundNotYetGenerated.Note"));
                            return;
                        }
                        if (!isFinal) {
                            wkx.getRegelwerk().setFormelID(FormelILS.ID);
                        }
                        ak = wkx.getRegelwerk().getAk(index);
                        discipline = 0;
                        int[] runden = ak.getDisziplin(discipline, maennlich).getRunden();
                        if (round < runden.length) {
                            qualification = runden[round];
                        }
                    }
                }
            }
            try {
                int anzahl = ak.getDiszAnzahl();
                boolean hlw = ak.hasHLW();
                boolean einzel = wkx instanceof EinzelWettkampf;
                if ((JResultTable.getColumnCount(anzahl, hlw, einzel, qualification) != result.getColumnCount())) {
                    result = JResultTable.getResultTable(ak, anzahl, hlw, wkx instanceof EinzelWettkampf, false,
                            qualification,
                            wk.getRegelwerk().getZusatzwertungShort());
                    if (anzahl == 1 && !hlw) {
                        JTableUtils.hideColumnAndRemoveData(result,
                                JResultTable.PREFIX + JResultTable.D_POINTS_OFFSET + (einzel ? 1 : 0));
                        if (qualification > 0) {
                            JTableUtils.hideColumnAndRemoveData(result, JResultTable.DIFF_OFFSET + (einzel ? 1 : 0));
                            JTableUtils.hideColumnAndRemoveData(result, JResultTable.SCORE_OFFSET + (einzel ? 1 : 0));
                        }
                    }
                    scroller.setViewportView(result);
                    result.addMouseListener(mouseListener);
                    new JEditPopup();
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                elements.clear();
            }
            elements = SearchUtils.getSchwimmer(wkx, ak, maennlich);
            result.updateResult(wkx, ak, maennlich, null, ak.hasHLW(), qualification);

            // This is required to update the column headers for empty tables
            if (result.getRowCount() == 0) {
                scroller.getViewport().remove(result);
                scroller.getViewport().add(result);
            }

            if (doPrint) {
                boolean[][] selection = new boolean[2][wkx.getRegelwerk().size()];
                for (int x = 0; x < 2; x++) {
                    for (int y = 0; y < selection[x].length; y++) {
                        selection[x][y] = x == geschlecht.getSelectedIndex() && y == index;
                    }
                }
                if (isPartOnly) {
                    selection = new boolean[2][1];
                    for (int x = 0; x < 2; x++) {
                        selection[x][0] = x == geschlecht.getSelectedIndex();
                    }

                }
                if (isHeatBased()) {
                    if (isPartOnly) {
                        PrintExecutor.print(getPrintable(wkx, !isCompleteDiscipline, round, qualification, selection),
                                I18n.get("Result"), getController().getWindow());
                    } else {
                        PrintExecutor.print(getPrintable(wkx, false, 0, 0, selection), I18n.get("Results"),
                                getController().getWindow());
                    }
                } else {
                    PrintExecutor.print(
                            getPrintable(wkx, false, 0, 0,
                                    new boolean[][] { { geschlecht.getSelectedIndex() == 0 },
                                            { geschlecht.getSelectedIndex() == 1 } }),
                            I18n.get("Results"), getController().getWindow());
                }
            }
        } finally {
            print.setEnabled(glas.isEnabled() && !elements.isEmpty());
        }
    }

    @SuppressWarnings({ "rawtypes", "hiding" })
    private Printable getPrintable(AWettkampf wk, boolean isRound, int round, int qualification, boolean[][] selected) {
        LinkedList<Printable> ps = getPrintable(selected, wk, qualification);
        MessageFormat header;
        if (isRound) {
            header = new MessageFormat(I18n.getRound(round, qualification == 0));
        } else {
            header = new MessageFormat("");
        }
        return PrintManager.getFinalPrintable(new MultiplePrintable(ps), wk.getLastChangedDate(), header,
                I18n.get("Einzelwertung"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "hiding" })
    protected LinkedList<Printable> getPrintable(boolean[][] selected, AWettkampf wk, int qualification) {
        return PrintUtils.getFullResultsPrintable(selected, wk, true, PrintUtils.printPointsInDisciplineResults,
                qualification);
    }

    private void initListeners() {
        mouseListener = new InternalMouseListener();
        ResultAKSexItemListener itemListener1 = new ResultAKSexItemListener();
        ResultAKSexItemListener itemListener2 = itemListener1;
        ResultDisciplineItemListener itemListener3 = new ResultDisciplineItemListener();
        ResultRoundItemListener itemListener4 = new ResultRoundItemListener();

        altersklasse.addItemListener(itemListener1);
        geschlecht.addItemListener(itemListener2);
        disziplin.addItemListener(itemListener3);
        runde.addItemListener(itemListener4);
    }

    void initGUI() {
        result = JResultTable.getResultTable(null, 0, false, false, false, 0, "-");
        elements = new LinkedList<>();

        altersklasse = new JComboBox<>();
        geschlecht = new JComboBox<>();
        disziplin = new JComboBox<>();
        runde = new JComboBox<>();
        update = new JButton(I18n.get("Update"));
        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));

        disziplin.setEnabled(false);
        runde.setEnabled(false);
        update.setEnabled(false);
        print.setEnabled(false);

        altersklasse.setToolTipText(I18n.getToolTip("SelectAgeGroup"));

        geschlecht.addItem(I18n.get("female"));
        geschlecht.addItem(I18n.get("male"));
        geschlecht.setSelectedIndex(0);
        geschlecht.setToolTipText(I18n.getToolTip("SelectSex"));

        disziplin.addItem(I18n.get("Mehrkampf"));
        disziplin.setSelectedIndex(0);

        runde.addItem("-");
        runde.setSelectedIndex(0);

        update.addActionListener(e -> {
            updateResult(true);
        });
        print.addActionListener(e -> {
            print();
        });

        String horizontal = "4dlu,fill:default:grow," + FormLayoutUtils.createLayoutString(10)
                + ",fill:default:grow,4dlu";

        FormLayout layout = new FormLayout(horizontal, "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 20, 22 }, { 6, 10, 14, 18 }, { 4, 8, 12, 16 } });
        panel = new JPanel(layout);
        panel.setName(I18n.get("Results"));

        panel.add(new JLabel(I18n.get("AgeGroup")), CC.xy(4, 2));
        panel.add(altersklasse, CC.xy(6, 2));
        panel.add(new JLabel(I18n.get("Sex")), CC.xy(8, 2));
        panel.add(geschlecht, CC.xy(10, 2));
        panel.add(new JLabel(I18n.get("Discipline")), CC.xy(12, 2));
        panel.add(disziplin, CC.xy(14, 2));
        panel.add(new JLabel(I18n.get("Round")), CC.xy(16, 2));
        panel.add(runde, CC.xy(18, 2));
        panel.add(update, CC.xy(20, 2));
        panel.add(print, CC.xy(22, 2));

        scroller = new JScrollPane(result);
        scroller.setBorder(new ShadowBorder());

        glas = new JGlassPanel<>(scroller);
        panel.add(glas, CC.xyw(2, 4, 23));

        initListeners();
    }

    private boolean isUpdating() {
        return isUpdatingAks || isUpdatingDisciplines || isUpdatingRounds;
    }

    private boolean isUpdatingAks = false;
    private boolean isUpdatingDisciplines = false;
    private boolean isUpdatingRounds = false;

    private void updateAks() {
        if (isUpdatingAks) {
            return;
        }
        try {
            isUpdatingAks = true;
            boolean updatable = false;
            if (wk == null) {
                altersklasse.removeAllItems();
            } else {
                updatable = wk.isHeatBased();
                int index = altersklasse.getSelectedIndex();
                if (index < 0) {
                    index = 0;
                }
                Regelwerk aks = wk.getRegelwerk();
                boolean ok = false;
                if (altersklasse.getItemCount() == aks.size()) {
                    ok = true;
                    for (int x = 0; x < aks.size(); x++) {
                        if (!altersklasse.getItemAt(x).equals(aks.getAk(x).getName())) {
                            ok = false;
                            break;
                        }
                    }
                }
                if (!ok) {
                    altersklasse.removeAllItems();
                    for (int x = 0; x < aks.size(); x++) {
                        altersklasse.addItem(aks.getAk(x).toString());
                    }
                    try {
                        if (altersklasse.getItemCount() > index) {
                            altersklasse.setSelectedIndex(index);
                        } else {
                            altersklasse.setSelectedIndex(0);
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        altersklasse.setSelectedIndex(0);
                    }
                }
                updateDisciplines();
            }
            update.setEnabled(updatable);
        } finally {
            isUpdatingAks = false;
        }
    }

    private boolean isHeatBased() {
        return wk.isHeatBased();
    }

    private void updateDisciplines() {
        if (isUpdatingDisciplines) {
            return;
        }
        try {
            isUpdatingDisciplines = true;
            if (!isHeatBased()) {
                if (disziplin.isEnabled()) {
                    disziplin.setEnabled(false);

                    disziplin.removeAllItems();
                    disziplin.addItem(I18n.get("Mehrkampf"));
                    disziplin.setSelectedIndex(0);
                }
            } else {
                disziplin.setEnabled(true);

                int index = disziplin.getSelectedIndex();
                disziplin.removeAllItems();
                if (altersklasse.getSelectedIndex() >= 0) {
                    Regelwerk aks = wk.getRegelwerk();
                    Altersklasse ak = aks.getAk(altersklasse.getSelectedIndex());
                    for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                        disziplin.addItem(ak.getDisziplin(x, geschlecht.getSelectedIndex() == 1).getName());
                    }
                    disziplin.addItem("Mehrkampf");
                    if (index >= disziplin.getItemCount() || index <= 0) {
                        index = disziplin.getItemCount() - 1;
                    }
                    try {
                        disziplin.setSelectedIndex(index);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        disziplin.setSelectedIndex(0);
                    }
                }
                updateRounds();
            }
        } finally {
            isUpdatingDisciplines = false;
        }
    }

    private void updateRounds() {
        if (isUpdatingRounds) {
            return;
        }
        try {
            isUpdatingRounds = true;
            if (!isHeatBased()) {
                if (runde.isEnabled()) {
                    runde.setEnabled(false);

                    runde.removeAllItems();
                    runde.addItem("-");
                    runde.setSelectedIndex(0);
                }
            } else {
                runde.setEnabled(true);

                int index = runde.getSelectedIndex();
                runde.removeAllItems();

                if (disziplin.getSelectedIndex() + 1 == disziplin.getItemCount()) {
                    runde.setEnabled(false);
                    runde.addItem("-");
                    runde.setSelectedIndex(0);
                } else if (disziplin.getSelectedIndex() >= 0) {
                    Regelwerk aks = wk.getRegelwerk();
                    Altersklasse ak = aks.getAk(altersklasse.getSelectedIndex());
                    Disziplin d = ak.getDisziplin(disziplin.getSelectedIndex(), geschlecht.getSelectedIndex() == 1);
                    int[] runden = d.getRunden();
                    for (int x = 0; x < runden.length; x++) {
                        runde.addItem(I18n.getRound(x, false));
                    }
                    runde.addItem(I18n.getRound(0, true));
                    runde.addItem(I18n.get("Overall"));
                    if (index >= runde.getItemCount()) {
                        index = runde.getItemCount() - 1;
                    } else if (index <= 0) {
                        index = 0;
                    }
                    try {
                        runde.setSelectedIndex(index);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        runde.setSelectedIndex(0);
                    }
                }
                updateResult(false);
            }
        } finally {
            isUpdatingRounds = false;
        }
    }

    void updateWk() {
        wk = core.getWettkampf();
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        controller = c;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        editor = (FEditorPlugin) controller.getFeature("de.df.jauswertung.editor", uid);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] {
                new PanelInfo(I18n.get("Results"), IconManager.getBigIcon("resulttable"), false, false, 1500) {
                    @Override
                    public JPanel getPanelI() {
                        if (panel == null) {
                            initGUI();
                            dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                        }
                        return panel;
                    }
                } };
    }

    class ResultAKSexItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (isUpdating()) {
                return;
            }
            if (core.getWettkampf().isHeatBased()) {
                sendAKSexChanged();
            } else {
                updateResult(false);
            }
        }
    }

    class ResultDisciplineItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (isUpdating()) {
                return;
            }
            if (core.getWettkampf().isHeatBased()) {
                sendDisciplineChanged();
            }
        }
    }

    class ResultRoundItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (isUpdating()) {
                return;
            }
            if (core.getWettkampf().isHeatBased()) {
                sendRoundChanged();
            }
        }
    }

    final class InternalMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent evt) {
            if ((!evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
                int column = result.getSelectedColumn();
                if (result.isInPrefix(column)) {
                    editSchwimmer();
                } else {
                    ASchwimmer s = getSelectedSwimmer();
                    int disz = result.getSelectedDiscipline();
                    if (result.isHLW()) {
                        editHLW();
                    } else {
                        if ((disz >= 0) && s.isDisciplineChosen(disz)) {
                            if (result.isPenaltyColumn(column)) {
                                editPenalty();
                            } else {
                                editTime();
                            }
                        }
                    }
                }
            }
        }
    }

    void sendAKSexChanged() {
        updateDisciplines();
    }

    private void sendDisciplineChanged() {
        updateRounds();
    }

    private void sendRoundChanged() {
        updateResult(false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void showStarts() {
        if (!isHeatBased()) {
            ASchwimmer s = getSelectedSwimmer();
            new JStartsViewer(getController().getWindow(), wk, s).setVisible(true);
        }
    }

    void editHLW() {
        ASchwimmer s = getSelectedSwimmer();
        editor.runZWEditor(s);
    }

    @SuppressWarnings("unchecked")
    void editTime() {
        ASchwimmer s = getSelectedSwimmer();
        if (isHeatBased()) {
            s = SearchUtils.getSchwimmer(wk, s);
            if (disziplin.getSelectedIndex() + 1 == disziplin.getItemCount()) {
                // editor.runPenaltyEditor(wk, s, ASchwimmer.DISCIPLINE_NUMBER_SELF);
                // editor.runTimeEditor(s, disz);
            } else {
                // String disz = OWDisziplin.getId(altersklasse.getSelectedIndex(),
                // geschlecht.getSelectedIndex() == 1,
                // disziplin.getSelectedIndex(), runde.getSelectedIndex());
                // editor.runPenaltyEditor(wk, s, disz);
                // editor.runTimeEditor(s, disz);
            }
        } else {
            int disz = result.getSelectedDiscipline();
            editor.runTimeEditor(s, disz);
        }

    }

    void editSchwimmer() {
        ASchwimmer s = getSelectedSwimmer();
        editor.editSchwimmer(controller.getWindow(), s, true);
    }

    @SuppressWarnings("unchecked")
    void editPenalty() {
        ASchwimmer s = getSelectedSwimmer();
        if (isHeatBased()) {
            if (disziplin.getSelectedIndex() + 1 == disziplin.getItemCount()) {
                int diszx = result.getSelectedDiscipline();
                if (diszx >= 0) {
                    String disz = OWDisziplin.getId(altersklasse.getSelectedIndex(), geschlecht.getSelectedIndex() == 1,
                            diszx, 0);
                    if (wk.getLauflisteOW().getDisziplin(disz) != null) {
                        editor.runPenaltyEditor(wk, s, disz);
                    }
                } else {
                    editor.runPenaltyEditor(wk, s, ASchwimmer.DISCIPLINE_NUMBER_SELF);
                }
            } else {
                int round = runde.getSelectedIndex();
                if (round + 1 == runde.getItemCount()) {
                    round--;
                }
                String disz = OWDisziplin.getId(altersklasse.getSelectedIndex(), geschlecht.getSelectedIndex() == 1,
                        disziplin.getSelectedIndex(), round);
                editor.runPenaltyEditor(wk, s, disz);
            }
        } else {
            int disz = result.getSelectedDiscipline();
            editor.runPenaltyEditor(wk, s, disz);
        }
    }

    void withdraw() {
        ASchwimmer s = getSelectedSwimmer();
        if (isHeatBased()) {
            if (disziplin.getSelectedIndex() + 1 == disziplin.getItemCount()) {
                int diszx = result.getSelectedDiscipline();
                if (diszx >= 0) {
                    String disz = OWDisziplin.getId(altersklasse.getSelectedIndex(), geschlecht.getSelectedIndex() == 1,
                            diszx, 0);
                    if (wk.getLauflisteOW().getDisziplin(disz) != null) {
                        s.addStrafe(disz, new Strafe("", "WD", Strafarten.NICHTS, 0));
                        sendDataUpdateEvent("Withdraw", UpdateEventConstants.REASON_PENALTY);
                    }
                }
            } else {
                int round = runde.getSelectedIndex();
                if (round + 1 == runde.getItemCount()) {
                    round--;
                }
                String disz = OWDisziplin.getId(altersklasse.getSelectedIndex(), geschlecht.getSelectedIndex() == 1,
                        disziplin.getSelectedIndex(), round);
                s.addStrafe(disz, new Strafe("", "WD", Strafarten.NICHTS, 0));
                sendDataUpdateEvent("Withdraw", UpdateEventConstants.REASON_PENALTY);
            }
        }
    }

    private class JEditPopup extends JPopupMenu {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3256441404417716535L;

        JMenuItem edit = null;
        JMenuItem delete = null;
        JMenuItem heatsitem = null;
        JMenuItem penalty = null;
        JMenuItem withdraw = null;

        public JEditPopup() {
            result.addMouseListener(new Listener());

            edit = new JMenuItem(I18n.get("Edit"), IconManager.getSmallIcon("edit"));
            edit.addActionListener(e -> {
                ASchwimmer s = getSelectedSwimmer();
                editor.editSchwimmer(s, true);
            });
            delete = new JMenuItem(I18n.get("Delete"), IconManager.getSmallIcon("delete"));
            delete.addActionListener(e -> {
                ASchwimmer s = getSelectedSwimmer();
                core.remove(s);
            });
            heatsitem = new JMenuItem(I18n.get("HeatsViewer"));
            heatsitem.addActionListener(e -> {
                showStarts();
            });
            penalty = new JMenuItem(I18n.get("SetPenalty"));
            penalty.addActionListener(e -> {
                editPenalty();
            });
            withdraw = new JMenuItem(I18n.get("Withdraw"));
            withdraw.addActionListener(e -> {
                withdraw();
            });

            setLayout(new ListLayout(1));
            add(new JGradientLabel(I18n.get("Information")));
            add(heatsitem);
            add(new JGradientLabel(I18n.get("ChangeData")));
            add(edit);
            add(delete);
            add(penalty);
            add(withdraw);
        }

        class Listener extends MouseAdapter {

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    int column = result.columnAtPoint(me.getPoint());
                    result.setColumnSelectionInterval(column, column);

                    int row = result.rowAtPoint(me.getPoint());
                    if (row > -1) {
                        result.setRowSelectionInterval(row, row);
                        show(me.getComponent(), me.getX(), me.getY());
                    }
                    boolean enabled = (result.getSelectedRowCount() == 1);
                    edit.setEnabled(enabled);
                    delete.setEnabled(enabled);
                    heatsitem.setEnabled(enabled && wk.hasLaufliste() && !wk.isHeatBased());
                    penalty.setEnabled(enabled);
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mousePressed(me);
            }
        }
    }
}