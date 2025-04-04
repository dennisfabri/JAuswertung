package de.df.jauswertung.test.results.indoorfinals;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_AAE_FASTEST_HEAT_UNTOUCHED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_AVOID_ALMOST_EMPTY;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_EMPTY_LIST;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_FIRST_HEAT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_JOIN_HEATS;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_MIXED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_MIXED_IN_FRONT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_NOT_COMPETING_MIXED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_REGISTERED_POINTS_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_RESPECT_QUALIFICATIONS;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_ROTATE;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_SORTING_ORDER;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;

import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.util.ergebnis.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jutils.gui.jtable.JTableUtils;

public class FormelILSFinalsDisqualificationTest {

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
        erzeugeLaufliste(wk, selectionVorlauf);
        trageZeitenEin(wk, selectionVorlauf);
        ergebnisVorlauf = erstelleErgebnis(wk, selectionVorlauf);

        selectionFinale = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, Discipline, Round + 1);
        erzeugeLaufliste(wk, selectionFinale);
        trageZeitenFinaleEin(wk, selectionFinale);
        ergebnisFinale = erstelleErgebnis(wk, selectionFinale);

        ergebnisGesamt = erstelleGesamtergebnis(wk);

        OutputManager.speichereWettkampf("FormelILSFinalsDisqualification.wk", wk);
    }

    private void erstelleWettkampf() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        stream(wk.getRegelwerk().getAks()).forEach(ak -> ak.setLaufsortierung(Reihenfolge.ILSPool.getValue()));
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

    private static void erzeugeLaufliste(EinzelWettkampf wk, OWSelection selection) {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            ak.getDisziplin(y, true).setRunden(new int[] { QualiPlaetze }, new int[] { 4 * y + 1, 4 * y + 2 });
            ak.getDisziplin(y, false).setRunden(new int[] { QualiPlaetze }, new int[] { 4 * y + 3, 4 * y + 4 });
        }

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
        // wk.setProperty(HEATS_LANE_SELECTION, lanes.getLaneSelection());

        wkx.setProperty(HEATS_REGISTERED_POINTS_INDEX, 0);

        // wk.setProperty(DSM_MODE_DATA, moving.getData());
        wkx.getLaufliste().erzeugen();

        HeatsUtils.save(wk, selection, wkx);
    }

    private static void trageZeitenEin(EinzelWettkampf wk, OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(6000 + x);
        }
    }

    private static void trageZeitenFinaleEin(EinzelWettkampf wk, OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        for (int x = 1; x <= 16; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(6000 - x);
        }
        SearchUtils.getSchwimmer(wk, 2).getEingabe(selection.getId(), true)
                .addStrafe(new Strafe("Disqualification Test 1", "DQ1", Strafarten.DISQUALIFIKATION, 0));
        SearchUtils.getSchwimmer(wk, 11).getEingabe(selection.getId(), true)
                .addStrafe(new Strafe("Disqualification Test 2", "DQ2", Strafarten.DISQUALIFIKATION, 0));

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
        assertEquals(3, laufliste.size());
        assertEquals(6, laufliste.get(0).getAllSchwimmer().size());
        assertEquals(7, laufliste.get(1).getAllSchwimmer().size());
        assertEquals(7, laufliste.get(2).getAllSchwimmer().size());

        assertEquals(1, laufliste.get(2).getSchwimmer(3).getStartnummer());
        assertEquals(2, laufliste.get(1).getSchwimmer(3).getStartnummer());
        assertEquals(3, laufliste.get(0).getSchwimmer(3).getStartnummer());

        assertEquals(4, laufliste.get(2).getSchwimmer(4).getStartnummer());
        assertEquals(5, laufliste.get(1).getSchwimmer(4).getStartnummer());
        assertEquals(6, laufliste.get(0).getSchwimmer(4).getStartnummer());

        assertEquals(7, laufliste.get(2).getSchwimmer(2).getStartnummer());
        assertEquals(8, laufliste.get(1).getSchwimmer(2).getStartnummer());
        assertEquals(9, laufliste.get(0).getSchwimmer(2).getStartnummer());

        assertEquals(10, laufliste.get(2).getSchwimmer(5).getStartnummer());
        assertEquals(11, laufliste.get(1).getSchwimmer(5).getStartnummer());
        assertEquals(12, laufliste.get(0).getSchwimmer(5).getStartnummer());

        assertEquals(13, laufliste.get(2).getSchwimmer(1).getStartnummer());
        assertEquals(14, laufliste.get(1).getSchwimmer(1).getStartnummer());
        assertEquals(15, laufliste.get(0).getSchwimmer(1).getStartnummer());

        assertEquals(16, laufliste.get(2).getSchwimmer(6).getStartnummer());
        assertEquals(17, laufliste.get(1).getSchwimmer(6).getStartnummer());
        assertEquals(18, laufliste.get(0).getSchwimmer(6).getStartnummer());

        assertEquals(19, laufliste.get(2).getSchwimmer(0).getStartnummer());
        assertEquals(20, laufliste.get(1).getSchwimmer(0).getStartnummer());
    }

    @Test
    void ergebnisVorlauf() {
        assertEquals(AnzahlSchwimmer, ergebnisVorlauf.getRowCount());

        for (int x = 0; x < AnzahlSchwimmer; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1), ergebnisVorlauf.getResult(x).getSchwimmer().getName());
            assertEquals(x < QualiPlaetze ? "Q" : "",
                    ergebnisVorlauf.getModel().getValueAt(x, ergebnisVorlauf.getModel().getColumnCount() - 1));
        }
    }

    @Test
    void lauflisteFinale() {
        OWDisziplin<Teilnehmer> disziplin = wk.getLauflisteOW().getDisziplin(selectionFinale.getId());

        LinkedList<OWLauf<Teilnehmer>> laufliste = disziplin.getLaeufe();
        assertEquals(2, laufliste.size());

        OWLauf<Teilnehmer> finalB = laufliste.get(0);
        OWLauf<Teilnehmer> finalA = laufliste.get(1);

        assertEquals(8, finalB.getAllSchwimmer().size());
        assertEquals(8, finalA.getAllSchwimmer().size());

        assertEquals(1, finalA.getSchwimmer(3).getStartnummer());
        assertEquals(2, finalA.getSchwimmer(4).getStartnummer());
        assertEquals(3, finalA.getSchwimmer(2).getStartnummer());
        assertEquals(4, finalA.getSchwimmer(5).getStartnummer());
        assertEquals(5, finalA.getSchwimmer(1).getStartnummer());
        assertEquals(6, finalA.getSchwimmer(6).getStartnummer());
        assertEquals(7, finalA.getSchwimmer(0).getStartnummer());
        assertEquals(8, finalA.getSchwimmer(7).getStartnummer());

        assertEquals(9, finalB.getSchwimmer(3).getStartnummer());
        assertEquals(10, finalB.getSchwimmer(4).getStartnummer());
        assertEquals(11, finalB.getSchwimmer(2).getStartnummer());
        assertEquals(12, finalB.getSchwimmer(5).getStartnummer());
        assertEquals(13, finalB.getSchwimmer(1).getStartnummer());
        assertEquals(14, finalB.getSchwimmer(6).getStartnummer());
        assertEquals(15, finalB.getSchwimmer(0).getStartnummer());
        assertEquals(16, finalB.getSchwimmer(7).getStartnummer());
    }

    @Test
    void ergebnisAFinalePlatzierungen() {
        assertEquals(16, ergebnisFinale.getRowCount());

        assertEquals(String.format("Teilnehmer, %d", 2), ergebnisFinale.getResult(7).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 1), ergebnisFinale.getResult(6).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 3), ergebnisFinale.getResult(5).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 4), ergebnisFinale.getResult(4).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 5), ergebnisFinale.getResult(3).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 6), ergebnisFinale.getResult(2).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 7), ergebnisFinale.getResult(1).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 8), ergebnisFinale.getResult(0).getSchwimmer().getName());
    }

    @Test
    void ergebnisAFinalePunkte() {
        assertEquals(16, ergebnisFinale.getRowCount());

        assertEquals(10, ergebnisFinale.getResult(7).getPoints());
        assertEquals(11, ergebnisFinale.getResult(6).getPoints());
        assertEquals(12, ergebnisFinale.getResult(5).getPoints());
        assertEquals(13, ergebnisFinale.getResult(4).getPoints());
        assertEquals(14, ergebnisFinale.getResult(3).getPoints());
        assertEquals(16, ergebnisFinale.getResult(2).getPoints());
        assertEquals(18, ergebnisFinale.getResult(1).getPoints());
        assertEquals(20, ergebnisFinale.getResult(0).getPoints());
    }

    @Test
    void ergebnisBFinalePlatzierungen() {
        assertEquals(16, ergebnisFinale.getRowCount());

        assertEquals(String.format("Teilnehmer, %d", 11), ergebnisFinale.getResult(15).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 9), ergebnisFinale.getResult(14).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 10), ergebnisFinale.getResult(13).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 12), ergebnisFinale.getResult(12).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 13), ergebnisFinale.getResult(11).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 14), ergebnisFinale.getResult(10).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 15), ergebnisFinale.getResult(9).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 16), ergebnisFinale.getResult(8).getSchwimmer().getName());
    }

    @Test
    void ergebnisBFinalePunkte() {
        assertEquals(16, ergebnisFinale.getRowCount());

        assertEquals(1, ergebnisFinale.getResult(15).getPoints());
        assertEquals(2, ergebnisFinale.getResult(14).getPoints());
        assertEquals(3, ergebnisFinale.getResult(13).getPoints());
        assertEquals(4, ergebnisFinale.getResult(12).getPoints());
        assertEquals(5, ergebnisFinale.getResult(11).getPoints());
        assertEquals(6, ergebnisFinale.getResult(10).getPoints());
        assertEquals(7, ergebnisFinale.getResult(9).getPoints());
        assertEquals(8, ergebnisFinale.getResult(8).getPoints());
    }

    @Test
    void ergebnisGesamt() {
        assertEquals(20, ergebnisGesamt.getRowCount());

        // Aus A-Finale
        assertEquals(String.format("Teilnehmer, %d", 2), ergebnisGesamt.getResult(7).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 1), ergebnisGesamt.getResult(6).getSchwimmer().getName());
        for (int x = 2; x < 8; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1),
                    ergebnisGesamt.getResult(7 - x).getSchwimmer().getName());
        }

        // Aus B-Finale
        assertEquals(String.format("Teilnehmer, %d", 9), ergebnisGesamt.getResult(14).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 10), ergebnisGesamt.getResult(13).getSchwimmer().getName());
        assertEquals(String.format("Teilnehmer, %d", 11), ergebnisGesamt.getResult(15).getSchwimmer().getName());
        for (int x = 3; x < 8; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 9),
                    ergebnisGesamt.getResult(15 - x).getSchwimmer().getName());
        }

        // Aus Vorlauf
        for (int x = 16; x < AnzahlSchwimmer; x++) {
            assertEquals(String.format("Teilnehmer, %d", x + 1), ergebnisGesamt.getResult(x).getSchwimmer().getName());
        }
    }
}
