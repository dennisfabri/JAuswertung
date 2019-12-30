package de.df.jauswertung.daten;

import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.gui.util.SchwimmerUtils;

/**
 * @author Dennis Fabri
 * @version 0.1
 * @since 9. Februar 2001, 12:11
 */
public class Teilnehmer extends ASchwimmer {

    private static final long serialVersionUID = -817140386007358149L;

    @XStreamAsAttribute
    private int               jahrgang;
    @XStreamAsAttribute
    private String            vorname;
    @XStreamAsAttribute
    private String            nachname;

    /** Creates new Teilnehmer */
    Teilnehmer(EinzelWettkampf ewk, String name, String vname, int tJahrgang, boolean geschlecht, String gliederung, int ak, String bemerkung) {
        super(ewk, geschlecht, gliederung, ak, bemerkung);
        setVorname(vname.trim());
        setNachname(name.trim());
        setJahrgang(tJahrgang);
    }

    public String getVorname() {
        return vorname;
    }

    @Override
    public String getName() {
        return nachname + ", " + vorname;
    }

    /**
     * Gibt an, wie oft der Schwimmer an der Zusatzwertung teilnehmen muss.
     * 
     * @return Anzahl der ZW-Teilnahmen.
     */
    @Override
    public int getMaximaleHLW() {
        return 1;
    }

    @Override
    public int getMinMembers() {
        return 1;
    }

    @Override
    public int getMaxMembers() {
        return 1;
    }

    public synchronized void setVorname(String s) {
        if (s == null) {
            throw new NullPointerException("Forename must not be null!");
        }
        s = s.replace('\t', ' ');
        s = s.trim();
        if (s.length() == 0) {
            return;
        }
        vorname = s;
    }

    public String getNachname() {
        return nachname;
    }

    public synchronized void setNachname(String s) {
        if (s == null) {
            throw new NullPointerException("Surname must not be null!");
        }
        s = s.replace('\t', ' ');
        s = s.trim();
        if (s.length() == 0) {
            return;
        }
        nachname = s;
    }

    public boolean fitsAgeGroup() {
        int base = getWettkampf().getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION, Calendar.getInstance().get(Calendar.YEAR));
        return getAK().fitsYearOfBirth(getJahrgang(), base);
    }

    public int getJahrgang() {
        return jahrgang;
    }

    public synchronized void setJahrgang(int tjahrgang) {
        if (tjahrgang < 0) {
            throw new IllegalArgumentException("Year of birth must not be negative!");
        }
        jahrgang = SchwimmerUtils.ermittleJahrgang(tjahrgang);
    }

    public int getAlter(int year) {
        if (getJahrgang() <= 0) {
            return 0;
        }
        return Math.max(0, year - getJahrgang());
    }
}