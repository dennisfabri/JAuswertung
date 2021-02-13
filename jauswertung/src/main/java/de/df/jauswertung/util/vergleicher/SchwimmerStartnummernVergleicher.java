package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerStartnummernVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        return o1.getStartnummer() - o2.getStartnummer();
    }
}