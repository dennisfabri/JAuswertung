package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerGliederungVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        return ((ASchwimmer) o1).getGliederung().compareToIgnoreCase(((ASchwimmer) o2).getGliederung());
    }
}