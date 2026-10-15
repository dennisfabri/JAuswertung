/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public class FormelDirectPoints<T extends ASchwimmer> extends APunkteformel<T> {

    public static final String ID = "DirectPoints";

    @Override
    public String getID() {
        return FormelDirectPoints.ID;
    }

    @Override
    public String getName() {
        return "Direkte Punktevergabe";
    }

    @Override
    public String getDescription() {
        return "Direkte Punktevergabe";
    }

    public FormelDirectPoints() {
        super();
    }

    @Override
    public String getFormel() {
        return "-";
    }

    @Override
    double getResult(int seconds, int rec, Strafarten misc, int strafe) {
        if (seconds == 0) {
            return 0.0;
        }
        if ((misc != Strafarten.STRAFPUNKTE) && (misc != Strafarten.NICHTS)) {
            return 0.0;
        }

        double ergebnis = seconds;

        ergebnis = ergebnis / 100.0 - strafe;
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}