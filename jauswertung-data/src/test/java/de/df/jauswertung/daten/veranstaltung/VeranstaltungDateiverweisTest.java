package de.df.jauswertung.daten.veranstaltung;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class VeranstaltungDateiverweisTest {

    private static final Path VERZEICHNIS = Path.of("veranstaltung").toAbsolutePath();
    private static final String VERANSTALTUNGSDATEI = VERZEICHNIS.resolve("event.vs").toString();

    private static Veranstaltung veranstaltung(String... dateien) {
        Veranstaltung vs = new Veranstaltung();
        vs.setCompetitions(Arrays.stream(dateien).map(d -> new CompetitionContainer("WK", d)).toList());
        return vs;
    }

    private static List<String> dateien(Veranstaltung vs) {
        return vs.getCompetitions().stream().map(CompetitionContainer::getFilename).toList();
    }

    @Test
    void relativierenSpeichertDateienImVerzeichnisMitDateinamen() {
        Veranstaltung vs = veranstaltung(VERZEICHNIS.resolve("a.wk").toString(), VERZEICHNIS.resolve("b.wk").toString());

        vs.verweiseRelativieren(VERANSTALTUNGSDATEI);

        assertEquals(List.of("a.wk", "b.wk"), dateien(vs));
    }

    @Test
    void relativierenLaesstDateienAusserhalbDesVerzeichnissesAbsolut() {
        String innen = VERZEICHNIS.resolve("a.wk").toString();
        String unterverzeichnis = VERZEICHNIS.resolve("sub").resolve("b.wk").toString();
        String anderswo = Path.of("anderswo", "c.wk").toAbsolutePath().toString();
        Veranstaltung vs = veranstaltung(innen, unterverzeichnis, anderswo);

        vs.verweiseRelativieren(VERANSTALTUNGSDATEI);

        assertEquals(List.of("a.wk", unterverzeichnis, anderswo), dateien(vs));
    }

    @Test
    void relativierenVergleichtVerzeichnisseAlsText() {
        Veranstaltung vs = veranstaltung("C:\\Wettkampf\\a.wk", "C:\\wettkampf\\b.wk", "C:\\Wettkampf\\.\\c.wk");

        vs.verweiseRelativieren("C:\\Wettkampf\\event.vs");

        assertEquals(List.of("a.wk", "C:\\wettkampf\\b.wk", "C:\\Wettkampf\\.\\c.wk"), dateien(vs));
    }

    @Test
    void relativierenLaesstLeereUndRelativeDateienUnveraendert() {
        Veranstaltung vs = veranstaltung("", "a.wk");

        vs.verweiseRelativieren(VERANSTALTUNGSDATEI);

        assertEquals(List.of("", "a.wk"), dateien(vs));
    }

    @Test
    void aufloesenErgaenztVerzeichnisBeiDateinamen() {
        Veranstaltung vs = veranstaltung("a.wk", "");

        vs.verweiseAufloesen(VERANSTALTUNGSDATEI);

        assertEquals(List.of(VERZEICHNIS.resolve("a.wk").toString(), ""), dateien(vs));
    }

    @Test
    void aufloesenLaesstPfadeUnveraendert() {
        String unix = Path.of("anderswo", "a.wk").toAbsolutePath().toString();
        Veranstaltung vs = veranstaltung(unix, "C:\\Wettkampf\\b.wk", "sub/c.wk");

        vs.verweiseAufloesen(VERANSTALTUNGSDATEI);

        assertEquals(List.of(unix, "C:\\Wettkampf\\b.wk", "sub/c.wk"), dateien(vs));
    }
}
