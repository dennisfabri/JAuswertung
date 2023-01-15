package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Hashtable;

public class Ergebnisfreigabe implements Serializable {

    private final Hashtable<String, Boolean> freigabe = new Hashtable<>();

    public Ergebnisfreigabe() {
    }

    private boolean get(int ak, boolean male, int discipline, String suffix) {
        return freigabe.containsKey(generateKey(ak, male, discipline, suffix));
    }

    private void set(int ak, boolean male, int discipline, String suffix, boolean value) {
        String key = generateKey(ak, male, discipline, suffix);
        if (value) {
            if (!freigabe.containsKey(key)) {
                freigabe.put(key, true);
            }
        } else {
            if (!freigabe.containsKey(key)) {
                freigabe.remove(key);
            }
        }
    }

    private String generateKey(int ak, boolean male, int discipline, String suffix) {
        return "" + ak + "x" + male + "x" + discipline + "x" + suffix;
    }

    public boolean isZeitFreigegeben(int ak, boolean male, int discipline) {
        return get(ak, male, discipline, "time");
    }

    public boolean isHLWFreigegeben(int ak, boolean male) {
        return get(ak, male, 0, "zw");
    }

    public boolean isStrafeFreigegeben(int ak, boolean male, int discipline) {
        return get(ak, male, discipline, "penalty");
    }

    public void setZeitFreigegeben(int ak, boolean male, int discipline, boolean value) {
        set(ak, male, discipline, "time", value);
    }

    public void setHLWFreigegeben(int ak, boolean male, boolean value) {
        set(ak, male, 0, "zw", value);
    }

    public void setStrafeFreigegeben(int ak, boolean male, int discipline, boolean value) {
        set(ak, male, discipline, "penalty", value);
    }
}
