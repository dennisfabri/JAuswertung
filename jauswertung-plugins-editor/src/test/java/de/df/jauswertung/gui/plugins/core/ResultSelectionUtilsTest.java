package de.df.jauswertung.gui.plugins.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

class ResultSelectionUtilsTest {

    private static final String GRUPPE = "Gruppe";
    private static final int GRUPPEN_AK = 2;

    private EinzelWettkampf wk;
    private Wertungsgruppe gruppe;

    @BeforeEach
    void wettkampf() {
        Disziplin[] disziplinen = { new Disziplin("A", 6000, 50, 1), new Disziplin("B", 6000, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(2, true, FormelDLRG2007.ID);
        for (int x = 0; x < 2; x++) {
            Altersklasse ak = new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, true);
            ak.setWertungsgruppe(GRUPPE);
            ak.setEinzelwertung(false);
            ak.setEinzelwertungHlw(false);
            ak.setMehrkampfwertung(false);
            ak.setStrafeIstDisqualifikation(false);
            regelwerk.setAk(x, ak);
        }
        gruppe = new Wertungsgruppe(GRUPPE);
        gruppe.setProtokollMitMehrkampfwertung(true);
        gruppe.setProtokollMitEinzelwertung(true);
        gruppe.setEinzelwertungHlw(true);
        gruppe.setStrafeIstDisqualifikation(true);
        regelwerk.setWertungsgruppen(new Wertungsgruppe[] { gruppe });
        wk = new EinzelWettkampf(regelwerk, new Strafen());

        schwimmer("Alpha", 0, 6500);
        schwimmer("Bravo", 1, 6500);
        schwimmer("Charlie", 0, 0);
    }

    private void schwimmer(String name, int ak, int zeit) {
        Teilnehmer t = wk.createTeilnehmer("", name, "Vorname", 2000, false, "Gliederung", ak, "");
        t.setZeit(0, zeit);
        t.setZeit(1, 6500);
        wk.addSchwimmer(t);
    }

    private static List<String> namen(AWettkampf<ASchwimmer> wk, int ak) {
        return SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(ak)).stream()
                .map(s -> ((Teilnehmer) s).getNachname()).sorted().toList();
    }

    @Test
    void wertungsgruppeWirdAlsZusaetzlicheAltersklasseAngehaengt() {
        AWettkampf<ASchwimmer> result = ResultSelectionUtils.getResultWettkampf(wk);

        assertEquals(3, result.getRegelwerk().size());
        assertEquals(GRUPPE, result.getRegelwerk().getAk(GRUPPEN_AK).getName());
        assertEquals(List.of("Alpha", "Bravo", "Charlie"), namen(result, GRUPPEN_AK));
        assertEquals(List.of("Alpha", "Charlie"), namen(result, 0));
        assertEquals(List.of("Bravo"), namen(result, 1));
    }

    @Test
    void wertungsgruppeOhneMehrkampfwertungImProtokollWirdNichtAngehaengt() {
        gruppe.setProtokollMitMehrkampfwertung(false);

        assertEquals(2, ResultSelectionUtils.getResultWettkampf(wk).getRegelwerk().size());
    }

    @Test
    void eingabestatusBeruecksichtigtDenZuletztVerschobenenTeilnehmer() {
        AWettkampf<ASchwimmer> result = ResultSelectionUtils.getResultWettkampf(wk);

        assertFalse(result.isAgegroupComplete(GRUPPEN_AK, false));
    }

    @Test
    void einstellungenKommenAusDerWertungsgruppe() {
        Altersklasse ak = ResultSelectionUtils.getResultWettkampf(wk).getRegelwerk().getAk(GRUPPEN_AK);

        assertAll(() -> assertTrue(ak.isStrafeIstDisqualifikation(), "Strafe ist Disqualifikation"),
                () -> assertTrue(ak.hasEinzelwertung(), "Einzelwertung"),
                () -> assertTrue(ak.isEinzelwertungHlw(), "Einzelwertung HLW"),
                () -> assertTrue(ak.hasMehrkampfwertung(), "Mehrkampfwertung"));
    }
}
