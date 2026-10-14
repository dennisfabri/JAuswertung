package de.df.jauswertung.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jutils.print.printables.EmptyPrintable;

class PenaltiesPrintableTest {

    private EinzelWettkampf wk;

    @BeforeEach
    void wettkampfMitNichtAngetretenem() {
        Disziplin[] disziplinen = { new Disziplin("A", 6000, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, true, FormelDLRG2007.ID);
        regelwerk.setAk(0, new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false));
        wk = new EinzelWettkampf(regelwerk, new Strafen());
        Teilnehmer t = wk.createTeilnehmer("", "Alpha", "Vorname", 2000, false, "Gliederung", 0, "");
        t.setStrafen(0, new LinkedList<>(List.of(new Strafe("Nicht angetreten", "NA", Strafarten.NICHT_ANGETRETEN, 0))));
        wk.addSchwimmer(t);
    }

    @AfterEach
    void optionZuruecksetzen() {
        PrintUtils.printDidNotStart = false;
    }

    @Test
    void nichtAngetretenFehltOhneOption() {
        PrintUtils.printDidNotStart = false;

        assertSame(EmptyPrintable.Instance, PrintableCreator.createPenaltiesPrintable(wk, true));
    }

    @Test
    void nichtAngetretenWirdMitOptionGedruckt() {
        PrintUtils.printDidNotStart = true;

        assertNotSame(EmptyPrintable.Instance, PrintableCreator.createPenaltiesPrintable(wk, true));
    }

    @Test
    void titelOhneFilter() {
        assertEquals(I18n.get("Penaltylist"), PrintableCreator.penaltiesTitle(wk));
    }

    @Test
    void titelNenntDenAktivenFilter() {
        wk.setFilter(new Filter[] { new Filter(I18n.get("Filter.NoFilter"), null),
                new Filter("Nord", new String[] { "Gliederung" }) });
        wk.setCurrentFilterIndex(1);

        assertEquals(I18n.get("Penaltylist") + " (Nord)", PrintableCreator.penaltiesTitle(wk));
    }
}
