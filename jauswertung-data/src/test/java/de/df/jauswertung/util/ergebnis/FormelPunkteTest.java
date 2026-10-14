package de.df.jauswertung.util.ergebnis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

/**
 * Points of a single discipline. Expected values were computed independently from the published formulas.
 */
class FormelPunkteTest {

    private static final int REC = 6000;
    private static final double GENAU = 0.0001;

    private static void pruefe(FormelDLRG2007<ASchwimmer> formel, int zeit, double punkte, double mitStrafe) {
        assertEquals(punkte, formel.getResult(zeit, REC, Strafarten.NICHTS, 0), GENAU, "ohne Strafe");
        assertEquals(mitStrafe, formel.getResult(zeit, REC, Strafarten.STRAFPUNKTE, 50), GENAU, "50 Strafpunkte");
    }

    @ParameterizedTest
    @CsvSource({ "3000, 1650.25, 1600.25", "6000, 1000.00, 950.00", "7200, 805.28, 755.28", "9000, 583.25, 533.25",
            "12000, 400.00, 350.00", "15000, 333.33, 283.33", "18000, 266.67, 216.67", "24000, 133.33, 83.33",
            "29000, 22.22, 0", "30000, 0, 0", "36000, 0, 0" })
    void dlrg1999Und2007(int zeit, double punkte, double mitStrafe) {
        pruefe(new FormelDLRG2007<>(), zeit, punkte, mitStrafe);
        pruefe(new FormelDLRG<>(), zeit, punkte, mitStrafe);
    }

    @ParameterizedTest
    @CsvSource({ "3000, 1818.00, 1768.00", "6000, 1000.00, 950.00", "9000, 460.67, 410.67", "12000, 200.00, 150.00",
            "13500, 140.00, 90.00", "15000, 80.00, 30.00", "16500, 40.00, 0", "18000, 0, 0", "19000, 0, 0" })
    void deutschlandPokal(int zeit, double punkte, double mitStrafe) {
        pruefe(new FormelDP<>(), zeit, punkte, mitStrafe);
    }

    @ParameterizedTest
    @CsvSource({ "3000, 8000, 7950", "5000, 1728, 1678", "6000, 1000, 950", "9000, 296, 246", "12000, 125, 75" })
    void dsv(int zeit, double punkte, double mitStrafe) {
        pruefe(new FormelDSV<>(), zeit, punkte, mitStrafe);
    }

    @ParameterizedTest
    @CsvSource({ "3000, 1125.00, 1075.00", "6000, 1000.00, 950.00", "9000, 875.00, 825.00", "12000, 750.00, 700.00",
            "24000, 250.00, 200.00", "30000, 0, 0", "36000, 0, 0" })
    void linear(int zeit, double punkte, double mitStrafe) {
        pruefe(new FormelLinear<>(), zeit, punkte, mitStrafe);
    }

    @ParameterizedTest
    @CsvSource({ "1, 0.01, 0", "5025, 50.25, 0.25", "100000, 1000.00, 950.00" })
    void direktePunkte(int zeit, double punkte, double mitStrafe) {
        FormelDirectPoints<ASchwimmer> formel = new FormelDirectPoints<>();
        assertEquals(punkte, formel.getResult(zeit, REC, Strafarten.NICHTS, 0), GENAU, "ohne Strafe");
        assertEquals(punkte, formel.getResult(zeit, 0, Strafarten.NICHTS, 0), GENAU, "ohne Rec-Wert");
        assertEquals(mitStrafe, formel.getResult(zeit, REC, Strafarten.STRAFPUNKTE, 50), GENAU, "50 Strafpunkte");
    }

    @ParameterizedTest
    @EnumSource(value = Strafarten.class, names = { "NICHT_ANGETRETEN", "DISQUALIFIKATION", "AUSSCHLUSS" })
    void schwereStrafeGibtKeinePunkte(Strafarten art) {
        assertEquals(0, new FormelDLRG2007<>().getResult(6000, REC, art, 0), GENAU);
        assertEquals(0, new FormelDP<>().getResult(6000, REC, art, 0), GENAU);
        assertEquals(0, new FormelDLRG<>().getResult(6000, REC, art, 0), GENAU);
        assertEquals(0, new FormelDSV<>().getResult(6000, REC, art, 0), GENAU);
        assertEquals(0, new FormelLinear<>().getResult(6000, REC, art, 0), GENAU);
        assertEquals(0, new FormelDirectPoints<>().getResult(6000, REC, art, 0), GENAU);
    }

    @ParameterizedTest
    @CsvSource({ "0, 6000", "6000, 0" })
    void ohneZeitOderRecWertGibtEsKeinePunkte(int zeit, int rec) {
        assertEquals(0, new FormelDLRG2007<>().getResult(zeit, rec, Strafarten.NICHTS, 0), GENAU);
        assertEquals(0, new FormelDP<>().getResult(zeit, rec, Strafarten.NICHTS, 0), GENAU);
        assertEquals(0, new FormelDLRG<>().getResult(zeit, rec, Strafarten.NICHTS, 0), GENAU);
        assertEquals(0, new FormelDSV<>().getResult(zeit, rec, Strafarten.NICHTS, 0), GENAU);
        assertEquals(0, new FormelLinear<>().getResult(zeit, rec, Strafarten.NICHTS, 0), GENAU);
    }

    @Test
    void direktePunkteOhneZeit() {
        assertEquals(0, new FormelDirectPoints<>().getResult(0, REC, Strafarten.NICHTS, 0), GENAU);
    }
}
