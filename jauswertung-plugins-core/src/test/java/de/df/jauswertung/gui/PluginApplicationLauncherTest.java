package de.df.jauswertung.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class PluginApplicationLauncherTest {

    private static List<String> aktionen(String... args) {
        return Arrays.stream(PluginApplicationLauncher.processCommandline(args))
                .map(a -> a.getVerb() + "=" + a.getCommand()).toList();
    }

    @Test
    void ohneArgumente() {
        assertEquals(List.of(), aktionen());
        assertEquals(0, PluginApplicationLauncher.processCommandline(null).length);
    }

    @Test
    void paareAusVerbUndWert() {
        assertEquals(List.of("open=datei.wk", "print=ergebnisse"),
                aktionen("-open", "datei.wk", "-print", "ergebnisse"));
    }

    @Test
    void leerzeichenWerdenEntfernt() {
        assertEquals(List.of("open=datei.wk"), aktionen(" -open ", " datei.wk "));
    }

    @Test
    void verbOhneBindestrichWirdIgnoriert() {
        assertEquals(List.of("print=ergebnisse"), aktionen("open", "datei.wk", "-print", "ergebnisse"));
    }

    @Test
    void verbOhneWertWirdIgnoriert() {
        assertEquals(List.of("open=datei.wk"), aktionen("-open", "datei.wk", "-print"));
    }

    @Test
    void fehlendeEintraegeWerdenIgnoriert() {
        assertEquals(List.of("print=ergebnisse"), aktionen("-open", null, "-print", "ergebnisse"));
    }
}
