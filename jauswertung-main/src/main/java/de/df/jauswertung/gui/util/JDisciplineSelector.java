package de.df.jauswertung.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JDisciplineSelector extends JFrame {

    private ISimpleCallback<OWSelection[]> callback;

    private final AWettkampf<?>            wk;
    private final OWSelection[]            data;

    private JTable                         table;

    private boolean                        cancelled = false;

    public JDisciplineSelector(String title, String text, AWettkampf<?> wk, OWSelection[] data, boolean multiselect, ISimpleCallback<OWSelection[]> callback) {
        this.callback = callback;
        this.wk = wk;
        this.data = Arrays.stream(data).sorted(new Comparator<OWSelection>() {

            @Override
            public int compare(OWSelection o1, OWSelection o2) {
                int pos1 = wk.getRegelwerk().getRundenId(o1);
                int pos2 = wk.getRegelwerk().getRundenId(o2);
                return pos1 - pos2;
            }
        }).toArray(OWSelection[]::new);
        setTitle(title);
        setIconImages(IconManager.getTitleImages());
        buildUI(text, multiselect);
        pack();
        WindowUtils.center(this);
        setMinimumSize(new Dimension(400, 300));
        WindowUtils.checkMinimumSize(this);
        UIStateUtils.uistatemanage(this, "JDisciplineSelector");
    }

    private void buildUI(String text, boolean multiselect) {
        buildTable(multiselect);

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(new JLabel(text), CC.xy(2, 2));
        add(UIUtils.surroundWithScroller(table), CC.xy(2, 4));
        add(createButtons(), CC.xy(2, 6, "right,fill"));
    }

    private String getAmount(OWSelection ow) {
        if (ow.round == 0) {
            return "" + SearchUtils.getSchwimmer(wk, ow.ak, ow.male, ow.discipline).size();
        }
        try {
            // OWDisziplin<?> d = wk.getLauflisteOW().getDisziplin(ow.akNummer, ow.male, ow.discipline, ow.round - 1);
            int[] quali = wk.getRegelwerk().getAk(ow.akNummer).getDisziplin(ow.discipline, ow.male).getRunden();
            return "~" + quali[ow.round - 1];
        } catch (Exception ex) {
            // Nothing to do
        }
        return "-";
    }

    private void buildTable(boolean multiselect) {
        String[][] tableData = Arrays.asList(data).stream()
                .map(j -> new String[] { "" + wk.getRegelwerk().getRundenId(j), I18n.getAgeGroupAsString(wk.getRegelwerk(), j.ak, j.male),
                        j.ak.getDisziplin(j.discipline, j.male).getName(), I18n.getRound(j.round, j.isFinal), getAmount(j) })
                .toArray(j -> new String[data.length][0]);
        table = new JTable(new SimpleTableModel(tableData, new Object[] { "Id", "Altersklasse", "Disziplin", "Runde", "Anzahl" }));
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

    private JPanel createButtons() {
        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(2, 4, 0), FormLayoutUtils.createLayoutString(1, 4, 0));
        layout.setColumnGroup(2, 4);
        JPanel p = new JPanel(layout);

        JButton ok = new JButton(I18n.get("Ok"));
        JButton cancel = new JButton(I18n.get("Cancel"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    private void doOk() {
        if (table.getSelectedRowCount() > 0) {
            if (callback != null) {
                ArrayList<OWSelection> selected = new ArrayList<OWSelection>();
                int[] rows = table.getSelectedRows();
                for (int x : rows) {
                    selected.add(data[x]);
                }

                callback.callback(selected.toArray(new OWSelection[selected.size()]));
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
        if (v == false) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}