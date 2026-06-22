package de.df.jauswertung.test.daten.laufliste;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.Laufliste.Einteilung;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;

class LauflisteTest {

    // Indices into the "DLRG 2023" individual rulebook (Regelwerk.getAks()).
    // AK 12 / AK 13/14: laufsortierung=Meldepunkte, 3 mandatory disciplines, laufrotation=true.
    // AK 15/16 / AK 17/18 / AK Offen: laufsortierung=Meldezeiten, 6 disciplines with discipline
    // choice (1-4 chosen), laufrotation=false.
    private static final int AK_12 = 0;
    private static final int AK_13_14 = 1;
    private static final int AK_15_16 = 2;
    private static final int AK_17_18 = 3;
    private static final int AK_OFFEN = 4;
    private static final int[] AK_12_TO_OFFEN = { AK_12, AK_13_14, AK_15_16, AK_17_18, AK_OFFEN };
    private static final int CHOSEN_DISCIPLINES = 4;

    private EinzelWettkampf wk;

    @BeforeEach
    void setup() {
        Regelwerk aks = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe");
        Strafen strafen = InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 2023", true);
        wk = new EinzelWettkampf(aks, strafen);
    }

    private static Stream<Arguments> ageGroupsAndGenders() {
        return Stream.of(
                Arguments.of(AK_12, true),
                Arguments.of(AK_12, false),
                Arguments.of(AK_OFFEN, true),
                Arguments.of(AK_OFFEN, false));
    }

    private static Stream<Arguments> teamCountsWithAgeGroupsAndGenders() {
        int[] teamCounts = { 1, 2, 3, 5, 9, 10, 16, 18, 20 };
        return ageGroupsAndGenders().flatMap(
                args -> IntStream.of(teamCounts).mapToObj(count -> Arguments.of(args.get()[0], args.get()[1], count)));
    }

    private static Stream<Arguments> gendersAndTeamCounts() {
        int[] teamCounts = { 1, 2, 3, 5, 9, 10, 16, 18, 20 };
        return Stream.of(true, false)
                .flatMap(gender -> IntStream.of(teamCounts).mapToObj(count -> Arguments.of(gender, count)));
    }

    // AKs with discipline choice (15/16, 17/18, Offen) require an explicit selection (1-4 of
    // their 6 disciplines); every participant here chooses the same fixed subset {0,1,2,3}, the
    // maximum allowed.
    private Teilnehmer createTeilnehmer(int ak, boolean maennlich, int x) {
        Teilnehmer t = wk.createTeilnehmer("ID" + ak + "-" + x, "Nachname " + x, "Vorname " + x, 2000, maennlich,
                "Gliederung", ak, "");
        if (wk.getRegelwerk().getAk(ak).isDisciplineChoiceAllowed()) {
            for (int d = 0; d < CHOSEN_DISCIPLINES; d++) {
                t.setDisciplineChoice(d, true);
            }
        }
        return t;
    }

    private int effectiveDisziplinAnzahl(int ak) {
        Altersklasse altersklasse = wk.getRegelwerk().getAk(ak);
        if (altersklasse.isDisciplineChoiceAllowed()) {
            return CHOSEN_DISCIPLINES;
        }
        return altersklasse.getDiszAnzahl();
    }

    private void addTeilnehmer(int ak, boolean maennlich, int count) {
        for (int x = 0; x < count; x++) {
            wk.addSchwimmer(createTeilnehmer(ak, maennlich, x));
        }
    }

    @Test
    void emptyWettkampfProducesEmptyLaufliste() {
        wk.getLaufliste().erzeugen();

        assertTrue(wk.getLaufliste().getLaufliste().isEmpty());
    }

    @Test
    void erzeugenWithReversedAufteilungReversesDisciplineOrder() {
        assertReversingAufteilungReversesDisciplineOrder(true, false);
    }

    @Test
    void erzeugenWithReversedAufteilungReversesDisciplineOrderForFemaleOnly() {
        assertReversingAufteilungReversesDisciplineOrder(false);
    }

    // Expected to fail: Laufliste.erzeugen's discipline-reordering math (the `r` array passed to
    // AWettkampf.reorderDisciplines) is derived solely from reihenfolge[sg][0] (the female
    // entries), then applied to BOTH genders' Disziplin arrays. With no female participants,
    // that female reihenfolge is never touched and stays at its identity default, so the
    // physical reorder is a no-op for male too - even though the male entries themselves were
    // reversed. Heat generation still uses the correct reversed mapping internally; only the
    // AK's own stored Disziplin order is left unreversed for a male-only population.
    @Test
    void erzeugenWithReversedAufteilungReversesDisciplineOrderForMaleOnly() {
        assertReversingAufteilungReversesDisciplineOrder(true);
    }

