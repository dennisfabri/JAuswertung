package de.df.jauswertung.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.BestTimeUtils;
import de.df.jauswertung.util.BestTimeUtils.BestTime;

class BestTimeUtilsTest {

    private static final int AK = 0;
    private static final boolean MALE = true;
    private static final int DISCIPLINE = 0;

    private EinzelWettkampf wk;

    @BeforeEach
    void prepare() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        ak.getDisziplin(DISCIPLINE, MALE).setRunden(new int[] { 8 }, new int[] { 1, 2 });
    }

    @Test
    void heatBasedCompetitionWithTimesHasBestTimes() {
        add("Anna", 0, 6000);
        assertTrue(wk.isHeatBased());
        assertTrue(wk.hasBestTimes());
    }

    @Test
    void fastestValidTimeOfAllRounds() {
        Teilnehmer finalist = add("Berta", 0, 6000);
        time(finalist, 1, 5900);
        Teilnehmer penalized = add("Clara", 0, 5800);
        penalized.addStrafe(id(0), new Strafe("Strafe", "S", Strafarten.STRAFPUNKTE, 50));
        time(penalized, 1, 5950);
        add("Anna", 0, 5900);
        Teilnehmer excluded = add("Dora", 0, 5000);
        excluded.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                new Strafe("Ausschluss", "A", Strafarten.AUSSCHLUSS, 0));
        add("Emma", 0, 0);

        BestTime<Teilnehmer> best = BestTimeUtils.getBestTimeOverAllRounds(wk, AK, MALE, DISCIPLINE);

        assertEquals(5900, best.time());
        assertEquals(List.of("Anna", "Berta"), best.swimmers().stream().map(Teilnehmer::getVorname).toList());
    }

    @Test
    void noBestTimeWithoutValidTimes() {
        Teilnehmer s = add("Anna", 0, 6000);
        s.addStrafe(id(0), new Strafe("Disqualifikation", "DQ", Strafarten.DISQUALIFIKATION, 0));
        add("Berta", 0, 0);

        assertNull(BestTimeUtils.getBestTimeOverAllRounds(wk, AK, MALE, DISCIPLINE));
        assertNull(BestTimeUtils.getBestTimeOverAllRounds(wk, AK, !MALE, DISCIPLINE));
    }

    private Teilnehmer add(String vorname, int round, int zeit) {
        Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", vorname, 2000, MALE, "Test", AK, "");
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            t.setDisciplineChoice(y, y == DISCIPLINE);
        }
        wk.addSchwimmer(t);
        time(t, round, zeit);
        return t;
    }

    private static void time(Teilnehmer t, int round, int zeit) {
        t.getEingabe(id(round), true).setZeit(zeit);
    }

    private static String id(int round) {
        return OWDisziplin.getId(AK, MALE, DISCIPLINE, round);
    }
}
