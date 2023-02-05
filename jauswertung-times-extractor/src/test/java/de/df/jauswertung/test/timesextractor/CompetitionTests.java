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
        assertEquals("", actual.getAcronym());
    }

    @Test
    void createEmptyCompetitionWithName() {
        Competition actual = new Competition("a name", "a short name");

        assertEquals("a name", actual.getName());
        assertEquals("a short name", actual.getAcronym());
    }

    @Test
    void addEmptyEvent() {
        Competition actual = new Competition("a name", "a short name");

        actual.addEvent(new Event("agegroup", CompetitorType.Team, "sex", "discipline", 0, true, ValueTypes.TimeInMillis));
    }
}
