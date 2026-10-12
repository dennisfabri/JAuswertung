package de.df.jauswertung.test.daten.schwimmer;

import static de.df.jauswertung.daten.ASchwimmer.DISCIPLINE_NUMBER_SELF;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

class StartetNichtTest {

    @Nested
    class Becken {

        private static final int AK_OFFEN = 4;

        private Teilnehmer t;

        @BeforeEach
        void setup() {
            EinzelWettkampf wk = new EinzelWettkampf(
                    AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 2023", true));
            t = wk.createTeilnehmer("ID", "Nachname", "Vorname", 2000, true, "Gliederung", AK_OFFEN, "");
            t.setDisciplineChoice(0, true);
            t.setDisciplineChoice(1, true);
            wk.addSchwimmer(t);
        }

        @Test
        void ohneStrafeStartetJeder() {
            assertFalse(t.startetNicht(0));
            assertFalse(t.startetNicht(1));
        }

        @Test
        void ausschlussInEinerDisziplinGiltNurFuerDieseDisziplin() {
            t.addStrafe(1, Strafe.AUSSCHLUSS);

            assertFalse(t.startetNicht(0));
            assertTrue(t.startetNicht(1));
        }

        @Test
        void allgemeinerAusschlussGiltFuerAlleDisziplinen() {
            t.addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

            assertTrue(t.startetNicht(0));
            assertTrue(t.startetNicht(1));
        }

        @Test
        void abmeldungInEinerDisziplinGiltNurFuerDieseDisziplin() {
            t.addStrafe(1, Strafe.WITHDRAW);

            assertFalse(t.startetNicht(0));
            assertTrue(t.startetNicht(1));
        }

        @Test
        void abmeldungVorDemErstenStartInEinerDisziplinGiltNurFuerDieseDisziplin() {
            t.addStrafe(1, Strafe.WITHDRAW_BEFORE_FIRST_START);

            assertFalse(t.startetNicht(0));
            assertTrue(t.startetNicht(1));
        }

        @Test
        void allgemeineAbmeldungGiltFuerAlleDisziplinen() {
            t.addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.WITHDRAW);

            assertTrue(t.startetNicht(0));
            assertTrue(t.startetNicht(1));
        }

        @Test
        void ausschlussGiltAuchFuerDisziplinMitZeit() {
            t.setZeit(1, 6000);
            t.addStrafe(1, Strafe.AUSSCHLUSS);

            assertTrue(t.startetNicht(1));
        }

        @Test
        void andereStrafenZaehlenNicht() {
            t.addStrafe(0, Strafe.DISQUALIFIKATION);
            t.addStrafe(1, Strafe.NICHT_ANGETRETEN);
            t.addStrafe(DISCIPLINE_NUMBER_SELF, new Strafe(10));

            assertFalse(t.startetNicht(0));
            assertFalse(t.startetNicht(1));
        }
    }

    @Nested
    class Runden {

        private static final int AK = 0;
        private static final boolean MALE = true;
        private static final int ANZAHL = 10;

        private EinzelWettkampf wk;
        private OWSelection disziplin0;
        private OWSelection disziplin1;

