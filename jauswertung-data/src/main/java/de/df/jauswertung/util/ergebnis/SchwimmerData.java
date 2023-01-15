/*
 * Created on 15.02.2005
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;

/**
 * @author Fabri
 */
public class SchwimmerData<T extends ASchwimmer> {

    private int time = 0;
    private T schwimmer = null;
    private Strafe strafe = null;
    private int rank = 0;
    private double points = 0;

    public SchwimmerData(T s, int disz, boolean wertung) {
        schwimmer = s;
        if (wertung) {
            time = s.getZeit(disz);
            Altersklasse ak = s.getAK();
            strafe = new Strafe(s.getAkkumulierteStrafe(disz), ak.isStrafeIstDisqualifikation());
        } else {
            time = 0;
            strafe = Strafe.NICHTS;
        }
    }

    public void overridePenalty(Strafe s) {
        if (s != null) {
            strafe = s;
        }
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public T getSchwimmer() {
        return schwimmer;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        if (time >= 0) {
            this.time = time;
        }
    }

    public Strafe getStrafe() {
        return strafe;
    }

    public Strafarten getStrafart() {
        if (strafe == null) {
            return Strafarten.NICHTS;
        }
        return strafe.getArt();
    }

    public boolean isWithdraw() {
        if (strafe == null) {
            return false;
        }
        return (strafe.getArt() == Strafarten.NICHTS && strafe.getShortname().equalsIgnoreCase("WD"));
    }
}