package de.df.jauswertung.daten.laufliste;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;

public class OWDisziplin<T extends ASchwimmer> implements Serializable, Comparable<OWDisziplin<T>> {

    public final String Id;
    public final int akNummer;
    public final boolean maennlich;
    public final int disziplin;
    public final int round;

    public HashSet<T> Schwimmer = new HashSet<>();

    public List<OWLauf<T>> laeufe = new ArrayList<>();

    public static String getId(int akNummer, boolean male, int disziplin, int round) {
        return String.format("%d-%s-%d-%d", akNummer, male ? "m" : "f", disziplin, round);
    }

    public OWDisziplin(int ak, boolean m, int disz, int round) {
        Id = getId(ak, m, disz, round);
        akNummer = ak;
        maennlich = m;
        disziplin = disz;
        this.round = round;
    }

    public void remove(T t) {
        Schwimmer.remove(t);
        for (OWLauf<T> l : laeufe) {
            l.remove(t);
        }
    }

    public boolean isEmpty() {
        return laeufe == null || laeufe.isEmpty();
    }

    public int getBahnen() {
        if (isEmpty()) {
            return 0;
        }
        return laeufe.get(0).getBahnen();
    }

    public LinkedList<OWLauf<T>> getLaeufe() {
        return new LinkedList<>(laeufe);
    }

    public void addSchwimmer(T t) {
        if (!Schwimmer.contains(t)) {
            Schwimmer.add(t);
        }
    }

    public LinkedList<T> getSchwimmer() {
        if (round == 0) {
            return null;
        }
        return new LinkedList<>(Schwimmer);
    }

    @Override
    public int compareTo(OWDisziplin<T> o) {
        int diff = akNummer - o.akNummer;
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = (maennlich ? 1 : 0) - (o.maennlich ? 1 : 0);
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = disziplin - o.disziplin;
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = round - o.round;
        return (int) Math.signum(diff);

    }

    public boolean contains(T m) {
        if (laeufe == null) {
            return false;
        }
        return laeufe.stream()
                .anyMatch(l -> l.getAllSchwimmer().stream().anyMatch(s -> s.getStartnummer() == m.getStartnummer()));
    }
}
