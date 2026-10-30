package de.df.jauswertung.daten.kampfrichter;

public class KampfrichterPosition {

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
