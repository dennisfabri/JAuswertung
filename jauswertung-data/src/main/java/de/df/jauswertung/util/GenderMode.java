package de.df.jauswertung.util;

public enum GenderMode {
    Female(0), Male(1), Both(2);

    private final int value;

    GenderMode(int value) {
        this.value = value;
    }

    public static GenderMode of(int value) {
        for (GenderMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return Both;
    }
}
