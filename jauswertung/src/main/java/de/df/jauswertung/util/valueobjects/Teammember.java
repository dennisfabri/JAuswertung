package de.df.jauswertung.util.valueobjects;

import java.io.IOException;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.util.I18n;

public class Teammember {

    private final static String[] forbidden = new String[] { "<", ">", ";", "#" };

    private final int             index;

    private final String          lastname;
    private final String          firstname;
    private final Geschlecht      geschlecht;
    private final int             Jahrgang;

    private final String          code;

    public Teammember(Mannschaft m, int index) {
        this(index, m.getMannschaftsmitglied(index).getVorname(), m.getMannschaftsmitglied(index).getNachname(),
                m.getMannschaftsmitglied(index).getGeschlecht(), m.getMannschaftsmitglied(index).getJahrgang());
    }

    private Teammember(int index, String firstname, String lastname, Geschlecht s, int jg) {
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

        code = createCode();
    }

    private String createCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(index);
        sb.append("<");
        sb.append(cleanup(lastname));
        sb.append("<");
        sb.append(cleanup(firstname));
        sb.append("<");
        switch (geschlecht) {
        case weiblich:
            sb.append(0);
            break;
        case maennlich:
            sb.append(1);
            break;
        default:
            sb.append(2);
            break;
        }
        sb.append("<");
        sb.append(Jahrgang);
        return sb.toString();
    }

    private static String cleanup(String text) {
        for (String s : forbidden) {
            text = text.replace(s, "");
        }
        return text;
    }

    public boolean isEmpty() {
        return lastname.length() == 0 && firstname.length() == 0;
    }

    public int getIndex() {
        return index;
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
        switch (geschlecht) {
        case maennlich:
            return "  " + I18n.get("maleShort") + "  ";
        case weiblich:
            return "  " + I18n.get("femaleShort") + "  ";
        case unbekannt:
            return "     ";
        }
        return "     ";
    }

    public int getJahrgang() {
        return Jahrgang;
    }

    public String getJahrgangAsString() {
        if (Jahrgang <= 0) {
            return "";
        }
        return "" + Jahrgang;
    }

    public String getCode() {
        return code;
    }

    public static Teammember FromCode(String code) throws IOException {
        String[] parts = code.split("<");
        if (parts.length != 5) {
            throw new IOException("Expected 5 entries but found " + parts.length);
        }
        int index = Integer.parseInt(parts[0]);
        String lastname = parts[1];
        String firstname = parts[2];
        int sex = Integer.parseInt(parts[3]);
        int jg = Integer.parseInt(parts[4]);
        Geschlecht s = Geschlecht.unbekannt;
        switch (sex) {
        case 0:
            s = Geschlecht.weiblich;
            break;
        case 1:
            s = Geschlecht.maennlich;
            break;
        default:
            s = Geschlecht.unbekannt;
            break;
        }
        return new Teammember(index, firstname, lastname, s, jg);
    }
}