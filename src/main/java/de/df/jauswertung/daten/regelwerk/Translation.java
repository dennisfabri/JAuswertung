package de.df.jauswertung.daten.regelwerk;

import java.io.Serializable;
import java.util.Hashtable;

public class Translation implements Serializable {

    public Hashtable<String, String> translations = new Hashtable<>();

    public Translation() {
        translations.put("Male", "M�nnlich");
        translations.put("male", "m�nnlich");
        translations.put("maleShort", "m");
        translations.put("Female", "Weiblich");
        translations.put("female", "weiblich");
        translations.put("femaleShort", "w");
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
}