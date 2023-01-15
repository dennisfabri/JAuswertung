package de.df.jauswertung.gui.plugins.heats;

/*
 * ListTransferHandler.java is used by the 1.4 ExtendedDnDDemo.java example.
 */
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

class ListTransferHandler<T extends ASchwimmer> extends AHeatTransferHandler<T> {

    private static final long serialVersionUID = 3373858402029357418L;

    private int index = -1;

    // Bundle up the selected items in the list
    // as a single string, for export.
    @SuppressWarnings("unchecked")
    @Override
    protected SchwimmerDisziplin<T> exportHeat(JComponent c) {
        JList list = (JList) c;
        index = list.getSelectedIndex();
        return (SchwimmerDisziplin<T>) list.getSelectedValue();
    }

    // Take the incoming string and wherever there is a
    // newline, break it into a separate item in the list.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void importHeat(JComponent c, SchwimmerDisziplin<T> zw, JComponent source) {
        if (c == source) {
            return;
        }
        JList list = (JList) c;
        HeatListModel<T> model = (HeatListModel) list.getModel();
        model.addElement(zw);
    }

    // If the remove argument is true, the drop has been
    // successful and it's time to remove the selected items
    // from the list. If the remove argument is false, it
    // was a Copy operation and the original list is left
    // intact.
    @Override
    protected void cleanup(JComponent c, boolean remove, JComponent dest) {
        if (c == dest) {
            return;
        }
        if (dest == null) {
            return;
        }
        if (remove && (index > -1)) {
            JList list = (JList) c;
            @SuppressWarnings("rawtypes")
            HeatListModel model = (HeatListModel) list.getModel();
            // If we are moving items around in the same list, we
            // need to adjust the indices accordingly, since those
            // after the insertion point have moved.
            SwingUtilities.invokeLater(new CleanUpRunnable(model, index));
        }
        index = -1;
    }

    private static class CleanUpRunnable implements Runnable {

        @SuppressWarnings("rawtypes")
        private HeatListModel model;
        private int x;

        @SuppressWarnings("rawtypes")
        public CleanUpRunnable(HeatListModel model, int x) {
            this.x = x;
            this.model = model;
        }

        @Override
        public void run() {
            model.remove(x);
        }
    }
}