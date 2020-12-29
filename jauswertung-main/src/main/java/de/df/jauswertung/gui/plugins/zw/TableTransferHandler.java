package de.df.jauswertung.gui.plugins.zw;

/*
 * TableTransferHandler.java is used by the 1.4 ExtendedDnDDemo.java example.
 */
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWLauf;

class TableTransferHandler<T extends ASchwimmer> extends AZWTransferHandler<T> {

    private static final long      serialVersionUID = -587285451733079256L;

    private int                    row              = -1;
    private int                    col              = -1;

    private AWettkampf<T>          wk;
    private JHlwlisteBearbeiten<T> parent;

    TableTransferHandler(JHlwlisteBearbeiten<T> hlw, AWettkampf<T> w) {
        parent = hlw;
        wk = w;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected T exportZW(JComponent c) {
        JTable table = (JTable) c;

        row = table.getSelectedRow();
        col = table.getSelectedColumn();

        if (col < 2) {
            return null;
        }

        ZWTableModel<T> model = (ZWTableModel<T>) table.getModel();
        if (model.isPause(row)) {
            return null;
        }

        parent.setChanged();

        return (T) table.getModel().getValueAt(row, col);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void importZW(JComponent c, T sh) {
        JTable target = (JTable) c;
        ZWTableModel<T> model = (ZWTableModel<T>) target.getModel();
        int irow = target.getSelectedRow();
        int icol = target.getSelectedColumn();

        if (target.getSelectedColumn() < 1) {
            return;
        }

        parent.setChanged();

        model.setValueAt(sh, irow, icol);

        int[] index = model.rowToIndex(irow);

        wk.getHLWListe().set(sh, index, icol - 2);
        HLWLauf<T> hlw = wk.getHLWListe().get(index);
        model.setValueAt(hlw.getAltersklasse(), irow, 1);
    }

    @Override
    protected void cleanup(JComponent c, boolean remove) {
        JTable source = (JTable) c;
        if (remove && (row > -1) && (col > -1)) {
            TableModel model = source.getModel();

            // wk.getHLWListe().remove(row, col - 2);

            // If we are moving items around in the same table, we
            // need to adjust the rows accordingly, since those
            // after the insertion point have moved.
            model.setValueAt(null, row, col);
            // HLWLauf<T> hlw = wk.getHLWListe().getLaufliste().get(row);
            // model.setValueAt(hlw.getAltersklasse(), row, 1);
        }
        row = -1;
        col = -1;
    }
}