package de.df.jauswertung.gui.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.gui.plugins.AFilePlugin.SaveActions;

class AFilePluginTest {

    @Test
    void ohneDateinameSpeichertDerKnopfUnterNeuemNamen() {
        assertEquals(new SaveActions(false, true, true), AFilePlugin.saveActions(false, false));
    }

    @Test
    void ohneDateinameMitAenderungen() {
        assertEquals(new SaveActions(false, true, true), AFilePlugin.saveActions(true, false));
    }

    @Test
    void mitDateinameUndAenderungen() {
        assertEquals(new SaveActions(true, true, true), AFilePlugin.saveActions(true, true));
    }

    @Test
    void mitDateinameOhneAenderungen() {
        assertEquals(new SaveActions(false, true, false), AFilePlugin.saveActions(false, true));
    }
}
