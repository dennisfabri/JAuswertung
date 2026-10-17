package de.df.jauswertung.gui.veranstaltung;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.df.jauswertung.daten.veranstaltung.CompetitionContainer;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.io.InputManager;

class VeranstaltungsdateiTest {

    @TempDir
    Path verzeichnis;

    private static List<String> dateien(Veranstaltung vs) {
        return vs.getCompetitions().stream().map(CompetitionContainer::getFilename).toList();
    }

    private Veranstaltung veranstaltung(Path wettkampfverzeichnis) {
        Veranstaltung vs = new Veranstaltung();
        vs.setCompetitions(List.of(new CompetitionContainer("Einzel", wettkampfverzeichnis.resolve("einzel.wk").toString())));
        return vs;
    }

    @Test
    void wettkaempfeImVerzeichnisStehenOhneVerzeichnisInDerDatei() throws Exception {
        String datei = verzeichnis.resolve("event.vs").toString();

        Veranstaltungsdatei.speichere(datei, veranstaltung(verzeichnis));

        assertEquals(List.of("einzel.wk"), dateien((Veranstaltung) InputManager.ladeObject(datei)));
    }

    @Test
    void speichernVeraendertDieVeranstaltungNicht() throws Exception {
        Veranstaltung vs = veranstaltung(verzeichnis);

        Veranstaltungsdatei.speichere(verzeichnis.resolve("event.vs").toString(), vs);

        assertEquals(List.of(verzeichnis.resolve("einzel.wk").toString()), dateien(vs));
    }

    @Test
    void verschobeneVeranstaltungFindetIhreWettkaempfe() throws Exception {
        Path alt = Files.createDirectory(verzeichnis.resolve("alt"));
        Path neu = verzeichnis.resolve("neu");
        Veranstaltungsdatei.speichere(alt.resolve("event.vs").toString(), veranstaltung(alt));
        Files.move(alt, neu);

        Veranstaltung vs = Veranstaltungsdatei.lade(neu.resolve("event.vs").toString());

        assertEquals(List.of(neu.resolve("einzel.wk").toString()), dateien(vs));
    }

    @Test
    void wettkaempfeAusserhalbDesVerzeichnissesStehenAbsolutInDerDatei() throws Exception {
        String datei = verzeichnis.resolve("event.vs").toString();
        Path unterverzeichnis = verzeichnis.resolve("unterverzeichnis");

        Veranstaltungsdatei.speichere(datei, veranstaltung(unterverzeichnis));

        assertEquals(List.of(unterverzeichnis.resolve("einzel.wk").toString()),
                dateien((Veranstaltung) InputManager.ladeObject(datei)));
    }

    @Test
    void alteVeranstaltungVerweistAbsolut() {
        Veranstaltung vs = Veranstaltungsdatei.lade("src/test/resources/veranstaltung/Gesamtwertung.vs");

        assertEquals(List.of("C:\\Users\\DLRG\\Desktop\\dmm2021\\dmm2021_Einzel.wk",
                "C:\\Users\\DLRG\\Desktop\\dmm2021\\dmm2021_Mannschaft.wk"), dateien(vs));
    }
}
