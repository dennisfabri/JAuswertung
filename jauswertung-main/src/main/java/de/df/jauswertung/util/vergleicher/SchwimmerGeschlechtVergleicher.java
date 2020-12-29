package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerGeschlechtVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    @Override
    public int compare(T o1, T o2) {
        if (o1.isMaennlich() == o2.isMaennlich()) {
            return 0;
        }
        return (o1.isMaennlich() == false ? -1 : 1);
    }
}