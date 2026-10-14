package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.model.StartersImportDto;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jutils.util.Feedback;

class ImportSheetLoopTest {

    private static final String EINZEL = "src/test/resources/compatibility/2026/Einzel.wk";
    private static final String MANNSCHAFT = "src/test/resources/compatibility/2026/Mannschaft.wk";

    @FunctionalInterface
    interface Import {
        Object run(AWettkampf<ASchwimmer> wk, Feedback fb, String[] sheets, Object[][][] tables) throws Exception;
    }

    private record Fall(String name, String datei, Import importer) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final Fall MITGLIEDER = new Fall("Mannschaftsmitglieder", MANNSCHAFT,
            (wk, fb, s, t) -> ImportUtils.tablesToTeammembers(wk, fb, s, t));
    private static final Fall STARTER = new Fall("Starter", MANNSCHAFT,
            (wk, fb, s, t) -> ImportUtils.tablesToStarters(wk, fb, s, t));
    private static final Fall ZW = new Fall("Zusatzwertung", EINZEL,
            (wk, fb, s, t) -> ImportUtils.tablesToZWResult(wk, fb, s, t, null));
    private static final Fall MELDUNG = new Fall("Meldung", EINZEL,
            (wk, fb, s, t) -> ImportUtils.tablesToRegistration(wk, fb, s, t, null));
    private static final Fall AKTUALISIERUNG = new Fall("Meldeaktualisierung", EINZEL,
            (wk, fb, s, t) -> ImportUtils.tablesToRegistrationUpdate(wk, fb, s, t, null));
    private static final Fall KAMPFRICHTER = new Fall("Kampfrichter", EINZEL,
            (wk, fb, s, t) -> ImportUtils.tablesToReferees(wk, fb, s, t, null));

    @SuppressWarnings("unchecked")
    private static AWettkampf<ASchwimmer> wettkampf(String datei) {
        return (AWettkampf<ASchwimmer>) InputManager.ladeWettkampf(datei);
    }

    private static List<String> importiere(Fall fall, AWettkampf<ASchwimmer> wk, Object[][] tabelle) {
        List<String> feedback = new ArrayList<>();
        try {
            fall.importer().run(wk, feedback::add, new String[] { "Blatt" }, new Object[][][] { tabelle });
        } catch (Exception e) {
            // Imports without results end with an exception; only the feedback is checked here
        }
        return feedback;
    }

    static Stream<Fall> alleImporte() {
        return Stream.of(MITGLIEDER, STARTER, ZW, MELDUNG, AKTUALISIERUNG, KAMPFRICHTER);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("alleImporte")
    void leeresBlattWirdGemeldetUndUebersprungen(Fall fall) {
        List<String> feedback = importiere(fall, wettkampf(fall.datei()), new Object[0][0]);

        assertEquals(I18n.get("ImportingSheet", "Blatt"), feedback.get(0));
        assertEquals(I18n.get("Error.SheetEmpty"), feedback.get(1));
        assertTrue(feedback.contains(I18n.get("Error.NoValidSheetFound")), feedback::toString);
    }

    static Stream<Arguments> fehlendeSpalten() {
        String zw = wettkampf(EINZEL).getRegelwerk().getZusatzwertungShort();
        return Stream.of(Arguments.of(MITGLIEDER, List.of("Startnumber", "Surname", "FirstName")),
                Arguments.of(STARTER, List.of("Startnumber", "Discipline")),
                Arguments.of(ZW, List.of("Startnumber", "#" + zw)),
                Arguments.of(MELDUNG, List.of("Organisation", "AgeGroup", "Sex", "FirstName", "FamilyName")),
                Arguments.of(KAMPFRICHTER, List.of("Category", "Position", "Name", "Level")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fehlendeSpalten")
    void fehlendeSpaltenWerdenEinheitlichGemeldet(Fall fall, List<String> spalten) {
        Object[][] tabelle = { { "Unbekannt" }, { "x" } };

        List<String> feedback = importiere(fall, wettkampf(fall.datei()), tabelle);

        String namen = "\t" + String.join(", ",
                spalten.stream().map(s -> s.startsWith("#") ? s.substring(1) : I18n.get(s)).toList());
        assertTrue(feedback.contains(I18n.get("Error.NotAllHeadersFound", namen)), feedback::toString);
        assertTrue(feedback.contains(I18n.get("Error.NoValidSheetFound")), feedback::toString);
    }

    @Test
    @SuppressWarnings("unchecked")
    void zusatzwertungWirdEingelesen() throws Exception {
        AWettkampf<ASchwimmer> wk = wettkampf(EINZEL);
        ASchwimmer s = wk.getSchwimmer().getFirst();
        Object[][] tabelle = { { "Startnummer", wk.getRegelwerk().getZusatzwertungShort() },
                { "" + s.getStartnummer(), 150.0 } };

        Hashtable<ZWStartnummer, Double> result = (Hashtable<ZWStartnummer, Double>) ZW.importer().run(wk,
                t -> {
                }, new String[] { "Blatt" }, new Object[][][] { tabelle });

        assertEquals(List.of(150.0), new ArrayList<>(result.values()));
    }

    @Test
    void kampfrichterWerdenEingelesen() throws Exception {
        AWettkampf<ASchwimmer> wk = wettkampf(EINZEL);
        KampfrichterEinheit einheit = wk.getKampfrichterverwaltung().getEinheit(0);
        String position = einheit.getPositionen()[0];
        Object[][] tabelle = { { "Kategorie", "Position", "Name", "Stufe" },
                { einheit.getName(), position, "Mustermann", "D12" } };

        KampfrichterVerwaltung kv = (KampfrichterVerwaltung) KAMPFRICHTER.importer().run(wk, t -> {
        }, new String[] { "Blatt" }, new Object[][][] { tabelle });

        assertEquals("Mustermann", kv.getEinheit(einheit.getName()).getKampfrichter(position)[0].getName());
    }

    @Test
    void starterWerdenEingelesen() throws Exception {
        AWettkampf<ASchwimmer> wk = wettkampf(MANNSCHAFT);
        Mannschaft m = (Mannschaft) wk.getSchwimmer().getFirst();
        String disziplin = m.getAK().getDisziplin(0, m.isMaennlich()).getName();
        Object[][] tabelle = { { "Startnummer", "Disziplin", "Starter1", "Starter2" },
                { "" + m.getStartnummer(), disziplin, "2", "1" } };

        StartersImportDto result = (StartersImportDto) STARTER.importer().run(wk, t -> {
        }, new String[] { "Blatt" }, new Object[][][] { tabelle });

        assertEquals(1, result.teams().size());
        assertEquals(m.getStartnummer(), result.teams().getFirst().getStartnumber());
    }
}
