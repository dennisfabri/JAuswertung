/*
 * Created on 08.01.2006
 */
package de.df.jauswertung.print;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;

import static de.df.jauswertung.daten.PropertyConstants.ROUND_ID;

public class Zieleinlaufkarte {

    private int event;
    private String lauf;
    private String disziplin;
    private String startgruppe;

    @SuppressWarnings("rawtypes")
    public Zieleinlaufkarte(AWettkampf wk, Lauf lauf) {
        if (lauf != null) {
            this.event = wk.getIntegerProperty(ROUND_ID, 0);
            this.lauf = lauf.getName(wk.getHeatsNumberingScheme());
            this.disziplin = lauf.getDisziplin();
            this.startgruppe = lauf.getStartgruppe();
        } else {
            this.event = 0;
            this.lauf = "";
            this.disziplin = "";
            this.startgruppe = "";
        }
    }

    public int getEvent() {
        return event;
    }

    public String getStartgruppe() {
        return startgruppe;
    }

    public String getDisziplin() {
        return disziplin;
    }

    public String getLauf() {
        return lauf;
    }
}