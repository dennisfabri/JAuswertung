package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;

public class SchwimmerMeldepunkteVergleicher<T extends ASchwimmer> implements java.util.Comparator<T>, Serializable {

    private final int prefix;
    private final int index;

    public SchwimmerMeldepunkteVergleicher(boolean descending, int index) {
        this.prefix = (descending ? 1 : -1);
        this.index = index;
    }

    public SchwimmerMeldepunkteVergleicher(int index) {
        this(true, index);
    }

    public SchwimmerMeldepunkteVergleicher() {
        this(true, 0);
    }

    @Override
    public int compare(T o1, T o2) {
        return prefix * (int) Math.round(o2.getMeldepunkte(index) * 100 - o1.getMeldepunkte(index) * 100);
    }
}