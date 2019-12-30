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
public class FormelDP<T extends ASchwimmer> extends FormelDLRG2007<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "DeutschlandPokal";

    @Override
    public String getID() {
        return FormelDP.ID;
    }

    @Override
    public String getName() {
        return "DLRG Deutschland-Pokal (bis 2017)";
    }

    @Override
    public String getFormel() {
        return "1672/3*(zeit/rec)^2 - 2472*(zeit/rec) + 8744/3, ...";
    }

    @Override
    public String getDescription() {
        return "Dies ist die Formel des Deutschland-Pokals";
    }

    public FormelDP() {
        super();
    }

    @Override
    double getResult(int seconds, int rec, Strafarten misc, int strafe) {
        if (rec == 0) {
            return 0.0;
        }
        if (seconds <= 0) {
            return 0.0;
        }
        if ((misc != Strafarten.STRAFPUNKTE) && (misc != Strafarten.NICHTS)) {
            return 0.0;
        }

        double ergebnis = 0.0;
        double quo = ((double) seconds) / ((double) rec);

        // 0 <zeit =< 2*rec: ((1672/3)*(zeit/rec))-2472*(zeit/rec)+8744/3
        // 2*rec<zeit=<2,5*rec: 680-240*(zeit/rec)
        // 2,5*rec<zeit=<3*rec: 480-160*(zeit/rec)

        if (seconds <= 2 * rec) {
            double r1 = 1672.0 / 3.0 * quo * quo;
            double r2 = 2472.0 * quo;
            ergebnis = Math.round((r1 - r2 + (8744.0 / 3.0)) * 100.0);
        } else if (seconds <= 2.5 * rec) {
            double r1 = 680;
            double r2 = 240.0 * quo;
            ergebnis = Math.round((r1 - r2) * 100.0);
        } else if (seconds <= 3 * rec) {
            double r1 = 480;
            double r2 = 160.0 * quo;
            ergebnis = Math.round((r1 - r2) * 100.0);
        }
        ergebnis = ergebnis / 100.0 - strafe;
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}