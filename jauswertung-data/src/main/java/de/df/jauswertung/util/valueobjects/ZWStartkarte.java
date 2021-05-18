/*
 * Startkarte.java Created on 4. August 2001, 17:22
 */

package de.df.jauswertung.util.valueobjects;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri @version
 */
public class ZWStartkarte<T extends ASchwimmer> implements Comparable<ZWStartkarte<T>> {

    private final String  ak;
    private final String  name;
    private final String  member;
    private final String  gliederung;
    private final String  qgliederung;
    private final String  bahn;
    private final String  sn;
    private final int     startnummer;
    private final boolean maennlich;
    private final Time    zeit;

    private final int     bahncode;

    private final boolean multiple;
    private final int     zaehler;

    private final T       schwimmer;

    public ZWStartkarte(T s, Time zeit, int bahn, int zaehler) {
        ak = s.getAK().toString();
        this.zeit = zeit;
        this.zaehler = zaehler;
        this.schwimmer = s;
        if (zaehler < 0) {
            throw new IllegalArgumentException();
        }
        if (bahn < 1) {
            this.bahn = "";
            bahncode = -1;
        } else {
            this.bahn = "" + bahn;
            bahncode = bahn - 1;
        }
        String n = s.getName();
        if (n == null) {
            name = "";
        } else {
            name = n;
        }
        String mem = null;
        if (s instanceof Mannschaft) {
            Mannschaft m = (Mannschaft) s;
            if (!m.getMannschaftsmitglied(zaehler).isEmpty()) {
                mem = m.getMitgliedsname(zaehler).trim();
            }
        }
        member = mem;
        String g = s.getGliederung();
        if (g == null) {
            gliederung = "";
        } else {
            gliederung = g;
        }
        String q = s.getQualifikationsebene();
        if (q == null) {
            qgliederung = "";
        } else {
            qgliederung = q;
        }
        sn = StartnumberFormatManager.format(s);
        startnummer = s.getStartnummer();
        maennlich = s.isMaennlich();
        multiple = (s.getMaximaleHLW() > 1);
    }

    public T getSchwimmer() {
        return schwimmer;
    }

    public String getVorname() {
        if (schwimmer instanceof Teilnehmer) {
            return ((Teilnehmer) schwimmer).getVorname();
        }
        Mannschaft m = (Mannschaft) schwimmer;
        return m.getMannschaftsmitglied(zaehler).getVorname();
    }

    public String getNachname() {
        if (schwimmer instanceof Teilnehmer) {
            return ((Teilnehmer) schwimmer).getNachname();
        }
        Mannschaft m = (Mannschaft) schwimmer;
        return m.getMannschaftsmitglied(zaehler).getNachname();
    }

    public int getJahrgang() {
        if (schwimmer instanceof Teilnehmer) {
            return ((Teilnehmer) schwimmer).getJahrgang();
        }
        Mannschaft m = (Mannschaft) schwimmer;
        return m.getMannschaftsmitglied(zaehler).getJahrgang();
    }

    @Override
    public String toString() {
        return "S#" + startnummer + " - " + name + " - " + gliederung + " (" + qgliederung + ")" + " - Uhrzeit " + zeit + " - Bahn " + bahn + " - " + ak
                + (maennlich ? " männlich" : " weiblich");
    }

    public String getAK() {
        return ak;
    }

    public String getName() {
        return name;
    }

    public String getMember() {
        return member;
    }

    public String getExtendedName() {
        if (member == null) {
            return getName();
        }
        StringBuilder sb = new StringBuilder(name);
        if (name.length() > 0) {
            sb.append(" - ");
        }
        sb.append(member);
        return sb.toString();
    }

    public int getTimecode() {
        return zeit.getTimeInMinutes();
    }

    public String getQualifikationsebene() {
        return qgliederung;
    }

    public String getUhrzeit() {
        if (zeit == null) {
            return "";
        }
        return zeit.toString();
    }

    public String getStartnummer() {
        return "" + sn + (multiple ? " " + StringTools.asText(zaehler) : "");
    }

    public int getStartnummerWert() {
        return startnummer;
    }

    public int getStarterIndex() {
        return zaehler;
    }

    public String getBahn() {
        return bahn;
    }

    public int getBahnindex() {
        return bahncode;
    }

    public String getGliederung() {
        return gliederung;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ZWStartkarte) {
            return compareTo((ZWStartkarte<T>) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (bahn + zeit).hashCode();
    }

    @Override
    public int compareTo(ZWStartkarte<T> sk) {
        if (sk == null) {
            return -1;
        }
        if (getBahn().compareTo(sk.getBahn()) != 0) {
            return getBahn().compareTo(sk.getBahn());
        }

        return zeit.compareTo(sk.zeit);
    }

    public boolean isMaennlich() {
        return maennlich;
    }
}