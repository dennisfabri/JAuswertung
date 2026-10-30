package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.laufliste.Laufliste.BlockEinteilung;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

class LauflisteBlocksSpeichernTest {

    @Test
    void reihenfolgeDerBloeckeBleibtNachSpeichernUndLadenErhalten(@TempDir Path temp) {
        Regelwerk regelwerk = new Regelwerk(2, true, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("50m " + x, 6000, 50, 1) };
            regelwerk.setAk(x, new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, false));
        }
        EinzelWettkampf wk = new EinzelWettkampf(regelwerk, new Strafen());
        for (int ak = 0; ak < 2; ak++) {
            wk.addSchwimmer(wk.createTeilnehmer("", "Nachname", "Vorname", 2000, false, "Gliederung", ak, ""));
        }
        wk.getLaufliste().erzeugen(new BlockEinteilung[] { new BlockEinteilung(1, true), new BlockEinteilung(1, false),
                new BlockEinteilung(0, true), new BlockEinteilung(0, false) });
        String datei = temp.resolve("blocks.wk").toString();

        assertTrue(OutputManager.speichereWettkampf(datei, wk));
        AWettkampf<?> geladen = InputManager.ladeWettkampf(datei);

        List<String> bloecke = Arrays.stream(geladen.getLaufliste().getBlocks())
                .map(b -> b.getStartgruppe() + (b.isMaennlich() ? "m" : "w")).toList();
        assertEquals(List.of("1m", "1w", "0m", "0w"), bloecke);
    }
}
