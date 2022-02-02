package de.df.jauswertung.util.ergebnis;

import java.util.Arrays;
import java.util.Iterator;

import de.df.jauswertung.daten.ASchwimmer;

public class Results<T extends ASchwimmer> implements Iterable<SchwimmerResult<T>> {

    private final SchwimmerResult<T>[] entries;

    public Results(SchwimmerResult<T>[] entries) {
        this.entries = entries;
    }

    @SuppressWarnings("unchecked")
    public Results() {
        this(new SchwimmerResult[0]);
    }

    public SchwimmerResult<T> getResult(int pos) {
        return entries[pos];
    }

    public int size() {
        return entries.length;
    }
    
    public ASchwimmer getSchwimmer(int y) {
        return entries[y].getSchwimmer();
    }

    public boolean hasKeineWertung(int y) {
        return entries[y].hasKeineWertung();
    }

    public int getPlace(int y) {
        return entries[y].getPlace();
    }

    public double getPoints(int y) {
        return entries[y].getPoints();
    }

    public double getPointsDifferenceToFirst(int y) {
        return getPoints(0) - getPoints(y);
    }

    public SchwimmerData<T>[] getResults(int y) {
        return entries[y].getResults();
    }

    public boolean isAusserKonkurrenz(int y) {
        return getSchwimmer(y).isAusserKonkurrenz();
    }

    @Override
    public Iterator<SchwimmerResult<T>> iterator() {
        return Arrays.stream(entries).iterator();
    }
}
