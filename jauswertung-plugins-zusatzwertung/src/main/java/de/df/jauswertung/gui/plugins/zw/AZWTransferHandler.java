package de.df.jauswertung.gui.plugins.zw;

/*
 * StringTransferHandler.java is used by the 1.4 ExtendedDnDDemo.java example.
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.IconManager;

abstract class AZWTransferHandler<T extends ASchwimmer> extends TransferHandler {

    abstract T exportZW(JComponent c);

    abstract void importZW(JComponent c, T zw);

    abstract void cleanup(JComponent c, boolean remove);

    @Override
    protected Transferable createTransferable(JComponent c) {
        T sh = exportZW(c);
        if (sh == null) {
            return null;
        }
        return new ZWTransferable<T>(sh);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return IconManager.getBigIcon("idcard");
    }

    @Override
    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_MOVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                T sh = (T) t.getTransferData(ZWTransferable.getFlavor());
                importZW(c, sh);
                return true;
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                // Nothing to do
            } catch (IOException ioe) {
                ioe.printStackTrace();
                // Nothing to do
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == DnDConstants.ACTION_MOVE);
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (flavor.equals(ZWTransferable.getFlavor())) {
                return true;
            }
        }
        return false;
    }
}