    // Laufliste.erzeugen permanently reorders each Altersklasse's own Disziplin array (via
    // AWettkampf.reorderDisciplines) to match the requested Einteilung order - it does not just
    // relabel heats. Reversing every entry's discipline index therefore reverses the AK's
    // discipline order itself, for every age group from AK 12 to AK Offen, for every gender with
    // participants, regardless of discipline choice settings. A gender with no participants gets
    // no entries in the standard distribution and is left untouched.
    private void assertReversingAufteilungReversesDisciplineOrder(boolean... genders) {
        int participantsPerGroup = 3;
        for (int ak : AK_12_TO_OFFEN) {
            for (boolean maennlich : genders) {
                for (int x = 0; x < participantsPerGroup; x++) {
                    wk.addSchwimmer(createTeilnehmer(ak, maennlich, x));
                }
            }
        }

        Map<Integer, Map<Boolean, String[]>> original = new HashMap<>();
        for (int ak : AK_12_TO_OFFEN) {
            Map<Boolean, String[]> perGender = new HashMap<>();
            for (boolean maennlich : genders) {
                perGender.put(maennlich, disciplineNames(ak, maennlich));
            }
            original.put(ak, perGender);
        }

        Laufliste<Teilnehmer> laufliste = wk.getLaufliste();
        Einteilung[] reversedAufteilung = reverseDisciplineOrder(laufliste.getStandardVerteilung());

        laufliste.erzeugen(reversedAufteilung);

        for (int ak : AK_12_TO_OFFEN) {
            for (boolean maennlich : genders) {
                assertArrayEquals(reverse(original.get(ak).get(maennlich)), disciplineNames(ak, maennlich),
                        "AK " + ak + (maennlich ? " male" : " female"));
            }
        }
    }

    private String[] disciplineNames(int ak, boolean maennlich) {
        Altersklasse altersklasse = wk.getRegelwerk().getAk(ak);
        int n = altersklasse.getDiszAnzahl();
        String[] names = new String[n];
        for (int x = 0; x < n; x++) {
            names[x] = altersklasse.getDisziplin(x, maennlich).getName();
        }
        return names;
    }

    private static String[] reverse(String[] values) {
        String[] result = values.clone();
        Collections.reverse(Arrays.asList(result));
        return result;
    }

