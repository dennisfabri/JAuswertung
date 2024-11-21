package de.df.jauswertung.timesextractor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record JAuswertungPenalty(String name, JAuswertungPenaltyType type, int points) {

    @JsonIgnore
    public boolean isRealPenalty() {
        if (type == JAuswertungPenaltyType.None) {
            return false;
        }
        if (type == JAuswertungPenaltyType.Points) {
            return points > 0;
        }
        return true;
    }

}
