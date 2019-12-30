/*
 * Created on 12.09.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

import javax.swing.JTable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

class JTableDropTargetListener<E extends ASchwimmer, T> implements DropTargetListener {

    private JTable tabelle;

    public JTableDropTargetListener(JTable tabelle) throws TooManyListenersException {
        tabelle.getDropTarget().setDefaultActions(DnDConstants.ACTION_MOVE);
        tabelle.getDropTarget().addDropTargetListener(this);

        this.tabelle = tabelle;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        dragOver(dtde);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        boolean ok = false;
        DataFlavor[] flavors = dtde.getCurrentDataFlavors();
        for (DataFlavor flavor : flavors) {
            if (flavor.equals(HeatTransferable.getFlavor())) {
                ok = true;
            }
        }
        if (!ok) {
            dtde.rejectDrag();
            return;
        }

        int col = tabelle.columnAtPoint(dtde.getLocation());
        if (col < 3) {
            dtde.rejectDrag();
            return;
        }

        int row = tabelle.rowAtPoint(dtde.getLocation());

        @SuppressWarnings("unchecked")
        SchwimmerDisziplin<E> sh = (SchwimmerDisziplin<E>) tabelle.getValueAt(row, col);
        if ((sh != null) && (sh.getSchwimmer() != null)) {
            dtde.rejectDrag();
            return;
        }
        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        dragOver(dtde);
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // Nothing to do
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        boolean ok = false;
        DataFlavor[] flavors = dtde.getCurrentDataFlavors();
        for (DataFlavor flavor : flavors) {
            if (flavor.equals(HeatTransferable.getFlavor())) {
                ok = true;
            }
        }
        if (!ok) {
            dtde.rejectDrop();
            return;
        }

        int col = tabelle.columnAtPoint(dtde.getLocation());
        if (col < 3) {
            dtde.rejectDrop();
            return;
        }

        int row = tabelle.rowAtPoint(dtde.getLocation());

        @SuppressWarnings("unchecked")
        SchwimmerDisziplin<E> sh = (SchwimmerDisziplin<E>) tabelle.getValueAt(row, col);
        if ((sh != null) && (sh.getSchwimmer() != null)) {
            dtde.rejectDrop();
            return;
        }

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
    }
}