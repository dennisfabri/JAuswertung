package de.df.jauswertung.util;

public enum AusserKonkurrenzMode {
    No(0), Yes(1), Both(2);

    private final int value;

    AusserKonkurrenzMode(int value) {
        this.value = value;
    }

    public static AusserKonkurrenzMode of(int value) {
        for (AusserKonkurrenzMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return Both;
    }
}
