package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Hashtable;

public class Ergebnisfreigabe implements Serializable {

    private final Hashtable<String, Boolean> freigabe = new Hashtable<>();

    public Ergebnisfreigabe() {
    }
}
