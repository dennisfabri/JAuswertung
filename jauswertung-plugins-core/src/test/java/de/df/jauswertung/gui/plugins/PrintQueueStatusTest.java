package de.df.jauswertung.gui.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.gui.util.I18n;

class PrintQueueStatusTest {

    private static final int VERZOEGERUNG = 20;

    private JFrame fenster;
    private final List<String> statustexte = new ArrayList<>();
    private final List<String> fehler = new ArrayList<>();
    private PrintQueueStatus status;

    @BeforeEach
    void aufbauen() throws Exception {
        SwingUtilities.invokeAndWait(() -> fenster = new JFrame());
        status = new PrintQueueStatus(() -> fenster, statustexte::add,
                (parent, title, message, note) -> fehler.add(title + "|" + message + "|" + note), VERZOEGERUNG);
    }

    @AfterEach
    void abbauen() throws Exception {
        SwingUtilities.invokeAndWait(fenster::dispose);
    }

    private static void edtAbarbeiten() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    private static void warten() throws Exception {
        Thread.sleep(10L * VERZOEGERUNG);
        edtAbarbeiten();
    }

    @Test
    void statustextFolgtDerWarteschlange() throws Exception {
        status.jobAdded("A", 2);
        status.jobStarted("B", 1);
        status.jobFinished("B", 0);
        status.queueEmpty();
        edtAbarbeiten();

        List<String> erwartet = new ArrayList<>(List.of(I18n.get("PrintingJobOfJobs", "A", 2),
                I18n.get("PrintingJobOfJobs", "B", 1), ""));
        erwartet.add(null);
        assertEquals(erwartet, statustexte);
    }

    @Test
    void fehlerBeiFreigegebenemFensterWirdSofortGezeigt() throws Exception {
        status.jobError("A", 0, "Titel", "Text", "Hinweis");
        edtAbarbeiten();

        assertEquals(List.of("Titel|Text|Hinweis"), fehler);
        assertEquals(List.of(""), statustexte);
    }

    @Test
    void fehlerWartetBisDasFensterFreigegebenIst() throws Exception {
        SwingUtilities.invokeAndWait(() -> fenster.setEnabled(false));

        status.jobError("A", 0, "Titel", "Text", "Hinweis");
        warten();
        assertEquals(List.of(), fehler);

        SwingUtilities.invokeAndWait(() -> fenster.setEnabled(true));
        warten();
        warten();
        assertEquals(List.of("Titel|Text|Hinweis"), fehler);
    }
}
