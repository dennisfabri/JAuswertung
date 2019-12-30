/**
 * 
 */
package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Startnummern implements Serializable {

    private static final long   serialVersionUID = 8239945102164481284L;

    @XStreamAsAttribute
    private int                 min;
    private LinkedList<Integer> recycler;

    public Startnummern() {
        min = 1;
        recycler = new LinkedList<Integer>();
    }

    public synchronized int get() {
        if (recycler.size() == 0) {
            min++;
            return min - 1;
        }

        int sn = recycler.getFirst();
        recycler.removeFirst();
        return sn;
    }

    public synchronized int viewNext() {
        if (recycler.isEmpty()) {
            return min;
        }
        return recycler.getFirst();
    }

    public synchronized boolean isFree(int startnummer) {
        if (min <= startnummer) {
            // Hohe nummer
            return true;
        }
        if (recycler.size() == 0) {
            // Startnummer ist bereits vergeben
            return false;
        }
        // Liste der Startnummern durchsuchen
        ListIterator<Integer> li = recycler.listIterator();
        while (li.hasNext()) {
            int sn = li.next();
            if (sn == startnummer) {
                return true;
            }
        }
        // Wenn nichts gefunden wurde
        return false;
    }

    public synchronized boolean get(int startnummer) {
        if (min <= startnummer) {
            // Niedrigere Startnummern in die Liste einfügen
            for (int y = min; y < startnummer; y++) {
                recycler.addLast(y);
            }
            // Neues Minimum speichern
            min = startnummer + 1;
            return true;
        }
        if (recycler.size() == 0) {
            // Startnummer ist bereits vergeben
            return false;
        }
        // Liste der Startnummern durchsuchen
        ListIterator<Integer> li = recycler.listIterator();
        while (li.hasNext()) {
            int sn = li.next();
            if (sn == startnummer) {
                li.remove();
                return true;
            }
        }
        // Wenn nichts gefunden wurde
        return false;
    }

    public synchronized void recycle(int startnummer) {
        recycler.add(startnummer);
        Collections.sort(recycler);
    }

    public void clear() {
        recycler.clear();
    }
}