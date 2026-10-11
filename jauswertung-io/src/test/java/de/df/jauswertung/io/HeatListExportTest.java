package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeatListExportTest {

    @Test
    void offeredForCompetitionsWithoutFinals() {
        assertTrue(ExportManager.isEnabled(
                InputManager.ladeWettkampf("src/test/resources/compatibility/2026/Einzel.wk"),
                ImportExportTypes.HEAT_LIST));
    }

    @Test
    void notOfferedForCompetitionsWithFinals() {
        assertFalse(ExportManager.isEnabled(
                InputManager.ladeWettkampf("src/test/resources/compatibility/2026/Finale-Einzel.wk"),
                ImportExportTypes.HEAT_LIST));
        assertTrue(ExportManager.isEnabled(
                InputManager.ladeWettkampf("src/test/resources/compatibility/2026/Finale-Einzel.wk"),
                ImportExportTypes.STARTKARTEN));
    }
}
