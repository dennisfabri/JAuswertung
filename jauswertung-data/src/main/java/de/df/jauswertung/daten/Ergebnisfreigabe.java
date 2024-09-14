package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Hashtable;

@Deprecated
public class Ergebnisfreigabe implements Serializable {

    @Deprecated
    @SuppressWarnings("unused")
    private final Hashtable<String, Boolean> freigabe = new Hashtable<>();

    public Ergebnisfreigabe() {
    }
}
