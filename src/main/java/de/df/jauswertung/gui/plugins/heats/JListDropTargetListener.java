/*
 * Created on 07.09.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

import javax.swing.JList;

class JListDropTargetListener<T> implements DropTargetListener {

    public JListDropTargetListener(JList c) throws TooManyListenersException {
        c.getDropTarget().setDefaultActions(DnDConstants.ACTION_MOVE);
        c.getDropTarget().addDropTargetListener(this);
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

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
    }
}