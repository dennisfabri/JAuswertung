/*
 * Created on 12.09.2005
 */
package de.df.jauswertung.gui.plugins.zw;

import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.gui.util.I18n;

class ZWTableModel<T extends ASchwimmer> implements TableModel {

    private LinkedList<TableModelListener> listeners;
    private AWettkampf<T>                  wk;
    private HLWListe<T>                    hlw;

    public ZWTableModel(AWettkampf<T> wk) {
        this.wk = wk;
        hlw = wk.getHLWListe();
        listeners = new LinkedList<TableModelListener>();
    }

    @Override
    public int getRowCount() {
        if (hlw.isEmpty()) {
            return 0;
        }
        int rows = 0;
        for (int x = 0; x < hlw.getLauflistenCount(); x++) {
            rows++;
            rows += hlw.getLaufliste(x).size();
        }
        return rows - 1;
    }

    public int[] rowToIndex(int row) {
        int[] index = new int[2];
        index[0] = 0;
        while ((hlw.getLauflistenCount() > index[0]) && (row >= hlw.getLaufliste(index[0]).size())) {
            row -= hlw.getLaufliste(index[0]).size();
            // remove the pause row
            row--;

            index[0]++;
        }
        index[1] = row;

        return index;
    }

    public boolean isPause(int row) {
        return rowToIndex(row)[1] < 0;
    }

    public void addRow(int row) {
        int[] index = rowToIndex(row);
        if (index[1] < 0) {
            index[0]--;
            index[1] = hlw.getLaufliste(index[0]).size();
        }
        hlw.add(index);
        fireTableChange(row, true);
    }

    public void addPause(int row, Time restart) {
        int[] index = rowToIndex(row);
        hlw.split(index, restart);
        fireTableChange(row, true);
    }

    public void update(T s) {
        if (hlw.isEmpty()) {
            return;
        }
        ListIterator<LinkedList<HLWLauf<T>>> lli = hlw.getIterator();
        int row = 0;
        while (lli.hasNext()) {
            ListIterator<HLWLauf<T>> li = lli.next().listIterator();
            while (li.hasNext()) {
                HLWLauf<T> lauf = li.next();
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    if (lauf.getSchwimmer(x) == null) {
                        fireTableChange(row, x + 2);
                    } else {
                        if (s.equals(lauf.getSchwimmer(x))) {
                            fireTableChange(row, x + 2);
                        }
                    }
                }
                row++;
            }
            row++;
        }
    }

    public void update() {
        fireTableChange(0, getRowCount() - 1, 0);
    }

    public ASchwimmer[] removeRow(int row) {
        int[] index = rowToIndex(row);
        if (isPause(row)) {
            hlw.merge(index[0] - 1, index[0]);
            fireTableChange(row, false);
            return new ASchwimmer[0];
        }

        LinkedList<T> list = new LinkedList<T>();
        HLWLauf<T> lauf = hlw.get(index);
        for (int x = 0; x < lauf.getBahnen(); x++) {
            if (lauf.getSchwimmer(x) != null) {
                list.addLast(lauf.getSchwimmer(x));
                lauf.removeSchwimmer(x);
            }
        }
        hlw.remove(index);
        fireTableChange(row, false);
        return list.toArray(new ASchwimmer[list.size()]);
    }

    @Override
    public int getColumnCount() {
        return wk.getIntegerProperty(ZW_LANES) + 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return I18n.get("Time");
        case 1:
            return I18n.get("AgeGroup");
        default:
            return I18n.get("LaneNumber", columnIndex - 1);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < 2) {
            return String.class;
        }
        return ASchwimmer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int[] index = rowToIndex(rowIndex);
        if (index[1] < 0) {
            if (columnIndex == 1) {
                return I18n.get("Pause");
            }
            return "";
        }

        switch (columnIndex) {
        case 0:
            return hlw.get(index).getName();
        case 1:
            return hlw.get(index).getAltersklasse();
        default:
            return hlw.get(index).getSchwimmer(columnIndex - 2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex < 2) {
            return;
        }
        T s = null;
        if (aValue != null) {
            if (!(aValue instanceof ASchwimmer)) {
                return;
            }
            s = (T) aValue;
        }

        int[] index = rowToIndex(rowIndex);
        if (s != null) {
            hlw.get(index).setSchwimmer(s, ASchwimmer.DISCIPLINE_NUMBER_ZW, columnIndex - 2);
        } else {
            hlw.get(index).removeSchwimmer(columnIndex - 2);
        }

        hlw.refreshTime();

        fireTableChange(rowIndex, columnIndex);
        fireTableChange(rowIndex, 1);
        fireTableChange(rowIndex, getRowCount() - 1, 0);
    }

    protected void fireTableChange(int row, int col) {
        fireTableChange(row, row, col);
    }

    protected void fireTableChange(int row1, int row2, int col) {
        TableModelEvent tme = new TableModelEvent(this, row1, row2, col, TableModelEvent.UPDATE);
        ListIterator<TableModelListener> li = listeners.listIterator();
        while (li.hasNext()) {
            li.next().tableChanged(tme);
        }
    }

    protected void fireTableChange(int row, boolean insert) {
        TableModelEvent tme = new TableModelEvent(this, row, row, 0, (insert ? TableModelEvent.INSERT : TableModelEvent.DELETE));

        ListIterator<TableModelListener> li = listeners.listIterator();
        while (li.hasNext()) {
            li.next().tableChanged(tme);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.addLast(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}