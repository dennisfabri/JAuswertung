package de.df.jauswertung.misc.times;

import java.util.Arrays;

import de.df.jauswertung.timesextractor.model.JAuswertungPenalty;

public record Time(String competition, boolean team, String name, String details, String organization, String agegroup,
        boolean male, String discipline, int timeInHundrets, JAuswertungPenalty[] penalties) {

    public boolean hasRealPenalty() {
        if (penalties == null) {
            return false;
        }
        return Arrays.stream(penalties).anyMatch(p -> p.isRealPenalty());
    }
}
