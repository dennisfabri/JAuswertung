package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerAKVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        int i = o1.getAKNummer() - o2.getAKNummer();
        if (i != 0) {
            return i * 2;
        }
        i = (o1.isMaennlich() ? 1 : 0) - (o2.isMaennlich() ? 1 : 0);
        if (i != 0) {
            return i;
        }
        return 0;
    }
}