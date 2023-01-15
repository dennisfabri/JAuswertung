/*
 * Created on 02.05.2005
 */
package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Einspruch implements Serializable {

    private static final long serialVersionUID = -403898828787336185L;

    @XStreamAsAttribute
    private String gliederung;
    private Altersklasse ak;
    @XStreamAsAttribute
    private boolean maennlich;
    @XStreamAsAttribute
    private String text;
    @XStreamAsAttribute
    private boolean accepted;

    public Einspruch(String gliederung, Altersklasse ak, boolean maennlich, String text, boolean accepted) {
        this.gliederung = gliederung;
        this.text = text;
        this.accepted = accepted;
        this.ak = ak;
        this.maennlich = maennlich;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Altersklasse getAk() {
        return ak;
    }

    public String getGliederung() {
        return gliederung;
    }

    public boolean isMaennlich() {
        return maennlich;
    }

    public String getText() {
        return text;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setAk(Altersklasse ak) {
        this.ak = ak;
    }

    public void setGliederung(String gliederung) {
        this.gliederung = gliederung;
    }

    public void setMaennlich(boolean maennlich) {
        this.maennlich = maennlich;
    }

    public void setText(String text) {
        this.text = text;
    }
}