        @BeforeEach
        void setup() {
            wk = new EinzelWettkampf(
                    AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
            Altersklasse ak = wk.getRegelwerk().getAk(AK);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                ak.getDisziplin(y, true).setRunden(new int[] { 8 }, new int[] { 4 * y + 1, 4 * y + 2 });
                ak.getDisziplin(y, false).setRunden(new int[] { 8 }, new int[] { 4 * y + 3, 4 * y + 4 });
            }
            for (int x = 1; x <= ANZAHL; x++) {
                Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, MALE, "Test", AK, "");
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    t.setDisciplineChoice(y, y < 2);
                }
                t.setMeldezeit(0, x);
                t.setMeldezeit(1, x);
                wk.addSchwimmer(t);
            }
            disziplin0 = new OWSelection(ak, AK, MALE, 0, 0);
            disziplin1 = new OWSelection(ak, AK, MALE, 1, 0);
            erzeugeLaufliste(disziplin0);
            erzeugeLaufliste(disziplin1);
        }

        private void erzeugeLaufliste(OWSelection selection) {
            AWettkampf<Teilnehmer> wkx = ResultUtils.createCompetitionFor(wk, selection);
            wkx.setProperty(HEATS_LANES, 8);
            wkx.getLaufliste().erzeugen();
            HeatsUtils.save(wk, selection, wkx);
        }

        private Map<Integer, Boolean> startetNichtInLaufliste(OWSelection selection) {
            AWettkampf<Teilnehmer> wkx = ResultUtils.createCompetitionFor(wk, selection);
            Map<Integer, Boolean> result = new TreeMap<>();
            for (Lauf<Teilnehmer> lauf : wkx.getLaufliste().getLaufliste()) {
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    Teilnehmer t = lauf.getSchwimmer(x);
                    if (t != null) {
                        result.put(t.getStartnummer(), t.startetNicht(lauf.getDisznummer(x)));
                    }
                }
            }
            assertEquals(ANZAHL, result.size());
            return result;
        }

        private Teilnehmer schwimmer(int startnummer) {
            return SearchUtils.getSchwimmer(wk, startnummer);
        }

        @Test
        void ohneStrafeStartetJeder() {
            assertFalse(startetNichtInLaufliste(disziplin0).containsValue(true));
        }

        @Test
        void abmeldungInEinerRundeGiltNurFuerDieseRunde() {
            schwimmer(3).addStrafe(disziplin1.getId(), Strafe.WITHDRAW);

            assertFalse(startetNichtInLaufliste(disziplin0).get(3));
            assertTrue(startetNichtInLaufliste(disziplin1).get(3));
            assertFalse(startetNichtInLaufliste(disziplin1).get(4));
        }

        @Test
        void abmeldungVorDemErstenStartGiltNurFuerDieseRunde() {
            schwimmer(3).addStrafe(disziplin1.getId(), Strafe.WITHDRAW_BEFORE_FIRST_START);

            assertFalse(startetNichtInLaufliste(disziplin0).get(3));
            assertTrue(startetNichtInLaufliste(disziplin1).get(3));
        }

        @Test
        void allgemeineAbmeldungGiltFuerAlleRunden() {
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.WITHDRAW);

            assertTrue(startetNichtInLaufliste(disziplin0).get(3));
            assertTrue(startetNichtInLaufliste(disziplin1).get(3));
        }

        @Test
        void ausschlussInEinerRundeGiltNurFuerDieseRunde() {
            schwimmer(3).addStrafe(disziplin0.getId(), Strafe.AUSSCHLUSS);

            assertTrue(startetNichtInLaufliste(disziplin0).get(3));
            assertFalse(startetNichtInLaufliste(disziplin1).get(3));
            assertFalse(startetNichtInLaufliste(disziplin0).get(4));
        }

        @Test
        void rundenkopieEnthaeltDieAllgemeinenStrafen() {
            schwimmer(3).addStrafe(disziplin0.getId(), Strafe.DISQUALIFIKATION);
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, new Strafe(10));

            AWettkampf<Teilnehmer> wkx = ResultUtils.createCompetitionFor(wk, disziplin0);
            Teilnehmer kopie = SearchUtils.getSchwimmer(wkx, 3);

            assertEquals(List.of(Strafe.DISQUALIFIKATION, new Strafe(10)), kopie.getStrafen(0));
            assertTrue(kopie.getAllgemeineStrafen().isEmpty());
        }

        @Test
        void allgemeinerAusschlussNachEinteilungInDieFolgerundeLaesstDasErgebnisDerRundeStehen() {
            OWSelection finale = schwimmeVorlaufUndErzeugeFinale();
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

            assertEquals(List.of(), rundenkopie(disziplin0, 3).getStrafen(0));
            assertEquals(List.of(Strafe.AUSSCHLUSS), rundenkopie(finale, 3).getStrafen(0));
        }

        @Test
        void allgemeineStrafenNachEinteilungInDieFolgerundeLassenDasErgebnisDerRundeStehen() {
            OWSelection finale = schwimmeVorlaufUndErzeugeFinale();
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, new Strafe(10));

            assertEquals(List.of(), rundenkopie(disziplin0, 3).getStrafen(0));
            assertEquals(List.of(new Strafe(10)), rundenkopie(finale, 3).getStrafen(0));
        }

        @Test
        void allgemeinerAusschlussOhneEinteilungInDieFolgerundeGiltFuerDieRunde() {
            schwimmeVorlaufUndErzeugeFinale();
            schwimmer(ANZAHL).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

            assertEquals(List.of(Strafe.AUSSCHLUSS), rundenkopie(disziplin0, ANZAHL).getStrafen(0));
        }

        @Test
        void allgemeinerAusschlussVorDerEinteilungDerFolgerundeGiltFuerDieRunde() {
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

            assertEquals(List.of(Strafe.AUSSCHLUSS), rundenkopie(disziplin0, 3).getStrafen(0));
        }

        private OWSelection schwimmeVorlaufUndErzeugeFinale() {
            for (int x = 1; x <= ANZAHL; x++) {
                schwimmer(x).getEingabe(disziplin0.getId(), true).setZeit(6000 + x);
            }
            OWSelection finale = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, 0, 1);
            erzeugeLaufliste(finale);
            assertTrue(wk.getLauflisteOW().getDisziplin(finale).getSchwimmer().contains(schwimmer(3)));
            assertFalse(wk.getLauflisteOW().getDisziplin(finale).getSchwimmer().contains(schwimmer(ANZAHL)));
            return finale;
        }

        private Teilnehmer rundenkopie(OWSelection selection, int startnummer) {
            return SearchUtils.getSchwimmer(ResultUtils.createCompetitionFor(wk, selection), startnummer);
        }

        @Test
        void allgemeinerAusschlussGiltFuerAlleRunden() {
            schwimmer(3).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.AUSSCHLUSS);

            assertTrue(startetNichtInLaufliste(disziplin0).get(3));
            assertTrue(startetNichtInLaufliste(disziplin1).get(3));
        }
    }
}
