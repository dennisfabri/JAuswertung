package de.df.jauswertung.test.results.outdoorfinales;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.FormelILSOutdoor;
import de.df.jutils.gui.jtable.JTableUtils;

public class FormelILSOutdoorFinalsTest {

    private static final int AnzahlSchwimmer = 20;
    private static final int QualiPlaetze = 16;

    private static final int AnzahlRunden = 2;
    private static final boolean MALE = true;
    private static final int AK = 0;
    private static final int Discipline = 0;
    private static final int Round = 0;

    // Places per heat and lane: every heat has the places 1 to its number of swimmers
    private static final int[] PLAETZE_VORLAUF = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    private static final int[] PLAETZE_FINALE = { 1, 2, 3, 4, 5, 6, 7, 8 };
    private static final int[][] VORLAUF_OHNE_GLEICHSTAND = { PLAETZE_VORLAUF, PLAETZE_VORLAUF };
    // First heat is the B final, second the A final
    private static final int[][] FINALE_OHNE_GLEICHSTAND = { PLAETZE_FINALE, PLAETZE_FINALE };

    private EinzelWettkampf wk;
    private OWSelection selectionVorlauf;
    private JResultTable ergebnisVorlauf;
    private OWSelection selectionFinale;
    private JResultTable ergebnisFinale;
    private JResultTable ergebnisGesamt;

    private void aufbauen(int[][] plaetzeVorlauf, int[][] plaetzeFinale) {
        erstelleWettkampf();
        schwimmerHinzufuegen(wk);

        selectionVorlauf = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, Discipline, Round);
        erzeugeLaufliste(wk, selectionVorlauf, 10);
        tragePlaetzeEin(wk, selectionVorlauf, plaetzeVorlauf);
        ergebnisVorlauf = erstelleErgebnis(wk, selectionVorlauf);

