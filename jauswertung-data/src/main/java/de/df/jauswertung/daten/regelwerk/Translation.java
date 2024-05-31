package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;
import java.util.Hashtable;

public class Translation implements Serializable {

    private final Hashtable<String, String> translations;

    public Translation() {
        translations = new Hashtable<>();
        translations.put("Male", "Männlich");
        translations.put("male", "männlich");
        translations.put("maleShort", "m");
        translations.put("Female", "Weiblich");
        translations.put("female", "weiblich");
        translations.put("femaleShort", "w");
    }

    private Translation(Hashtable<String, String> translations) {
        this.translations = translations;
    }

    public void put(String key, String value) {
        translations.put(key, value);
    }

    public String get(String key, String defaultValue) {
        if (translations.containsKey(key)) {
            return translations.get(key);
        }
        return defaultValue;
    }

    public Translation copyOf() {
        return new Translation(new Hashtable<>(translations));
    }
}