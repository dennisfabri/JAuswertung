package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.TimelimitUtils;
import de.df.jauswertung.util.TimelimitUtils.BrokenTimelimit;

/**
 * The 2026 fixture has two sets of time limits for the first discipline of the first age group: "PZ1" is a lower
 * limit (record), "PZ2" an upper limit (required time).
 */
class TimelimitUtilsTest {

    private static AWettkampf<ASchwimmer> load() {
        @SuppressWarnings("unchecked")
        AWettkampf<ASchwimmer> wk = InputManager.ladeWettkampf("src/test/resources/compatibility/2026/Einzel.wk");
        return wk;
    }

    @Test
    void hasTimelimitsOnlyWhenDefined() {
        assertTrue(load().getTimelimits().hasTimelimits());
        assertFalse(new EinzelWettkampf(InputManager.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true)).getTimelimits()
                .hasTimelimits());
    }

    @Test
    void findsBrokenTimelimits() {
        List<BrokenTimelimit<ASchwimmer>> broken = TimelimitUtils.getBrokenTimelimits(load(), -1);

        assertEquals(List.of("Ullrich, Susanne", "Wahl, Melanie", "Schulz, David", "Meier, Daniel"),
                broken.stream().map(b -> b.swimmer().getName()).toList());
        assertTrue(broken.stream().allMatch(b -> b.timelimits().getShortname().equals("PZ2")));
        assertTrue(broken.stream().allMatch(b -> b.penalty().isStrafe()));
    }

    @Test
    void restrictsToAnAgeGroup() {
        AWettkampf<ASchwimmer> wk = load();

        assertEquals(4, TimelimitUtils.getBrokenTimelimits(wk, 0).size());
        assertEquals(0, TimelimitUtils.getBrokenTimelimits(wk, 1).size());
        assertTrue(TimelimitUtils.hasBrokenTimelimits(wk, 0));
        assertFalse(TimelimitUtils.hasBrokenTimelimits(wk, 1));
    }

    @Test
    void findsATimeBelowTheLowerLimit() {
        AWettkampf<ASchwimmer> wk = load();
        Timelimits record = wk.getTimelimits().getTimelimits(0);
        assertEquals("PZ1", record.getShortname());
        ASchwimmer swimmer = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(0), false).stream()
                .filter(s -> s.hasInput(0)).findFirst().orElseThrow();

        swimmer.setStrafen(0, new LinkedList<>());
        swimmer.setZeit(0, record.getLimits()[0].getTime() - 1);

        assertTrue(TimelimitUtils.getBrokenTimelimits(wk, -1).stream()
                .anyMatch(b -> b.swimmer() == swimmer && b.timelimits() == record));
    }
}
