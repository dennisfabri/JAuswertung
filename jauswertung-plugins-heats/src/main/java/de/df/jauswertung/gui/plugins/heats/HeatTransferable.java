/*
 * Created on 07.09.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

class HeatTransferable<T extends ASchwimmer> implements Transferable {

    private static final DataFlavor FLAVOR;

    private DnDContainer<T> container;

    static {
        DataFlavor flavor = null;
        try {
            flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        FLAVOR = flavor;
    }

    public static DataFlavor getFlavor() {
        return FLAVOR;
    }

    public HeatTransferable(SchwimmerDisziplin<T> data, JComponent source) {
        this.container = new DnDContainer<>(data, source);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor fl) {
        return fl.equals(FLAVOR);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public DnDContainer getTransferData(DataFlavor fl) throws UnsupportedFlavorException, IOException {
        return container;
    }

    public static class DnDContainer<E extends ASchwimmer> {

        public DnDContainer(SchwimmerDisziplin<E> d, JComponent c) {
            data = d;
            source = c;
        }

        public final SchwimmerDisziplin<E> data;
        public final JComponent source;
        public JComponent destination = null;
    }
}