package de.df.jauswertung.gui.util;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class JDisciplineSelector extends JFrame {

    private static boolean showAllState = false;

    private final Consumer<OWSelection[]> callback;

    private final AWettkampf<?> wk;
    private final OWSelection[] defaultSelection;
    private final OWSelection[] fullSelection;
    private OWSelection[] data;

    private JTable table;
    private JCheckBox showAll;

    private boolean cancelled = false;

    public JDisciplineSelector(String title, String text, AWettkampf<?> wk, OWSelection[] selection, OWSelection[] fullSelection, boolean multiselect,
                               Consumer<OWSelection[]> callback) {
        this.callback = callback;
        this.wk = wk;
        this.defaultSelection = sort(selection);
        this.fullSelection = sort(fullSelection);
        this.data = (showAllState && this.fullSelection.length > defaultSelection.length) ? this.fullSelection : defaultSelection;
        setTitle(title);
        setIconImages(IconManager.getTitleImages());
        buildUI(text, multiselect);
        pack();
        setMinimumSize(new Dimension(400, 300));
        UIStateUtils.uistatemanage(this, "JDisciplineSelector");
    }

    private OWSelection[] sort(OWSelection[] arr) {
        return Arrays.stream(arr).sorted((o1, o2) -> {
            int pos1 = wk.getRegelwerk().getRundenId(o1);
            int pos2 = wk.getRegelwerk().getRundenId(o2);
            return pos1 - pos2;
        }).toArray(OWSelection[]::new);
    }

    private void buildUI(String text, boolean multiselect) {
        buildTable(multiselect);

        boolean hasMore = fullSelection.length > defaultSelection.length;
        String rowSpec = hasMore
                ? "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu"
                : "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu";
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", rowSpec);
        setLayout(layout);

        add(new JLabel(text), CC.xy(2, 2));
        add(UIUtils.surroundWithScroller(table), CC.xy(2, 4));
        if (hasMore) {
            showAll = new JCheckBox("Alle anzeigen", showAllState);
            showAll.addActionListener(e -> onShowAllToggled());
            add(showAll, CC.xy(2, 6));
            add(createButtons(), CC.xy(2, 8, "right,fill"));
        } else {
            add(createButtons(), CC.xy(2, 6, "right,fill"));
        }
    }

    private void onShowAllToggled() {
        showAllState = showAll.isSelected();
        data = showAllState ? fullSelection : defaultSelection;
        updateTableModel();
    }

    private String getAmount(OWSelection ow) {
        if (ow.round == 0) {
            return "" + SearchUtils.getSchwimmer(wk, ow.ak, ow.male, ow.discipline).size();
        }
        try {
            // OWDisziplin<?> d = wk.getLauflisteOW().getDisziplin(ow.akNummer, ow.male,
            // ow.discipline, ow.round - 1);
            int[] quali = wk.getRegelwerk().getAk(ow.akNummer).getDisziplin(ow.discipline, ow.male).getRunden();
            return "~" + quali[ow.round - 1];
        } catch (Exception ex) {
            // Nothing to do
        }
        return "-";
    }

    private String[][] buildTableData() {
        return Arrays.stream(data)
                .map(j -> new String[]{"" + wk.getRegelwerk().getRundenId(j),
                        I18n.getAgeGroupAsString(wk.getRegelwerk(), j.ak, j.male),
                        j.ak.getDisziplin(j.discipline, j.male).getName(), I18n.getRound(j.round, j.isFinal),
                        getAmount(j)})
                .toArray(String[][]::new);
    }

    private void buildTable(boolean multiselect) {
        table = new JTable(
                new SimpleTableModel(buildTableData(), new Object[]{"Id", "Altersklasse", "Disziplin", "Runde", "Anzahl"}));
        if (!multiselect) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else {
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
        JTableUtils.setAlternatingTableCellRenderer(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if ((!evt.isPopupTrigger()) && (evt.getClickCount() == 2) && table.getSelectedRowCount() > 0) {
                    doOk();
                }
            }
        });
    }

    private void updateTableModel() {
        table.setModel(new SimpleTableModel(buildTableData(), new Object[]{"Id", "Altersklasse", "Disziplin", "Runde", "Anzahl"}));
        JTableUtils.setAlternatingTableCellRenderer(table);
    }

    private JPanel createButtons() {
        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(2, 4, 0),
                                           FormLayoutUtils.createLayoutString(1, 4, 0));
        layout.setColumnGroup(2, 4);
        JPanel p = new JPanel(layout);

        JButton ok = new JButton(I18n.get("Ok"));
        JButton cancel = new JButton(I18n.get("Cancel"));
        ok.addActionListener(e -> {
            doOk();
        });
        cancel.addActionListener(e -> {
            doCancel();
        });

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    private void doOk() {
        if (table.getSelectedRowCount() > 0) {
            if (callback != null) {
                ArrayList<OWSelection> selected = new ArrayList<>();
                int[] rows = table.getSelectedRows();
                for (int x : rows) {
                    selected.add(data[x]);
                }

                callback.accept(selected.toArray(new OWSelection[0]));
            }
        }
        setVisible(false);
    }

    private void doCancel() {
        cancelled = true;
        setVisible(false);
    }

    @Override
    public void setVisible(boolean v) {
        if (isVisible() == v) {
            return;
        }
        if (v) {
            cancelled = false;
        }
        super.setVisible(v);
        if (!v) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
