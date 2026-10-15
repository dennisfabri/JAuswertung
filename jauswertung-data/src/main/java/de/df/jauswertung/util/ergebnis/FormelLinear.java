package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

public class FormelLinear<T extends ASchwimmer> extends FormelDLRG2007<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "Linear";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getFormel() {
        return "1250 - 250*(zeit/rec)";
    }

    @Override
    public String getName() {
        return "Lineare Formel";
    }

    @Override
    public String getDescription() {
        return "Diese Formel ist eine einfache lineare Funktion in Anlehnung an die Formel des DLRG-Regelwerks.";
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

        double quo = ((double) seconds) / ((double) rec);
        double ergebnis = Math.round(100.0 * (-250.0 * quo + 1250.0)) / 100.0 - strafe;
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}
