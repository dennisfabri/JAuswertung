package de.df.jauswertung.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;

class RoundCompetitionTest {

    @ParameterizedTest
    @ValueSource(strings = { "Finale-Einzel", "Finale-Mannschaft" })
    <T extends ASchwimmer> void roundCompetitionContainsOnlyTheHeatsOfTheSelectedRound(String name) {
        @SuppressWarnings("unchecked")
        AWettkampf<T> wk = (AWettkampf<T>) InputManager
                .ladeWettkampf("src/test/resources/compatibility/2026/" + name + ".wk");
        OWDisziplin<T>[] rounds = wk.getLauflisteOW().getDisziplinen();
        assertTrue(rounds.length > 1);

        for (OWDisziplin<T> round : rounds) {
            OWSelection selection = new OWSelection(wk.getRegelwerk().getAk(round.akNummer), round.akNummer,
                    round.maennlich, round.disziplin, round.round);

            AWettkampf<T> wkx = ResultUtils.createCompetitionFor(wk, selection);

            assertNotNull(wkx);
            assertTrue(wkx.getLauflisteOW().isEmpty());
            assertEquals(heats(round), heats(wkx));
            assertEquals(rounds.length, wk.getLauflisteOW().getDisziplinen().length);
        }
    }

    private static <T extends ASchwimmer> List<List<Integer>> heats(OWDisziplin<T> round) {
        List<List<Integer>> heats = new ArrayList<>();
        for (OWLauf<T> heat : round.getLaeufe()) {
            List<Integer> lanes = new ArrayList<>();
            for (int x = 0; x < heat.getBahnen(); x++) {
                T s = heat.getSchwimmer(x);
                lanes.add(s == null ? 0 : s.getStartnummer());
            }
            heats.add(lanes);
        }
        return heats;
    }

    private static <T extends ASchwimmer> List<List<Integer>> heats(AWettkampf<T> wk) {
        List<List<Integer>> heats = new ArrayList<>();
        for (Lauf<T> heat : wk.getLaufliste().getLaufliste()) {
            List<Integer> lanes = new ArrayList<>();
            for (int x = 0; x < heat.getBahnen(); x++) {
                T s = heat.getSchwimmer(x);
                lanes.add(s == null ? 0 : s.getStartnummer());
            }
            heats.add(lanes);
        }
        return heats;
    }
}
