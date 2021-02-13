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
public class FormelDSV<T extends ASchwimmer> extends FormelDLRG2007<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "DSV2005";

    @Override
    public String getID() {
        return FormelDSV.ID;
    }

    @Override
    public String getName() {
        return "DSV: Schwimmsportliche Leistungstabelle";
    }

    @Override
    public String getFormel() {
        return "1000*(rec/zeit)^3";
    }

    @Override
    public String getDescription() {
        return "Dies ist die Formel des DSV-Regelwerks";
    }

    public FormelDSV() {
        super();
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

        double quo = ((double) rec) / ((double) seconds);

        double ergebnis = Math.floor(quo * quo * quo * 1000.0) - strafe;

        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}