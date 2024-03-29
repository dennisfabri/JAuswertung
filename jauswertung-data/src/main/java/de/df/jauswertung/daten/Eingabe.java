package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.regelwerk.Strafe;

public class Eingabe implements Serializable {

    @XStreamAsAttribute
    private int zeit = 0;
    private int[] starter = null;
    private LinkedList<Strafe> strafen = new LinkedList<>();

    public LinkedList<Strafe> getStrafen() {
        return new LinkedList<>(strafen);
    }

    public void setStrafen(List<Strafe> s) {
        if (s == null) {
            s = new LinkedList<>();
        }
        strafen = new LinkedList<>(s);
    }

    public void addStrafe(Strafe s) {
        strafen.add(s);
    }

    void removeStrafe(int x) {
        strafen.remove(x);
    }

    public int getZeit() {
        return zeit;
    }

    public void setZeit(int zeit) {
        this.zeit = zeit;
    }

    public int[] getStarter() {
        return starter;
    }

    public void setStarter(int[] starter) {
        this.starter = starter;
    }
}
