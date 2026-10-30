package de.df.jauswertung.daten.laufliste;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Laufliste.BlockEinteilung;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

class LauflisteBlocksTest {

    @Test
    void gespeicherteReihenfolgeBleibtErhalten() {
        EinzelWettkampf wk = wettkampf();
        schwimmer(wk, 0);
        schwimmer(wk, 1);

        wk.getLaufliste().erzeugen(blocks("1m", "1w", "0m", "0w"));

        assertEquals(List.of("1m", "1w", "0m", "0w"), namen(wk.getLaufliste().getBlocks()));
    }

    @Test
    void neueStartgruppeWirdAngehaengt() {
        EinzelWettkampf wk = wettkampf();
        schwimmer(wk, 0);
        wk.getLaufliste().erzeugen(blocks("0m", "0w"));

        schwimmer(wk, 1);

        assertEquals(List.of("0m", "0w", "1w", "1m"), namen(wk.getLaufliste().getBlocks()));
    }

    @Test
    void leereStartgruppeEntfaellt() {
        EinzelWettkampf wk = wettkampf();
        schwimmer(wk, 0);
        Teilnehmer zweite = schwimmer(wk, 1);
        wk.getLaufliste().erzeugen(blocks("1m", "0m", "1w", "0w"));

        wk.removeSchwimmer(zweite);

        assertEquals(List.of("0m", "0w"), namen(wk.getLaufliste().getBlocks()));
    }

    static EinzelWettkampf wettkampf() {
        Regelwerk regelwerk = new Regelwerk(2, true, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("50m " + x, 6000, 50, 1) };
            regelwerk.setAk(x, new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, false));
        }
        return new EinzelWettkampf(regelwerk, new Strafen());
    }

    static Teilnehmer schwimmer(EinzelWettkampf wk, int ak) {
        Teilnehmer t = wk.createTeilnehmer("", "Nachname", "Vorname", 2000, false, "Gliederung", ak, "");
        wk.addSchwimmer(t);
        return t;
    }

    static BlockEinteilung[] blocks(String... namen) {
        return Arrays.stream(namen)
                .map(n -> new BlockEinteilung(n.charAt(0) - '0', n.charAt(1) == 'm'))
                .toArray(BlockEinteilung[]::new);
    }

    static List<String> namen(BlockEinteilung[] blocks) {
        return Arrays.stream(blocks).map(b -> b.getStartgruppe() + (b.isMaennlich() ? "m" : "w")).toList();
    }
}
