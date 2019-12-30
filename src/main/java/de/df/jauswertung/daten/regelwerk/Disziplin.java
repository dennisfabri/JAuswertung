/*
 * Disziplin.java Created on 9. Februar 2001, 14:52
 */

package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;
import java.util.Arrays;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Enthaelt alle Informationen einer Disiplin
 * 
 * @author Dennis Fabri
 * @version 0.2
 */

public class Disziplin implements Serializable {

    private static final long serialVersionUID = 3831806024414464536L;

    @XStreamAsAttribute
    private String            name             = "";
    @XStreamAsAttribute
    private int               rec              = 1;
    @XStreamAsAttribute
    private int               laenge           = 50;
    @XStreamAsAttribute
    private int               laps             = 0;

    /**
     * Qualifikanten pro Runde
     * Finals werden nicht mitgezaehlt
     **/
    private int[]             runden           = new int[0];
    private int[]             rundenIds        = new int[0];

    /**
     * Erzeugt eine leere Disziplin.
     */
    public Disziplin() {
        this("-", 1, 50, 1);
    }

    public Disziplin(Disziplin d) {
        this(d.getName(), d.getRec(), d.getLaenge(), d.getLaps());
    }

    /**
     * Creates new Disziplin
     * 
     * @param _name
     *            Enthaelt den Namen der Disziplin
     * @param _rec
     *            Enthaelt den zugehoerigen rec-Wert
     */
    public Disziplin(String disziplinName, int recwert, int laenge, int runden) {
        setRec(recwert);
        setName(disziplinName);
        setLaenge(laenge);
        setLaps(runden);
    }

    public int getLaps() {
        if (laps <= 0) {
            return 1;
        }
        return laps;
    }

    public void setLaps(int l) {
        laps = l;
    }

    /**
     * Liefert den Rec-Wert zurueck.
     * 
     * @return Enthaelt den Rec-Wert
     */
    public int getRec() {
        return rec;
    }

    /**
     * Setzt einen neuen Rec-Wert
     * 
     * @param _rec
     *            Enthaelt den zu setzenden Rec-Wert
     */
    public synchronized void setRec(int rek) {
        rec = rek;
    }

    /**
     * Liefert den Namen
     * 
     * @return Enthaelt den Namen
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt einen neuen Namen.
     * 
     * @param _name
     *            Enthaelt den zu setzenden Namen.
     */
    public synchronized void setName(String n) {
        if (n == null) {
            return;
        }
        if (n.length() == 0) {
            return;
        }
        name = n;
    }

    /**
     * Liefert den Namen der Disziplin.
     * 
     * @return Enthaelt den Namen der Disziplin
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Disziplin) {
            Disziplin d = (Disziplin) o;
            if (!name.equals(d.name)) {
                return false;
            }
            if (laenge != d.laenge) {
                return false;
            }
            return rec == d.rec;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void setLaenge(int laenge) {
        this.laenge = laenge;
    }

    public int getLaenge() {
        if (laenge <= 0) {
            return 50;
        }
        return laenge;
    }

    public boolean isValid() {
        return name.length() > 0;
    }

    public int[] getRunden() {
        if (runden == null) {
            runden = new int[0];
        }
        return Arrays.copyOf(runden, runden.length);
    }

    public void setRunden(int[] r, int[] rIds) {
        if (r == null) {
            throw new IllegalArgumentException("Runden duerfen nicht null sein.");
        }
        if (rIds == null) {
            throw new IllegalArgumentException("RundenIds duerfen nicht null sein.");
        }
        if (r.length + 1 != rIds.length) {
            throw new IllegalArgumentException("RundenIds muessen genau um 1 laenger sein als Runden.");
        }
        for (int rx : r) {
            if (rx <= 0) {
                throw new IllegalArgumentException("Alle Werte muessen groesser als 0 sein.");
            }
        }
        for (int rx : rIds) {
            if (rx < 0) {
                throw new IllegalArgumentException("Alle Werte muessen mindestens 0 sein.");
            }
        }
        runden = r;
        rundenIds = rIds;
    }

    public int[] getRundenIds() {
        if (rundenIds == null) {
            rundenIds = new int[getRunden().length + 1];
        }
        if (rundenIds.length != getRunden().length + 1) {
            rundenIds = new int[getRunden().length + 1];
        }
        return Arrays.copyOf(rundenIds, rundenIds.length);
    }

    public int getRundenId(int round) {
        if (getRundenIds().length <= round) {
            return 0;
        }
        return getRundenIds()[round];
    }

    public void copyFrom(Disziplin disziplin) {
        name = disziplin.name;
        rec = disziplin.rec;
        laenge = disziplin.laenge;
        runden = Arrays.copyOf(disziplin.runden, disziplin.runden.length);
        rundenIds = Arrays.copyOf(disziplin.rundenIds, disziplin.rundenIds.length);
    }
}