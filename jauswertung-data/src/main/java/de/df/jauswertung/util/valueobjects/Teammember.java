package de.df.jauswertung.util.valueobjects;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.util.I18n;

public class Teammember {

    private final int index;

    private final String lastname;
    private final String firstname;
    private final Geschlecht geschlecht;
    private final int Jahrgang;
    private final String importId;

    public Teammember(Mannschaft m, int index) {
        this(index, m.getMannschaftsmitglied(index).getVorname(), m.getMannschaftsmitglied(index).getNachname(),
             m.getMannschaftsmitglied(index).getGeschlecht(), m.getMannschaftsmitglied(index).getJahrgang(), m.getMannschaftsmitglied(index).getImportId());
    }

    public Teammember(int index, String firstname, String lastname, Geschlecht s, int jg, String importId) {
        this.index = index;
        this.firstname = firstname;
        this.lastname = lastname;
        if (!isEmpty()) {
            this.geschlecht = s;
            this.Jahrgang = jg;
        } else {
            this.geschlecht = Geschlecht.unbekannt;
            this.Jahrgang = 0;
        }
        this.importId = importId;
    }

    public boolean isEmpty() {
        return lastname.isEmpty() && firstname.isEmpty();
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public Geschlecht getGeschlecht() {
        return geschlecht;
    }

    public String getGeschlechtAsString() {
        return switch (geschlecht) {
            case maennlich -> "  " + I18n.get("maleShort") + "  ";
            case weiblich -> "  " + I18n.get("femaleShort") + "  ";
            case unbekannt -> "     ";
        };
    }

    public int getJahrgang() {
        return Jahrgang;
    }

    public String getJahrgangAsString() {
        return I18n.yearToString(Jahrgang);
    }

    public String getImportId() {
        return importId;
    }

    int getIndex() {
        return index;
    }
}
