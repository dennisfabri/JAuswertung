/*
 * GesamtwertungSchwimmer.java Created on 23. Juni 2003, 18:36
 */

package de.df.jauswertung.util;

import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.*;

/**
 * @author Dennis Fabri
 */
public class GesamtwertungSchwimmer extends ASchwimmer {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3688785886313854264L;
    private double[]          punkte;
    private Strafarten        misc             = Strafarten.STRAFPUNKTE;

    /** Creates a new instance of GesamtwertungSchwimmer */
    public GesamtwertungSchwimmer(AWettkampf<GesamtwertungSchwimmer> wk, String gliederung, int aks) {
        super(wk, true, gliederung, 0, "");
        punkte = new double[aks * 2];
    }

    @Override
    public String getName() {
        return getGliederung();
    }

    public int getAmount() {
        return punkte.length;
    }

    public double getPunkte() {
        if (misc != Strafarten.STRAFPUNKTE) {
            return 0.0;
        }
        double r = 0;
        for (double aPunkte : punkte) {
            r += aPunkte;
        }
        return r;
    }

    public double getPunkte(int ak, boolean male) {
        int index = ak * 2 + (male ? 1 : 0);
        return punkte[index];
    }

    public void setPunkte(int ak, boolean male, double d) {
        if (d < 0) {
            return;
        }
        int index = ak * 2 + (male ? 1 : 0);
        punkte[index] = d;
    }

    public void addPunkte(int ak, boolean male, double d) {
        if (d < 0) {
            return;
        }
        int index = ak * 2 + (male ? 1 : 0);
        punkte[index] += d;
    }

    @Override
    public Strafe getAkkumulierteStrafe(int x) {
        if (x > 0) {
            throw new IndexOutOfBoundsException(x + " > 0");
        }
        return new Strafe("", "", misc, 0);
    }

    @Override
    public LinkedList<Strafe> getStrafen(int disz) {
        LinkedList<Strafe> ls = new LinkedList<Strafe>();
        ls.addLast(getAkkumulierteStrafe(disz));
        return ls;
    }

    public synchronized void nichtAngetreten() {
        misc = Strafarten.NICHT_ANGETRETEN;
    }

    @Override
    public int getMaxMembers() {
        return 1;
    }

    @Override
    public int getMaximaleHLW() {
        return 1;
    }

    @Override
    public int getMinMembers() {
        return 1;
    }
}
