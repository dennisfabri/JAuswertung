package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.CompetitionUtils.MedalCount;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

class CompetitionUtilsMedaillenspiegelTest {

    @Test
    void ohneSchwimmerLeer() {
        assertEquals(List.of(), CompetitionUtils.generateMedaillenspiegel(new EinzelWettkampf(regelwerk(), new Strafen())));
    }

    @Test
    void zaehltMedaillenUndVergibtBeiGleichstandDenselbenPlatz() {
        EinzelWettkampf wk = new EinzelWettkampf(regelwerk(), new Strafen());
        // AK 0: X vor Y vor Z, der vierte Platz zählt nicht
        schwimmer(wk, 0, "X", 5000);
        schwimmer(wk, 0, "Y", 5100);
        schwimmer(wk, 0, "Z", 5200);
        schwimmer(wk, 0, "X", 5300);
        // AK 1: Y vor X
        schwimmer(wk, 1, "Y", 5000);
        schwimmer(wk, 1, "X", 5100);

        List<MedalCount> spiegel = CompetitionUtils.generateMedaillenspiegel(wk);

        // The order of tied organisations is not defined; only the first of them shows the rank.
        assertEquals(List.of(1, 0, 3), spiegel.stream().map(MedalCount::rank).toList());
        assertEquals(Set.of("X", "Y"), Set.of(spiegel.get(0).gliederung(), spiegel.get(1).gliederung()));
        for (MedalCount m : spiegel.subList(0, 2)) {
            assertEquals(List.of(1, 1, 0), List.of(m.gold(), m.silver(), m.bronze()));
        }
        assertEquals(new MedalCount(3, "Z", 0, 0, 1), spiegel.get(2));
    }

    private static void schwimmer(EinzelWettkampf wk, int ak, String gliederung, int zeit) {
        Teilnehmer t = wk.createTeilnehmer("", "Nachname", "Vorname", 2000, false, gliederung, ak, "");
        wk.addSchwimmer(t);
        t.setZeit(0, zeit);
    }

    private static Regelwerk regelwerk() {
        Regelwerk regelwerk = new Regelwerk(2, true, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("50m", 6000, 50, 1) };
            regelwerk.setAk(x, new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, false));
        }
        return regelwerk;
    }
}
