package de.df.jauswertung.test.results.outdoorfinales;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
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

    private EinzelWettkampf wk;
    private OWSelection selectionVorlauf;
    private JResultTable ergebnisVorlauf;
    private OWSelection selectionFinale;
    private JResultTable ergebnisFinale;
    private JResultTable ergebnisGesamt;

    @BeforeEach
    void prepare() {
        erstelleWettkampf();
        schwimmerHinzufuegen(wk);

        selectionVorlauf = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, Discipline, Round);
        erzeugeLaufliste(wk, selectionVorlauf, 10);
        trageZeitenEin(wk, selectionVorlauf);
        ergebnisVorlauf = erstelleErgebnis(wk, selectionVorlauf);

        selectionFinale = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, Discipline, Round + 1);
        erzeugeLaufliste(wk, selectionFinale, 8);
        trageZeitenFinaleEin(wk, selectionFinale);
        ergebnisFinale = erstelleErgebnis(wk, selectionFinale);

        ergebnisGesamt = erstelleGesamtergebnis(wk);

        OutputManager.speichereWettkampf("FormelILSFinals.wk", wk);
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

    private static void trageZeitenEin(EinzelWettkampf wk, OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        int[] plaetze = new int[20];
        plaetze[0] = 1;
        plaetze[1] = 1;
        plaetze[2] = 2;
        plaetze[3] = 3;
        plaetze[4] = 2;
        plaetze[5] = 3;
        plaetze[6] = 4;
        plaetze[7] = 5;
        plaetze[8] = 6;
        plaetze[9] = 4;
        plaetze[10] = 5;
        plaetze[11] = 7;
        plaetze[12] = 6;
        plaetze[13] = 7;
        plaetze[14] = 8;
        plaetze[15] = 8;
        plaetze[16] = 9;
        plaetze[17] = 10;
        plaetze[18] = 9;
        plaetze[19] = 10;

        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(plaetze[x - 1]);
        }
    }

    private static void trageZeitenFinaleEin(EinzelWettkampf wk, OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        for (int x = 1; x <= 16; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(17 - x);
        }
        SearchUtils.getSchwimmer(wk, 8).getEingabe(selection.getId(), true).setZeit(17 - 9);
        SearchUtils.getSchwimmer(wk, 9).getEingabe(selection.getId(), true).setZeit(17 - 10);
        SearchUtils.getSchwimmer(wk, 10).getEingabe(selection.getId(), true).setZeit(17 - 8);
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
        assertEquals(AnzahlSchwimmer, wk.getSchwimmer().size());
    }

    @Test
    void lauflisteVorlauf() {
        OWDisziplin<Teilnehmer> disziplin = wk.getLauflisteOW().getDisziplin(selectionVorlauf.getId());

        LinkedList<OWLauf<Teilnehmer>> laufliste = disziplin.getLaeufe();
        assertEquals(2, laufliste.size());

        OWLauf<Teilnehmer> heat1 = laufliste.get(0);
        OWLauf<Teilnehmer> heat2 = laufliste.get(1);

        assertEquals(10, heat1.getAllSchwimmer().size());
        assertEquals(10, heat2.getAllSchwimmer().size());

        assertEquals(2, heat1.getSchwimmer(0).getStartnummer());
        assertEquals(8, heat1.getSchwimmer(1).getStartnummer());
        assertEquals(3, heat1.getSchwimmer(2).getStartnummer());
        assertEquals(17, heat1.getSchwimmer(3).getStartnummer());
        assertEquals(7, heat1.getSchwimmer(4).getStartnummer());
        assertEquals(4, heat1.getSchwimmer(5).getStartnummer());
        assertEquals(18, heat1.getSchwimmer(6).getStartnummer());
        assertEquals(12, heat1.getSchwimmer(7).getStartnummer());
        assertEquals(9, heat1.getSchwimmer(8).getStartnummer());
        assertEquals(15, heat1.getSchwimmer(9).getStartnummer());

        assertEquals(14, heat2.getSchwimmer(0).getStartnummer());
        assertEquals(13, heat2.getSchwimmer(1).getStartnummer());
        assertEquals(20, heat2.getSchwimmer(2).getStartnummer());
        assertEquals(11, heat2.getSchwimmer(3).getStartnummer());
        assertEquals(10, heat2.getSchwimmer(4).getStartnummer());
        assertEquals(19, heat2.getSchwimmer(5).getStartnummer());
        assertEquals(16, heat2.getSchwimmer(6).getStartnummer());
        assertEquals(5, heat2.getSchwimmer(7).getStartnummer());
        assertEquals(6, heat2.getSchwimmer(8).getStartnummer());
        assertEquals(1, heat2.getSchwimmer(9).getStartnummer());

    }

    @Test
    void ergebnisVorlauf() {
        assertEquals(AnzahlSchwimmer, ergebnisVorlauf.getRowCount());

        final String[] expected = { "Teilnehmer, 1", "Teilnehmer, 2", "Teilnehmer, 3", "Teilnehmer, 5", "Teilnehmer, 4",
                "Teilnehmer, 6", "Teilnehmer, 7", "Teilnehmer, 10", "Teilnehmer, 8", "Teilnehmer, 11", "Teilnehmer, 9",
                "Teilnehmer, 13", "Teilnehmer, 12", "Teilnehmer, 14", "Teilnehmer, 15", "Teilnehmer, 16",
                "Teilnehmer, 17", "Teilnehmer, 19", "Teilnehmer, 18", "Teilnehmer, 20" };
        String[] actual = new String[AnzahlSchwimmer];

        for (int x = 0; x < AnzahlSchwimmer; x++) {
            actual[x] = ergebnisVorlauf.getResult(x).getSchwimmer().getName();
            assertEquals(x < QualiPlaetze ? "Q" : "",
                    ergebnisVorlauf.getModel().getValueAt(x, ergebnisVorlauf.getModel().getColumnCount() - 1));
        }

        assertArrayEquals(expected, actual);
    }

    @Test
    void lauflisteFinale() {
        OWDisziplin<Teilnehmer> disziplin = wk.getLauflisteOW().getDisziplin(selectionFinale.getId());

        LinkedList<OWLauf<Teilnehmer>> laufliste = disziplin.getLaeufe();
        assertEquals(2, laufliste.size());

        OWLauf<Teilnehmer> bFinal = laufliste.get(0);
        OWLauf<Teilnehmer> aFinal = laufliste.get(1);

        assertEquals(8, bFinal.getAllSchwimmer().size());
        assertEquals(8, aFinal.getAllSchwimmer().size());

        String[] expectedA = new String[] { "Teilnehmer, 1", "Teilnehmer, 2", "Teilnehmer, 3", "Teilnehmer, 4",
                "Teilnehmer, 5", "Teilnehmer, 6", "Teilnehmer, 7", "Teilnehmer, 10" };
        String[] expectedB = new String[] { "Teilnehmer, 8", "Teilnehmer, 9", "Teilnehmer, 11", "Teilnehmer, 12",
                "Teilnehmer, 13", "Teilnehmer, 14", "Teilnehmer, 15", "Teilnehmer, 16" };

        Teilnehmer[] actualTeilnehmerA = new Teilnehmer[8];
        Teilnehmer[] actualTeilnehmerB = new Teilnehmer[8];

        for (int x = 0; x < 8; x++) {
            actualTeilnehmerA[x] = aFinal.getSchwimmer(x);
            actualTeilnehmerB[x] = bFinal.getSchwimmer(x);
        }

        Arrays.sort(actualTeilnehmerA, comparingInt(ASchwimmer::getStartnummer));
        Arrays.sort(actualTeilnehmerB, comparingInt(ASchwimmer::getStartnummer));

        assertArrayEquals(expectedA, stream(actualTeilnehmerA).map(Teilnehmer::getName).toArray());
        assertArrayEquals(expectedB, stream(actualTeilnehmerB).map(Teilnehmer::getName).toArray());
    }

    @Test
    void ergebnisFinale() {
        assertEquals(16, ergebnisFinale.getRowCount());

        assertEquals(String.format("Teilnehmer, 10"),
                ergebnisFinale.getResult(0).getSchwimmer().getName());
        for (int x = 0; x < 7; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1),
                    ergebnisFinale.getResult(7 - x).getSchwimmer().getName());
        }
        for (int x = 2; x < 8; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 9),
                    ergebnisFinale.getResult(15 - x).getSchwimmer().getName());
        }
        assertEquals(String.format("Teilnehmer, 9"),
                ergebnisFinale.getResult(14).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, 8"),
                ergebnisFinale.getResult(15).getSchwimmer().getName());
    }

    @Test
    void ergebnisGesamt() {
        assertEquals(20, ergebnisGesamt.getRowCount());

        assertEquals(String.format("Teilnehmer, 10"),
                ergebnisFinale.getResult(0).getSchwimmer().getName());
        for (int x = 0; x < 7; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1),
                    ergebnisFinale.getResult(7 - x).getSchwimmer().getName());
        }
        for (int x = 2; x < 8; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 9),
                    ergebnisFinale.getResult(15 - x).getSchwimmer().getName());
        }
        assertEquals(String.format("Teilnehmer, 9"),
                ergebnisFinale.getResult(14).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, 8"),
                ergebnisFinale.getResult(15).getSchwimmer().getName());

        String[] last = new String[AnzahlSchwimmer - 16];
        for (int x = 16; x < AnzahlSchwimmer; x++) {
            last[x - 16] = ergebnisGesamt.getResult(x).getSchwimmer().getName();
        }
        Arrays.sort(last);

        for (int x = 16; x < AnzahlSchwimmer; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1), last[x - 16]);
        }

    }
}
