package de.df.jauswertung.daten.kampfrichter;

import java.io.Serializable;

public class KampfrichterPosition implements Serializable {

    private static final long serialVersionUID = -7701938416920708064L;

    private KampfrichterStufe minstufe;
    private String position;

    public KampfrichterPosition(String position, KampfrichterStufe stufe) {
        setPosition(position);
        setMinimaleStufe(stufe);
    }

    public KampfrichterStufe getMinimaleStufe() {
        return minstufe;
    }

    public void setMinimaleStufe(KampfrichterStufe minstufe) {
        this.minstufe = minstufe;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        if (position == null) {
            throw new NullPointerException();
        }
        this.position = position;
    }

}
