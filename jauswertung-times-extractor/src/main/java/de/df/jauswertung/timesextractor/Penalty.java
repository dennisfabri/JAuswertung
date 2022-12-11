package de.df.jauswertung.timesextractor;

public record Penalty(String name, PenaltyType type, int points) {

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
