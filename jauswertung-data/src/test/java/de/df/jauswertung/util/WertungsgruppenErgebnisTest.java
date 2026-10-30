package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import de.df.jauswertung.daten.regelwerk.WertungsgruppeException;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

/**
 * Results of a scoring group: AK 0 and AK 1 form group "A", AK 2 belongs to none.
 */
class WertungsgruppenErgebnisTest {

    @Test
    void mehrkampfwertungDerWertungsgruppe() {
        AWettkampf<ASchwimmer> merged = CompetitionUtils.generateWertungsgruppenwettkampf(wettkampf());

        Altersklasse ak = merged.getRegelwerk().getAk(0);
        assertEquals("A", ak.getName());
        assertEquals(List.of("1 Clara", "2 Anna", "3 Bernd", "4 Dora"), ergebnis(merged, ak));
    }

    @Test
    void einzelwertungDerWertungsgruppe() {
        AWettkampf<ASchwimmer> einzel = ResultUtils.generateEinzelwertungswettkampf(wettkampf(), "A", false);

        assertEquals(2, einzel.getRegelwerk().size());
        assertEquals(List.of("1 Clara", "2 Anna", "3 Bernd", "4 Dora"), ergebnis(einzel, einzel.getRegelwerk().getAk(0)));
        assertEquals(List.of("1 Dora", "2 Bernd", "3 Anna", "4 Clara"),
                ergebnis(einzel, einzel.getRegelwerk().getAk(1)));
    }

    /**
     * Printouts split the scoring group by name, the CSV and Excel exports split the merged age group.
     */
    @Test
    void einzelwertungIstInAusdruckUndExportGleich() {
        AWettkampf<ASchwimmer> wk = wettkampf();

        AWettkampf<ASchwimmer> ausdruck = ResultUtils.generateEinzelwertungswettkampf(wk, "A", false);
        AWettkampf<ASchwimmer> export = ResultUtils
                .generateEinzelwertungswettkampf(CompetitionUtils.generateWertungsgruppenwettkampf(wk), 0, false);

        assertEquals(ausdruck.getRegelwerk().size(), export.getRegelwerk().size());
        for (int x = 0; x < ausdruck.getRegelwerk().size(); x++) {
            assertEquals(ergebnis(ausdruck, ausdruck.getRegelwerk().getAk(x)),
                    ergebnis(export, export.getRegelwerk().getAk(x)));
        }
    }

    @Test
    void unbekannteWertungsgruppe() {
        assertNull(ResultUtils.generateEinzelwertungswettkampf(wettkampf(), "B", false));
    }

    @Test
    void wertungsgruppeOhneSchwimmerHatKeineEinzelwertung() {
        AWettkampf<ASchwimmer> wk = wettkampf();
        wk.removeSchwimmer(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(0)));
        wk.removeSchwimmer(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(1)));

        AWettkampf<ASchwimmer> einzel = ResultUtils.generateEinzelwertungswettkampf(wk, "A", false);

        assertTrue(einzel == null || !einzel.hasSchwimmer());
    }

    @Test
    void abweichendeAltersklassenWerdenGemeldet() {
        AWettkampf<ASchwimmer> wk = wettkampf();
        wk.getRegelwerk().getAk(1).setHLW(true);

        assertThrows(WertungsgruppeException.class, () -> CompetitionUtils.generateWertungsgruppenwettkampf(wk));
        assertThrows(WertungsgruppeException.class,
                () -> ResultUtils.generateEinzelwertungswettkampf(wk, "A", false));
    }

    @Test
    void abweichendeAltersklassenOhneSchwimmerStoerenNicht() {
        AWettkampf<ASchwimmer> wk = wettkampf();
        wk.getRegelwerk().getAk(1).setHLW(true);
        wk.removeSchwimmer(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(0)));
        wk.removeSchwimmer(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(1)));

        assertEquals(3, CompetitionUtils.generateWertungsgruppenwettkampf(wk).getRegelwerk().size());
    }

    static List<String> ergebnis(AWettkampf<ASchwimmer> wk, Altersklasse ak) {
        List<String> result = new ArrayList<>();
        for (SchwimmerResult<ASchwimmer> r : ResultCalculator.getResults(wk, ak, false)) {
            result.add(r.getPlace() + " " + ((Teilnehmer) r.getSchwimmer()).getVorname());
        }
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static AWettkampf<ASchwimmer> wettkampf() {
        Regelwerk regelwerk = new Regelwerk(3, true, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("50m", 6000, 50, 1), new Disziplin("100m", 12000, 100, 1) };
            Altersklasse ak = new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, false);
            if (x < 2) {
                ak.setWertungsgruppe("A");
            }
            regelwerk.setAk(x, ak);
        }
        Wertungsgruppe gruppe = new Wertungsgruppe("A");
        gruppe.setProtokollMitEinzelwertung(true);
        gruppe.setProtokollMitMehrkampfwertung(true);
        regelwerk.setWertungsgruppen(new Wertungsgruppe[] { gruppe });
        EinzelWettkampf wk = new EinzelWettkampf(regelwerk, new Strafen());
        schwimmer(wk, "Anna", 0, 6500, 13000);
        schwimmer(wk, "Bernd", 0, 7000, 12500);
        schwimmer(wk, "Clara", 1, 6000, 14000);
        schwimmer(wk, "Dora", 1, 0, 12000);
        schwimmer(wk, "Emil", 2, 6000, 12000);
        return (AWettkampf) wk;
    }

    private static void schwimmer(EinzelWettkampf wk, String vorname, int ak, int zeit1, int zeit2) {
        Teilnehmer t = wk.createTeilnehmer("", "Nachname", vorname, 2000, false, "Gliederung", ak, "");
        wk.addSchwimmer(t);
        t.setZeit(0, zeit1);
        t.setZeit(1, zeit2);
    }
}
