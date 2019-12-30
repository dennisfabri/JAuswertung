package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerNameVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        return o1.getName().compareTo(o2.getName());
    }
}