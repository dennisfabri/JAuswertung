package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;

/**
 * Files written before the id was derived contain it as an element of each discipline.
 */
class OWDisziplinIdTest {

    private static final String ALTE_DATEI = "src/test/resources/compatibility/2026/Finale-Einzel.wk";

    @Test
    void idWirdAusDenFeldernAbgeleitet() {
        AWettkampf<?> wk = InputManager.ladeWettkampf(ALTE_DATEI);

        OWDisziplin<?>[] disziplinen = wk.getLauflisteOW().getDisziplinen();
        assertTrue(disziplinen.length > 0);
        for (OWDisziplin<?> d : disziplinen) {
            assertEquals(OWDisziplin.getId(d.akNummer, d.maennlich, d.disziplin, d.round), d.getId());
            assertSame(d, wk.getLauflisteOW().getDisziplin(d.getId()));
        }
    }

    @Test
    void idWirdNichtMehrGeschrieben() throws IOException {
        AWettkampf<?> wk = InputManager.ladeWettkampf(ALTE_DATEI);

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        IOUtils.toXML(wk.getLauflisteOW(), xml);

        assertFalse(xml.toString(StandardCharsets.UTF_8).contains("<Id>"));
    }
}
