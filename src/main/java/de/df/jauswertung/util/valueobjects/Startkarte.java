/*
 * Startkarte.java Created on 4. August 2001, 17:22
 */

package de.df.jauswertung.util.valueobjects;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Mueller
 * @version
 */

public class Startkarte implements Comparable<Startkarte> {

    private final String  ak;
    private final String  disz;
    private final String  lauf;
    private final String  name;
    private final String  gliederung;
    private final String  qgld;
    private final String  bahn;
    private final String  startnummer;
    private final boolean maennlich;
    private final int     event;

    public Startkarte() {
        ak = "";
        disz = "";
        lauf = "";
        name = "";
        gliederung = "";
        bahn = "";
        startnummer = "";
        qgld = "";
        maennlich = false;
        event = 0;
    }

    public Startkarte(ASchwimmer s, int event, int diszNummer, String lauf, int bahn) {
        if (lauf == null) {
            this.lauf = "";
        } else {
            this.lauf = lauf;
        }
        if (bahn < 1) {
            this.bahn = "";
        } else {
            this.bahn = "" + bahn;
        }

        if (s != null) {
            ak = s.getAK().toString() + " " + I18n.geschlechtToString(s);
            String d = s.getAK().getDisziplin(diszNummer, s.isMaennlich()).toString();
            if (d == null) {
                disz = "";
            } else {
                disz = d;
            }
            if (s.getWettkampf().isMultiline() && (s instanceof Mannschaft)) {
                Mannschaft m = (Mannschaft) s;
                name = StringTools.shorten(m.getStarterShort(diszNummer, ","), 25, "...");
            } else {
                name = s.getName() == null ? "" : s.getName();
            }
            if (s.getGliederung() == null) {
                gliederung = "";
            } else {
                gliederung = s.getGliederung();
            }
            if (s.getQualifikationsebene() != null) {
                qgld = s.getQualifikationsebene();
            } else {
                qgld = "";
            }
            startnummer = StartnumberFormatManager.format(s);
            maennlich = s.isMaennlich();
            this.event = event;
        } else {
            ak = "";
            disz = "";
            name = "";
            gliederung = "";
            startnummer = "";
            maennlich = false;
            qgld = "";
            this.event = 0;
        }
    }

    @Override
    public String toString() {
        return "S#" + startnummer + " - " + name + " - " + gliederung + " Wettkampf " + event + " - Lauf " + lauf + " - Bahn " + bahn + " - " + disz + " - "
                + ak + (maennlich ? " männlich" : " weiblich");
    }

    public String getAK() {
        return ak;
    }

    public String getName() {
        return name;
    }

    public String getDisziplin() {
        return disz;
    }

    public String getLauf() {
        return lauf;
    }

    public String getStartnummer() {
        return startnummer;
    }

    public String getBahn() {
        return bahn;
    }

    public String getGliederung() {
        return gliederung;
    }

    public String getQualifikationsgliederung() {
        return qgld;
    }

    public int getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Startkarte) {
            return compareTo((Startkarte) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(Startkarte sk) {
        if (sk == null) {
            return -1;
        }
        if (getBahn().compareTo(sk.getBahn()) != 0) {
            return getBahn().compareTo(sk.getBahn());
        }

        int event1 = event;
        int event2 = sk.event;

        if (event1 - event2 != 0) {
            return event1 - event2;
        }

        int lauf1 = 0;
        int lauf2 = 0;
        try {
            lauf1 = Integer.parseInt(lauf);
        } catch (RuntimeException re) {
            lauf1 = Integer.parseInt(lauf.substring(0, lauf.length() - 1));
        }
        try {
            lauf2 = Integer.parseInt(sk.lauf);
        } catch (RuntimeException re) {
            lauf2 = Integer.parseInt(sk.lauf.substring(0, sk.lauf.length() - 1));
        }
        if (lauf1 - lauf2 != 0) {
            return lauf1 - lauf2;
        }

        int zahl = lauf.length() - sk.lauf.length();
        if (zahl != 0) {
            return zahl;
        }

        return lauf.compareTo(sk.lauf);
    }
}