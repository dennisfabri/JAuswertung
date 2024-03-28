package de.df.jauswertung.daten.laufliste;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.util.StringTools;

public class OWLauf<T extends ASchwimmer> implements Serializable {

    @SuppressWarnings("unchecked")
    private T[] schwimmer = (T[]) new ASchwimmer[0];

    private int laufnummer = 0;
    private int laufbuchstabe = 0;
    private final String disciplineId;

    @SuppressWarnings("unchecked")
    public OWLauf(AWettkampf<T> wk, String dId, Lauf<T> lauf) {
        disciplineId = dId;
        setLaufnummer(lauf.getLaufnummer());
        setLaufbuchstabe(lauf.getLaufbuchstabe());
        schwimmer = (T[]) new ASchwimmer[lauf.getBahnen()];
        for (int x = 0; x < schwimmer.length; x++) {
            T s = lauf.getSchwimmer(x);
            if (s != null) {
                schwimmer[x] = SearchUtils.getSchwimmer(wk, s);
            } else {
                schwimmer[x] = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public OWLauf(AWettkampf<T> wk, String dId, OWLauf<T> lauf, boolean deep) {
        disciplineId = dId;
        setLaufnummer(lauf.getLaufnummer());
        setLaufbuchstabe(lauf.getLaufbuchstabe());
        schwimmer = (T[]) new ASchwimmer[lauf.getBahnen()];
        if (deep) {
            for (int x = 0; x < schwimmer.length; x++) {
                T s = lauf.getSchwimmer(x);
                if (s != null) {
                    schwimmer[x] = SearchUtils.getSchwimmer(wk, s);
                } else {
                    schwimmer[x] = null;
                }
            }
        }
    }

    public String getDisciplineId() {
        return disciplineId;
    }

    public int getBahnen() {
        return schwimmer.length;
    }

    public void remove(T t) {
        for (int x = 0; x < schwimmer.length; x++) {
            if (schwimmer[x] != null && schwimmer[x].getStartnummer() == t.getStartnummer()) {
                schwimmer[x] = null;
            }
        }
    }

    public LinkedList<T> getAllSchwimmer() {
        return Arrays.stream(schwimmer).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedList::new));
    }

    public T getSchwimmer(int index) {
        return schwimmer[index];
    }

    public int getLaufnummer() {
        return laufnummer;
    }

    public void setLaufnummer(int x) {
        if (x < 0) {
            return;
        }
        laufnummer = x;
    }

    public int getLaufbuchstabe() {
        return laufbuchstabe;
    }

    public void setLaufbuchstabe(int x) {
        if ((x < 0) || (x >= 26)) {
            return;
        }
        laufbuchstabe = x;
    }

    public String getName() {
        final int zahl = laufbuchstabe;
        return "" + laufnummer + StringTools.characterString(zahl);
    }

    public boolean alleZeitenEingegeben() {
        for (int x = 0; x < getBahnen(); x++) {
            T s = getSchwimmer(x);
            if (s != null) {
                if (!s.hasInput(disciplineId)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int GetSchwimmerIndex(int startnummer) {
        for (int x = 0; x < schwimmer.length; x++) {
            if (schwimmer[x] != null && schwimmer[x].getStartnummer() == startnummer) {
                return x;
            }
        }
        return -1;
    }

    public T getSchwimmer() {
        return Arrays.stream(schwimmer).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public void setSchwimmer(int x, T s) {
        schwimmer[x] = s;
    }

    public boolean isEmpty() {
        return getSchwimmer() == null;
    }

    public boolean isMale() {
        T t = getSchwimmer();
        return t == null || t.isMaennlich();
    }

}
