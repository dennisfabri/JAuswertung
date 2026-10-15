package de.df.jauswertung.gui.veranstaltung;

import static de.df.jauswertung.gui.veranstaltung.Veranstaltungsbeispiel.schwimmer;
import static de.df.jauswertung.gui.veranstaltung.Veranstaltungsbeispiel.wettkampf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.TableModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.print.util.TableUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;

/**
 * Which competition every age-group column of the event result belongs to.
 */
class VeranstaltungsergebnisTest {

    @TempDir
    Path verzeichnis;

    private static List<String> spalten(Veranstaltungsergebnis ergebnis) {
        TableModel tm = TableUtils.buildGesamtwertungsergebnis(ergebnis.wettkampf(), false, null);
        return ResultsPrinter.wettkampfJeSpalte((ExtendedTableModel) tm, ergebnis.wettkampfnamen());
    }

    private static List<List<String>> seiten(Veranstaltung vs, boolean gliederungen) {
        return Arrays.stream(VeranstaltungsUtils.veranstaltungsergebnisse(vs, gliederungen, true))
                .filter(e -> e.wettkampf().hasSchwimmer()).map(VeranstaltungsergebnisTest::spalten).toList();
    }

    @Test
    void ersteAltersklasseOhnePunkte() {
        Veranstaltung vs = Veranstaltungsbeispiel.ersteAltersklasseOhnePunkte(verzeichnis);

        assertEquals(List.of("Einzel", "Mannschaft"), seiten(vs, true).getFirst());
    }

    @Test
    void altersklasseNurMitSchwimmernAusserKonkurrenz() {
        EinzelWettkampf einzel = wettkampf("AK 12", "AK 13/14");
        schwimmer(einzel, 0, false, "Nord", 6500).setAusserKonkurrenz(true);
        schwimmer(einzel, 1, false, "Nord", 6500);
        EinzelWettkampf mannschaft = wettkampf("AK 12");
        schwimmer(mannschaft, 0, false, "Süd", 6500);
        Veranstaltung vs = new Veranstaltungsbeispiel(verzeichnis).mit("Einzel", einzel).mit("Mannschaft", mannschaft)
                .veranstaltung();

        assertEquals(List.of("Einzel", "Mannschaft"), seiten(vs, true).getFirst());
    }

    @Test
    void wertungNachQualifikationsebene() {
        EinzelWettkampf einzel = wettkampf("AK 12", "AK 13/14");
        schwimmer(einzel, 0, false, "Nord", 6500);
        schwimmer(einzel, 1, false, "Nord", 6500).setQualifikationsebene("Bezirk A");
        EinzelWettkampf mannschaft = wettkampf("AK 12");
        schwimmer(mannschaft, 0, false, "Süd", 6500).setQualifikationsebene("Bezirk B");
        Veranstaltung vs = new Veranstaltungsbeispiel(verzeichnis).mit("Einzel", einzel).mit("Mannschaft", mannschaft)
                .veranstaltung();

        assertEquals(List.of("Einzel", "Mannschaft"), seiten(vs, false).getFirst());
    }

    @Test
    void jedesGeschlechtAufEigenerSeite() {
        EinzelWettkampf einzel = wettkampf("AK 12", "AK 13/14");
        schwimmer(einzel, 0, true, "Nord", 6500);
        schwimmer(einzel, 1, false, "Nord", 6500);
        EinzelWettkampf mannschaft = wettkampf("AK 12");
        schwimmer(mannschaft, 0, false, "Süd", 6500);
        Veranstaltung vs = new Veranstaltungsbeispiel(verzeichnis).mit("Einzel", einzel).mit("Mannschaft", mannschaft)
                .veranstaltung();

        List<List<String>> seiten = seiten(vs, true);

        assertEquals(List.of("Einzel", "Einzel", "Mannschaft"), seiten.getFirst());
        assertEquals(List.of(List.of("Einzel"), List.of("Einzel", "Mannschaft")),
                seiten.subList(1, seiten.size()).stream().sorted((a, b) -> a.size() - b.size()).toList());
    }
}
