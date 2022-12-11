package de.df.jauswertung.timesextractor;

import lombok.Value;

@Value
public class Entry {
    private final String startnumber;
    private final String name;
    private final String organization;
    private final int value;
    private final Penalty[] penalties;
    private final Swimmer[] swimmer;
}
