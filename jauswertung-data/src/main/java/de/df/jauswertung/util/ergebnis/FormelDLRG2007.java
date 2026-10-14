/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

/**
 * @author Dennis Fabri
 * @date 10.06.2007
 */
public class FormelDLRG2007<T extends ASchwimmer> extends APunkteformel<T> {

    public static final String ID = "DLRG2007";

    @Override
    public String getID() {
        return ID;
    }

    public FormelDLRG2007() {
        super();
    }

    @Override
    public String getFormel() {
        return "467*(zeit/rec)^2 - 2001*(zeit/rec) + 2534, ...";
    }

    @Override
    public String toString() {
        return getFormel();
    }

    @Override
    public String getName() {
        return "DLRG Regelwerk 2007 und neuer";
    }

    @Override
    public String getDescription() {
        return "Diese Formel entspricht den Regelwerken der DLRG seit 2007.";
    }

    @Override
    double getResult(int seconds, int rec, Strafarten misc, int strafe) {
        if (rec == 0) {
            return 0.0;
        }
        if (seconds == 0) {
            return 0.0;
        }
        if ((misc != Strafarten.STRAFPUNKTE) && (misc != Strafarten.NICHTS)) {
            return 0.0;
        }

        double ergebnis = 0.0;
        double quo = ((double) seconds) / ((double) rec);

        if (seconds <= 2 * rec) {
            double r1 = 467.0 * quo * quo;
            double r2 = 2001.0 * quo;
            ergebnis = Math.round((r1 - r2 + 2534.0) * 100.0);
        } else if (seconds <= 5 * rec) {
            double r1 = 200000.0 / 3.0;
            double r2 = (40000.0 / 3.0) * quo;
            ergebnis = Math.round(r1 - r2);
        }
        ergebnis = ergebnis / 100.0 - strafe;
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}
