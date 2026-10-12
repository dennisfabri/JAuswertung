package de.df.jauswertung.test.daten.schwimmer;

import static de.df.jauswertung.daten.ASchwimmer.DISCIPLINE_NUMBER_SELF;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.FormelILSFinals;
import de.df.jauswertung.util.ergebnis.FormelILSOutdoorFinals;

/**
 * Drei Vorläufe mit je zehn Teilnehmern; die Platzierung im Vorlauf entspricht der Bahn.
 */
class NachrueckerTest {

    private static final int AK = 0;
    private static final boolean MALE = true;
    private static final int LAEUFE = 3;
    private static final int BAHNEN = 10;

    private EinzelWettkampf wk;
    private OWSelection vorlauf;
    private OWSelection zwischenlauf;
    /** Startnummern je Vorlauf, nach Bahn sortiert. */
    private List<List<Integer>> laeufe;

    @BeforeEach
    void setup() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        wk.getRegelwerk().setFormelID(FormelILSOutdoorFinals.ID);
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int x = 1; x <= LAEUFE * BAHNEN; x++) {
            Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, MALE, "Test", AK, "");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                t.setDisciplineChoice(y, y == 0);
            }
            t.setMeldezeit(0, x);
            wk.addSchwimmer(t);
        }
        qualifizierte(27);
        vorlauf = new OWSelection(ak, AK, MALE, 0, 0);
        zwischenlauf = new OWSelection(ak, AK, MALE, 0, 1);
        erzeugeLaufliste(vorlauf);

        laeufe = new ArrayList<>();
        for (OWLauf<Teilnehmer> lauf : wk.getLauflisteOW().getDisziplin(vorlauf).getLaeufe()) {
            List<Integer> lauf1 = new ArrayList<>();
            for (int bahn = 0; bahn < lauf.getBahnen(); bahn++) {
                lauf1.add(lauf.getSchwimmer(bahn).getStartnummer());
            }
            laeufe.add(lauf1);
        }
        assertEquals(LAEUFE, laeufe.size());
        for (List<Integer> lauf : laeufe) {
            for (int platz = 1; platz <= BAHNEN; platz++) {
                schwimmer(lauf, platz).getEingabe(vorlauf.getId(), true).setZeit(platz);
            }
        }
    }

    private void qualifizierte(int anzahl) {
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        ak.getDisziplin(0, true).setRunden(new int[] { anzahl }, new int[] { 1, 2 });
        ak.getDisziplin(0, false).setRunden(new int[] { anzahl }, new int[] { 3, 4 });
    }

    private void erzeugeLaufliste(OWSelection selection) {
        AWettkampf<Teilnehmer> wkx = ResultUtils.createCompetitionFor(wk, selection);
        wkx.setProperty(HEATS_LANES, BAHNEN);
        wkx.getLaufliste().erzeugen();
        HeatsUtils.save(wk, selection, wkx);
    }

    private Teilnehmer schwimmer(List<Integer> lauf, int platz) {
        return SearchUtils.getSchwimmer(wk, lauf.get(platz - 1));
    }

    private Set<Integer> plaetze(int lauf, int bis) {
        return laeufe.get(lauf).subList(0, bis).stream().collect(Collectors.toCollection(TreeSet::new));
    }

    private Set<Integer> erwartet(int bis1, int bis2, int bis3) {
        Set<Integer> result = new TreeSet<>(plaetze(0, bis1));
        result.addAll(plaetze(1, bis2));
        result.addAll(plaetze(2, bis3));
        return result;
    }

    /** Die Ersten jedes Laufs, im Lauf {@code lauf} rückt der 10. für den 9. nach. */
    private Set<Integer> mitNachrueckerAus(int lauf) {
        int[] bis = { 9, 9, 9 };
        bis[lauf] = 10;
        Set<Integer> result = erwartet(bis[0], bis[1], bis[2]);
        result.remove(laeufe.get(lauf).get(8));
        return result;
    }

    private Set<Integer> qualifiziert() {
        return ResultUtils.getQualifizierte(wk, zwischenlauf).stream().map(ASchwimmer::getStartnummer)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Set<Integer> imZwischenlauf() {
        return ResultUtils.createCompetitionFor(wk, zwischenlauf).getSchwimmer().stream()
                .map(ASchwimmer::getStartnummer).collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    void ausJedemLaufQualifizierenSichDieErsten() {
        assertEquals(erwartet(9, 9, 9), qualifiziert());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void abmeldenNachErzeugenDerLauflisteIstUnwirksam(int lauf) {
        erzeugeLaufliste(zwischenlauf);
        schwimmer(laeufe.get(lauf), 9).addStrafe(vorlauf.getId(), Strafe.WITHDRAW);

        assertEquals(erwartet(9, 9, 9), imZwischenlauf());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void nachrueckerBeimErstenErzeugenDerFolgerunde(int lauf) {
        schwimmer(laeufe.get(lauf), 9).addStrafe(vorlauf.getId(), Strafe.WITHDRAW);

        assertEquals(mitNachrueckerAus(lauf), imZwischenlauf());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void allgemeineAbmeldungVorDerEinteilungMachtDenPlatzFrei(int lauf) {
        schwimmer(laeufe.get(lauf), 9).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.WITHDRAW);

        assertEquals(mitNachrueckerAus(lauf), imZwischenlauf());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void allgemeineAbmeldungNachEinteilungInDieFolgerundeAendertDieQualifikationNicht(int lauf) {
        erzeugeLaufliste(zwischenlauf);
        schwimmer(laeufe.get(lauf), 9).addStrafe(DISCIPLINE_NUMBER_SELF, Strafe.WITHDRAW);

        assertEquals(erwartet(9, 9, 9), qualifiziert());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void erschoepfterLaufLaesstPlaetzeFrei(int lauf) {
        schwimmer(laeufe.get(lauf), 9).addStrafe(vorlauf.getId(), Strafe.WITHDRAW);
        schwimmer(laeufe.get(lauf), 10).addStrafe(vorlauf.getId(), Strafe.DISQUALIFIKATION);

        int[] bis = { 9, 9, 9 };
        bis[lauf] = 8;
        assertEquals(erwartet(bis[0], bis[1], bis[2]), qualifiziert());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void gleichePlatzierungAmSchnittQualifiziertBeide(int lauf) {
        schwimmer(laeufe.get(lauf), 10).getEingabe(vorlauf.getId()).setZeit(9);

        int[] bis = { 9, 9, 9 };
        bis[lauf] = 10;
        assertEquals(erwartet(bis[0], bis[1], bis[2]), qualifiziert());
    }
    @Test
    void ungeradeAnzahlQualifiziertJeLaufAufgerundet() {
        qualifizierte(20);

        assertEquals(erwartet(7, 7, 7), qualifiziert());
    }

    @Test
    void zeitbasierteWertungRueckeDenNaechstSchnellstenNach() {
        wk.getRegelwerk().setFormelID(FormelILSFinals.ID);
        int zeit = 6000;
        List<Integer> nachZeit = new ArrayList<>();
        for (int platz = 1; platz <= BAHNEN; platz++) {
            for (List<Integer> lauf : laeufe) {
                schwimmer(lauf, platz).getEingabe(vorlauf.getId()).setZeit(zeit++);
                nachZeit.add(lauf.get(platz - 1));
            }
        }
        // Der Schnellste aus Lauf 3 fällt aus, der 28. der Gesamtwertung (Platz 10 aus Lauf 1) rückt nach.
        schwimmer(laeufe.get(2), 1).addStrafe(vorlauf.getId(), Strafe.WITHDRAW);

        Set<Integer> expected = new TreeSet<>(nachZeit.subList(0, 28));
        expected.remove(laeufe.get(2).get(0));
        assertEquals(expected, qualifiziert());
    }
}