        selectionFinale = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, Discipline, Round + 1);
        erzeugeLaufliste(wk, selectionFinale, 8);
        tragePlaetzeEin(wk, selectionFinale, plaetzeFinale);
        ergebnisFinale = erstelleErgebnis(wk, selectionFinale);

        ergebnisGesamt = erstelleGesamtergebnis(wk);
    }

    private void erstelleWettkampf() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Ocean.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Ocean", true));
        stream(wk.getRegelwerk().getAks()).forEach(ak -> ak.setLaufsortierung(Reihenfolge.ILSOpenWater.getValue()));
    }

    private static void schwimmerHinzufuegen(EinzelWettkampf wk) {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, true, "Test", 0, "");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                t.setDisciplineChoice(y, y == Discipline);
            }
            t.setMeldezeit(0, x);
            wk.addSchwimmer(t);
            assertEquals(x, t.getStartnummer());
        }
    }

    private static void erzeugeLaufliste(EinzelWettkampf wk, OWSelection selection, int bahnen) {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            ak.getDisziplin(y, true).setRunden(new int[] { QualiPlaetze }, new int[] { 4 * y + 1, 4 * y + 2 });
            ak.getDisziplin(y, false).setRunden(new int[] { QualiPlaetze }, new int[] { 4 * y + 3, 4 * y + 4 });
        }

        EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);

        wkx.setProperty(HEATS_LANES, bahnen);
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

        wkx.getLaufliste().erzeugen(13);

        HeatsUtils.save(wk, selection, wkx);
    }

    /**
     * Enters the places of each heat, given per heat and lane.
     */
    private static void tragePlaetzeEin(EinzelWettkampf wk, OWSelection selection, int[][] plaetze) {
        LinkedList<OWLauf<Teilnehmer>> laeufe = wk.getLauflisteOW().<Teilnehmer> getDisziplin(selection.getId())
                .getLaeufe();
        assertEquals(plaetze.length, laeufe.size());
        for (int lauf = 0; lauf < plaetze.length; lauf++) {
            for (int bahn = 0; bahn < plaetze[lauf].length; bahn++) {
                laeufe.get(lauf).getSchwimmer(bahn).getEingabe(selection.getId(), true).setZeit(plaetze[lauf][bahn]);
            }
        }
    }

    private static JResultTable erstelleErgebnis(EinzelWettkampf wk, OWSelection t) {
        EinzelWettkampf wkx = createCompetitionFor(wk, t);
        boolean[][] selection = new boolean[2][wkx.getRegelwerk().size()];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < selection[y].length; x++) {
                selection[y][x] = (x == t.akNummer) && (y == (t.male ? 1 : 0));
            }
        }
        int qualification = 0;
        int[] runden = wkx.getRegelwerk().getAk(t.akNummer).getDisziplin(0, t.male).getRunden();
        if (runden.length > t.round) {
            qualification = runden[t.round];
        }

        return JResultTable.getResultTable(wkx, wkx.getRegelwerk().getAk(AK), MALE, false, false, qualification);
    }

    private static EinzelWettkampf createCompetitionFor(EinzelWettkampf wk, OWSelection t) {
        EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, t);
        if (!t.isFinal) {
            wkx.getRegelwerk().setFormelID(wk.getDataType() == DataType.RANK ? FormelILSOutdoor.ID : FormelILS.ID);
        }
        return wkx;
    }

    private static boolean isHeatBased() {
        return true;
    }

    private static JResultTable erstelleGesamtergebnis(EinzelWettkampf wk) {
        boolean isPartOnly = true;
        int round = 1;

        int index = AK;
        boolean maennlich = MALE;
        int discipline = Discipline;
        EinzelWettkampf wkx = wk;
        Altersklasse ak = wkx.getRegelwerk().getAk(index);
        int qualification = 0;
        boolean isFinal = false;
        boolean isCompleteDiscipline = false;
        if (isHeatBased()) {
            if (!isPartOnly) {
                ak = wkx.getRegelwerk().getAk(index);
                if (!ak.hasMehrkampfwertung()) {
                    wkx = Utils.copy(wk);
                    wkx.getRegelwerk().setFormelID(FormelILS.ID);
                    wkx.removeSchwimmer(wkx.getSchwimmer());
                    ak = wkx.getRegelwerk().getAk(index);
                }
            } else {
                // round = Round;
                isCompleteDiscipline = round + 1 == AnzahlRunden;
                if (round < 0) {
                    // Todo
                } else if (isCompleteDiscipline) {
                    wkx = (EinzelWettkampf) ResultUtils.generateEinzelwertungswettkampf(wkx, index, maennlich, false);
                    if (wkx == null) {
                        return null;
                    }
                    index = discipline;
                    ak = wkx.getRegelwerk().getAk(discipline);
                    discipline = 0;
                } else {
                    isFinal = round == ak.getDisziplin(discipline, maennlich).getRunden().length;
                    OWSelection t = new OWSelection(ak, index, maennlich, discipline, round);
                    wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wkx, t);
                    if (wkx == null) {
                        return null;
                    }
                    if (!isFinal) {
                        wkx.getRegelwerk().setFormelID(FormelILS.ID);
                    }
                    ak = wkx.getRegelwerk().getAk(index);
                    discipline = 0;
                    int[] runden = ak.getDisziplin(discipline, maennlich).getRunden();
                    if (round < runden.length) {
                        qualification = runden[round];
                    }
                }
            }
        }
        JResultTable result = null;
        int anzahl = ak.getDiszAnzahl();
        boolean hlw = ak.hasHLW();
        boolean einzel = wkx instanceof EinzelWettkampf;
        if ((result == null)
                || (JResultTable.getColumnCount(anzahl, hlw, einzel, qualification) != result.getColumnCount())) {
            result = JResultTable.getResultTable(ak, anzahl, hlw, wkx instanceof EinzelWettkampf, false, qualification,
                    wk.getRegelwerk().getZusatzwertungShort());
            if (anzahl == 1 && !hlw) {
                JTableUtils.hideColumnAndRemoveData(result,
                        JResultTable.PREFIX + JResultTable.D_POINTS_OFFSET + (einzel ? 1 : 0));
                if (qualification > 0) {
                    JTableUtils.hideColumnAndRemoveData(result, JResultTable.DIFF_OFFSET + (einzel ? 1 : 0));
                    JTableUtils.hideColumnAndRemoveData(result, JResultTable.SCORE_OFFSET + (einzel ? 1 : 0));
                }
            }
        }
        result.updateResult(wkx, ak, maennlich, null, ak.hasHLW(), qualification);
        return result;
    }

    @Test
    void anzahlTeilnehmer() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertEquals(AnzahlSchwimmer, wk.getSchwimmer().size());
    }

    @Test
    void lauflisteVorlauf() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertArrayEquals(new int[][] { { 20, 7, 9, 16, 14, 18, 12, 15, 19, 4 }, { 11, 6, 5, 3, 13, 17, 10, 8, 1, 2 } },
                bahnen(selectionVorlauf));
    }

    @Test
    void ergebnisVorlauf() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertEquals(plaetze(11, 1, 20, 1, 6, 3, 7, 3, 5, 5, 9, 5, 3, 7, 16, 7, 13, 9, 14, 9, 17, 11, 18, 11, 10, 13, 12,
                13, 8, 15, 15, 15, 1, 17, 19, 17, 2, 19, 4, 19), plaetze(ergebnisVorlauf));
        assertQualifiziert(ergebnisVorlauf);
    }

    @Test
    void lauflisteFinale() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertArrayEquals(new int[][] { { 8, 12, 17, 13, 14, 18, 10, 15 }, { 3, 9, 6, 11, 20, 7, 5, 16 } },
                bahnen(selectionFinale));
    }

    @Test
    void ergebnisFinale() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertEquals(plaetze(3, 1, 9, 2, 6, 3, 11, 4, 20, 5, 7, 6, 5, 7, 16, 8, 8, 9, 12, 10, 17, 11, 13, 12, 14, 13, 18,
                14, 10, 15, 15, 16), plaetze(ergebnisFinale));
    }

    @Test
    void ergebnisGesamt() {
        aufbauen(VORLAUF_OHNE_GLEICHSTAND, FINALE_OHNE_GLEICHSTAND);

        assertEquals(plaetze(3, 1, 9, 2, 6, 3, 11, 4, 20, 5, 7, 6, 5, 7, 16, 8, 8, 9, 12, 10, 17, 11, 13, 12, 14, 13, 18,
                14, 10, 15, 15, 16, 1, 17, 19, 17, 2, 19, 4, 19), plaetze(ergebnisGesamt));
    }

    @Test
    void gleichstandImVorlaufUeberspringtDenNaechstenPlatz() {
        // Lanes 2 and 3 of heat 1 share place 2, place 3 is skipped
        aufbauen(new int[][] { { 1, 2, 2, 4, 5, 6, 7, 8, 9, 10 }, PLAETZE_VORLAUF }, FINALE_OHNE_GLEICHSTAND);

        assertEquals(plaetze(11, 1, 20, 1, 6, 3, 7, 3, 9, 3, 5, 6, 3, 7, 16, 7, 13, 9, 14, 9, 17, 11, 18, 11, 10, 13, 12,
                13, 8, 15, 15, 15, 1, 17, 19, 17, 2, 19, 4, 19), plaetze(ergebnisVorlauf));
        assertQualifiziert(ergebnisVorlauf);
        assertArrayEquals(new int[][] { { 8, 10, 12, 13, 14, 15, 17, 18 }, { 3, 5, 6, 7, 9, 11, 16, 20 } },
                finalisten(selectionFinale));
    }

    @Test
    void mehrereGleichstaendeInVorlaufUndFinale() {
        // Heat 1: places 2 and 6 shared; heat 2: places 1 and 9 shared
        // B final: place 2 shared; A final: place 4 shared
        aufbauen(new int[][] { { 1, 2, 2, 4, 5, 6, 6, 8, 9, 10 }, { 1, 1, 3, 4, 5, 6, 7, 8, 9, 9 } },
                new int[][] { { 1, 2, 2, 4, 5, 6, 7, 8 }, { 1, 2, 3, 4, 4, 6, 7, 8 } });

        assertEquals(plaetze(6, 1, 11, 1, 20, 1, 7, 4, 9, 4, 5, 6, 3, 7, 16, 7, 13, 9, 14, 9, 12, 11, 17, 11, 18, 11, 10,
                14, 8, 15, 15, 15, 1, 17, 2, 17, 19, 17, 4, 20), plaetze(ergebnisVorlauf));
        assertQualifiziert(ergebnisVorlauf);
        assertArrayEquals(new int[][] { { 8, 10, 12, 13, 14, 15, 17, 18 }, { 3, 5, 6, 7, 9, 11, 16, 20 } },
                finalisten(selectionFinale));
        assertEquals(plaetze(3, 1, 7, 2, 20, 3, 6, 4, 11, 4, 9, 6, 5, 7, 16, 8, 8, 9, 12, 10, 18, 10, 13, 12, 14, 13, 17,
                14, 10, 15, 15, 16), plaetze(ergebnisFinale));
        assertEquals(plaetze(3, 1, 7, 2, 20, 3, 6, 4, 11, 4, 9, 6, 5, 7, 16, 8, 8, 9, 12, 10, 18, 10, 13, 12, 14, 13, 17,
                14, 10, 15, 15, 16, 1, 17, 2, 17, 19, 17, 4, 20), plaetze(ergebnisGesamt));
    }

    /**
     * Start numbers per heat in lane order.
     */
    private int[][] bahnen(OWSelection selection) {
        return wk.getLauflisteOW().<Teilnehmer> getDisziplin(selection.getId()).getLaeufe().stream()
                .map(lauf -> IntStream.range(0, lauf.getBahnen()).map(b -> lauf.getSchwimmer(b).getStartnummer())
                        .toArray())
                .toArray(int[][]::new);
    }

    /**
     * Start numbers per heat, sorted.
     */
    private int[][] finalisten(OWSelection selection) {
        return Arrays.stream(bahnen(selection)).map(lauf -> IntStream.of(lauf).sorted().toArray()).toArray(int[][]::new);
    }

    /**
     * Place per swimmer, keyed by the number in the name: the overall ranking is built from copies with new start
     * numbers.
     */
    private static Map<Integer, Integer> plaetze(JResultTable ergebnis) {
        Map<Integer, Integer> result = new TreeMap<>();
        for (int x = 0; x < ergebnis.getRowCount(); x++) {
            Teilnehmer t = (Teilnehmer) ergebnis.getResult(x).getSchwimmer();
            result.put(Integer.parseInt(t.getVorname()), ergebnis.getResult(x).getPlace());
        }
        return result;
    }

    private static Map<Integer, Integer> plaetze(int... schwimmerUndPlatz) {
        Map<Integer, Integer> result = new TreeMap<>();
        for (int x = 0; x < schwimmerUndPlatz.length; x += 2) {
            result.put(schwimmerUndPlatz[x], schwimmerUndPlatz[x + 1]);
        }
        return result;
    }

    private static void assertQualifiziert(JResultTable ergebnis) {
        for (int x = 0; x < ergebnis.getRowCount(); x++) {
            assertEquals(x < QualiPlaetze ? "Q" : "",
                    ergebnis.getModel().getValueAt(x, ergebnis.getModel().getColumnCount() - 1));
        }
    }
}
