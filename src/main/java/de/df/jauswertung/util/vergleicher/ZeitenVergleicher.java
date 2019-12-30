package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;
import java.util.Comparator;

import de.df.jauswertung.daten.ASchwimmer;

public class ZeitenVergleicher implements Comparator<ASchwimmer>, Serializable {

    private int disz = 0;

    public ZeitenVergleicher(int x) {
        disz = x;
    }

    @Override
    public int compare(ASchwimmer s1, ASchwimmer s2) {
        if ((s1.getZeit(disz) == 0) && (s2.getZeit(disz) == 0)) {
            return 0;
        }
        if (s1.getZeit(disz) == 0) {
            return 1;
        }
        if (s2.getZeit(disz) == 0) {
            return -1;
        }
        return (s1.getZeit(disz) - s2.getZeit(disz));
    }
}