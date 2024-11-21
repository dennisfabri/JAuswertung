package de.df.jauswertung.test.timesextractor;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetitorType;
import de.df.jauswertung.timesextractor.model.JAuswertungEvent;
import de.df.jauswertung.timesextractor.model.JAuswertungValueTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JAuswertungCompetitionTests {

    @Test
    void createEmptyCompetition() {
        JAuswertungCompetition actual = new JAuswertungCompetition();

        assertEquals("", actual.getName());
        assertEquals("", actual.getAcronym());
        assertEquals("", actual.getLengthOfCourse());
        assertEquals("", actual.getDate());
    }

    @Test
    void createEmptyCompetitionWithName() {
        JAuswertungCompetition actual = new JAuswertungCompetition("a name", "a short name", "50m", "2021-01-02");

        assertEquals("a name", actual.getName());
        assertEquals("a short name", actual.getAcronym());
        assertEquals("50m", actual.getLengthOfCourse());
        assertEquals("2021-01-02", actual.getDate());
    }

    @Test
    void addEmptyEvent() {
        JAuswertungCompetition actual = new JAuswertungCompetition("a name", "a short name", "50m", "2021-01-02");

        actual.addEvent(new JAuswertungEvent("agegroup", JAuswertungCompetitorType.Team, "sex", "discipline", 0, true,
                JAuswertungValueTypes.TimeInMillis));
    }
}
