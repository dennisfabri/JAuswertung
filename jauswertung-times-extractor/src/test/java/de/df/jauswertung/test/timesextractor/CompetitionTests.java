package de.df.jauswertung.test.timesextractor;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.timesextractor.Competition;
import de.df.jauswertung.timesextractor.CompetitorType;
import de.df.jauswertung.timesextractor.Event;
import de.df.jauswertung.timesextractor.ValueTypes;

class CompetitionTests {

    @Test
    void createEmptyCompetition() {
        Competition actual = new Competition();

        assertEquals("", actual.getName());
    }

    @Test
    void createEmptyCompetitionWithName() {
        Competition actual = new Competition("a name");

        assertEquals("a name", actual.getName());
    }

    @Test
    void addEmptyEvent() {
        Competition actual = new Competition("a name");

        actual.addEvent(new Event("agegroup", CompetitorType.Team, "sex", "discipline", 0, true, ValueTypes.TimeInMillis));
    }
}
