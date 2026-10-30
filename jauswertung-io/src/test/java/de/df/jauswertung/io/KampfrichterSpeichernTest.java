package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;

/**
 * Files written before the referees moved into their positions keep them in a map of the unit, keyed by the position
 * name.
 */
class KampfrichterSpeichernTest {

    private static final String ALTE_DATEI = "src/test/resources/compatibility/2020/Einzel.wk";

    @Test
    void altesFormatWirdInDiePositionenUebernommen() {
        KampfrichterEinheit einheit = InputManager.ladeWettkampf(ALTE_DATEI).getKampfrichterverwaltung().getEinheit(0);

        assertEquals("Veranstaltungsleitung", einheit.getName());
        assertEquals("Haro Hunn", einheit.getKampfrichter("Veranstaltungsleiter")[0].getName());
        assertEquals("Pekko von Kleist-Damen", einheit.getKampfrichter("Veranstaltungssprecher")[0].getName());
        assertEquals("Alrich Reichel", einheit.getPosition("Protokollführer").getKampfrichter()[0].getName());
    }

    @Test
    void schreibtKeineZuordnungNachPositionsnamenMehr() throws IOException {
        AWettkampf<?> wk = InputManager.ladeWettkampf(ALTE_DATEI);

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        IOUtils.toXML(wk.getKampfrichterverwaltung(), xml);

        assertFalse(xml.toString(StandardCharsets.UTF_8).contains("<entry>"));
    }

    @Test
    void kampfrichterBleibenNachSpeichernUndLadenErhalten(@TempDir Path temp) {
        AWettkampf<?> wk = InputManager.ladeWettkampf(ALTE_DATEI);
        String datei = temp.resolve("kampfrichter.wk").toString();

        assertTrue(OutputManager.speichereWettkampf(datei, wk));
        AWettkampf<?> geladen = InputManager.ladeWettkampf(datei);

        for (int x = 0; x < wk.getKampfrichterverwaltung().getEinheitenCount(); x++) {
            assertArrayEquals(wk.getKampfrichterverwaltung().getEinheit(x).getInhalt(),
                    geladen.getKampfrichterverwaltung().getEinheit(x).getInhalt());
        }
    }

    @Test
    void umbenennenBehaeltDieKampfrichter() {
        KampfrichterEinheit einheit = InputManager.ladeWettkampf(ALTE_DATEI).getKampfrichterverwaltung().getEinheit(0);

        einheit.renamePosition(einheit.getPosition("Veranstaltungsleiter"), "Leitung");

        assertEquals("Haro Hunn", einheit.getKampfrichter("Leitung")[0].getName());
        assertEquals(0, einheit.getKampfrichter("Veranstaltungsleiter").length);
    }
}
