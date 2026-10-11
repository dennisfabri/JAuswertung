package de.df.jauswertung.test.results.indoorfinals;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

class DisqualificationPerRoundTest {

    private static final int AK = 0;
    private static final boolean MALE = true;
    private static final int DISCIPLINE = 0;

    /**
     * One discipline with the given qualification sizes; swimmer n is the n-th fastest in every round. The swimmers
     * with the start numbers {@code disqualified} are disqualified in round {@code roundOfDisqualification}.
     */
    static final class Scenario {
        final EinzelWettkampf wk;
        final int rounds;

        Scenario(int swimmers, int[] qualified, int roundOfDisqualification, int... disqualified) {
            wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
            stream(wk.getRegelwerk().getAks()).forEach(ak -> ak.setLaufsortierung(Reihenfolge.ILSPool.getValue()));
            Altersklasse ak = wk.getRegelwerk().getAk(AK);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                int[] ids = new int[qualified.length + 1];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = 100 * (y + 1) + i;
                }
                ak.getDisziplin(y, true).setRunden(qualified, ids);
                ak.getDisziplin(y, false).setRunden(qualified, ids);
            }
            for (int x = 1; x <= swimmers; x++) {
                Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, MALE, "Test", AK, "");
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    t.setDisciplineChoice(y, y == DISCIPLINE);
                }
                t.setMeldezeit(0, x);
                wk.addSchwimmer(t);
            }
            rounds = qualified.length + 1;
            for (int round = 0; round < rounds; round++) {
                OWSelection selection = selection(round);
                createHeats(selection);
                OWDisziplin<Teilnehmer> d = wk.getLauflisteOW().getDisziplin(selection);
                for (int x = 1; x <= swimmers; x++) {
                    Teilnehmer t = SearchUtils.getSchwimmer(wk, x);
                    if (d.contains(t)) {
                        t.getEingabe(selection.getId(), true).setZeit(6000 + x);
                        if (round == roundOfDisqualification && contains(disqualified, x)) {
                            t.getEingabe(selection.getId(), true)
                                    .addStrafe(new Strafe("Disqualifikation", "DQ", Strafarten.DISQUALIFIKATION, 0));
                        }
                    }
                }
            }
        }

        private static boolean contains(int[] values, int value) {
            return Arrays.stream(values).anyMatch(v -> v == value);
        }

        OWSelection selection(int round) {
            return new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, DISCIPLINE, round);
        }

        private void createHeats(OWSelection selection) {
            EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);
            wkx.setProperty(HEATS_LANES, 8);
            wkx.setProperty(HEATS_FIRST_HEAT, 1);
            wkx.setProperty(HEATS_SORTING_ORDER, Reihenfolge.Regelwerk.getValue());
            wkx.setProperty(HEATS_AVOID_ALMOST_EMPTY, true);
            wkx.setProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED, true);
            wkx.setProperty(HEATS_MIXED, false);
            wkx.setProperty(HEATS_MIXED_IN_FRONT, false);
            wkx.setProperty(HEATS_JOIN_HEATS, false);
            wkx.setProperty(HEATS_ROTATE, false);
            wkx.setProperty(HEATS_NOT_COMPETING_MIXED, false);
            wkx.setProperty(HEATS_RESPECT_QUALIFICATIONS, true);
            wkx.setProperty(HEATS_EMPTY_LIST, false);
            wkx.setProperty(HEATS_REGISTERED_POINTS_INDEX, 0);
            wkx.getLaufliste().erzeugen();
            HeatsUtils.save(wk, selection, wkx);
        }

        /** Results of one round as shown for that round. */
        SchwimmerResult<Teilnehmer>[] round(int round) {
            OWSelection selection = selection(round);
            EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);
            if (!selection.isFinal) {
                wkx.getRegelwerk().setFormelID(FormelILS.ID);
            }
            return ResultCalculator.getResults(wkx, wkx.getRegelwerk().getAk(AK), MALE);
        }

        /** Overall result of the discipline over all rounds. */
        SchwimmerResult<Teilnehmer>[] overall() {
            EinzelWettkampf w = (EinzelWettkampf) ResultUtils.generateEinzelwertungswettkampf(wk, AK, MALE, false);
            return ResultCalculator.getResults(w, w.getRegelwerk().getAk(DISCIPLINE), MALE);
        }
    }

    /**
     * Competitions for single results renumber the swimmers, so they are identified by the first name.
     */
    private static SchwimmerResult<Teilnehmer> result(SchwimmerResult<Teilnehmer>[] results, int swimmer) {
        return Arrays.stream(results).filter(r -> r.getSchwimmer().getVorname().equals("" + swimmer)).findFirst()
                .orElseThrow();
    }

    private static void assertPlaced(SchwimmerResult<Teilnehmer> result, int place, double points) {
        assertFalse(result.hasKeineWertung());
        assertEquals(place, result.getPlace());
        assertEquals(points, result.getPoints());
    }

    private static void assertUnranked(SchwimmerResult<Teilnehmer> result) {
        assertTrue(result.hasKeineWertung());
        assertTrue(result.getPlace() <= 0);
        assertEquals(0, result.getPoints());
    }

    @Test
    void geradesFinale() {
        Scenario scenario = new Scenario(10, new int[0], 0, 3);

        assertUnranked(result(scenario.round(0), 3));
        assertPlaced(result(scenario.round(0), 4), 3, 16);

        assertUnranked(result(scenario.overall(), 3));
        assertPlaced(result(scenario.overall(), 4), 3, 16);
    }

    @Test
    void finaleMit17Startern() {
        Scenario scenario = new Scenario(20, new int[] { 17 }, 1, 2, 17);

        // A-Finale
        assertPlaced(result(scenario.round(1), 2), 8, 10);
        assertPlaced(result(scenario.round(1), 3), 2, 18);
        // Letzter Platz, jenseits der Punktetabelle
        assertPlaced(result(scenario.round(1), 17), 17, 0);

        assertPlaced(result(scenario.overall(), 2), 8, 10);
        assertPlaced(result(scenario.overall(), 17), 17, 0);
        assertPlaced(result(scenario.overall(), 18), 18, 0);
    }

    @Test
    void zwischenlaufMitMehrAls16() {
        Scenario scenario = new Scenario(30, new int[] { 24, 8 }, 1, 2);

        assertUnranked(result(scenario.round(1), 2));
        assertPlaced(result(scenario.round(1), 24), 23, 0);

        // Letzter des Zwischenlaufs
        assertPlaced(result(scenario.overall(), 2), 24, 0);
        assertPlaced(result(scenario.overall(), 24), 23, 0);
        assertPlaced(result(scenario.overall(), 25), 25, 0);
    }

    @Test
    void zwischenlaufMit16() {
        Scenario scenario = new Scenario(20, new int[] { 16, 8 }, 1, 2);

        assertUnranked(result(scenario.round(1), 2));
        assertPlaced(result(scenario.round(1), 16), 15, 2);

        // Letzter des Zwischenlaufs
        assertPlaced(result(scenario.overall(), 2), 16, 1);
        assertPlaced(result(scenario.overall(), 16), 15, 2);
        assertPlaced(result(scenario.overall(), 17), 17, 0);
    }
}
