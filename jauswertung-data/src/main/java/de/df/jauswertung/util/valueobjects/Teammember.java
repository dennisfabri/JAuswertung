package de.df.jauswertung.util.valueobjects;

import java.io.IOException;
import java.util.List;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.util.I18n;

public class Teammember {

    private final static String[] forbidden = new String[]{"<", ">", ";", "#"};

    private final int index;

    private final String lastname;
    private final String firstname;
    private final Geschlecht geschlecht;
    private final int Jahrgang;
    private final String importId;

    private final String code;

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

        code = createCode();
    }

    private String createCode() {
        return String.join("<", List.of("" + index,
                                        cleanup(lastname),
                                        cleanup(firstname),
                                        geschlechtToIndex(),
                                        "" + Jahrgang,
                                        cleanup(importId)));
    }

    private String geschlechtToIndex() {
        return switch (geschlecht) {
            case weiblich -> "0";
            case maennlich -> "1";
            default -> "2";
        };
    }

    private static String cleanup(String text) {
        for (String s : forbidden) {
            text = text.replace(s, "");
        }
        return text;
    }

    public boolean isEmpty() {
        return lastname.isEmpty() && firstname.isEmpty();
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

    public String getCode() {
        return code;
    }

    public static Teammember FromCode(String code) throws IOException {
        String[] parts = code.split("<");
        if (parts.length != 6) {
            throw new IOException("Expected 6 entries but found " + parts.length);
        }
        int index = Integer.parseInt(parts[0]);
        String lastname = parts[1];
        String firstname = parts[2];
        int sex = Integer.parseInt(parts[3]);
        int jg = Integer.parseInt(parts[4]);
        Geschlecht s = switch (sex) {
            case 0 -> Geschlecht.weiblich;
            case 1 -> Geschlecht.maennlich;
            default -> Geschlecht.unbekannt;
        };
        String importId = parts[5];
        return new Teammember(index, firstname, lastname, s, jg, importId);
    }
}
