package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Wertungsgruppe implements Serializable {

    @XStreamAsAttribute
    private String name = "";
    @XStreamAsAttribute
    private boolean protokollMitEinzelwertung = true;
    @XStreamAsAttribute
    private boolean protokollMitMehrkampfwertung = false;
    @XStreamAsAttribute
    private boolean einzelwertunghlw = false;
    @XStreamAsAttribute
    private boolean strafeIstDisqualifikation = false;

    public Wertungsgruppe(String name) {
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

    public boolean isProtokollMitEinzelwertung() {
        return protokollMitEinzelwertung;
    }

    public void setProtokollMitEinzelwertung(boolean protokollMitEinzelwertung) {
        this.protokollMitEinzelwertung = protokollMitEinzelwertung;
    }

    public boolean isProtokollMitMehrkampfwertung() {
        return protokollMitMehrkampfwertung;
    }

    public void setProtokollMitMehrkampfwertung(boolean protokollMitMehrkampfwertung) {
        this.protokollMitMehrkampfwertung = protokollMitMehrkampfwertung;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isEinzelwertungHlw() {
        return einzelwertunghlw;
    }

    public void setEinzelwertungHlw(boolean einzelwertunghlw) {
        this.einzelwertunghlw = einzelwertunghlw;
    }

    public boolean isStrafeIstDisqualifikation() {
        return strafeIstDisqualifikation;
    }

    public void setStrafeIstDisqualifikation(boolean strafeIstDisqualifikation) {
        this.strafeIstDisqualifikation = strafeIstDisqualifikation;
    }

}
