/*
 * Created on 26.03.2004
 */
package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.plugins.starter.StarterPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JStartsViewer;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JGradientLabel;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.jtable.ColumnFittingMouseAdapter;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.jtable.TableSorter;
import de.df.jutils.gui.layout.ListLayout;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.io.csv.FixedDecimal;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 26.03.2004
 */
public class PSearchPlugin extends ANullPlugin {

    static final String[] IDS = new String[] { "StartnumberShort", "Name", "Organisation", "Qualifikationsebene",
            "YearOfBirth", "ReportedPoints", "Comment",
            "AgeGroup", "Sex", "QualifiedShort", "AusserKonkurrenz" };

    static final String[] TITLES = new String[IDS.length];

    static {
        for (int x = 0; x < TITLES.length; x++) {
            TITLES[x] = I18n.get(IDS[x]);
        }
    }

    static int getJahrgangsindex() {
        return 4;
    }

    private static final long REASON_UPDATE = REASON_LOAD_WK | REASON_NEW_TN | REASON_NEW_WK | REASON_SWIMMER_DELETED
            | REASON_SWIMMER_CHANGED
            | REASON_GLIEDERUNG_CHANGED;

    JPanel panel;
    JTable result;
    TableSorter sorter;

    JEditPopup popup;

    SimpleTableModel model;
    private boolean[] enabledColumns;
    private JIntegerField startnummer;
    private JWarningTextField name;
    private JDoubleField points;
    private JWarningTextField bemerkung;
    private JWarningTextField gliederung;
    private JWarningTextField quali;
    private JComboBox<String> altersklasse;
    private JComboBox<String> geschlecht;
    private JComboBox<String> ausserk;

    private JButton edit;
    private JButton togglenames;
    private JButton delete;

    @SuppressWarnings("rawtypes")
    private LinkedList elements;
    CorePlugin core = null;
    FEditorPlugin editor = null;
    StarterPlugin starter = null;
    boolean einzel = true;

    public PSearchPlugin() {
        super();
    }

    private void createResultTable() {
        int count = 0;
        for (boolean enabledColumn : enabledColumns) {
            if (enabledColumn) {
                count++;
            }
        }
        Object[] titles = new Object[count];
        int pos = 0;
        for (int x = 0; x < enabledColumns.length; x++) {
            if (enabledColumns[x]) {
                titles[pos] = TITLES[x];
                pos++;
            }
        }
        model = new SimpleTableModel(new Object[0][0], titles) {
            private static final long serialVersionUID = 1817151708284665793L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return PSearchPlugin.this.getColumnClass(columnIndex);
            }
        };
        sorter = new TableSorter(model);
        result = new JTable(sorter);
        sorter.setTableHeader(result.getTableHeader());
        sorter.setColumnComparator(Object.class, null);
        result.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        result.getTableHeader().setReorderingAllowed(false);
        JTableUtils.setTableCellRenderer(result, new AlignmentCellRenderer(new int[0], SwingConstants.CENTER));

        JTableUtils.setAlternatingTableCellRenderer(result);
        ColumnFittingMouseAdapter.enable(result);
        DataTipManager.get().register(result);

