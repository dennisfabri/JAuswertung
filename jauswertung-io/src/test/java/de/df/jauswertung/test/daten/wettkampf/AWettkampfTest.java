package de.df.jauswertung.test.daten.wettkampf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;

class AWettkampfTest {

    private MannschaftWettkampf wk;

    @BeforeEach
    void setup() {
        Regelwerk aks = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwm");
        Strafen strafen = InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", false);
        wk = new MannschaftWettkampf(aks, strafen);
    }

    // Adds a team with an explicit, pre-set Startnummer. Setting it before addSchwimmer()
    // avoids its auto-assignment branch, which itself calls viewNextStartnummer() and would
    // otherwise interfere with the scenario under test.
    private Mannschaft addWithSn(int sn) {
        Mannschaft m = wk.createMannschaft("Team " + sn, false, "Gliederung", 0, "");
        m.setStartnummer(sn);
        wk.addSchwimmer(m);
        return m;
    }

    @Test
    void emptyCompetitionReturnsOne() {
        assertEquals(1, wk.viewNextStartnummer());
    }

    @Test
    void singleEntryReturnsItsStartnummer() {
        addWithSn(5);
        assertEquals(1, wk.viewNextStartnummer());
    }

    @Test
    void gapBeforeMaxIsFound() {
        addWithSn(1);
        addWithSn(2);
        addWithSn(4);

        assertEquals(3, wk.viewNextStartnummer());
    }

    @Test
    void gapAtStartIsFound() {
        addWithSn(2);
        addWithSn(4);

        assertEquals(1, wk.viewNextStartnummer());
    }

    @Test
    void consecutiveRunReturnsLastUsedStartnummer() {
        // Characterizes existing behavior: viewNextStartnummer() never checks the last
        // array slot, so a gap-free run of n entries (1..n) returns n itself instead of
        // n + 1 - the returned value is already in use by the last entry.
        addWithSn(1);
        addWithSn(2);
        addWithSn(3);

        assertEquals(4, wk.viewNextStartnummer());
        assertEquals(false, wk.isStartnummerUsed(wk.viewNextStartnummer()));
    }

    @Test
    void removingFromConsecutiveRunFreesLastStartnummer() {
        addWithSn(1);
        addWithSn(2);
        addWithSn(3);
        Mannschaft four = addWithSn(4);

        wk.removeSchwimmer(four);

        assertEquals(4, wk.viewNextStartnummer());
    }

    @Test
    void removingFromMiddleOfRunFreesItsGap() {
        addWithSn(1);
        addWithSn(2);
        Mannschaft three = addWithSn(3);
        addWithSn(4);

        wk.removeSchwimmer(three);

        assertEquals(3, wk.viewNextStartnummer());
    }
}
