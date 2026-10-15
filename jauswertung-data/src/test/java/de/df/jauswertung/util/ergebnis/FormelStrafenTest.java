package de.df.jauswertung.util.ergebnis;

import static de.df.jauswertung.daten.ASchwimmer.DISCIPLINE_NUMBER_SELF;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;

class FormelStrafenTest {

    private static final int ZEIT = 6500;

    private static EinzelWettkampf wettkampf(String formel) {
        Disziplin[] disziplinen = { new Disziplin("A", 6000, 50, 1), new Disziplin("B", 6000, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, true, formel);
        Altersklasse ak = new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false);
        ak.setStrafeIstDisqualifikation(true);
        regelwerk.setAk(0, ak);
        return new EinzelWettkampf(regelwerk, new Strafen());
    }

    private static Teilnehmer schwimmer(EinzelWettkampf wk, String name) {
        Teilnehmer t = wk.createTeilnehmer("", name, "Vorname", 2000, false, "Gliederung", 0, "");
        t.setZeit(0, ZEIT);
        t.setZeit(1, ZEIT);
        wk.addSchwimmer(t);
        return t;
    }

    private static SchwimmerResult<Teilnehmer> ergebnis(EinzelWettkampf wk, String name) {
        return Arrays.stream(ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(0), false))
                .filter(r -> r.getSchwimmer().getNachname().equals(name)).findFirst().orElseThrow();
    }

    @ParameterizedTest
    @ValueSource(strings = { FormelDLRG2007.ID, FormelLinear.ID })
    void allgemeineStrafeWirdZurDisqualifikation(String formel) {
        EinzelWettkampf wk = wettkampf(formel);
        schwimmer(wk, "Bestraft").addStrafe(DISCIPLINE_NUMBER_SELF,
                new Strafe("Strafe", "S", Strafarten.STRAFPUNKTE, 10));
        schwimmer(wk, "Normal");

        SchwimmerResult<Teilnehmer> r = ergebnis(wk, "Bestraft");

        assertAll(() -> assertEquals(0.0, r.getResults()[0].getPoints(), "Disziplin A"),
                () -> assertEquals(0.0, r.getResults()[1].getPoints(), "Disziplin B"));
    }

    @ParameterizedTest
    @ValueSource(strings = { FormelDLRG2007.ID, FormelLinear.ID })
    void nichtAngetretenInAllenDisziplinenOhneWertung(String formel) {
        EinzelWettkampf wk = wettkampf(formel);
        Teilnehmer t = schwimmer(wk, "Abwesend");
        for (int d = 0; d < 2; d++) {
            t.setZeit(d, 0);
            t.addStrafe(d, new Strafe("Nicht angetreten", "NA", Strafarten.NICHT_ANGETRETEN, 0));
        }
        schwimmer(wk, "Normal");

        assertTrue(ergebnis(wk, "Abwesend").hasKeineWertung());
    }
}
