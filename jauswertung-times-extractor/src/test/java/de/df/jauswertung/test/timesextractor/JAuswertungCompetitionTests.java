package de.df.jauswertung.test.timesextractor;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetitorType;
import de.df.jauswertung.timesextractor.model.JAuswertungEvent;
import de.df.jauswertung.timesextractor.model.JAuswertungValueTypes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JAuswertungCompetitionTests {

    private static final Pattern pattern = Pattern.compile("^.* [(](.*)[)]$");

    @ParameterizedTest
    @ValueSource(strings = { "Netherlands (NED)",
            "Italy (ITA)",
            "Switzerland (SUI)" })
    void testPattern(String input) {
        Matcher matcher = pattern.matcher(input);
        assertEquals(true, matcher.find());
        assertEquals(1, matcher.groupCount());
        assertEquals(input, matcher.group(0));
        assertEquals(input.substring(input.indexOf("(") + 1, input.length() - 1), matcher.group(1));
    }

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
