package de.df.jauswertung.daten.laufliste;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum HeatsNumberingScheme {
    Standard("Standard"), Hundreds("1-01");

    private final String value;

    HeatsNumberingScheme(String value) {
        this.value = value;
    }

    public static HeatsNumberingScheme fromString(String value) {
        return Arrays.stream(HeatsNumberingScheme.values())
                .filter(scheme -> scheme.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(Standard);
    }
}
