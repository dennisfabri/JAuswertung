package de.df.jauswertung.gui.plugins.heats;

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
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

abstract class AHeatTransferHandler<T extends ASchwimmer> extends TransferHandler {

    abstract SchwimmerDisziplin<T> exportHeat(JComponent c);

    abstract void importHeat(JComponent c, SchwimmerDisziplin<T> zw, JComponent source);

    abstract void cleanup(JComponent c, boolean remove, JComponent source);

    @Override
    protected Transferable createTransferable(JComponent c) {
        SchwimmerDisziplin<T> sh = exportHeat(c);
        if (sh == null) {
            return null;
        }
        if (sh.getSchwimmer() == null) {
            return null;
        }
        return new HeatTransferable<T>(sh, c);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return IconManager.getBigIcon("idcard");
    }

    @Override
    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_MOVE;
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        try {
            @SuppressWarnings("unchecked")
            HeatTransferable.DnDContainer<T> dc = ((HeatTransferable.DnDContainer<T>) t
                    .getTransferData(HeatTransferable.getFlavor()));
            SchwimmerDisziplin<T> sh = dc.data;
            importHeat(c, sh, dc.source);
            dc.destination = c;
            return true;
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
        try {
            if (t != null) {
                @SuppressWarnings("unchecked")
                HeatTransferable.DnDContainer<T> dc = (HeatTransferable.DnDContainer<T>) t
                        .getTransferData(HeatTransferable.getFlavor());
                cleanup(c, action == DnDConstants.ACTION_MOVE, dc.destination);
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (flavor.equals(HeatTransferable.getFlavor())) {
                return true;
            }
        }
        return false;
    }
}