        result.getModel().addTableModelListener(e -> {
            updateButtons();
        });
        result.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateButtons();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                updateButtons();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateButtons();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateButtons();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateButtons();
            }
        });
        result.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                updateButtons();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateButtons();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                updateButtons();
            }

        });
    }

    void updateButtons() {
        boolean e = (core.getEinzelWettkampf() != null);
        int selected = result.getSelectedRows().length;
        edit.setEnabled(selected == 1);
        delete.setEnabled(selected >= 1);
        togglenames.setEnabled(e && (selected > 0));
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (panel == null) {
            return;
        }
        if ((due.getChangeReason() & REASON_AKS_CHANGED) > 0) {
            updateAks();
            updateSexes();
        }
        if ((due.getChangeReason() & (REASON_LOAD_WK | REASON_NEW_WK)) > 0) {
            updateTable();
            updateMenu();
        }
        if ((due.getChangeReason() & (REASON_UPDATE)) > 0) {
            search();
        }
        updateButtons();
    }

    private void updateSexes() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = (getController() == null ? null : core.getWettkampf());
        String[] items = null;
        if (wk == null) {
            items = new String[] { I18n.get("female"), I18n.get("male"), I18n.get("All") };
        } else {
            Regelwerk rw = wk.getRegelwerk();
            items = new String[] { I18n.geschlechtToStringSubject(rw, false), I18n.geschlechtToStringSubject(rw, true),
                    I18n.get("All") };
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void search() {
        AWettkampf wk = core.getWettkampf();
        elements = SearchUtils.search(startnummer.getText(), name.getText(), points.getDouble(), bemerkung.getText(),
                gliederung.getText(), quali.getText(),
                altersklasse.getSelectedIndex(), geschlecht.getSelectedIndex(), ausserk.getSelectedIndex(), wk);
        if (result.getRowCount() > elements.size()) {
            while (result.getRowCount() > elements.size()) {
                model.removeRow(model.getRowCount() - 1);
            }
            result.tableChanged(new TableModelEvent(result.getModel()));
        } else if (result.getRowCount() < elements.size()) {
            while (result.getRowCount() < elements.size()) {
                Vector<String> v = new Vector<>();
                for (int x = 0; x < result.getColumnCount(); x++) {
                    v.add("");
                }
                model.addRow(v);
            }
            result.tableChanged(new TableModelEvent(result.getModel()));
        }

        updateTable();
    }

    private static Object getPointRepresentation(double d) {
        if (d < 0.005) {
            return new FixedDecimal();
        }
        return new FixedDecimal(d, 2);
    }

    Class<?> getColumnClass(int column) {
        int index = 0;
        if (isTableColumnEnabled(0)) {
            if (column == index) {
                return Integer.class;
            }
            index++;
        }
        if (isTableColumnEnabled(1)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(2)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(3)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(4)) {
            if (column == index) {
                if (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf) {
                    return String.class;
                }
                return Object.class;
            }
            index++;
        }
        if (isTableColumnEnabled(5)) {
            if (column == index) {
                return FixedDecimal.class;
            }
            index++;
        }
        if (isTableColumnEnabled(6)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(7)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(8)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(9)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        if (isTableColumnEnabled(10)) {
            if (column == index) {
                return String.class;
            }
            index++;
        }
        return Object.class;
    }

    private void updateMenu() {
        popup.starteritem.setEnabled(((AWettkampf<?>) core.getWettkampf()) instanceof MannschaftWettkampf);
    }

    private void updateTable() {
        if (einzel != (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf)) {
            int index = getJahrgangsindex();
            einzel = !einzel;
            if (einzel) {
                if (isTableColumnEnabled(index)) {
                    enabledColumns[index] = false;
                    setTableColumnEnabled(index, true);
                }
            } else {
                if (enabledColumns[index]) {
                    setTableColumnEnabled(index, false);
                    enabledColumns[index] = true;
                }
            }
        }

        updateTableHeader();

        @SuppressWarnings("rawtypes")
        ListIterator li = elements.listIterator();
        while (li.hasNext()) {
            int y = li.nextIndex();
            ASchwimmer t = (ASchwimmer) li.next();
            int index = 0;
            if (isTableColumnEnabled(0)) {
                model.setValueAt(StartnumberFormatManager.format(t), y, index);
                index++;
            }
            if (isTableColumnEnabled(1)) {
                model.setValueAt(t.getName(), y, index);
                index++;
            }
            if (isTableColumnEnabled(2)) {
                model.setValueAt(t.getGliederung(), y, index);
                index++;
            }
            if (isTableColumnEnabled(3)) {
                model.setValueAt(t.getQualifikationsebene(), y, index);
                index++;
            }
            if (isTableColumnEnabled(4)) {
                if (t instanceof Teilnehmer) {
                    Teilnehmer x = (Teilnehmer) t;
                    model.setValueAt(x.getJahrgang() == 0 ? null : I18n.yearToObject(x.getJahrgang()), y, index);
                }
                index++;
            }
            if (isTableColumnEnabled(5)) {
                model.setValueAt(getPointRepresentation(t.getMeldepunkte(0)), y, index);
                index++;
            }
            if (isTableColumnEnabled(6)) {
                model.setValueAt(t.getBemerkung(), y, index);
                index++;
            }
            if (isTableColumnEnabled(7)) {
                model.setValueAt(t.getAK().toString(), y, index);
                index++;
            }
            if (isTableColumnEnabled(8)) {
                model.setValueAt(I18n.geschlechtToString(t), y, index);
                index++;
            }
            if (isTableColumnEnabled(9)) {
                model.setValueAt(t.getQualifikation().toString(), y, index);
                index++;
            }
            if (isTableColumnEnabled(10)) {
                model.setValueAt((t.isAusserKonkurrenz() ? I18n.get("yes") : I18n.get("no")), y, index);
                index++;
            }
        }
        JTableUtils.setPreferredCellWidths(result);
        // JPrintTable.initPrintableJTable(result);
    }

    private void updateTableHeader() {
        int index = 0;
        for (int i = 0; i < enabledColumns.length; i++) {
            if (isTableColumnEnabled(i)) {
                result.getColumnModel().getColumn(index).setHeaderValue(I18n.get(IDS[i]));
                index++;
            }
        }
    }

    private void initListeners() {
        SearchKeyListener keyListener = new SearchKeyListener();
        SearchItemListener itemListener = new SearchItemListener();
        TableListener tableListener = new TableListener();

        startnummer.addKeyListener(keyListener);
        name.addKeyListener(keyListener);
        points.addKeyListener(keyListener);
        gliederung.addKeyListener(keyListener);
        quali.addKeyListener(keyListener);
        altersklasse.addItemListener(itemListener);
        geschlecht.addItemListener(itemListener);
        result.addMouseListener(tableListener);
        result.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    // Todo: Search for a way to prevent the row change
                    // bearbeiten();
                    // e.consume();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_CUT:
                    ASchwimmer[] s = getSchwimmer();
                    if (s != null) {
                        core.remove(s);
                    }
                    e.consume();
                    break;
                default:
                    break;
                }
            }
        });
        ausserk.addItemListener(itemListener);
        bemerkung.addKeyListener(keyListener);
    }

    void initGUI() {
        enabledColumns = new boolean[IDS.length];
        for (int i = 0; i < enabledColumns.length; i++) {
            enabledColumns[i] = true;
        }
        enabledColumns[4] = false;
        enabledColumns[5] = false;
        enabledColumns[6] = false;
        enabledColumns[9] = false;
        enabledColumns[10] = false;

        createResultTable();

        startnummer = new JIntegerField();
        startnummer.setToolTipText(I18n.getToolTip("SearchStartnumber"));
        name = new JNoSpaceTextfield();
        name.setToolTipText(I18n.getToolTip("SearchName"));
        points = new JDoubleField();
        points.setToolTipText(I18n.getToolTip("SearchReportedPoints"));
        bemerkung = new JNoSpaceTextfield();
        bemerkung.setToolTipText(I18n.getToolTip("SearchComment"));
        gliederung = new JNoSpaceTextfield();
        gliederung.setToolTipText(I18n.getToolTip("SearchOrganization"));
        quali = new JNoSpaceTextfield();
        quali.setToolTipText(I18n.getToolTip("SearchQualifikationsgliederung"));
        altersklasse = new JComboBox<>();
        altersklasse.setToolTipText(I18n.getToolTip("SearchAgeGroup"));
        geschlecht = new JComboBox<>(new String[] { I18n.get("female"), I18n.get("male"), I18n.get("All") });
        geschlecht.setSelectedIndex(2);
        geschlecht.setToolTipText(I18n.getToolTip("SearchSex"));
        ausserk = new JComboBox<>(new String[] { I18n.get("No"), I18n.get("Yes"), I18n.get("All") });
        ausserk.setSelectedIndex(2);
        ausserk.setToolTipText(I18n.getToolTip("SearchAusserKonkurrenz"));

        FormLayout layout = new FormLayout(
                "4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default:grow,"
                        + "fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,"
                        + "fill:default:grow,4dlu");
        layout.setRowGroups(new int[][] { { 4, 6, 8, 10, 12, 14, 16, 18, 20 } });
        layout.setColumnGroups(new int[][] { { 7, 9 } });

        panel = new JPanel(layout);
        panel.setName(I18n.get("Registrations"));
        panel.add(new JLabel(I18n.get("Startnumber")), CC.xy(2, 4));
        panel.add(new JLabel(I18n.get("Name")), CC.xy(2, 6));
        panel.add(new JLabel(I18n.get("ReportedPoints")), CC.xy(2, 8));
        panel.add(new JLabel(I18n.get("Comment")), CC.xy(2, 10));
        panel.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 12));
        panel.add(new JLabel(I18n.get("Qualifikationsebene")), CC.xy(2, 14));
        panel.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 16));
        panel.add(new JLabel(I18n.get("Sex")), CC.xy(2, 18));
        panel.add(new JLabel(I18n.get("AusserKonkurrenz")), CC.xy(2, 20));

        panel.add(startnummer, CC.xy(4, 4));
        panel.add(name, CC.xy(4, 6));
        panel.add(points, CC.xy(4, 8));
        panel.add(bemerkung, CC.xy(4, 10));
        panel.add(gliederung, CC.xy(4, 12));
        panel.add(quali, CC.xy(4, 14));
        panel.add(altersklasse, CC.xy(4, 16));
        panel.add(geschlecht, CC.xy(4, 18));
        panel.add(ausserk, CC.xy(4, 20));

        JScrollPane scr = new JScrollPane(result);
        scr.setBorder(new ShadowBorder());
        panel.add(scr, CC.xywh(6, 4, 4, 18));
        panel.add(getButtons(), CC.xywh(6, 2, 4, 1));

        startnummer.requestFocus();

        initListeners();
        popup = new JEditPopup();
        new JTableHeaderPopup();
    }

    private JPanel getButtons() {
        edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
        edit.setToolTipText(I18n.getToolTip("Edit"));
        edit.addActionListener(e -> {
            bearbeiten();
        });
        togglenames = new JTransparentButton(IconManager.getSmallIcon("togglenames"));
        togglenames.setToolTipText(I18n.getToolTip("ToggleNames"));
        togglenames.addActionListener(e -> {
            toggleNames();
        });
        delete = new JTransparentButton(IconManager.getSmallIcon("remove"));
        delete.setToolTipText(I18n.getToolTip("Remove"));
        delete.addActionListener(e -> {
            core.remove(getSchwimmer());
        });

        JPanel p = new JPanel();
        p.setLayout(
                new FormLayout("0dlu,fill:default,0dlu,fill:default,0dlu,fill:default,0dlu", "0dlu,fill:default,0dlu"));

        p.add(edit, CC.xy(2, 2));
        p.add(delete, CC.xy(4, 2));
        p.add(togglenames, CC.xy(6, 2));
        return p;
    }

    @SuppressWarnings("rawtypes")
    private void updateAks() {
        AWettkampf wk = (getController() == null ? null : core.getWettkampf());
        if (wk == null) {
            altersklasse.removeAllItems();
            String item = I18n.get("All");
            altersklasse.addItem(item);
            altersklasse.setSelectedIndex(0);
        } else {
            int index = altersklasse.getSelectedIndex();
            boolean isLast = (index == altersklasse.getItemCount() - 1);
            altersklasse.removeAllItems();
            Regelwerk aks = wk.getRegelwerk();
            for (int x = 0; x < aks.size(); x++) {
                altersklasse.addItem(aks.getAk(x).toString());
            }
            altersklasse.addItem(I18n.get("All"));
            if (isLast) {
                altersklasse.setSelectedIndex(altersklasse.getItemCount() - 1);
            } else {
                altersklasse.setSelectedIndex(index);
            }
        }
    }

    final static class JNoSpaceTextfield extends JWarningTextField {

        public JNoSpaceTextfield() {
            super(false, true);
            setColumns(15);
        }

        @Override
        public boolean isValidString() {
            String t = getText();
            return (t.trim().length() == t.length());
        }
    }

    class SearchItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            search();
        }
    }

    class SearchKeyListener extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent event) {
            search();
        }
    }

    class TableListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent me) {
            if ((!me.isPopupTrigger()) && (me.getClickCount() == 2)) {
                bearbeiten();
            }
        }
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        super.setController(controller, uid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        editor = (FEditorPlugin) controller.getFeature("de.df.jauswertung.editor", uid);
        starter = (StarterPlugin) controller.getFeature("de.df.jauswertung.starter", uid);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] {
                new PanelInfo(I18n.get("Registrations"), IconManager.getBigIcon("meldungen"), true, false, 300) {

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

    void bearbeiten() {
        if (result.getSelectedRows().length > 1) {
            return;
        }
        int index = result.getSelectedRow();
        if (index < 0) {
            return;
        }
        editor.editSchwimmer(getSchwimmer()[0], true);
    }

    ASchwimmer[] getSchwimmer() {
        int[] cols = result.getSelectedRows();
        if ((cols != null) && (cols.length > 0)) {
            ASchwimmer[] s = new ASchwimmer[cols.length];
            for (int x = 0; x < cols.length; x++) {
                s[x] = (ASchwimmer) elements.get(sorter.modelIndex(cols[x]));
            }
            return s;
        }
        return null;
    }

    void setTableColumnEnabled(int index, boolean value) {
        if (enabledColumns[index] != value) {
            enabledColumns[index] = value;
            if (value) {
                model.addColumn("");
            } else {
                model.removeColumn();
            }
            updateTable();
        }
    }

    boolean isTableColumnEnabled(int x) {
        if (x == getJahrgangsindex()) {
            if (!einzel) {
                return false;
            }
        }
        return enabledColumns[x];
    }

    class JEditPopup extends JPopupMenu {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3256441404417716535L;

        JMenuItem edititem = null;
        JMenuItem starteritem = null;
        JMenuItem heatsitem = null;
        JMenuItem deleteitem = null;
        JMenuItem penaltyitem = null;
        JMenuItem toggleitem = null;
        JMenuItem renameOrganization = null;
        JMenuItem renameQOrganization = null;

        public JEditPopup() {
            result.addMouseListener(new Listener());

            edititem = new JMenuItem(I18n.get("Edit"), IconManager.getSmallIcon("edit"));
            edititem.addActionListener(e -> {
                editor.editSchwimmer(getSchwimmer()[0], true);
            });
            starteritem = new JMenuItem(I18n.get("EditStarter"));
            starteritem.addActionListener(e -> {
                starter.editStarter((Mannschaft) (getSchwimmer()[0]));
            });
            heatsitem = new JMenuItem(I18n.get("HeatsViewer"));
            heatsitem.addActionListener(e -> {
                showStarts();
            });
            deleteitem = new JMenuItem(I18n.get("Delete"), IconManager.getSmallIcon("delete"));
            deleteitem.addActionListener(e -> {
                core.remove(getSchwimmer());
            });
            toggleitem = new JMenuItem(I18n.get("ToggleNames"));
            toggleitem.addActionListener(e -> {
                toggleNames();
            });
            penaltyitem = new JMenuItem(I18n.get("SetPenalty"));
            penaltyitem.addActionListener(e -> {
                showPenaltyEditor();
            });
            renameOrganization = new JMenuItem(I18n.get("Organization"));
            renameOrganization.addActionListener(e -> {
                renameOrganization();
            });
            renameQOrganization = new JMenuItem(I18n.get("Qualifikationsebene"));
            renameQOrganization.addActionListener(e -> {
                renameQOrganization();
            });

            setLayout(new ListLayout(1));
            add(new JGradientLabel(I18n.get("Information")));
            add(heatsitem);
            add(new JGradientLabel(I18n.get("ChangeData")));
            add(edititem);
            add(starteritem);
            add(deleteitem);
            add(toggleitem);
            add(penaltyitem);
            add(new JGradientLabel(I18n.get("Rename")));
            add(renameOrganization);
            add(renameQOrganization);
        }

        class Listener extends MouseAdapter {

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    int row = result.rowAtPoint(me.getPoint());
                    if (row > -1) {
                        int[] rows = result.getSelectedRows();
                        boolean selected = false;
                        for (int row1 : rows) {
                            if (row1 == row) {
                                selected = true;
                                break;
                            }
                        }
                        if (!selected) {
                            result.setRowSelectionInterval(row, row);
                        }

                        deleteitem.setEnabled(result.getSelectedRowCount() > 0);
                        boolean enabled = (result.getSelectedRowCount() == 1);
                        edititem.setEnabled(enabled);
                        heatsitem.setEnabled(enabled);
                        penaltyitem.setEnabled(enabled);
                        toggleitem
                                .setEnabled((result.getSelectedRowCount() > 0) && (core.getEinzelWettkampf() != null));

                        show(me.getComponent(), me.getX(), me.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mousePressed(me);
            }
        }
    }

    private class JTableHeaderPopup extends JPopupMenu {

        private static final long serialVersionUID = -2482446706877923019L;

        JMenuItem[] items = new JMenuItem[IDS.length];

        public JTableHeaderPopup() {
            result.getTableHeader().addMouseListener(new Listener());

            add(new JGradientLabel(I18n.get("Columns")));
            for (int i = 0; i < IDS.length; i++) {
                items[i] = createMenuItem(i);
                add(items[i]);
            }
        }

        @Override
        public void show(Component invoker, int x, int y) {
            items[getJahrgangsindex()].setVisible(einzel);
            if (countSelected() == 1) {
                for (JMenuItem item : items) {
                    item.setEnabled(!item.isSelected());
                }
            } else {
                for (JMenuItem item : items) {
                    item.setEnabled(true);
                }
            }
            super.show(invoker, x, y);
        }

        private int countSelected() {
            int selected = 0;
            for (JMenuItem item : items) {
                if (item.isSelected()) {
                    selected++;
                }
            }
            return selected;
        }

        private JMenuItem createMenuItem(int index) {
            JMenuItem item = new JCheckBoxMenuItem(I18n.get(IDS[index]));
            item.addActionListener(e -> {
                for (int i = 0; i < items.length; i++) {
                    if (e.getSource() == items[i]) {
                        setTableColumnEnabled(i, items[i].isSelected());
                    }
                }
            });
            item.setSelected(isTableColumnEnabled(index));
            return item;
        }

        class Listener extends MouseAdapter {

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    show(me.getComponent(), me.getX(), me.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                mousePressed(evt);
            }
        }
    }

    void toggleNames() {
        editor.toggleNames(getSchwimmer());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void showStarts() {
        new JStartsViewer(getController().getWindow(), core.getWettkampf(), getSchwimmer()[0]).setVisible(true);
    }

    void showPenaltyEditor() {
        editor.runPenaltyEditor(getController().getWindow(), core.getWettkampf(), getSchwimmer()[0]);
    }

    void renameOrganization() {
        editor.renameOrganization(getController().getWindow(), core.getWettkampf(), getSchwimmer()[0]);
    }

    void renameQOrganization() {
        editor.renameQualiOrganization(getController().getWindow(), core.getWettkampf(), getSchwimmer()[0]);
    }
}