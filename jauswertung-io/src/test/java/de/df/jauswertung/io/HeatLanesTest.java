package de.df.jauswertung.io;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;

class HeatLanesTest {

    @Test
    void changingTheLanesUpdatesTheHeatsOfALoadedCompetition() {
        AWettkampf<?> wk = InputManager.ladeWettkampf("src/test/resources/compatibility/2026/Einzel.wk");
        assertFalse(wk.getLaufliste().getLaufliste().isEmpty());
        int lanes = wk.getIntegerProperty(HEATS_LANES) + 2;

        wk.setProperty(HEATS_LANES, lanes);

        assertTrue(wk.getLaufliste().getLaufliste().stream().allMatch(lauf -> lauf.getBahnen() == lanes));
    }
}
