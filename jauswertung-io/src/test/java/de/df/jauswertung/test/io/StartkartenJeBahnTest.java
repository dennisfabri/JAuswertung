package de.df.jauswertung.test.io;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SchwimmerUtils;
import de.df.jauswertung.util.valueobjects.Startkarte;

class StartkartenJeBahnTest {

    private static final int AK_OFFEN = 4;
    private static final int ANZAHL = 4;

    private EinzelWettkampf wk;

    @BeforeEach
    void setup() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 2023", true));
        for (int x = 1; x <= ANZAHL; x++) {
            Teilnehmer t = wk.createTeilnehmer("ID" + x, "Nachname" + x, "Vorname" + x, 2000, true, "Gliederung",
                    AK_OFFEN, "");
            t.setDisciplineChoice(0, true);
            t.setDisciplineChoice(1, true);
            wk.addSchwimmer(t);
        }
        wk.getLaufliste().erzeugen();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3 })
    @SuppressWarnings("unchecked")
    void enthaeltNurDieKartenDerBahn(int bahn) {
        List<Startkarte> alle = SchwimmerUtils.toStartkarten(new AWettkampf[] { wk }, 1, true, true, 0, 0);
        List<Startkarte> erwartet = alle.stream().filter(k -> k.getBahn().equals("" + bahn)).toList();

        List<Startkarte> karten = SchwimmerUtils.toStartkarten(new AWettkampf[] { wk }, 1, true, true, 0, 0, bahn);

        assertFalse(erwartet.isEmpty());
        assertEquals(erwartet, karten);
    }

    @Test
    @SuppressWarnings("unchecked")
    void bahnAusserhalbDerBahnanzahlIstLeer() {
        int bahn = wk.getIntegerProperty(HEATS_LANES) + 1;

        List<Startkarte> karten = SchwimmerUtils.toStartkarten(new AWettkampf[] { wk }, 1, true, true, 0, 0, bahn);

        assertTrue(karten.isEmpty());
    }
}
