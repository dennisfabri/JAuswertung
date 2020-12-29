/*
 * Created on 07.09.2005
 */
package de.df.jauswertung.gui.plugins.zw;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import de.df.jauswertung.daten.ASchwimmer;

class ZWTransferable<T extends ASchwimmer> implements Transferable {

    private static final DataFlavor FLAVOR;
    private T                       data;

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

    public ZWTransferable(T data) {
        super();
        this.data = data;
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
    public T getTransferData(DataFlavor fl) throws UnsupportedFlavorException, IOException {
        return data;
    }

}
