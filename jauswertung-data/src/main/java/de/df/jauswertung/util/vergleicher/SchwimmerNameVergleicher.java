package de.df.jauswertung.util.vergleicher;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerNameVergleicher<T extends ASchwimmer> implements java.util.Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        return o1.getName().compareTo(o2.getName());
    }
}