package de.df.jauswertung.misc.meldetoolimport;

import java.util.Hashtable;

public class MMitglied {

    private Hashtable<String, String> data;

    public MMitglied(Hashtable<String, String> entry) {
        data = entry;
    }

    public String get(String key) {
        return data.get(key);
    }

    public String[] toInfo() {
        String x = get("Geschlecht");
        return new String[] { get("Vorname"), get("Nachname"), Meldungsimport.toJahrgang(get("Geburtstag")), get("Geschlecht").equals("1") ? "f" : "m", };
    }
}