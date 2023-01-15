/*
 * Created on 13.09.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

public class HeatTableModel<T extends ASchwimmer> implements TableModel {

    private AWettkampf<T> wk;
    private Laufliste<T> heats;
    private LinkedList<TableModelListener> listeners;

    public HeatTableModel(AWettkampf<T> wk) {
        this.wk = wk;
        heats = wk.getLaufliste();
        listeners = new LinkedList<TableModelListener>();
    }

    @Override
    public int getRowCount() {
        if (heats.getLaufliste() == null) {
            return 0;
        }
        return heats.getLaufliste().size();
    }

    @Override
    public int getColumnCount() {
        return wk.getIntegerProperty(HEATS_LANES) + 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return I18n.get("NumberShort");
        case 1:
            return I18n.get("AgeGroup");
        case 2:
            return I18n.get("Discipline");
        default:
            return I18n.get("LaneNumber", columnIndex - 2);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < 3) {
            return String.class;
        }
        return SchwimmerDisziplin.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return heats.getLaufliste().get(rowIndex).getName();
        case 1:
            if (heats.getLaufliste().get(rowIndex).getStartgruppe() != null) {
                return heats.getLaufliste().get(rowIndex).getStartgruppe();
            }
            return heats.getLaufliste().get(rowIndex).getAltersklasse();
        case 2:
            return I18n.getDisziplinShort(heats.getLaufliste().get(rowIndex).getDisziplin());
        default:
            Lauf<T> l = heats.getLaufliste().get(rowIndex);
            if (l.getSchwimmer(columnIndex - 3) == null) {
                return new SchwimmerDisziplin<T>();
            }
            return new SchwimmerDisziplin<T>(columnIndex - 3, l, true);
        }
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex < 3) {
            return;
        }
        T s = null;
        int disz = -1;
        if (aValue != null) {
            if (aValue instanceof SchwimmerDisziplin) {
                SchwimmerDisziplin<T> sd = (SchwimmerDisziplin<T>) aValue;
                s = sd.getSchwimmer();
                disz = sd.getDiscipline();
            } else {
                return;
            }
        }
        if (s == null) {
            heats.getLaufliste().get(rowIndex).removeSchwimmer(columnIndex - 3);
        } else {
            heats.getLaufliste().get(rowIndex).setSchwimmer(s, disz, columnIndex - 3);
        }
        fireUpdate(rowIndex, columnIndex);
    }

    public void addRow(int row) {
        heats.add(row);
        fireRowAdded(row);
    }

    @SuppressWarnings("unchecked")
    public SchwimmerDisziplin<T>[] removeRow(int row) {
        LinkedList<SchwimmerDisziplin<T>> list = new LinkedList<SchwimmerDisziplin<T>>();
        Lauf<T> heat = heats.getLaufliste().get(row);
        for (int x = 0; x < heat.getBahnen(); x++) {
            if (heat.getSchwimmer(x) != null) {
                list.addLast(new SchwimmerDisziplin<T>(x, heat, false));
            }
        }
        heats.remove(row);
        fireRowRemoved(row);

        return list.toArray(new SchwimmerDisziplin[list.size()]);
    }

    public void update(T s) {
        ListIterator<Lauf<T>> li = heats.getLaufliste().listIterator();
        int row = 0;
        while (li.hasNext()) {
            Lauf<T> lauf = li.next();
            for (int x = 0; x < lauf.getBahnen(); x++) {
                if ((lauf.getSchwimmer(x) == s) || (lauf.getSchwimmer(x) == null)) {
                    fireUpdate(row, x + 3);
                }
            }
            row++;
        }
    }

    public void updateNumbers() {
        fireEvent(new TableModelEvent(this, 0, getRowCount(), 0, TableModelEvent.UPDATE));
    }

    private void fireUpdate(int row, int col) {
        fireEvent(new TableModelEvent(this, row, row, col, TableModelEvent.UPDATE));
        fireEvent(new TableModelEvent(this, row, row, 1, TableModelEvent.UPDATE));
        fireEvent(new TableModelEvent(this, row, row, 2, TableModelEvent.UPDATE));
    }

    private void fireEvent(TableModelEvent tme) {
        ListIterator<TableModelListener> li = listeners.listIterator();
        while (li.hasNext()) {
            li.next().tableChanged(tme);
        }
    }

    private void fireRowRemoved(int row) {
        fireEvent(new TableModelEvent(this, row, row, 0, TableModelEvent.DELETE));
    }

    private void fireRowAdded(int row) {
        fireEvent(new TableModelEvent(this, row, row, 0, TableModelEvent.INSERT));
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