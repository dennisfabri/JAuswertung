package de.df.jauswertung.daten.kampfrichter;

import java.util.LinkedList;

public class KampfrichterPosition {

    private KampfrichterStufe minstufe;
    private String position;
    private LinkedList<Kampfrichter> kampfrichter = new LinkedList<>();

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

    public Kampfrichter[] getKampfrichter() {
        return kampfrichter.toArray(new Kampfrichter[kampfrichter.size()]);
    }

    void setKampfrichter(LinkedList<Kampfrichter> kr) {
        kampfrichter = kr;
    }

    void addKampfrichter(Kampfrichter kr) {
        kampfrichter.addLast(kr);
    }

    LinkedList<Kampfrichter> getKampfrichterListe() {
        return kampfrichter;
    }

    /**
     * Called after loading, where no constructor runs. Files written before the referees moved into their positions
     * have none here; {@link KampfrichterEinheit} moves them in afterwards.
     */
    private Object readResolve() {
        if (kampfrichter == null) {
            kampfrichter = new LinkedList<>();
        }
        return this;
    }
}
