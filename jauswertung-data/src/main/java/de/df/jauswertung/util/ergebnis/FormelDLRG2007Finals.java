package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;

public class FormelDLRG2007Finals<T extends ASchwimmer> extends FormelILSFinals<T> {

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.daten.regelwerk.Formel#getFormel()
     */
    @Override
    public String getFormel() {
        return "467*(zeit/rec)^2 - 2001*(zeit/rec) + 2534, ...";
    }

    @Override
    public String getName() {
        return "DLRG Regelwerk 2007 und neuer (mit Finals - Beta)";
    }

    @Override
    public String getDescription() {
        return "Diese Formel entspricht den Regelwerken der DLRG seit 2007 mit einer Erweiterung für Finals";
    }

    public static final String ID = "DLRG2007Finals";

    @Override
    public String getID() {
        return ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.daten.regelwerk.Formel#getResult(double, double,
     * double)
     */
    @Override
    protected double getPoints(int time, int rec, int rank, int amount, Strafe str, int heatSize) {
        if (rec == 0) {
            return 0.0;
        }
        if (time == 0) {
            return 0.0;
        }
        if ((str.getArt() != Strafarten.STRAFPUNKTE) && (str.getArt() != Strafarten.NICHTS)) {
            return 0.0;
        }

        double ergebnis = 0.0;
        double quo = ((double) time) / ((double) rec);

        if (time <= 2 * rec) {
            double r1 = 467.0 * quo * quo;
            double r2 = 2001.0 * quo;
            ergebnis = Math.round((r1 - r2 + 2534.0) * 100.0);
        } else if (time <= 5 * rec) {
            double r1 = 200000.0 / 3.0;
            double r2 = (40000.0 / 3.0) * quo;
            ergebnis = Math.round(r1 - r2);
        }
        ergebnis = ergebnis / 100.0 - str.getStrafpunkte();
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}