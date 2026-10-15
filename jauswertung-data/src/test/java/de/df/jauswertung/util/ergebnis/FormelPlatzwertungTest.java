package de.df.jauswertung.util.ergebnis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Discipline ranking of the formulas that give points by place. Each line: name, rank, points.
 */
class FormelPlatzwertungTest {

    @Test
    void sercWertetHoehereWerteZuerst() {
        Wertungsszenario sz = new Wertungsszenario(FormelILSSERC.ID, 1);
        sz.schwimmerMitZeiten("A", 3);
        sz.schwimmerMitZeiten("B", 1);
        sz.schwimmerMitZeiten("C", 2);
        sz.schwimmerMitZeiten("D", 2);

        assertEquals("""
                A 1 20.00
                B 4 14.00
                C 2 18.00
                D 2 18.00""", sz.disziplin(0));
    }
}
