package de.df.jauswertung.test.daten.schwimmer;

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
import static org.junit.Assert.assertEquals;

import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.FormelILSOutdoor;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.MultiplePrintable;

public class FormelILSFinalsTest {

    private static final int AnzahlSchwimmer = 20;

    private static final boolean MALE = true;
    private static final int AK = 0;
    private static final int Discipline = 0;
    private static final int Round = 0;

    private EinzelWettkampf wk;
    private Altersklasse ak;
    private OWSelection selectionVorlauf;
    private JResultTable ergebnisVorlauf;
    private OWSelection selectionFinale;
    private JResultTable ergebnisFinale;

    private static SchwimmerResult<Teilnehmer>[] results = null;

    @BeforeEach
    void prepare() {
        erstelleWettkampf();
        schwimmerHinzufuegen();

        selectionVorlauf = new OWSelection(ak, AK, MALE, Discipline, Round, false);
        erzeugeLaufliste(selectionVorlauf);
        trageZeitenEin(selectionVorlauf);
        ergebnisVorlauf = erstelleErgebnis(selectionVorlauf);

        selectionFinale = new OWSelection(ak, AK, MALE, Discipline, Round + 1, true);
        erzeugeLaufliste(selectionFinale);
        trageZeitenFinaleEin(selectionFinale);
        ergebnisFinale = erstelleErgebnis(selectionFinale);
    }

    private void erstelleWettkampf() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        ak = wk.getRegelwerk().getAk(AK);
    }

    private void schwimmerHinzufuegen() {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            Teilnehmer t = wk.createTeilnehmer("Teilnehmer", "" + x, 2000, true, "Test", 0, "");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                t.setDisciplineChoice(y, y == Discipline);
            }
            t.setMeldezeit(0, x);
            wk.addSchwimmer(t);
        }
    }

    private void erzeugeLaufliste(OWSelection selection) {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            ak.getDisziplin(y, true).setRunden(new int[] { 16 }, new int[] { 4 * y + 1, 4 * y + 2 });
            ak.getDisziplin(y, false).setRunden(new int[] { 16 }, new int[] { 4 * y + 3, 4 * y + 4 });
        }

        EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);

        wkx.setProperty(HEATS_LANES, 8);
        wkx.setProperty(HEATS_FIRST_HEAT, 1);
        wkx.setProperty(HEATS_SORTING_ORDER, Laufliste.REIHENFOLGE_REGELWERK);

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

        save(wkx, selection);
    }

    private void save(EinzelWettkampf wkx, OWSelection t) {
        Laufliste<Teilnehmer> heats = wkx.getLaufliste();
        OWDisziplin<Teilnehmer> disziplin = wk.getLauflisteOW().getDisziplin(t.akNummer, t.male, t.discipline, t.round);
        if (disziplin == null) {
            disziplin = wk.getLauflisteOW().addDisziplin(t.akNummer, t.male, t.discipline, t.round);
        }
        disziplin.laeufe.clear();
        for (Lauf<Teilnehmer> lauf : heats.getLaufliste()) {
            OWLauf<Teilnehmer> l = new OWLauf<Teilnehmer>(wk, disziplin.Id, lauf);
            for (int x = 0; x < l.getBahnen(); x++) {
                Teilnehmer tx = l.getSchwimmer(x);
                if (tx != null) {
                    Teilnehmer ti = SearchUtils.getSchwimmer(wk, tx);
                    disziplin.Schwimmer.add(ti);
                }
            }
            disziplin.laeufe.add(l);
        }
        wk.setProperty(PropertyConstants.HEATS_SORTING_ORDER,
                wkx.getIntegerProperty(PropertyConstants.HEATS_SORTING_ORDER, Laufliste.REIHENFOLGE_REGELWERK));
    }

    private void trageZeitenEin(OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(6000 + x);
        }
    }

    private void trageZeitenFinaleEin(OWSelection selection) {
        wk.getLauflisteOW().getDisziplin(selection);
        for (int x = 1; x <= AnzahlSchwimmer; x++) {
            SearchUtils.getSchwimmer(wk, x).getEingabe(selection.getId(), true).setZeit(6000 - x);
        }
    }

    private JResultTable erstelleErgebnis(OWSelection t) {
        EinzelWettkampf wkx = createCompetitionFor(t);
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

        return JResultTable.getResultTable(wk, ak, MALE, false, false, qualification);
    }

    private EinzelWettkampf createCompetitionFor(OWSelection t) {
        EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, t);
        if (!t.isFinal) {
            wkx.getRegelwerk().setFormelID(wk.getDataType() == DataType.RANK ? FormelILSOutdoor.ID : FormelILS.ID);
        }
        return wkx;
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
        for (int x = 0; x < AnzahlSchwimmer; x++) {
            assertEquals(x + 1, ergebnisVorlauf.getResult(x).getSchwimmer().getStartnummer());
        }
    }

    @Test
    void lauflisteFinale() {
        OWDisziplin<Teilnehmer> disziplin = wk.getLauflisteOW().getDisziplin(selectionFinale.getId());

        LinkedList<OWLauf<Teilnehmer>> laufliste = disziplin.getLaeufe();
        assertEquals(2, laufliste.size());
        assertEquals(8, laufliste.get(0).getAllSchwimmer().size());
        assertEquals(8, laufliste.get(1).getAllSchwimmer().size());

        assertEquals(1, laufliste.get(1).getSchwimmer(3).getStartnummer());
        assertEquals(2, laufliste.get(1).getSchwimmer(4).getStartnummer());
        assertEquals(3, laufliste.get(1).getSchwimmer(2).getStartnummer());
        assertEquals(4, laufliste.get(1).getSchwimmer(5).getStartnummer());
        assertEquals(5, laufliste.get(1).getSchwimmer(1).getStartnummer());
        assertEquals(6, laufliste.get(1).getSchwimmer(6).getStartnummer());
        assertEquals(7, laufliste.get(1).getSchwimmer(0).getStartnummer());
        assertEquals(8, laufliste.get(1).getSchwimmer(7).getStartnummer());

        assertEquals(9, laufliste.get(0).getSchwimmer(3).getStartnummer());
        assertEquals(10, laufliste.get(0).getSchwimmer(4).getStartnummer());
        assertEquals(11, laufliste.get(0).getSchwimmer(2).getStartnummer());
        assertEquals(12, laufliste.get(0).getSchwimmer(5).getStartnummer());
        assertEquals(13, laufliste.get(0).getSchwimmer(1).getStartnummer());
        assertEquals(14, laufliste.get(0).getSchwimmer(6).getStartnummer());
        assertEquals(15, laufliste.get(0).getSchwimmer(0).getStartnummer());
        assertEquals(16, laufliste.get(0).getSchwimmer(7).getStartnummer());
    }

    @Test
    void ergebnisFinale() {

        for (int x = 0; x < 8; x++) {
            assertEquals(x + 1, ergebnisFinale.getResult(7 - x).getSchwimmer().getStartnummer());
        }
        for (int x = 0; x < 8; x++) {
            assertEquals(x + 9, ergebnisFinale.getResult(15 - x).getSchwimmer().getStartnummer());
        }
    }

}
