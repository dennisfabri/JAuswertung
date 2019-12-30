/*
 * Created on 13.09.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.AbstractListModel;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;

@SuppressWarnings("serial")
class HeatListModel<T extends ASchwimmer> extends AbstractListModel<SchwimmerDisziplin<T>> {

    private LinkedList<SchwimmerDisziplin<T>> data;

    public HeatListModel(AWettkampf<T> wk) {
        data = new LinkedList<SchwimmerDisziplin<T>>();

        Hashtable<String, SchwimmerDisziplin<T>> storage = new Hashtable<String, SchwimmerDisziplin<T>>();
        {
            // Put everything in a list
            LinkedList<T> ll = wk.getSchwimmer();
            ListIterator<T> li = ll.listIterator();
            while (li.hasNext()) {
                T s = li.next();
                for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                    if (s.isDisciplineChosen(x)) {
                        storage.put("" + s.getStartnummer() + "x" + x, new SchwimmerDisziplin<T>(s, x));
                    }
                }
            }
        }

        {
            // Remove found items
            LinkedList<Lauf<T>> ll = wk.getLaufliste().getLaufliste();
            ListIterator<Lauf<T>> li = ll.listIterator();
            while (li.hasNext()) {
                Lauf<T> l = li.next();
                for (int x = 0; x < l.getBahnen(); x++) {
                    T t = l.getSchwimmer(x);
                    if (t != null) {
                        storage.remove("" + t.getStartnummer() + "x" + l.getDisznummer(x));
                    }
                }
            }
        }

        {
            Enumeration<SchwimmerDisziplin<T>> e = storage.elements();
            while (e.hasMoreElements()) {
                addElement(e.nextElement());
            }
        }
    }

    @Override
    public int getSize() {
        return data.size();
    }

    public void addElement(SchwimmerDisziplin<T> o) {
        ListIterator<SchwimmerDisziplin<T>> li = data.listIterator();
        while (li.hasNext()) {
            SchwimmerDisziplin<T> sd = li.next();
            if (sd.compareTo(o) >= 0) {
                if (sd.compareTo(o) == 0) {
                    return;
                }
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
        data.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public SchwimmerDisziplin<T> getElementAt(int index) {
        // Workaround for DataTipManager
        if (data.size() == 0) {
            return new SchwimmerDisziplin<T>();
        }
        return data.get(index);
    }
}