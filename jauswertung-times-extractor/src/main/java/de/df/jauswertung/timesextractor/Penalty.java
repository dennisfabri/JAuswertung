package de.df.jauswertung.timesextractor;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Penalty(String name, PenaltyType type, int points) {

    @JsonIgnore
    public boolean isRealPenalty() {
        if (type == PenaltyType.None) {
            return false;
        }
        if (type == PenaltyType.Points) {
            return points > 0;
        }
        return true;
    }

}
