package de.df.jauswertung.io;

import java.util.Arrays;

public enum ImportExportTypes {

    REGISTRATION(0),
    HEATLIST(1),
    STARTKARTEN(2),
    HEATS_OVERVIEW(3),
    RESULTS(4),
    ZW_RESULTS(5),
    PROTOCOL(6),
    REFEREES(7),
    WEITERMELDUNG(8),
    PENALTIES(9),
    TEAMMEMBERS(10),
    BEST_TIMES(11),
    HEATTIMES(12),
    TIMES(13),
    REGISTRATION_UPDATE(14),
    STARTERS(15);

    private final int value;

    private ImportExportTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ImportExportTypes getByValue(int selectedIndex) {
        return Arrays.stream(values()).filter(t -> t.getValue() == selectedIndex).findFirst().orElseGet(() -> null);
    }
}
