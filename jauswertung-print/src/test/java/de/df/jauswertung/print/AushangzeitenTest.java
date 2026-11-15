package de.df.jauswertung.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AushangzeitenTest {

    private static final LocalDateTime JETZT = LocalDateTime.of(2026, 11, 15, 14, 0);

    @Test
    void aushangIstJetztPlusVersatz() {
        Aushangzeiten zeiten = Aushangzeiten.ab(JETZT, 15);

        assertEquals(LocalDateTime.of(2026, 11, 15, 14, 15), zeiten.aushang());
    }

    @Test
    void einspruchsfristEndetDreissigMinutenNachAushang() {
        Aushangzeiten zeiten = Aushangzeiten.ab(JETZT, 15);

        assertEquals(LocalDateTime.of(2026, 11, 15, 14, 45), zeiten.endeDerEinspruchsfrist());
    }

    @Test
    void zeitenWerdenAlsStundenUndMinutenGedruckt() {
        Aushangzeiten zeiten = Aushangzeiten.ab(LocalDateTime.of(2026, 11, 15, 8, 5, 42), 0);

        assertEquals("08:05", zeiten.aushangText());
        assertEquals("08:35", zeiten.endeDerEinspruchsfristText());
    }
}
