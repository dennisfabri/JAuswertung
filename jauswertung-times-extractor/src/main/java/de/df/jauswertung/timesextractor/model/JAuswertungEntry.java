package de.df.jauswertung.timesextractor.model;

import lombok.Value;

@Value
public class JAuswertungEntry {
    private final String competitorId;
    private final String startnumber;
    private final String name;
    private final String organization;
    private final String nationality;
    private final int value;
    private final JAuswertungPenalty[] penalties;
    private final JAuswertungSwimmer[] swimmer;
    private final JAuswertungStart start;

    public JAuswertungEntry(String competitorId, String startnumber, String name, String organization,
            String nationality, int value, JAuswertungPenalty[] penalties, JAuswertungSwimmer[] swimmer,
            JAuswertungStart start) {
        this.competitorId = competitorId == null ? "" : competitorId;
        this.startnumber = startnumber;
        this.name = name;
        this.organization = organization;
        this.nationality = nationality == null || nationality.isBlank() ? "DE" : nationality;
        this.value = value;
        this.penalties = penalties;
        this.swimmer = swimmer;
        this.start = start;
    }
}
