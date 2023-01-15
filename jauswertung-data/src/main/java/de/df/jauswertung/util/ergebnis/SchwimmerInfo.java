package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafe;

public class SchwimmerInfo<T extends ASchwimmer> implements Comparable<SchwimmerInfo<T>> {
    public T schwimmer;
    public int runde;
    public int platz;
    public int lauf;
    public int zeit;
    public Strafe strafe;
    public boolean isFinal;

    @Override
    public int compareTo(SchwimmerInfo<T> o) {
        if (o == null) {
            return -1;
        }
        int diff = runde - o.runde;
        if (diff != 0) {
            return (int) -Math.signum(diff);
        }
        if (isFinal && runde > 0) {
            diff = lauf - o.lauf;
            if (diff != 0) {
                return (int) Math.signum(diff);
            }
        }
        if (platz <= 0 && o.platz <= 0) {
            return 0;
        }
        if (platz <= 0) {
            return 1;
        }
        if (o.platz <= 0) {
            return -1;
        }
        diff = platz - o.platz;
        return (int) Math.signum(diff);
    }
}