    // Builds an Einteilung[] identical in entry order to `standard` but with every entry's
    // discipline index flipped (diszAnzahl-1-discipline) within its own (startgroup, gender)
    // group. Laufliste.erzeugen() assigns the NEW discipline index to each (startgroup, gender)
    // pair purely by the order its entries appear in the array - feeding it discipline indices
    // in descending order therefore reverses that age group's discipline order.
    private Einteilung[] reverseDisciplineOrder(Einteilung[] standard) {
        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();
        Einteilung[] result = new Einteilung[standard.length];
        for (int i = 0; i < standard.length; i++) {
            Einteilung e = standard[i];
            Altersklasse ak = wk.getRegelwerk().getAKsForStartgroup(startgruppen[e.getStartgruppe()]).getFirst();
            int n = ak.getDiszAnzahl();
            result[i] = new Einteilung(e.getStartgruppe(), e.isMaennlich(), n - 1 - e.getDisziplin());
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("teamCountsWithAgeGroupsAndGenders")
    void everyTeamIsScheduledInEveryDiscipline(int ak, boolean maennlich, int teamCount) {
        addTeilnehmer(ak, maennlich, teamCount);

        Laufliste<Teilnehmer> laufliste = wk.getLaufliste();
        laufliste.erzeugen();

        int disziplinAnzahl = effectiveDisziplinAnzahl(ak);
        int lanes = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        int heatsPerDiscipline = (teamCount + lanes - 1) / lanes;

        assertEquals(disziplinAnzahl * heatsPerDiscipline, laufliste.getLaufliste().size());

        Map<Teilnehmer, Integer> occurrences = new HashMap<>();
        for (Lauf<Teilnehmer> lauf : laufliste.getLaufliste()) {
            for (int lane = 0; lane < lauf.getBahnen(); lane++) {
                Teilnehmer t = lauf.getSchwimmer(lane);
                if (t != null) {
                    occurrences.merge(t, 1, Integer::sum);
                }
            }
        }

        assertEquals(teamCount, occurrences.size());
        for (int count : occurrences.values()) {
            assertEquals(disziplinAnzahl, count);
        }
    }

    @ParameterizedTest
    @MethodSource("gendersAndTeamCounts")
    void heatsAreOrderedByFixedMeldepunkte(boolean maennlich, int teamCount) {
        List<Teilnehmer> teams = new ArrayList<>();
        for (int x = 0; x < teamCount; x++) {
            Teilnehmer t = createTeilnehmer(AK_12, maennlich, x);
            t.setMeldepunkte(0, x);
            wk.addSchwimmer(t);
            teams.add(t);
        }
        // No explicit HEATS_SORTING_ORDER override: the "DLRG 2023" rulebook already
        // defines Meldepunkte as the laufsortierung for AK 12.

        Laufliste<Teilnehmer> laufliste = wk.getLaufliste();
        laufliste.erzeugen();

        // Meldesorter sorts descending by Meldepunkte, i.e. exactly reversed creation order here.
        List<Teilnehmer> expectedOrder = new ArrayList<>(teams);
        Collections.reverse(expectedOrder);

        int lanes = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        int disziplinAnzahl = effectiveDisziplinAnzahl(AK_12);

        Map<Integer, List<Lauf<Teilnehmer>>> byDiscipline = groupHeatsByDiscipline(laufliste.getLaufliste(), lanes);
        assertEquals(disziplinAnzahl, byDiscipline.size());

        // Meldepunkte order is established once and is not re-sorted per discipline, but
        // AK 12 also has laufrotation=true, which activates per-discipline lane rotation
        // whenever HEATS_SORTING_ORDER is left at its "follow the rulebook" default.
        for (Map.Entry<Integer, List<Lauf<Teilnehmer>>> entry : byDiscipline.entrySet()) {
            assertHeatsMatchPriorityOrder(entry.getValue(), expectedOrder, lanes, disziplinAnzahl, entry.getKey(),
                    true);
        }
    }

    @ParameterizedTest
    @MethodSource("gendersAndTeamCounts")
    void heatsAreOrderedByFixedMeldezeitenPerDiscipline(boolean maennlich, int teamCount) {
        List<Teilnehmer> teams = new ArrayList<>();
        for (int x = 0; x < teamCount; x++) {
            Teilnehmer t = createTeilnehmer(AK_OFFEN, maennlich, x);
            wk.addSchwimmer(t);
            teams.add(t);
        }

        int disziplinAnzahl = effectiveDisziplinAnzahl(AK_OFFEN);
        // Give every discipline its own ranking: team x has rank (x + d) % teamCount in discipline d.
        for (int x = 0; x < teamCount; x++) {
            for (int d = 0; d < disziplinAnzahl; d++) {
                int rank = (x + d) % teamCount;
                teams.get(x).setMeldezeit(d, 100 * (rank + 1));
            }
        }
        // No explicit HEATS_SORTING_ORDER override: the "DLRG 2023" rulebook already
        // defines Meldezeiten as the laufsortierung for AK Offen.

        Laufliste<Teilnehmer> laufliste = wk.getLaufliste();
        laufliste.erzeugen();

        int lanes = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);

        Map<Integer, List<Lauf<Teilnehmer>>> byDiscipline = groupHeatsByDiscipline(laufliste.getLaufliste(), lanes);
        assertEquals(disziplinAnzahl, byDiscipline.size());

        for (int d = 0; d < disziplinAnzahl; d++) {
            // MeldezeitenComparator sorts ascending by registered time, lowest (fastest) first.
            List<Teilnehmer> expectedOrder = new ArrayList<>();
            for (int rank = 0; rank < teamCount; rank++) {
                int x = ((rank - d) % teamCount + teamCount) % teamCount;
                expectedOrder.add(teams.get(x));
            }
            // AK Offen has laufrotation=false, so no lane rotation is applied.
            assertHeatsMatchPriorityOrder(byDiscipline.get(d), expectedOrder, lanes, disziplinAnzahl, d, false);
        }
    }

    // Groups heats by the discipline they were generated for, preserving their relative order.
    private Map<Integer, List<Lauf<Teilnehmer>>> groupHeatsByDiscipline(List<Lauf<Teilnehmer>> heats, int lanes) {
        Map<Integer, List<Lauf<Teilnehmer>>> result = new LinkedHashMap<>();
        for (Lauf<Teilnehmer> lauf : heats) {
            int disziplin = -1;
            for (int lane = 0; lane < lanes; lane++) {
                if (lauf.getSchwimmer(lane) != null) {
                    disziplin = lauf.getDisznummer(lane);
                    break;
                }
            }
            result.computeIfAbsent(disziplin, k -> new ArrayList<>()).add(lauf);
        }
        return result;
    }

    // Replicates Lauf's middle-out lane assignment: lanes fill from the centre outwards,
    // alternating sides, so the highest-priority entry of a heat lands in the most central lane.
    private static int[] laneAssignmentOrder(int lanes) {
        int[] order = new int[lanes];
        int pos = (lanes - 1) / 2;
        int mod = 0;
        for (int x = 0; x < lanes; x++) {
            if (mod > 0) {
                pos = pos + mod;
                mod = -mod - 1;
            } else {
                pos = pos + mod;
                mod = -mod + 1;
            }
            order[x] = pos;
        }
        return order;
    }

    // Verifies that heats for a single discipline match the heat-batching, lane-assignment and
    // (optionally) lane-rotation algorithm used by Laufliste.erzeugen(): teams are taken in
    // priorityOrder (highest priority first), filled into heats of `lanes` size, and those heats
    // appear in reverse fill order - the heat containing the lowest-priority teams comes first,
    // the heat with the highest-priority teams comes last. Within a heat, teams are seeded
    // centre-out (see laneAssignmentOrder). When rotationEnabled, each heat's occupants are then
    // cyclically rotated by an amount depending on its discipline and position - see Lauf.rotieren
    // and the call sites in Laufliste.startgruppeVerteilen.
    private void assertHeatsMatchPriorityOrder(List<Lauf<Teilnehmer>> heats, List<Teilnehmer> priorityOrder,
            int lanes, int disziplinAnzahl, int disziplin, boolean rotationEnabled) {
        List<List<Teilnehmer>> batches = new ArrayList<>();
        for (int i = 0; i < priorityOrder.size(); i += lanes) {
            batches.add(new ArrayList<>(priorityOrder.subList(i, Math.min(i + lanes, priorityOrder.size()))));
        }
        Collections.reverse(batches);

        assertEquals(batches.size(), heats.size());

        // Mirrors Laufliste.startgruppeVerteilen: bahnen1 is based on the discipline's total
        // team count, not on any individual heat's size.
        int bahnen1 = Math.min(priorityOrder.size(), lanes);
        int rotationsWeite = (bahnen1 == 4) ? 1 : 2;
        int anzahl = disziplinAnzahl - disziplin - 1;

        int[] laneOrder = laneAssignmentOrder(lanes);
        for (int h = 0; h < batches.size(); h++) {
            List<Teilnehmer> batch = batches.get(h);
            Lauf<Teilnehmer> lauf = heats.get(h);

            int[] occupiedLanes = Arrays.copyOf(laneOrder, batch.size());
            int[] sortedLanes = occupiedLanes.clone();
            Arrays.sort(sortedLanes);

            Map<Integer, Teilnehmer> rawByLane = new HashMap<>();
            for (int k = 0; k < batch.size(); k++) {
                rawByLane.put(occupiedLanes[k], batch.get(k));
            }
            List<Teilnehmer> valuesBySortedLane = new ArrayList<>();
            for (int lane : sortedLanes) {
                valuesBySortedLane.add(rawByLane.get(lane));
            }

            if (rotationEnabled) {
                int shift;
                if (batch.size() == 2) {
                    // Lauf.rotieren's 2-element branch performs exactly one swap per call,
                    // ignoring the weite magnitude entirely - net effect depends only on
                    // whether `anzahl` (the number of calls) is odd or even.
                    shift = anzahl;
                } else {
                    // heats.get(0) is the heat built last (batches were reversed above), which
                    // gets the post-loop rotation call using weite=1 instead of rotationsWeite
                    // when it has exactly 4 occupants; every other heat is rotated with the
                    // standard weite when the heat after it starts filling.
                    int weite = (h == 0 && batch.size() == 4) ? 1 : rotationsWeite;
                    shift = anzahl * weite;
                }
                valuesBySortedLane = rotateLeft(valuesBySortedLane, shift);
            }

            boolean[] used = new boolean[lanes];
            for (int i = 0; i < sortedLanes.length; i++) {
                int lane = sortedLanes[i];
                used[lane] = true;
                assertEquals(valuesBySortedLane.get(i), lauf.getSchwimmer(lane), "heat " + h + ", lane " + lane);
            }
            for (int lane = 0; lane < lanes; lane++) {
                if (!used[lane]) {
                    assertNull(lauf.getSchwimmer(lane), "heat " + h + ", lane " + lane);
                }
            }
        }
    }

    // Replicates Lauf.rotieren: a cyclic left-rotation, by `amount` steps, of the values sitting
    // in occupied lanes ordered by ascending lane index.
    private static List<Teilnehmer> rotateLeft(List<Teilnehmer> values, int amount) {
        int n = values.size();
        if (n == 0) {
            return values;
        }
        int shift = ((amount % n) + n) % n;
        List<Teilnehmer> result = new ArrayList<>(values.subList(shift, n));
        result.addAll(values.subList(0, shift));
        return result;
    }
}
