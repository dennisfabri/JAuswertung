package de.df.jauswertung.test.daten.schwimmer;

import static de.df.jauswertung.daten.ASchwimmer.DISCIPLINE_NUMBER_SELF;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.FormelILSFinals;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

/**
 * Ein allgemeiner Ausschluss nach den Läufen lässt die Läufe aller Runden stehen, nimmt dem Teilnehmer aber die
 * Punkte der Disziplin.
 */
class AllgemeinerAusschlussTest {

    private static final int AK = 0;
    private static final boolean MALE = true;
    private static final int BAHNEN = 8;

    private EinzelWettkampf wk;

    private void erzeugeWettkampf(int anzahl, int... qualifizierte) {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        wk.getRegelwerk().setFormelID(FormelILSFinals.ID);
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        int[] ids = new int[qualifizierte.length + 1];
        for (int x = 0; x < ids.length; x++) {
            ids[x] = x + 1;
        }
        ak.getDisziplin(0, true).setRunden(qualifizierte, ids);
        for (int x = 1; x <= anzahl; x++) {
            Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, MALE, "Test", AK, "");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                t.setDisciplineChoice(y, y == 0);
            }
            t.setMeldezeit(0, x);
            wk.addSchwimmer(t);
        }
    }

    private OWSelection runde(int runde) {
        return new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, 0, runde);
    }

    /**
     * Erzeugt die Laufliste der Runde und schwimmt sie: Startnummer x schwimmt die Zeit basis + x.
     */
    private void schwimme(OWSelection runde, int basis) {
        AWettkampf<Teilnehmer> wkx = ResultUtils.createCompetitionFor(wk, runde);
        wkx.setProperty(HEATS_LANES, BAHNEN);
        wkx.getLaufliste().erzeugen();
        HeatsUtils.save(wk, runde, wkx);
        for (Teilnehmer t : wkx.getSchwimmer()) {
            schwimmer(t.getStartnummer()).getEingabe(runde.getId(), true).setZeit(basis + t.getStartnummer());
        }
    }

    private Teilnehmer schwimmer(int startnummer) {
        return SearchUtils.getSchwimmer(wk, startnummer);
    }

    private List<List<List<Integer>>> laeufe(OWSelection... runden) {
        List<List<List<Integer>>> result = new ArrayList<>();
        for (OWSelection runde : runden) {
            List<List<Integer>> laeufe = new ArrayList<>();
            for (Lauf<Teilnehmer> lauf : ResultUtils.createCompetitionFor(wk, runde).getLaufliste().getLaufliste()) {
                List<Integer> bahnen = new ArrayList<>();
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    Teilnehmer t = lauf.getSchwimmer(x);
                    bahnen.add(t == null ? 0 : t.getStartnummer());
                }
                laeufe.add(bahnen);
            }
            result.add(laeufe);
        }
        return result;
    }

    private boolean startet(OWSelection runde, int startnummer) {
        return SearchUtils.getSchwimmer(ResultUtils.createCompetitionFor(wk, runde), startnummer) != null;
    }

    private void assertTeilnehmerUnveraendert(OWSelection... runden) {
        for (OWSelection runde : runden) {
            assertTrue(HeatsUtils.pruefeTeilnehmer(wk, runde).isEmpty());
        }
    }

    private double punkte(int startnummer) {
        for (SchwimmerResult<Teilnehmer> r : ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(AK), MALE)) {
            if (r.getSchwimmer().getStartnummer() == startnummer) {
                return r.getResults()[0].getPoints();
            }
        }
        throw new IllegalArgumentException("Startnummer " + startnummer + " fehlt im Ergebnis.");
    }

    @Test
    void ausschlussNachDemFinaleLaesstDieLaeufeStehenUndNimmtDiePunkte() {
        erzeugeWettkampf(10, 8);
        OWSelection vorlauf = runde(0);
        OWSelection finale = runde(1);
        schwimme(vorlauf, 6000);
        schwimme(finale, 5000);
        int finalist = 3;
        List<List<List<Integer>>> laeufe = laeufe(vorlauf, finale);
        assertTrue(startet(finale, finalist));
        assertTrue(punkte(finalist) > 0);

        schwimmer(finalist).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

        assertEquals(laeufe, laeufe(vorlauf, finale));
        assertTeilnehmerUnveraendert(finale);
        assertEquals(0.0, punkte(finalist));
    }

    @Test
    void ausschlussNachDemBFinaleLaesstDieLaeufeStehenUndNimmtDiePunkte() {
        erzeugeWettkampf(20, 16);
        OWSelection vorlauf = runde(0);
        OWSelection finale = runde(1);
        schwimme(vorlauf, 6000);
        schwimme(finale, 5000);
        int imBFinale = 11;
        List<List<List<Integer>>> laeufe = laeufe(vorlauf, finale);
        List<List<Integer>> finalLaeufe = laeufe.get(1);
        assertEquals(2, finalLaeufe.size());
        assertTrue(finalLaeufe.getFirst().contains(imBFinale));
        assertTrue(punkte(imBFinale) > 0);

        schwimmer(imBFinale).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

        assertEquals(laeufe, laeufe(vorlauf, finale));
        assertTeilnehmerUnveraendert(finale);
        assertEquals(0.0, punkte(imBFinale));
    }

    @Test
    void ausschlussOhneDurchgefuehrtesBFinaleNimmtDiePunkteAusDemZwischenlauf() {
        erzeugeWettkampf(20, 16, 4);
        OWSelection vorlauf = runde(0);
        OWSelection zwischenlauf = runde(1);
        OWSelection finale = runde(2);
        schwimme(vorlauf, 6000);
        schwimme(zwischenlauf, 5000);
        schwimme(finale, 4000);
        assertEquals(2, ResultUtils.createCompetitionFor(wk, zwischenlauf).getLaufliste().getLaufliste().size());
        int imBFinale = 6;
        List<List<List<Integer>>> laeufe = laeufe(vorlauf, zwischenlauf, finale);
        assertTrue(startet(zwischenlauf, imBFinale));
        assertFalse(startet(finale, imBFinale));
        assertTrue(punkte(imBFinale) > 0);

        schwimmer(imBFinale).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

        assertEquals(laeufe, laeufe(vorlauf, zwischenlauf, finale));
        assertTeilnehmerUnveraendert(zwischenlauf, finale);
        assertEquals(0.0, punkte(imBFinale));
    }
}
