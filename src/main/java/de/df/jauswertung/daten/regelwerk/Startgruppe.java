package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.laufliste.Laufliste;

public class Startgruppe implements Serializable {

    @XStreamAsAttribute
    private String  name           = "";
    @XStreamAsAttribute
    private int     laufsortierung = Laufliste.REIHENFOLGE_MELDEZEITEN;
    @XStreamAsAttribute
    private boolean laufrotation   = true;

    public Startgruppe(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null!");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name must not be empty!");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLaufsortierung() {
        return laufsortierung;
    }

    public void setLaufsortierung(int laufsortierung) {
        this.laufsortierung = laufsortierung;
    }

    public boolean hasLaufrotation() {
        return laufrotation;
    }

    public void setLaufrotation(boolean laufrotation) {
        this.laufrotation = laufrotation;
    }
}