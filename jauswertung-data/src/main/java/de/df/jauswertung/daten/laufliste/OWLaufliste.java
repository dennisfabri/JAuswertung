package de.df.jauswertung.daten.laufliste;

import java.io.Serializable;
import java.util.Hashtable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;

public class OWLaufliste<T extends ASchwimmer> implements Serializable {

    final AWettkampf<T> wk;
    private Hashtable<String, OWDisziplin<T>> disziplinen = new Hashtable<String, OWDisziplin<T>>();

    public OWLaufliste(AWettkampf<T> wk) {
        if (wk == null) {
            throw new NullPointerException("wk must not be null");
        }
        this.wk = wk;
    }

    public OWDisziplin<T> getDisziplin(OWSelection sel) {
        return getDisziplin(sel.akNummer, sel.male, sel.discipline, sel.round);
    }

    public OWDisziplin<T> getDisziplin(int akNummer, boolean m, int disz, int round) {
        String id = OWDisziplin.getId(akNummer, m, disz, round);
        return getDisziplin(id);
    }

    public OWDisziplin<T> getDisziplin(String id) {
        if (id == null) {
            return null;
        }
        return disziplinen.get(id);
    }

    public void remove(T t) {
        for (OWDisziplin<T> d : disziplinen.values()) {
            d.remove(t);
        }
    }

    public void removeDiscipline(String id) {
        disziplinen.remove(id);
    }

    @SuppressWarnings({ "cast", "unchecked" })
    public OWDisziplin<T>[] getDisziplinen() {
        return (OWDisziplin<T>[]) disziplinen.values().toArray(new OWDisziplin[0]);
    }

    public boolean isEmpty() {
        return disziplinen.isEmpty();
    }

    public OWDisziplin<T> addDisziplin(int akNummer, boolean male, int discipline, int round) {
        String id = OWDisziplin.getId(akNummer, male, discipline, round);
        if (!disziplinen.containsKey(id)) {
            disziplinen.put(id, new OWDisziplin<T>(akNummer, male, discipline, round));
        }
        return disziplinen.get(id);
    }

    public void clear() {
        disziplinen.clear();
    }

    public void check(boolean orderingChanged) {
        if ((disziplinen == null) || (disziplinen.size() == 0)) {
            return;
        }
        if (wk.getSchwimmeranzahl() == 0) {
            clear();
            return;
        }
        if (!wk.isHeatBased()) {
            clear();
            return;
        }
        if (orderingChanged) {
            clear();
            return;
        }
    }

    public int getMaximaleAnzahlBahnen() {
        int max = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        for (OWDisziplin<T> disziplin : disziplinen.values()) {
            max = Math.max(max, disziplin.getBahnen());
        }
        return max;
    }
}
