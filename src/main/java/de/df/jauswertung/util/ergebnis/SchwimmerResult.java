/*
 * Created on 15.02.2005
 */
package de.df.jauswertung.util.ergebnis;

import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;

/**
 * @author Fabri
 */
public class SchwimmerResult<T extends ASchwimmer> {

    private T                            schwimmer    = null;
    private LinkedList<SchwimmerData<T>> results      = null;
    private double                       points       = 0;
    private int                          place        = 0;
    private int                          dnf          = 0;
    private Strafe                       strafe       = Strafe.NICHTS;
    private boolean                      keineWertung = false;

    public SchwimmerResult(T s) {
        schwimmer = s;
        strafe = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
        results = new LinkedList<SchwimmerData<T>>();
    }

    public void add(SchwimmerData<T> result) {
        results.addLast(result);
    }

    @SuppressWarnings({ "unchecked" })
    public SchwimmerData<T>[] getResults() {
        return results.toArray(new SchwimmerData[results.size()]);
    }

    public int getPlace() {
        return place;
    }

    public void setRank(int place) {
        this.place = place;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public int getDnf() {
        return dnf;
    }

    public void setDnf(int dnf) {
        this.dnf = dnf;
    }

    public T getSchwimmer() {
        return schwimmer;
    }

    public boolean hasKeineWertung() {
        if (strafe.getArt() == Strafarten.AUSSCHLUSS) {
            return true;
        }
        return keineWertung;
    }

    public void setStrafe(Strafe s) {
        if (s == null) {
            throw new NullPointerException();
        }
        strafe = s;
    }

    public Strafe getStrafe() {
        return strafe;
    }

    public void setKeineWertung(boolean keinewertung) {
        this.keineWertung = keinewertung;
    }
}