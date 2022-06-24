package de.df.jauswertung.timesextractor;

import lombok.Value;

@Value
public class Entry {
    private final String startnumber;
    private final int value;
    private final String[] penalties;
    private final Swimmer[] swimmer;
}
