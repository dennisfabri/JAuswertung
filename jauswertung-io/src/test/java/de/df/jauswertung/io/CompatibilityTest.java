package de.df.jauswertung.io;

import static de.df.jauswertung.daten.PropertyConstants.ART_DES_WETTKAMPFS;
import static de.df.jauswertung.daten.PropertyConstants.DSM_MODE_DATA;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_FIRST_HEAT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANE_SELECTION;
import static de.df.jauswertung.daten.PropertyConstants.UPLOAD_ID;
import static de.df.jauswertung.daten.PropertyConstants.URKUNDE;
import static de.df.jauswertung.daten.PropertyConstants.URKUNDE_EINZELWERTUNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.NullFeedback;

/**
 * Guards file compatibility while the data structures are being streamlined. The fixtures in
 * {@code src/test/resources/compatibility/<year>/} are created once a year by
 * {@code ErzeugeKompatibilitaetsWettkaempfe} and never changed. The tests pin the meaning of these files, not their XML structure: a pure refactoring of the model
 * must not change a single CSV snapshot.
 * <p>
 * Missing snapshots are written on the first run and the test fails until they are reviewed and committed. On a
 * mismatch the current output is written next to the snapshot as {@code *_actual.csv}.
 * <p>
 * XStream does not run constructors or field initializers when reading: a new field is {@code null} in every old file.
 * A removed or {@code transient} field still present in old files fails to load unless it is omitted or migrated in
 * {@link IOUtils#getXStream()}.
 */
class CompatibilityTest {

    private static final Path BASIS = Path.of("src/test/resources/compatibility");
    private static final String ACTUAL = "_actual.csv";
    private static final Set<ImportExportTypes> ALLE = EnumSet.allOf(ImportExportTypes.class);

    private static Locale locale;

