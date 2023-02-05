package de.df.jauswertung.timesextractor;

import lombok.Value;

@Value
public class Swimmer {
    private final String startnumber;
    private final String firstName;
    private final String lastName;
    private final String sex;
    private final int yearOfBirth;
}
