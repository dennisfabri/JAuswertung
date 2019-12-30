package de.df.jauswertung.gui.plugins.zw;

/*
 * ListTransferHandler.java is used by the 1.4 ExtendedDnDDemo.java example.
 */
import javax.swing.JComponent;
import javax.swing.JList;

import de.df.jauswertung.daten.ASchwimmer;

class ListTransferHandler<T extends ASchwimmer> extends AZWTransferHandler<T> {

    private static final long serialVersionUID = 3373858402029357418L;

    private int               index            = -1;

    // Bundle up the selected items in the list
    // as a single string, for export.
    @SuppressWarnings("unchecked")
    @Override
    protected T exportZW(JComponent c) {
        JList list = (JList) c;
        index = list.getSelectedIndex();
        return ((SchwimmerZW<T>) list.getSelectedValue()).getSchwimmer();
    }

    // Take the incoming string and wherever there is a
    // newline, break it into a separate item in the list.
    @SuppressWarnings("unchecked")
    @Override
    protected void importZW(JComponent c, T hlw) {
        JList source = (JList) c;
        ZWListModel<T> model = (ZWListModel<T>) source.getModel();
        model.addElement(hlw);
    }

    // If the remove argument is true, the drop has been
    // successful and it's time to remove the selected items
    // from the list. If the remove argument is false, it
    // was a Copy operation and the original list is left
    // intact.
    @Override
    protected void cleanup(JComponent c, boolean remove) {
        if (remove && (index > -1)) {
            JList source = (JList) c;
            @SuppressWarnings("rawtypes")
            ZWListModel model = (ZWListModel) source.getModel();
            // If we are moving items around in the same list, we
            // need to adjust the indices accordingly, since those
            // after the insertion point have moved.
            model.remove(index);
        }
        index = -1;
    }
}