    @BeforeAll
    static void deutschesZahlenformat() {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @AfterAll
    static void zahlenformatZuruecksetzen() {
        Locale.setDefault(locale);
    }

    static Stream<Path> dateien() throws IOException {
        try (Stream<Path> dateien = Files.walk(BASIS, 2)) {
            return dateien.filter(p -> p.toString().endsWith(".wk")).map(BASIS::relativize).sorted().toList()
                    .stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void laedtMitPassendemTyp(Path datei) throws IOException {
        AWettkampf<?> wk = laden(BASIS.resolve(datei));

        Class<?> erwartet = datei.getFileName().toString().contains("Mannschaft") ? MannschaftWettkampf.class
                : EinzelWettkampf.class;
        assertInstanceOf(erwartet, wk);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void exporteEntsprechenDenSnapshots(Path datei) throws IOException {
        vergleiche(datei, exporte(laden(BASIS.resolve(datei))), "", true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void exporteNachSpeichernUndLaden(Path datei, @TempDir Path temp) throws IOException {
        AWettkampf<?> wk = nachSpeichernUndLaden(laden(BASIS.resolve(datei)), temp);

        vergleiche(datei, exporte(wk), "_roundtrip", false);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void referenzenNachSpeichernUndLaden(Path datei, @TempDir Path temp) throws IOException {
        AWettkampf<?> wk = laden(BASIS.resolve(datei));
        pruefeReferenzen(wk);
        pruefeReferenzen(nachSpeichernUndLaden(wk, temp));
    }

    /**
     * Undo/redo keeps its snapshots as such copies.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void exporteUndReferenzenNachKopie(Path datei) throws IOException {
        AWettkampf<?> kopie = Utils.copy(laden(BASIS.resolve(datei)));

        pruefeReferenzen(kopie);
        vergleiche(datei, exporte(kopie), "_copy", false);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void datenOhneExport(Path datei, @TempDir Path temp) throws IOException {
        AWettkampf<?> wk = laden(BASIS.resolve(datei));
        assumeFalse(wk.isHeatBased(), "Finale enthalten nur Runden");

        int jahr = Integer.parseInt(datei.getName(0).toString());
        pruefeDatenOhneExport(wk, jahr);
        pruefeDatenOhneExport(nachSpeichernUndLaden(wk, temp), jahr);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateien")
    void rundenSindVollstaendig(Path datei) throws IOException {
        AWettkampf<?> wk = laden(BASIS.resolve(datei));
        assumeFalse(!wk.isHeatBased(), "Nur für Finale");

        OWSelection[] runden = OWUtils.getCreatedRounds(wk, false);
        assertTrue(runden.length > 0, "Keine Runden");
        assertTrue(Stream.of(runden).anyMatch(r -> r.round >= 2), "Keine Runde nach einem Halbfinale");
        assertEquals(0, OWUtils.getCreatableRounds(wk).length, "Nicht alle Runden erzeugt");
    }

    @SuppressWarnings("rawtypes")
    private static AWettkampf<?> laden(Path datei) throws IOException {
        AWettkampf wk = InputManager.ladeWettkampf(datei.toString());
        if (wk == null) {
            parseDirekt(datei);
            fail("Laden fehlgeschlagen: " + datei);
        }
        return wk;
    }

    /**
     * InputManager swallows the exception; parsing again directly reveals the XStream path of the failing element.
     */
    private static void parseDirekt(Path datei) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(datei))) {
            for (ZipEntry eintrag = zip.getNextEntry(); eintrag != null; eintrag = zip.getNextEntry()) {
                if (eintrag.getName().equals("data.xml")) {
                    try (InputStream xml = new ByteArrayInputStream(zip.readAllBytes())) {
                        IOUtils.fromXML(xml);
                    }
                }
            }
        }
    }

    private static AWettkampf<?> nachSpeichernUndLaden(AWettkampf<?> wk, Path temp) throws IOException {
        Path datei = temp.resolve("roundtrip.wk");
        assertTrue(OutputManager.speichereWettkampf(datei.toString(), wk), "Speichern fehlgeschlagen");
        return laden(datei);
    }

    private static Map<String, byte[]> exporte(AWettkampf<?> wk) {
        Map<String, byte[]> exporte = new TreeMap<>();
        wk.setCurrentFilterIndex(0);
        CsvExporter csv = new CsvExporter();
        for (ImportExportTypes typ : ALLE) {
            if (csv.isSupported(typ) && ExportManager.isEnabled(wk, typ)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (ExportManager.export(csv, out, typ, wk, new NullFeedback())) {
                    exporte.put(typ.name() + ".csv", out.toByteArray());
                }
            }
        }
        return exporte;
    }

    private static void vergleiche(Path datei, Map<String, byte[]> exporte, String variante, boolean anlegen)
            throws IOException {
        Path verzeichnis = BASIS.resolve(datei).resolveSibling(datei.getFileName().toString().replace(".wk", ""));
        List<String> fehler = new ArrayList<>();
        List<String> neu = new ArrayList<>();

        for (Map.Entry<String, byte[]> export : exporte.entrySet()) {
            Path snapshot = verzeichnis.resolve(export.getKey());
            Path actual = verzeichnis.resolve(export.getKey().replace(".csv", variante + ACTUAL));
            if (!Files.exists(snapshot)) {
                if (anlegen) {
                    Files.createDirectories(snapshot.getParent());
                    Files.write(snapshot, export.getValue());
                    neu.add(export.getKey());
                } else {
                    fehler.add(export.getKey() + ": Snapshot fehlt");
                }
            } else if (!zeilen(Files.readAllBytes(snapshot)).equals(zeilen(export.getValue()))) {
                Files.write(actual, export.getValue());
                fehler.add(export.getKey() + ": " + ersterUnterschied(Files.readAllBytes(snapshot), export.getValue())
                        + " (siehe " + actual.getFileName() + ")");
            } else {
                Files.deleteIfExists(actual);
            }
        }

        if (Files.isDirectory(verzeichnis)) {
            try (Stream<Path> snapshots = Files.walk(verzeichnis)) {
                for (Path snapshot : snapshots.filter(Files::isRegularFile)
                        .filter(p -> !p.toString().endsWith(ACTUAL)).toList()) {
                    String name = verzeichnis.relativize(snapshot).toString().replace('\\', '/');
                    if (!exporte.containsKey(name)) {
                        fehler.add(name + ": wird nicht mehr exportiert");
                    }
                }
            }
        }

        if (!neu.isEmpty()) {
            fehler.add("Neue Snapshots geschrieben, bitte prüfen und committen: " + neu);
        }
        if (!fehler.isEmpty()) {
            fail(datei + "\n  " + String.join("\n  ", fehler));
        }
    }

    /**
     * The CSV writer uses the platform line separator.
     */
    private static List<String> zeilen(byte[] csv) {
        return new String(csv, StandardCharsets.UTF_8).lines().toList();
    }

    private static String ersterUnterschied(byte[] erwartet, byte[] tatsaechlich) {
        List<String> a = zeilen(erwartet);
        List<String> b = zeilen(tatsaechlich);
        for (int x = 0; x < Math.max(a.size(), b.size()); x++) {
            String zeileA = x < a.size() ? a.get(x) : "<Ende>";
            String zeileB = x < b.size() ? b.get(x) : "<Ende>";
            if (!zeileA.equals(zeileB)) {
                return "Zeile " + (x + 1) + "\n    erwartet:    " + zeileA + "\n    tatsächlich: " + zeileB;
            }
        }
        return "Unterschied in der Kodierung";
    }

    private static <T extends ASchwimmer> void pruefeReferenzen(AWettkampf<T> wk) {
        Set<T> schwimmer = Collections.newSetFromMap(new IdentityHashMap<>());
        schwimmer.addAll(wk.getSchwimmer());

        Set<Integer> startnummern = wk.getSchwimmer().stream().map(ASchwimmer::getStartnummer)
                .collect(Collectors.toSet());
        assertEquals(wk.getSchwimmer().size(), startnummern.size(), "Startnummern nicht eindeutig");

        if (wk.getLaufliste().getLaufliste() != null) {
            for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
                for (int bahn = 0; bahn < lauf.getBahnen(); bahn++) {
                    T s = lauf.getSchwimmer(bahn);
                    assertTrue(s == null || schwimmer.contains(s), "Lauf " + lauf.getName() + " Bahn " + (bahn + 1));
                }
            }
        }
        for (Zielrichterentscheid<T> ze : wk.getZielrichterentscheide()) {
            for (T s : ze.getSchwimmer()) {
                assertTrue(schwimmer.contains(s), "Zielrichterentscheid " + s.getStartnummer());
            }
        }
        for (OWDisziplin<T> disziplin : wk.getLauflisteOW().getDisziplinen()) {
            for (OWLauf<T> lauf : disziplin.getLaeufe()) {
                for (int bahn = 0; bahn < lauf.getBahnen(); bahn++) {
                    T s = lauf.getSchwimmer(bahn);
                    assertTrue(s == null || schwimmer.contains(s), disziplin.Id + " Bahn " + (bahn + 1));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void pruefeDatenOhneExport(AWettkampf<?> wk, int jahr) {
        assertNotNull(wk.getLogo(), "Logo");
        if (jahr >= 2023) {
            assertTrue(((Hashtable<String, Object>[]) wk.getProperty(URKUNDE)).length > 0, "Urkunde");
            assertTrue(((Hashtable<String, Object>[]) wk.getProperty(URKUNDE_EINZELWERTUNG)).length > 0,
                    "Urkunde Einzelwertung");
        }
        assertFalse(((Hashtable<String, boolean[]>) wk.getProperty(HEATS_LANE_SELECTION)).isEmpty(), "Bahnauswahl");
        assertInstanceOf(int[][][].class, wk.getProperty(DSM_MODE_DATA), "DSM-Daten");
        if (jahr >= 2025) {
            assertFalse(wk.getStringProperty(UPLOAD_ID, "").isEmpty(), "Upload-Id");
        }

        assertNotEquals(Wettkampfart.SonstigerWettkampf, wk.getProperty(ART_DES_WETTKAMPFS), "Art des Wettkampfs");
        assertNotEquals(1, wk.getIntegerProperty(HEATS_FIRST_HEAT), "Erster Lauf");
        assertNotEquals(0, wk.getCurrentFilterIndex(), "Aktiver Filter");
        assertNotNull(wk.getFilter()[wk.getCurrentFilterIndex()].getGliederungen(), "Gliederungen im Filter");

        assertTrue(wk.getTimelimits().getTimelimits().length > 0, "Pflichtzeiten");
        assertNotNull(wk.getZulassungslimits(), "Zulassungslimits");
        assertTrue(wk.getKampfrichterverwaltung().getEinheitenCount() > 0, "Kampfrichter");
        assertFalse(wk.getZielrichterentscheide().isEmpty(), "Zielrichterentscheide");
        if (wk instanceof MannschaftWettkampf mwk) {
            assertTrue(mwk.getTeammembersRegistrationIds().length > 0, "Mannschaftsmeldungskennungen");
        }

        Regelwerk aks = wk.getRegelwerk();
        assertTrue(aks.getStartgruppen().length > 0, "Startgruppen");
        assertTrue(aks.getWertungsgruppen().length > 0, "Wertungsgruppen");
        assertNotEquals(GroupEvaluationMode.All, aks.getGesamtwertungsmodus(), "Gesamtwertungsmodus");
        assertNotEquals(Skalierungsmodus.KEINER, aks.getGesamtwertungSkalieren(), "Skalierung");
        assertNotEquals(200, aks.getZusatzwertungBasispunkte(), "Basispunkte");

        List<? extends ASchwimmer> schwimmer = wk.getSchwimmer();
        assertTrue(new HashSet<>(schwimmer.stream().map(ASchwimmer::getQualifikation).toList()).size() > 1,
                "Qualifikationen");
        assertTrue(new HashSet<>(schwimmer.stream().map(ASchwimmer::getStartunterlagen).toList()).size() > 1,
                "Startunterlagen");
        if (jahr >= 2025) {
            assertTrue(schwimmer.stream().allMatch(s -> s.getImportId() != null), "Import-Ids");
        }
        if (jahr >= 2026) {
            assertTrue(schwimmer.stream().anyMatch(s -> s.getMeldePlatz() > 0), "Meldeplatz");
        }
        assertTrue(schwimmer.stream().anyMatch(ASchwimmer::hasDopingkontrolle), "Dopingkontrolle");
        assertTrue(schwimmer.stream().anyMatch(ASchwimmer::isAusserKonkurrenz), "Außer Konkurrenz");
    }
}
