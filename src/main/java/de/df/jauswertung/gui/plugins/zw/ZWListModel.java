/*
 * Created on 07.09.2005
 */
package de.df.jauswertung.gui.plugins.zw;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.AbstractListModel;

import de.df.jauswertung.daten.ASchwimmer;

@SuppressWarnings("serial")
class ZWListModel<T extends ASchwimmer> extends AbstractListModel<SchwimmerZW<T>> {

    private LinkedList<SchwimmerZW<T>> data;

    public ZWListModel() {
        data = new LinkedList<SchwimmerZW<T>>();
    }

    @Override
    public int getSize() {
        return data.size();
    }

    public void addElement(T o) {
        SchwimmerZW<T> sh = new SchwimmerZW<T>(o, 1);
        addElement(sh);
    }

    public void addElement(SchwimmerZW<T> o) {
        ListIterator<SchwimmerZW<T>> li = data.listIterator();
        while (li.hasNext()) {
            SchwimmerZW<T> sd = li.next();
            if (sd.compareTo(o) == 0) {
                sd.increaseCount();
                int n = li.previousIndex();
                fireContentsChanged(this, n, n);
                return;
            }
            if (sd.compareTo(o) > 0) {
                li.previous();
                int n = li.nextIndex();
                li.add(o);
                fireIntervalAdded(this, n, n);
                return;
            }
        }

        data.addLast(o);
        fireIntervalAdded(this, data.size(), data.size());
    }

    public void remove(int index) {
        SchwimmerZW<T> sh = data.get(index);
        sh.decreaseCount();
        if (sh.getCount() <= 0) {
            data.remove(index);
            fireIntervalRemoved(this, index, data.size());
        } else {
            fireContentsChanged(this, index, index);
        }
    }

    @Override
    public SchwimmerZW<T> getElementAt(int index) {
        return data.get(index);
    }
}