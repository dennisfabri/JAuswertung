package de.df.jauswertung.daten.regelwerk;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

/**
 * AK 0 and AK 1 form scoring group "A", AK 2 belongs to none.
 */
class WertungsgruppenAltersklasseTest {

    private final Wertungsgruppe gruppe = new Wertungsgruppe("A");

    @Test
    void einstellungenDerWertungsgruppe() {
        gruppe.setStrafeIstDisqualifikation(true);
        gruppe.setProtokollMitEinzelwertung(true);
        gruppe.setEinzelwertungHlw(true);
        gruppe.setProtokollMitMehrkampfwertung(false);
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(0).setHLW(true);
        regelwerk.getAk(1).setHLW(true);

        Altersklasse ak = regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertAll(() -> assertEquals("A", ak.getName()),
                () -> assertTrue(ak.isStrafeIstDisqualifikation()),
                () -> assertTrue(ak.hasEinzelwertung()),
                () -> assertTrue(ak.isEinzelwertungHlw()),
                () -> assertFalse(ak.hasMehrkampfwertung()),
                () -> assertEquals("A", ak.getWertungsgruppe()));
    }

    @Test
    void mehrkampfwertungAusDerWertungsgruppe() {
        gruppe.setProtokollMitMehrkampfwertung(true);

        assertTrue(regelwerk(true).createWertungsgruppenAltersklasse(gruppe).hasMehrkampfwertung());
    }

    @Test
    void mitgliederBleibenUnveraendert() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(0).getDisziplin(0, false).setRec(7000);
        regelwerk.getAk(1).getDisziplin(0, false).setRec(5000);

        regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertEquals("AK 0", regelwerk.getAk(0).getName());
        assertEquals(7000, regelwerk.getAk(0).getDisziplin(0, false).getRec());
    }

    @Test
    void kleinsterRekordwertUngleichNull() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(0).getDisziplin(0, false).setRec(7000);
        regelwerk.getAk(1).getDisziplin(0, false).setRec(5000);
        regelwerk.getAk(0).getDisziplin(0, true).setRec(0);
        regelwerk.getAk(1).getDisziplin(0, true).setRec(5500);
        regelwerk.getAk(0).getDisziplin(1, false).setRec(0);
        regelwerk.getAk(1).getDisziplin(1, false).setRec(0);

        Altersklasse ak = regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertEquals(5000, ak.getDisziplin(0, false).getRec());
        assertEquals(5500, ak.getDisziplin(0, true).getRec());
        assertEquals(0, ak.getDisziplin(1, false).getRec());
    }

    @Test
    void altersgrenzenUmfassenAlleMitglieder() {
        Regelwerk regelwerk = regelwerk(false);
        grenzen(regelwerk.getAk(0), 12, 14, 40, 0);
        grenzen(regelwerk.getAk(1), 15, 16, 0, 80);

        Altersklasse ak = regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertAll(() -> assertEquals(12, ak.getMinimumAlter()),
                () -> assertEquals(16, ak.getMaximumAlter()),
                () -> assertEquals(0, ak.getMinimumAlterInSumme(), "ein Mitglied ohne Untergrenze"),
                () -> assertEquals(0, ak.getMaximumAlterInSumme(), "ein Mitglied ohne Obergrenze"));
    }

    @Test
    void altersgrenzeEntfaelltWennEinMitgliedKeineHat() {
        Regelwerk regelwerk = regelwerk(true);
        grenzen(regelwerk.getAk(0), 0, 14, 0, 0);
        grenzen(regelwerk.getAk(1), 15, 0, 0, 0);

        Altersklasse ak = regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertEquals(0, ak.getMinimumAlter());
        assertEquals(0, ak.getMaximumAlter());
    }

    @Test
    void gesamtwertungWennEinMitgliedSieHat() {
        Regelwerk regelwerk = regelwerk(true);
        for (int x = 0; x < 2; x++) {
            regelwerk.getAk(x).setGesamtwertung(false, false);
            regelwerk.getAk(x).setGesamtwertung(true, false);
        }
        regelwerk.getAk(1).setGesamtwertung(true, true);

        Altersklasse ak = regelwerk.createWertungsgruppenAltersklasse(gruppe);

        assertFalse(ak.getGesamtwertung(false));
        assertTrue(ak.getGesamtwertung(true));
    }

    @Test
    void startgruppeWirdEntfernt() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(0).setStartgruppe("S");

        assertNull(regelwerk.createWertungsgruppenAltersklasse(gruppe).getStartgruppe());
    }

    @Test
    void ohneMitgliederNull() {
        assertNull(regelwerk(true).createWertungsgruppenAltersklasse(new Wertungsgruppe("B")));
    }

    @Test
    void anzahlDerDisziplinenMussGleichSein() {
        assertAbweichung(true, ak -> ak.setDiszAnzahl(1), "der Anzahl der Disziplinen");
    }

    @Test
    void namenDerDisziplinenMuessenGleichSein() {
        assertAbweichung(true, ak -> ak.setDisziplin(new Disziplin("200m", 24000, 200, 1), 1, true),
                "den Namen der Disziplinen");
    }

    @Test
    void zusatzwertungMussGleichSein() {
        assertAbweichung(true, ak -> ak.setHLW(true), "der Zusatzwertung");
    }

    @Test
    void disziplinwahlMussGleichSein() {
        assertAbweichung(true, ak -> ak.setDisciplineChoiceAllowed(true), "der Disziplinwahl");
    }

    @Test
    void gewaehlteDisziplinenMuessenBeiDisziplinwahlGleichSein() {
        Consumer<Altersklasse> an = ak -> {
            ak.setDisciplineChoiceAllowed(true);
            ak.setChosenDisciplines(1, 1, 2);
        };
        Regelwerk regelwerk = regelwerk(true);
        an.accept(regelwerk.getAk(0));
        an.accept(regelwerk.getAk(1));
        assertTrue(regelwerk.isValid());

        regelwerk.getAk(1).setChosenDisciplines(1, 2, 2);

        assertFalse(regelwerk.isValid());
        assertThrows(WertungsgruppeException.class, () -> regelwerk.createWertungsgruppenAltersklasse(gruppe));
    }

    @Test
    void gewaehlteDisziplinenZaehlenOhneDisziplinwahlNicht() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(1).setChosenDisciplines(1, 1, 2);

        assertTrue(regelwerk.isValid());
    }

    @Test
    void mannschaftsgroesseMussGleichSein() {
        assertAbweichung(false, ak -> ak.setMemberCounts(3, 4), "der Mannschaftsgröße");
    }

    @Test
    void mannschaftsgroesseZaehltImEinzelNicht() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(1).setMemberCounts(3, 4);

        assertTrue(regelwerk.isValid());
    }

    @Test
    void altersklasseOhneWertungsgruppeZaehltNicht() {
        Regelwerk regelwerk = regelwerk(true);
        regelwerk.getAk(2).setDiszAnzahl(1);

        assertTrue(regelwerk.isValid());
        assertEquals(2, regelwerk.createWertungsgruppenAltersklasse(gruppe).getDiszAnzahl());
    }

    private void assertAbweichung(boolean einzel, Consumer<Altersklasse> aendern, String einstellung) {
        Regelwerk regelwerk = regelwerk(einzel);
        assertTrue(regelwerk.isValid());

        aendern.accept(regelwerk.getAk(1));

        assertFalse(regelwerk.isValid());
        WertungsgruppeException e = assertThrows(WertungsgruppeException.class,
                () -> regelwerk.createWertungsgruppenAltersklasse(gruppe));
        assertTrue(e.getMessage().contains("„A“"), e.getMessage());
        assertTrue(e.getMessage().contains(einstellung), e.getMessage());
        assertTrue(e.getMessage().contains("„AK 0“ und „AK 1“"), e.getMessage());
    }

    private static void grenzen(Altersklasse ak, int min, int max, int minSumme, int maxSumme) {
        ak.setMinimumAlter(min);
        ak.setMaximumAlter(max);
        ak.setMinimumAlterInSumme(minSumme);
        ak.setMaximumAlterInSumme(maxSumme);
    }

    private Regelwerk regelwerk(boolean einzel) {
        Regelwerk regelwerk = new Regelwerk(3, einzel, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[][] disziplinen = new Disziplin[2][];
            for (int y = 0; y < 2; y++) {
                disziplinen[y] = new Disziplin[] { new Disziplin("50m", 6000, 50, 1),
                        new Disziplin("100m", 12000, 100, 1) };
            }
            Altersklasse ak = new Altersklasse("AK " + x, disziplinen, false);
            if (x < 2) {
                ak.setWertungsgruppe("A");
            }
            regelwerk.setAk(x, ak);
        }
        regelwerk.setWertungsgruppen(new Wertungsgruppe[] { gruppe });
        return regelwerk;
    }
}
