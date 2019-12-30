package de.df.jauswertung.daten.kampfrichter;

import java.io.Serializable;

public class Kampfrichter implements Serializable {

    private static final long serialVersionUID = 5335749933632670503L;

    private String            name;
    private String            bemerkung;
    private String            gliederung;
    private KampfrichterStufe stufe;

    public Kampfrichter() {
        this("", "", "", KampfrichterStufe.KEINE);
    }

    public Kampfrichter(String name, String gliederung, String bemerkung, KampfrichterStufe stufe) {
        setName(name);
        setGliederung(gliederung);
        setBemerkung(bemerkung);
        setStufe(stufe);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        if (bemerkung == null) {
            throw new NullPointerException();
        }
        this.bemerkung = bemerkung;
    }

    public KampfrichterStufe getStufe() {
        return stufe;
    }

    public void setStufe(KampfrichterStufe stufe) {
        if (stufe == null) {
            throw new NullPointerException();
        }
        this.stufe = stufe;
    }

    @Override
    public String toString() {
        return getName() + " (" + getStufe() + "): " + getBemerkung();
    }

    public void setGliederung(String gliederung) {
        if (gliederung == null) {
            throw new NullPointerException();
        }
        this.gliederung = gliederung;
    }

    public String getGliederung() {
        if (gliederung == null) {
            gliederung = "";
        }
        return gliederung;
    }
}