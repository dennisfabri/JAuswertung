package de.df.jauswertung.gui.veranstaltung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

class VeranstaltungCorePluginTest {

    @TempDir
    Path verzeichnis;

    private final VeranstaltungCorePlugin core = new VeranstaltungCorePlugin();
    private final List<Boolean> geaendert = new ArrayList<>();

    @BeforeEach
    void verbinden() {
        IPluginManager controller = (IPluginManager) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { IPluginManager.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "setChanged" -> geaendert.add((Boolean) args[0]);
                    case "sendDataUpdateEvent" -> core
                            .dataUpdated(new UpdateEvent((String) args[0], (Long) args[1], (IFeature) args[2]));
                    case "acceptWarning" -> {
                        return true;
                    }
                    default -> {
                        // Nothing to do
                    }
                    }
                    return null;
                });
        core.setController(controller, "uid");
    }

    private boolean istGeaendert() {
        return geaendert.getLast();
    }

    private void aendern() {
        core.changed("Properties", UpdateEventConstants.REASON_PROPERTIES_CHANGED, null);
    }

    @Test
    void aenderungMarkiertDieVeranstaltungAlsGeaendert() {
        aendern();

        assertTrue(istGeaendert());
    }

    @Test
    void speichernUnterMerktSichDieDateiOhneAenderung() {
        String datei = verzeichnis.resolve("event.vs").toString();
        aendern();

        assertTrue(core.saveAs(datei));

        assertEquals(datei, core.getFilename());
        assertEquals(false, istGeaendert());
    }

    @Test
    void speichernSetztDieAenderungZurueck() {
        core.saveAs(verzeichnis.resolve("event.vs").toString());
        aendern();

        assertTrue(core.save());

        assertEquals(false, istGeaendert());
    }

    @Test
    void ladenUebernimmtDieVeranstaltungOhneAenderung() {
        String datei = verzeichnis.resolve("event.vs").toString();
        core.getVeranstaltung().setName("DMM");
        core.saveAs(datei);
        core.neu();
        aendern();

        assertTrue(core.load(datei));

        assertEquals("DMM", core.getVeranstaltung().getName());
        assertEquals(datei, core.getFilename());
        assertEquals(false, istGeaendert());
    }

    @Test
    void neuVerwirftVeranstaltungUndDatei() {
        core.getVeranstaltung().setName("DMM");
        core.saveAs(verzeichnis.resolve("event.vs").toString());
        aendern();

        core.neu();

        assertEquals("", core.getVeranstaltung().getName());
        assertNull(core.getFilename());
        assertEquals(false, istGeaendert());
    }
}
