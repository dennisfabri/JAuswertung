package de.df.jauswertung.timesextractor.model;

import lombok.NonNull;

public record JAuswertungSwimmer(@NonNull String competitorId, String startnumber, String firstName, String lastName, String sex,
                                 int yearOfBirth) {
}
