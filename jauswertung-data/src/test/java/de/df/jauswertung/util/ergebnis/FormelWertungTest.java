package de.df.jauswertung.util.ergebnis;

import static de.df.jauswertung.util.ergebnis.Wertungsszenario.allgemeineStrafe;
import static de.df.jauswertung.util.ergebnis.Wertungsszenario.hlw;
import static de.df.jauswertung.util.ergebnis.Wertungsszenario.strafe;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

/**
 * Ranking of the time based formulas that share their ranking code. Each line of a result reads: name, place, points,
 * "kW" for no rating, number of disciplines with a severe penalty, then rank and points per discipline.
 * <p>
 * Pins the current behaviour: points closer than 0.01 count as equal, so a swimmer without rating (-0.0001 points)
 * shares the place of swimmers with 0 points and keeps the input order among them.
 */
class FormelWertungTest {

    private static final String[] FORMELN = Wertungsszenario.FORMELN;

    @ParameterizedTest
    @FieldSource("FORMELN")
    void reihenfolgeNachPunktsumme(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 1000, 400);
        sz.schwimmer("B", 805.28, 583.25);
        sz.schwimmer("C", 583.25, 1000);
        sz.schwimmer("D", 400, 805.28);

        assertEquals("""
                C 1 1583.2500 - 0 | 3 583.25 | 1 1000.00
                A 2 1400.0000 - 0 | 1 1000.00 | 4 400.00
                B 3 1388.5300 - 0 | 2 805.28 | 3 583.25
                D 4 1205.2800 - 0 | 4 400.00 | 2 805.28""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void gleichstandTeiltDenPlatz(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 1000, 400);
        sz.schwimmer("B", 1000, 400);
        sz.schwimmer("C", 805.28, 583.25);

        assertEquals("""
                A 1 1400.0000 - 0 | 1 1000.00 | 2 400.00
                B 1 1400.0000 - 0 | 1 1000.00 | 2 400.00
                C 3 1388.5300 - 0 | 3 805.28 | 1 583.25""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void ohneZeitKeinPlatzInDerDisziplin(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 1000, 0);
        sz.schwimmer("B", 805.28, 400);

        assertEquals("""
                B 1 1205.2800 - 0 | 2 805.28 | 1 400.00
                A 2 1000.0000 - 0 | 1 1000.00 | -1 0.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void strafenInEinerDisziplin(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        strafe(sz.schwimmer("A", 1000, 1000), 0, Strafarten.DISQUALIFIKATION, 0);
        strafe(sz.schwimmer("B", 1000, 1000), 1, Strafarten.NICHT_ANGETRETEN, 0);
        strafe(sz.schwimmer("C", 1000, 1000), 0, Strafarten.AUSSCHLUSS, 0);
        strafe(sz.schwimmer("D", 1000, 1000), 0, Strafarten.STRAFPUNKTE, 250);

        assertEquals("""
                D 1 1750.0000 - 0 | 2 750.00 | 1 1000.00
                A 2 1000.0000 - 1 | -1 0.00 | 1 1000.00
                B 2 1000.0000 - 1 | 1 1000.00 | -1 0.00
                C 4 -0.0001 kW 1 | -1 0.00 | 1 1000.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void beiGleichstandEntscheidetDieAnzahlSchwererStrafen(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        strafe(sz.schwimmer("A", 1000, 1000), 0, Strafarten.DISQUALIFIKATION, 0);
        sz.schwimmer("B", 0, 1000);

        assertEquals("""
                B 1 1000.0000 - 0 | -1 0.00 | 1 1000.00
                A 1 1000.0000 - 1 | -1 0.00 | 1 1000.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void allgemeineStrafen(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        allgemeineStrafe(sz.schwimmer("A", 1000, 1000), Strafarten.DISQUALIFIKATION, 0);
        allgemeineStrafe(sz.schwimmer("B", 805.28, 805.28), Strafarten.STRAFPUNKTE, 100);
        allgemeineStrafe(sz.schwimmer("C", 583.25, 583.25), Strafarten.NICHT_ANGETRETEN, 0);
        allgemeineStrafe(sz.schwimmer("D", 400, 400), Strafarten.AUSSCHLUSS, 0);
        allgemeineStrafe(sz.schwimmer("E", 400, 400), Strafarten.STRAFPUNKTE, 1000);

        assertEquals("""
                B 1 1510.5600 - 0 | 1 805.28 | 1 805.28
                A 2 0.0000 - 0 | 4 0.00 | 4 0.00
                C 2 0.0000 - 0 | 2 583.25 | 2 583.25
                D 2 -0.0001 kW 0 | 4 0.00 | 4 0.00
                E 2 0.0000 - 0 | 3 400.00 | 3 400.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void strafpunkteAlsDisqualifikation(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.ak().setStrafeIstDisqualifikation(true);
        allgemeineStrafe(sz.schwimmer("A", 1000, 1000), Strafarten.STRAFPUNKTE, 100);
        sz.schwimmer("B", 400, 400);
        strafe(sz.schwimmer("C", 1000, 1000), 0, Strafarten.STRAFPUNKTE, 50);

        assertEquals("""
                C 1 1000.0000 - 1 | -1 0.00 | 1 1000.00
                B 2 800.0000 - 0 | 1 400.00 | 2 400.00
                A 3 0.0000 - 0 | 2 0.00 | 3 0.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void zielrichterentscheidInEinerDisziplin(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        Teilnehmer a = sz.schwimmer("A", 1000, 400);
        Teilnehmer b = sz.schwimmer("B", 1000, 583.25);
        Teilnehmer c = sz.schwimmer("C", 1000, 266.67);
        sz.zielrichterentscheid(0, c, a, b);

        assertEquals("""
                B 1 1583.2500 - 0 | 3 1000.00 | 1 583.25
                A 2 1400.0000 - 0 | 2 1000.00 | 2 400.00
                C 3 1266.6700 - 0 | 1 1000.00 | 3 266.67""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void zielrichterentscheidBeiEinerDisziplinGiltAuchFuerDenPlatz(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 1);
        Teilnehmer a = sz.schwimmer("A", 1000);
        Teilnehmer b = sz.schwimmer("B", 1000);
        sz.schwimmer("C", 805.28);
        sz.zielrichterentscheid(0, b, a);

        assertEquals("""
                B 1 1000.0000 - 0 | 1 1000.00
                A 2 1000.0000 - 0 | 2 1000.00
                C 3 805.2800 - 0 | 3 805.28""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void nichtAusgewaehlteDisziplinZaehltNicht(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 1000, 1000);
        sz.schwimmer("B", 805.28, 1000);

        assertEquals("""
                A 1 1000.0000 - 0 | 1 1000.00 | -1 0.00
                B 2 805.2800 - 0 | 2 805.28 | -1 0.00""", sz.ergebnis(new boolean[] { true, false }, true));
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void diebestenGewaehltenDisziplinenZaehlen(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 3);
        sz.ak().setDisciplineChoiceAllowed(true);
        sz.ak().setChosenDisciplines(2, 2, 3);
        sz.schwimmer("A", 1000, 400, 583.25);
        Teilnehmer b = sz.schwimmer("B", 805.28, 0, 0);
        b.setDisciplineChoice(1, false);
        b.setDisciplineChoice(2, false);
        strafe(sz.schwimmer("C", 1000, 805.28, 0), 2, Strafarten.NICHT_ANGETRETEN, 0);

        assertEquals("""
                C 1 1805.2800 - 1 | 1 1000.00 | 1 805.28 | -1 0.00
                A 2 1583.2500 - 0 | 1 1000.00 | 2 400.00 | 1 583.25
                B 3 -0.0001 kW 0 | 3 805.28 | -1 0.00 | -1 0.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void zuVieleGewaehlteDisziplinen(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 3);
        sz.ak().setDisciplineChoiceAllowed(true);
        sz.ak().setChosenDisciplines(1, 1, 2);
        sz.schwimmer("A", 1000, 400, 583.25);
        sz.schwimmer("B", 805.28, 400, 0).setDisciplineChoice(2, false);

        assertEquals("""
                B 1 805.2800 - 0 | 2 805.28 | 1 400.00 | -1 0.00
                A 2 -0.0001 kW 0 | 1 1000.00 | 1 400.00 | 1 583.25""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void ueberallNichtAngetretenOhneWertung(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        Teilnehmer a = sz.schwimmer("A", 0, 0);
        strafe(a, 0, Strafarten.NICHT_ANGETRETEN, 0);
        strafe(a, 1, Strafarten.NICHT_ANGETRETEN, 0);
        sz.schwimmer("B", 400, 400);

        assertEquals("""
                B 1 800.0000 - 0 | 1 400.00 | 1 400.00
                A 2 -0.0001 kW 2 | -1 0.00 | -1 0.00""", sz.ergebnis());
    }

    private static Wertungsszenario mitHlw(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2, true);
        for (String name : new String[] { "A", "B", "C" }) {
            Teilnehmer t = sz.schwimmer(name, 0, 0);
            strafe(t, 0, Strafarten.NICHT_ANGETRETEN, 0);
            strafe(t, 1, Strafarten.NICHT_ANGETRETEN, 0);
            switch (name) {
            case "A" -> hlw(t, 200);
            case "B" -> hlw(t, HLWStates.NICHT_ANGETRETEN);
            default -> {
            }
            }
        }
        hlw(sz.schwimmer("D", 400, 400), 100);
        return sz;
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void hlwZaehltMitUndVerhindertKeineWertung(String formel) {
        assertEquals("""
                D 1 900.0000 - 0 | 1 400.00 | 1 400.00
                A 2 200.0000 - 2 | -1 0.00 | -1 0.00
                B 3 -0.0001 kW 2 | -1 0.00 | -1 0.00
                C 3 -0.0001 kW 2 | -1 0.00 | -1 0.00""", mitHlw(formel).ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void ohneZusatzwertungZaehltDieHlwNicht(String formel) {
        assertEquals("""
                D 1 800.0000 - 0 | 1 400.00 | 1 400.00
                A 2 0.0000 - 2 | -1 0.00 | -1 0.00
                B 2 -0.0001 kW 2 | -1 0.00 | -1 0.00
                C 2 -0.0001 kW 2 | -1 0.00 | -1 0.00""", mitHlw(formel).ergebnis(null, false));
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void ausserKonkurrenzStehtHintenUndBehaeltDenPlatzDavor(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 1000, 1000).setAusserKonkurrenz(true);
        sz.schwimmer("B", 805.28, 805.28);
        sz.schwimmer("C", 400, 400);
        sz.schwimmer("D", 400, 400).setAusserKonkurrenz(true);

        assertEquals("""
                B 1 1610.5600 - 0 | 2 805.28 | 2 805.28
                C 2 800.0000 - 0 | 3 400.00 | 3 400.00
                A 2 2000.0000 - 0 | 1 1000.00 | 1 1000.00
                D 2 800.0000 - 0 | 3 400.00 | 3 400.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void knappeAbstaendeWerdenNichtAlsGleichstandGewertet(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2);
        sz.schwimmer("A", 333.33, 333.33);
        sz.schwimmer("B", 400, 333.33);

        assertEquals("""
                B 1 733.3300 - 0 | 1 400.00 | 1 333.33
                A 2 666.6600 - 0 | 2 333.33 | 1 333.33""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void allgemeineDisqualifikationNimmtAuchDieHlwPunkte(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 2, true);
        Teilnehmer a = sz.schwimmer("A", 400, 400);
        hlw(a, 200);
        allgemeineStrafe(a, Strafarten.DISQUALIFIKATION, 0);
        hlw(sz.schwimmer("B", 266.67, 266.67), 0);

        assertEquals("""
                B 1 533.3400 - 0 | 1 266.67 | 1 266.67
                A 2 0.0000 - 0 | 2 0.00 | 2 0.00""", sz.ergebnis());
    }

    @ParameterizedTest
    @FieldSource("FORMELN")
    void ausserKonkurrenzStehtAuchInGrossenAltersklassenHinten(String formel) {
        Wertungsszenario sz = new Wertungsszenario(formel, 1);
        for (int x = 0; x < 40; x++) {
            Teilnehmer t = sz.schwimmer("S" + x, x % 2 == 0 ? 1000 : 400);
            t.setAusserKonkurrenz(x % 5 == 0);
        }

        String[] zeilen = sz.ergebnis().split("\n");
        for (int x = 0; x < zeilen.length; x++) {
            int nummer = Integer.parseInt(zeilen[x].substring(1, zeilen[x].indexOf(' ')));
            assertEquals(x >= 32, nummer % 5 == 0, zeilen[x]);
        }
    }
}
