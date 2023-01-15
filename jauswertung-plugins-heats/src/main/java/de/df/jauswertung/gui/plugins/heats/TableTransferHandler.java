package de.df.jauswertung.gui.plugins.heats;

/*
 * TableTransferHandler.java is used by the 1.4 ExtendedDnDDemo.java example.
 */
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;
import de.df.jutils.gui.jtable.JTableUtils;

class TableTransferHandler<T extends ASchwimmer> extends AHeatTransferHandler<T> {

    private static final long serialVersionUID = -587285451733079256L;

    private int row = -1;
    private int col = -1;

    private AWettkampf<T> wk;
    private JLauflisteBearbeiten<T> parent;

    TableTransferHandler(JLauflisteBearbeiten<T> lb, AWettkampf<T> w) {
        parent = lb;
        wk = w;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SchwimmerDisziplin<T> exportHeat(JComponent c) {
        JTable table = (JTable) c;

        row = table.getSelectedRow();
        col = table.getSelectedColumn();

        if (col < 3) {
            return null;
        }

        parent.setChanged();

        return (SchwimmerDisziplin<T>) table.getModel().getValueAt(row, col);
    }

    @Override
    protected void importHeat(JComponent c, SchwimmerDisziplin<T> sh, JComponent source) {
        JTable target = (JTable) c;
        TableModel model = target.getModel();
        int irow = target.getSelectedRow();
        int icol = target.getSelectedColumn();

        if (target.getSelectedColumn() < 3) {
            return;
        }

        parent.setChanged();

        model.setValueAt(sh, irow, icol);
        wk.getLaufliste().set(sh.getSchwimmer(), sh.getDiscipline(), irow, icol - 3);
        Lauf<T> lauf = wk.getLaufliste().getLaufliste().get(irow);
        model.setValueAt(lauf.getAltersklasse(), irow, 1);
    }

    @Override
    protected void cleanup(JComponent c, boolean remove, JComponent source) {
        JTable table = (JTable) c;
        if (remove && (row > -1) && (col > -1)) {
            // If we are moving items around in the same table, we
            // need to adjust the rows accordingly, since those
            // after the insertion point have moved.
            SwingUtilities.invokeLater(new CleanUpRunnable(table, row, col));
        }
        row = -1;
        col = -1;
    }

    private static class CleanUpRunnable implements Runnable {

        private JTable table;
        private int x;
        private int y;

        public CleanUpRunnable(JTable model, int x, int y) {
            this.x = x;
            this.y = y;
            this.table = model;
        }

        @Override
        public void run() {
            table.getModel().setValueAt(null, x, y);
            JTableUtils.setPreferredRowHeight(table);
        }
    }
}