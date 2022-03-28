package de.df.jauswertung.gui.plugins.timelimit;

import java.util.Arrays;

import lombok.Value;

@Value
class Sex {
    private final boolean male;
    private final String[] values;
    
    Sex(boolean male, String... values) {
        this.male = male;
        this.values = values;
    }
    
    boolean matches(String value) {
        return Arrays.stream(values).anyMatch(v -> v.equalsIgnoreCase(value));
    }
}
