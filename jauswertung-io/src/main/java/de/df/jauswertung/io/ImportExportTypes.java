package de.df.jauswertung.io;

import java.util.Arrays;

public enum ImportExportTypes {
    
    REGISTRATION(0),
    HEATLIST(1),
    STARTKARTEN(2),
    HEATS_OVERVIEW(3),
    ZWLIST(4),
    ZW_STARTKARTEN(5),
    RESULTS(6),
    ZW_RESULTS(7),
    PROTOCOL(8),
    REFEREES(9),
    WEITERMELDUNG(10),
    PENALTIES(11),
    TEAMMEMBERS(12),
    BEST_TIMES(13),
    HEATTIMES(14),
    TIMES(15);

    
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
