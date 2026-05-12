package de.df.jauswertung.gui.plugins.importexport;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.ImportUtils;
import de.df.jauswertung.io.model.AgegroupGenderDisciplineRound;
import de.df.jauswertung.io.model.StartersImportDto;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.UpdateListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectionPage extends AWizardPage implements PageSwitchListener, UpdateListener {

    private final JPanel panel = new JPanel(new BorderLayout(4, 4));
    private final JScrollPane scroller = new JScrollPane();
    private final Supplier<MannschaftWettkampf> wettkampfSupplier;
    private final Supplier<Object> resultSupplier;
    private final Consumer<Object> resultConsumer;

    private final List<RowData> rows = new ArrayList<>();
    private final List<ColumnData> columns = new ArrayList<>();
    private final Map<AgegroupGenderDisciplineRound, List<TeamWithStarters>> entriesByCell = new HashMap<>();

    private final Set<AgegroupGenderDisciplineRound> selectedCells = new HashSet<>();

    private JTable table;
    private Object lastSource = null;
    private boolean updating = false;

    public Object updateResult(Object results) {
        if (results instanceof StartersImportDto dto) {
            return dto.with(selectedCells);
        }
        return results;
    }

    public SelectionPage(Supplier<MannschaftWettkampf> wettkampfSupplier, Supplier<Object> resultSupplier) {
        this(wettkampfSupplier, resultSupplier, null);
    }

    public SelectionPage(Supplier<MannschaftWettkampf> wettkampfSupplier, Supplier<Object> resultSupplier,
                         Consumer<Object> resultConsumer) {
        super(I18n.get("Selection"), I18n.get("Import.Selection.Information"));
        this.wettkampfSupplier = wettkampfSupplier;
        this.resultSupplier = resultSupplier;
        this.resultConsumer = resultConsumer;

        scroller.setPreferredSize(new Dimension(900, 500));
        panel.add(scroller, BorderLayout.CENTER);
    }

    @Override
    public JComponent getPage() {
        if (!isEnabled()) {
            return null;
        }
        rebuildIfNecessary();
        return panel;
    }

    public void update(ImportExportTypes type) {
        setEnabled(type == ImportExportTypes.STARTERS);
        if (isEnabled()) {
            rebuildIfNecessary();
        } else {
            publishSelection(null);
            scroller.setViewportView(null);
        }
    }

    @Override
    public void pageSwitch(boolean forward) {
        if (forward && isEnabled()) {
            rebuildIfNecessary();
        }
    }

    @Override
    public void update() {
        rebuildIfNecessary();
    }

    private void rebuildIfNecessary() {
        if (!isEnabled()) {
            return;
        }

        Object source = resultSupplier.get();
        if (source == null) {
            resetView();
            publishSelection(null);
            return;
        }

        if (source == lastSource && table != null) {
            return;
        }

        if (!(source instanceof StartersImportDto)) {
            resetView();
            publishSelection(null);
            return;
        }

        MannschaftWettkampf mwk = wettkampfSupplier.get();
        if (mwk == null) {
            resetView();
            publishSelection(null);
            return;
        }

        lastSource = source;
        rows.clear();
        columns.clear();
        entriesByCell.clear();
        selectedCells.clear();

        List<TeamWithStarters> starterEntries = ((StartersImportDto) source).teams();
        Map<Integer, Mannschaft> mannschaften = new LinkedHashMap<>();
        for (TeamWithStarters entry : starterEntries) {
            Mannschaft team = SearchUtils.getSchwimmer(mwk, entry.getStartnumber());
            if (team != null) {
                mannschaften.putIfAbsent(team.getStartnummer(), team);
            }
        }

        if (mannschaften.isEmpty()) {
            resetView();
            publishSelection(null);
            return;
        }

        buildGrid(mwk, starterEntries);

        if (rows.isEmpty() || columns.isEmpty()) {
            resetView();
            publishSelection(null);
            return;
        }

        table = new JGroupableTable(new SelectionTableModel()) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row >= 0 && row < rows.size() && rows.get(row).groupHeader()) {
                    c.setBackground(JTableUtils.getOddDefault());
                    c.setForeground(UIManager.getColor("Table.foreground") != null
                                            ? UIManager.getColor("Table.foreground")
                                            : Color.BLACK);
                    Font base = c.getFont();
                    c.setFont(base.deriveFont(Font.BOLD));
                } else if (!isCellSelected(row, column)) {
                    c.setBackground(UIManager.getColor("Table.background"));
                    c.setForeground(UIManager.getColor("Table.foreground") != null
                                            ? UIManager.getColor("Table.foreground")
                                            : Color.BLACK);
                    Font base = c.getFont();
                    c.setFont(base.deriveFont(Font.PLAIN));
                }
                return c;
            }
        };
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        applyRoundGrouping();
        installHeaderButtons();
        table.setFillsViewportHeight(true);
        scroller.setViewportView(table);
        publishSelection(getSelectedEntries());
    }

    private void resetView() {
        rows.clear();
        columns.clear();
        entriesByCell.clear();
        selectedCells.clear();
        table = null;
        JLabel noStartersLabel = new JLabel(I18n.get("NoStartersImported"), SwingConstants.CENTER);
        noStartersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noStartersLabel.setVerticalAlignment(SwingConstants.CENTER);
        scroller.setViewportView(noStartersLabel);
    }

    private void buildGrid(MannschaftWettkampf mwk, List<TeamWithStarters> starterEntries) {
        Set<RowKey> rowKeys = new HashSet<>();
        int maxRounds = 0;

        for (TeamWithStarters entry : starterEntries) {
            Mannschaft team = SearchUtils.getSchwimmer(mwk, entry.getStartnumber());
            if (team == null) {
                continue;
            }
            int ak = team.getAKNummer();
            int discipline = ImportUtils.getDisciplineIndex(team.getAK(), entry.getDiscipline());
            if (discipline < 0) {
                continue;
            }
            int round = mwk.isHeatBased() ? entry.getRound() : 0;
            AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(ak, team.isMaennlich(), discipline, round);
            entriesByCell.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
            rowKeys.add(new RowKey(ak, discipline));
            selectedCells.add(key);
            if (mwk.isHeatBased()) {
                maxRounds = Math.max(maxRounds, team.getAK().getDisziplin(discipline, team.isMaennlich()).getRunden().length);
            }
        }

        List<RowKey> sorted = rowKeys.stream()
                .sorted(Comparator.comparingInt(RowKey::ak).thenComparingInt(RowKey::discipline))
                .toList();

        int currentAk = -1;
        for (RowKey key : sorted) {
            String agegroup = mwk.getRegelwerk().getAk(key.ak()).getName();
            if (currentAk != key.ak()) {
                rows.add(RowData.groupHeader(key.ak(), agegroup));
                currentAk = key.ak();
            }
            rows.add(RowData.disciplineRow(key.ak(), key.discipline(), agegroup,
                                           mwk.getRegelwerk().getAk(key.ak())
                                                   .getDisziplin(key.discipline(), true)
                                                   .getName()));
        }
        if (mwk.isHeatBased()) {
            for (int round = 0; round <= maxRounds; round++) {
                String roundTitle = I18n.getRound(round, round == maxRounds);
                for (boolean male : new boolean[]{false, true}) {
                    String title = I18n.geschlechtToShortString(mwk.getRegelwerk(), male);
                    columns.add(new ColumnData(male, round, title, roundTitle));
                }
            }
        } else {
            String roundTitle = I18n.getRound(0, true);
            columns.add(new ColumnData(false, 0,
                                       I18n.geschlechtToShortString(mwk.getRegelwerk(), false), roundTitle));
            columns.add(new ColumnData(true, 0,
                                       I18n.geschlechtToShortString(mwk.getRegelwerk(), true), roundTitle));
        }
    }

    private void applyRoundGrouping() {
        if (!(table.getTableHeader() instanceof GroupableTableHeader gth)) {
            return;
        }
        Map<String, ColumnGroup> groups = new LinkedHashMap<>();
        for (int x = 2; x < table.getColumnCount(); x++) {
            ColumnData column = columns.get(x - 2);
            ColumnGroup group = groups.computeIfAbsent(column.roundTitle(), ColumnGroup::new);
            group.add(table.getColumnModel().getColumn(x));
        }
        groups.values().forEach(gth::addColumnGroup);
    }

    private void installHeaderButtons() {
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderCheckboxRenderer(header.getDefaultRenderer()));
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = header.columnAtPoint(e.getPoint());
                if (viewColumn < 2) {
                    return;
                }
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                if (modelColumn < 2 || modelColumn - 2 >= columns.size()) {
                    return;
                }
                ColumnData column = columns.get(modelColumn - 2);
                // Top half is the round group header, bottom half the leaf gender header.
                if (e.getY() < header.getHeight() / 2) {
                    toggleByRound(column.round());
                } else {
                    toggleByGender(column.male());
                }
            }
        });
    }

    private void publishSelection(List<TeamWithStarters> selected) {
        if (updating) {
            return;
        }
        updating = true;
        try {
            if (resultConsumer != null) {
                if (selected == null || selected.isEmpty()) {
                    resultConsumer.accept(null);
                } else {
                    resultConsumer.accept(new StartersImportDto(selected));
                }
            }
        } finally {
            updating = false;
        }
    }

    private List<TeamWithStarters> getSelectedEntries() {
        List<TeamWithStarters> selected = new ArrayList<>();
        for (RowData row : rows) {
            for (ColumnData column : columns) {
                AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
                if (selectedCells.contains(key)) {
                    List<TeamWithStarters> values = entriesByCell.get(key);
                    if (values != null) {
                        selected.addAll(values);
                    }
                }
            }
        }
        return selected;
    }

    private void toggleSelection(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return;
        }
        if (columnIndex < 2 || columnIndex - 2 >= columns.size()) {
            return;
        }
        RowData row = rows.get(rowIndex);
        if (row.groupHeader()) {
            return;
        }
        ColumnData column = columns.get(columnIndex - 2);
        AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
        if (!entriesByCell.containsKey(key)) {
            return;
        }
        if (selectedCells.contains(key)) {
            selectedCells.remove(key);
        } else {
            selectedCells.add(key);
        }
        publishSelection(getSelectedEntries());
        if (table != null) {
            ((SelectionTableModel) table.getModel()).fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    private boolean isGenderSelected(boolean male) {
        boolean found = false;
        for (RowData row : rows) {
            if (row.groupHeader()) {
                continue;
            }
            for (ColumnData column : columns) {
                if (column.male() != male) {
                    continue;
                }
                AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
                if (!entriesByCell.containsKey(key)) {
                    continue;
                }
                found = true;
                if (!selectedCells.contains(key)) {
                    return false;
                }
            }
        }
        return found;
    }

    private void toggleByGender(boolean male) {
        boolean targetSelected = !isGenderSelected(male);
        boolean changed = false;
        for (RowData row : rows) {
            if (row.groupHeader()) {
                continue;
            }
            for (ColumnData column : columns) {
                if (column.male() != male) {
                    continue;
                }
                AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
                if (!entriesByCell.containsKey(key)) {
                    continue;
                }
                if (targetSelected) {
                    changed |= selectedCells.add(key);
                } else {
                    changed |= selectedCells.remove(key);
                }
            }
        }
        if (changed) {
            publishSelection(getSelectedEntries());
            refreshTable();
        }
    }

    private boolean isRoundSelected(int round) {
        boolean found = false;
        for (RowData row : rows) {
            if (row.groupHeader()) {
                continue;
            }
            for (ColumnData column : columns) {
                if (column.round() != round) {
                    continue;
                }
                AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
                if (!entriesByCell.containsKey(key)) {
                    continue;
                }
                found = true;
                if (!selectedCells.contains(key)) {
                    return false;
                }
            }
        }
        return found;
    }

    private void toggleByRound(int round) {
        boolean targetSelected = !isRoundSelected(round);
        boolean changed = false;
        for (RowData row : rows) {
            if (row.groupHeader()) {
                continue;
            }
            for (ColumnData column : columns) {
                if (column.round() != round) {
                    continue;
                }
                AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
                if (!entriesByCell.containsKey(key)) {
                    continue;
                }
                if (targetSelected) {
                    changed |= selectedCells.add(key);
                } else {
                    changed |= selectedCells.remove(key);
                }
            }
        }
        if (changed) {
            publishSelection(getSelectedEntries());
            refreshTable();
        }
    }

    private void refreshTable() {
        if (table != null) {
            ((SelectionTableModel) table.getModel()).fireTableDataChanged();
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                header.repaint();
            }
        }
    }

    private boolean isAgegroupColumnSelected(int ak, ColumnData column) {
        boolean found = false;
        for (RowData row : rows) {
            if (row.groupHeader() || row.ak() != ak) {
                continue;
            }
            AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
            if (entriesByCell.containsKey(key)) {
                found = true;
                if (!selectedCells.contains(key)) {
                    return false;
                }
            }
        }
        return found;
    }

    private void toggleAgegroupColumn(int ak, ColumnData column) {
        boolean targetSelected = !isAgegroupColumnSelected(ak, column);
        boolean changed = false;
        for (RowData row : rows) {
            if (row.groupHeader() || row.ak() != ak) {
                continue;
            }
            AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
            if (!entriesByCell.containsKey(key)) {
                continue;
            }
            if (targetSelected) {
                changed |= selectedCells.add(key);
            } else {
                changed |= selectedCells.remove(key);
            }
        }
        if (changed) {
            publishSelection(getSelectedEntries());
            refreshTable();
        }
    }

    private final class SelectionTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size() + 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return I18n.get("AgeGroup");
            }
            if (column == 1) {
                return I18n.get("Discipline");
            }
            return columns.get(column - 2).title();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex < 2 ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex < 2) {
                return false;
            }
            RowData row = rows.get(rowIndex);
            ColumnData column = columns.get(columnIndex - 2);
            if (row.groupHeader()) {
                return rows.stream().anyMatch(r -> !r.groupHeader() && r.ak() == row.ak()
                        && entriesByCell.containsKey(new AgegroupGenderDisciplineRound(r.ak(), column.male(), r.discipline(), column.round())));
            }
            return entriesByCell.containsKey(new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round()));
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RowData row = rows.get(rowIndex);
            if (columnIndex == 0) {
                return row.groupHeader() ? row.agegroupName() : "";
            }
            if (columnIndex == 1) {
                return row.groupHeader() ? "" : row.disciplineName();
            }
            if (row.groupHeader()) {
                ColumnData column = columns.get(columnIndex - 2);
                return isAgegroupColumnSelected(row.ak(), column);
            }
            ColumnData column = columns.get(columnIndex - 2);
            AgegroupGenderDisciplineRound key = new AgegroupGenderDisciplineRound(row.ak(), column.male(), row.discipline(), column.round());
            return selectedCells.contains(key);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex < 2 || columnIndex - 2 >= columns.size()) {
                return;
            }
            RowData row = rows.get(rowIndex);
            ColumnData column = columns.get(columnIndex - 2);
            if (row.groupHeader()) {
                toggleAgegroupColumn(row.ak(), column);
            } else {
                toggleSelection(rowIndex, columnIndex);
            }
        }
    }

    private final class HeaderCheckboxRenderer extends DefaultTableCellRenderer {
        private final TableCellRenderer delegate;

        private HeaderCheckboxRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            Component base = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (modelColumn >= 2) {
                ColumnData columnData = columns.get(modelColumn - 2);
                boolean roundHeader = columns.stream().anyMatch(c -> c.roundTitle().equals(String.valueOf(value)));
                boolean selected = roundHeader ? isRoundSelected(columnData.round()) : isGenderSelected(columnData.male());
                JCheckBox checkBox = new JCheckBox(String.valueOf(value), selected);
                checkBox.setOpaque(true);
                checkBox.setBackground(base.getBackground());
                checkBox.setForeground(base.getForeground());
                checkBox.setFont(base.getFont());
                if (base instanceof JComponent jc) {
                    checkBox.setBorder(jc.getBorder());
                } else {
                    checkBox.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                }
                checkBox.setBorderPainted(true);
                checkBox.setFocusable(false);
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                return checkBox;
            }
            return base;
        }
    }

    private record RowKey(int ak, int discipline) {
    }

    private record RowData(int ak, int discipline, String agegroupName, String disciplineName, boolean groupHeader) {
        private static RowData groupHeader(int ak, String agegroupName) {
            return new RowData(ak, -1, agegroupName, "", true);
        }

        private static RowData disciplineRow(int ak, int discipline, String agegroupName, String disciplineName) {
            return new RowData(ak, discipline, agegroupName, disciplineName, false);
        }
    }

    private record ColumnData(boolean male, int round, String title, String roundTitle) {
    }

}
