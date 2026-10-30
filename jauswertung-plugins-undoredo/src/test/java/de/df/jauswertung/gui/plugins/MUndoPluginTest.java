package de.df.jauswertung.gui.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

class MUndoPluginTest {

    record Knoten(List<Object> nachbarn) {
    }

    private static boolean kopierenSchlaegtFehl = false;

    record Wechselhaft(int wert) {
        Wechselhaft {
            if (kopierenSchlaegtFehl) {
                throw new IllegalStateException("Kopieren schlägt fehl");
            }
        }
    }

    private final List<String> meldungen = new ArrayList<>();
    private AWettkampf<?> aktuell;
    private MUndoPlugin plugin;

    @BeforeEach
    void setUp() throws Exception {
        CorePlugin core = mock(CorePlugin.class);
        when(core.getWettkampf()).thenAnswer(i -> aktuell);
        doAnswer(i -> {
            aktuell = i.getArgument(0);
            return null;
        }).when(core).setWettkampf(any(), anyBoolean());
        IPluginManager controller = mock(IPluginManager.class);
        when(controller.getFeature("de.df.jauswertung.core", "uid")).thenReturn(core);

        aufEdt(() -> {
            plugin = new MUndoPlugin() {
                @Override
                void meldeFehler(String text, String hinweis) {
                    meldungen.add(text);
                }
            };
            plugin.setController(controller, "uid");
        });
        aktuell = wettkampf();
        ereignis("Load");
    }

    @AfterEach
    void tearDown() {
        kopierenSchlaegtFehl = false;
    }

    @Test
    void rueckgaengigStelltDenVorherigenStandWieder() throws Exception {
        AWettkampf<?> vorher = aktuell;
        schwimmerHinzufuegen();

        aufEdt(() -> plugin.undo(0));

        assertNotSame(vorher, aktuell);
        assertEquals(0, aktuell.getSchwimmeranzahl());
    }

    @Test
    void rueckgaengigMitWertOhneSerializable() throws Exception {
        aktuell.setProperty("Notiz", new Object());
        ereignis("ChangeProperties");
        schwimmerHinzufuegen();

        aufEdt(() -> plugin.undo(0));

        assertNotNull(aktuell);
        assertEquals(0, aktuell.getSchwimmeranzahl());
        assertNotNull(aktuell.getProperty("Notiz"));
    }

    @Test
    void wiederherstellenNachRueckgaengig() throws Exception {
        schwimmerHinzufuegen();
        aufEdt(() -> plugin.undo(0));

        aufEdt(() -> plugin.redo(0));

        assertEquals(1, aktuell.getSchwimmeranzahl());
    }

    @Test
    void fehlgeschlagenerSnapshotWirdGemeldetUndDerVerlaufBleibtNutzbar() throws Exception {
        Knoten knoten = new Knoten(new ArrayList<>());
        knoten.nachbarn().add(knoten);
        aktuell.setProperty("Zyklus", knoten);
        ereignis("ChangeProperties");
        assertEquals(1, meldungen.size(), meldungen.toString());

        aktuell.setProperty("Zyklus", null);
        ereignis("ChangeProperties");
        schwimmerHinzufuegen();
        aufEdt(() -> plugin.undo(0));

        assertEquals(1, meldungen.size(), meldungen.toString());
        assertEquals(0, aktuell.getSchwimmeranzahl());
    }

    @Test
    void fehlgeschlagenesRueckgaengigWirdGemeldetUndAendertNichts() throws Exception {
        aktuell.setProperty("Wert", new Wechselhaft(1));
        ereignis("ChangeProperties");
        schwimmerHinzufuegen();
        AWettkampf<?> vorher = aktuell;

        kopierenSchlaegtFehl = true;
        aufEdt(() -> plugin.undo(0));
        kopierenSchlaegtFehl = false;

        assertEquals(1, meldungen.size(), meldungen.toString());
        assertSame(vorher, aktuell);
        aufEdt(() -> plugin.undo(0));
        assertEquals(0, aktuell.getSchwimmeranzahl());
        assertTrue(aktuell.getProperty("Wert") instanceof Wechselhaft);
    }

    private void schwimmerHinzufuegen() throws Exception {
        EinzelWettkampf wk = (EinzelWettkampf) aktuell;
        Teilnehmer t = wk.createTeilnehmer("", "Nachname", "Vorname", 2000, false, "Gliederung", 0, "");
        wk.addSchwimmer(t);
        ereignis("NewPerson");
    }

    private void ereignis(String titel) throws Exception {
        aufEdt(() -> plugin.dataUpdated(new UpdateEvent(titel, 0, null)));
    }

    private static void aufEdt(Runnable r) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(r);
    }

    private static EinzelWettkampf wettkampf() {
        Disziplin[] disziplinen = { new Disziplin("A", 6000, 50, 1), new Disziplin("B", 6000, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, true, FormelDLRG2007.ID);
        regelwerk.setAk(0, new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false));
        return new EinzelWettkampf(regelwerk, new Strafen());
    }
}
