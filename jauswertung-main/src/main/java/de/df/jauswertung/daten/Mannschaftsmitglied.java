package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Mannschaftsmitglied implements Serializable {

    @XStreamAsAttribute
    private String     Vorname;
    @XStreamAsAttribute
    private String     Nachname;
    @XStreamAsAttribute
    private Geschlecht geschlecht;
    @XStreamAsAttribute
    private int        Jahrgang;

    public Mannschaftsmitglied() {
        clear();
    }

    public boolean isEmpty() {
        return !HasName();
    }

    public boolean HasName() {
        if (Nachname.trim().isEmpty()) {
            return false;
        }
        if (Vorname.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public String getVorname() {
        return Vorname;
    }

    public void setVorname(String vorname) {
        if (vorname == null) {
            vorname = "";
        }
        Vorname = vorname.trim();
    }

    public String getNachname() {
        return Nachname;
    }

    public void setNachname(String nachname) {
        if (nachname == null) {
            nachname = "";
        }
        Nachname = nachname.trim();
    }

    public Geschlecht getGeschlecht() {
        return geschlecht;
    }

    public void setGeschlecht(Geschlecht geschlecht) {
        this.geschlecht = geschlecht;
    }

    public int getJahrgang() {
        if (isEmpty()) {
            return 0;
        }
        return Jahrgang;
    }

    public void setJahrgang(int jahrgang) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (jahrgang < 0 || jahrgang > year) {
            throw new IllegalArgumentException("Year must be at least 0 but not higher than " + year + " but was " + jahrgang);
        }
        if (jahrgang == 0) {
            // Nothing to do
        } else if (jahrgang < 100) {
            jahrgang = (jahrgang <= year % 100) ? 2000 + jahrgang : 1900 + jahrgang;
        } else if (jahrgang < 1900) {
            throw new IllegalArgumentException("Year must be at least 1900 but was " + jahrgang + ".");
        }
        Jahrgang = jahrgang;
    }

    public String getName() {
        boolean v = !Vorname.isEmpty();
        boolean n = !Nachname.isEmpty();
        if (!v && !n) {
            return "";
        }
        if (v != n) {
            if (v) {
                return Vorname.trim();
            }
            return Nachname.trim();
        }
        return Vorname.trim() + " " + Nachname.trim();
    }

    public void copyTo(Mannschaftsmitglied mannschaftsmitglied) {
        mannschaftsmitglied.geschlecht = this.geschlecht;
        mannschaftsmitglied.Jahrgang = this.Jahrgang;
        mannschaftsmitglied.Nachname = this.Nachname;
        mannschaftsmitglied.Vorname = this.Vorname;
    }

    @Override
    public String toString() {
        String sex = "";
        switch (geschlecht) {
        case maennlich:
            sex = "m";
            break;
        case weiblich:
            sex = "w";
            break;
        case unbekannt:
            sex = "-";
            break;
        }
        return Vorname + " " + Nachname + " (" + sex + ") " + getJahrgang();
    }

    public void clear() {
        Nachname = "";
        Vorname = "";
        Jahrgang = 0;
        geschlecht = Geschlecht.unbekannt;
    }

    public boolean isComplete() {
        if (Nachname.trim().isEmpty()) {
            return false;
        }
        if (Vorname.trim().isEmpty()) {
            return false;
        }
        if (Jahrgang <= 0) {
            return false;
        }
        if (geschlecht == Geschlecht.unbekannt) {
            return false;
        }
        return true;
    }
}
