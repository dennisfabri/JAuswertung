package de.df.jauswertung.test.io.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Teilnehmer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.TableEntryException;
import de.df.jauswertung.io.TableException;
import de.df.jauswertung.io.TableFormatException;
import de.df.jauswertung.io.portal.PortalImporter;
import de.df.jutils.util.Feedback;

class PortalImporterTests {

    private PortalImporter importer;

    @BeforeEach
    void setup() {
        importer = new PortalImporter();
    }

    @Test
    void suffixTest() {
        String[] actual = importer.getSuffixes();

        assertArrayEquals(new String[] { ".json" }, actual);
    }

    @Test
    void importiereLeereMeldung() throws IOException, TableFormatException, TableEntryException, TableException {
        Strafen strafen = new Strafen();
        Regelwerk aks = InputManager.ladeAKs("/src/test/resources/rulebooks/DLRG 2023.rwm");
        MannschaftWettkampf wk = new MannschaftWettkampf(aks, strafen);
        Feedback fb = null;
        try (InputStream is = getClass().getResourceAsStream("/portal/Empty.json")) {
            LinkedList<Mannschaft> actual = importer.registration(is, wk, fb, null, "Empty");

            assertNotNull(actual);
            assertEquals(0, actual.size());
        }
    }

    @Test
    void importierMeldungMitLeerenGliederungen()
            throws IOException, TableFormatException, TableEntryException, TableException {
        Strafen strafen = new Strafen();
        Regelwerk aks = InputManager.ladeAKs("/src/test/resources/rulebooks/DLRG 2023.rwm");
        MannschaftWettkampf wk = new MannschaftWettkampf(aks, strafen);
        Feedback fb = null;
        try (InputStream is = getClass().getResourceAsStream("/portal/LeereGliederungen.json")) {
            LinkedList<Mannschaft> actual = importer.registration(is, wk, fb, null, "LeereGliederungen");

            assertNotNull(actual);
            assertEquals(0, actual.size());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void importiereMeldungMitEinerMannschaft(boolean maennlich)
            throws IOException, TableFormatException, TableEntryException, TableException {
        Strafen strafen = new Strafen();
        Regelwerk aks = InputManager.ladeAKs("/src/test/resources/rulebooks/DLRG 2023.rwm");
        MannschaftWettkampf wk = new MannschaftWettkampf(aks, strafen);
        Feedback fb = null;
        try (InputStream is = getClass()
                .getResourceAsStream("/portal/Mannschaft-1OG-" + (maennlich ? "male" : "female") + ".json")) {
            LinkedList<Mannschaft> actual = importer.registration(is, wk, fb, null, "Mannschaft-1OG");

            assertNotNull(actual);
            assertEquals(1, actual.size());

            Mannschaft m1 = actual.get(0);
            assertEquals("Test-Team", m1.getName());
            assertEquals("Test-OG", m1.getGliederung());
            assertEquals("", m1.getQualifikationsebene());
            assertEquals(maennlich, m1.isMaennlich());
            assertEquals("AK Offen", m1.getAK().getName());
            assertEquals(1234.56, m1.getMeldepunkte(0));
            assertEquals("8e15a3a2-905a-48be-9f4e-1065ea2b194b", m1.getImportId());
            assertEquals("Bemerkungstest", m1.getBemerkung());

            assertEquals(8345, m1.getMeldezeit(0));
            assertEquals(15456, m1.getMeldezeit(1));
            assertEquals(22567, m1.getMeldezeit(2));
            assertEquals(29678, m1.getMeldezeit(3));

            assertEquals(4, m1.getMannschaftsmitgliederAnzahl());

            List<Mannschaftsmitglied> mitglieder = List.of(m1.getMannschaftsmitglied(0), m1.getMannschaftsmitglied(1),
                    m1.getMannschaftsmitglied(2), m1.getMannschaftsmitglied(3)).stream()
                    .sorted(Comparator.comparingInt(Mannschaftsmitglied::getJahrgang)).toList();
            Mannschaftsmitglied mm1 = mitglieder.get(0);
            Mannschaftsmitglied mm2 = mitglieder.get(1);
            Mannschaftsmitglied mm3 = mitglieder.get(2);
            Mannschaftsmitglied mm4 = mitglieder.get(3);

            assertEquals("VN1 NN1", mm1.getName());
            assertEquals(maennlich ? Geschlecht.maennlich : Geschlecht.weiblich, mm1.getGeschlecht());
            assertEquals(1999, mm1.getJahrgang());
            assertEquals("407c7e5b-e4f7-4e2a-9d14-1c5f9cfc2f4c", mm1.getImportId());

            assertEquals("VN2 NN2", mm2.getName());
            assertEquals(maennlich ? Geschlecht.maennlich : Geschlecht.weiblich, mm2.getGeschlecht());
            assertEquals(2000, mm2.getJahrgang());
            assertEquals("375424bc-f631-4488-ad1f-74f28ae336ff", mm2.getImportId());

            assertEquals("VN3 NN3", mm3.getName());
            assertEquals(maennlich ? Geschlecht.maennlich : Geschlecht.weiblich, mm3.getGeschlecht());
            assertEquals(2001, mm3.getJahrgang());
            assertEquals("05cfdfd9-bb1e-4dd7-989d-faab7f17c1f7", mm3.getImportId());

            assertEquals("VN4 NN4", mm4.getName());
            assertEquals(maennlich ? Geschlecht.maennlich : Geschlecht.weiblich, mm4.getGeschlecht());
            assertEquals(2002, mm4.getJahrgang());
            assertEquals("77b0a206-f8ec-4a8c-8cc3-5f7e4f7f6edf", mm4.getImportId());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void importiereMeldungMitEinemTeilnehmer(boolean maennlich)
            throws IOException, TableFormatException, TableEntryException, TableException {
        Strafen strafen = new Strafen();
        Regelwerk aks = InputManager.ladeAKs("/src/test/resources/rulebooks/DLRG 2023.rwe");
        EinzelWettkampf wk = new EinzelWettkampf(aks, strafen);
        Feedback fb = null;
        try (InputStream is = getClass()
                .getResourceAsStream("/portal/Einzel-1OG-" + (maennlich ? "male" : "female") + ".json")) {
            LinkedList<Teilnehmer> actual = importer.registration(is, wk, fb, null, "Einzel-1OG");

            assertNotNull(actual);
            assertEquals(1, actual.size());

            Teilnehmer m1 = actual.get(0);
            assertEquals("NX1, TN1", m1.getName());
            assertEquals("Beispiel OG", m1.getGliederung());
            assertEquals("", m1.getQualifikationsebene());
            assertEquals(maennlich, m1.isMaennlich());
            assertEquals("AK 17/18", m1.getAK().getName());
            assertEquals(2345.67, m1.getMeldepunkte(0));
            assertEquals("6acc739a-b0f2-4695-9a65-5bde3f923f66", m1.getImportId());
            assertEquals("Bemerkungstest Einzel", m1.getBemerkung());

            assertArrayEquals(new boolean[] { true, false, true, true, true, false }, m1.getDisciplineChoice());

            assertEquals(12000, m1.getMeldezeit(0));
            assertEquals(166999, m1.getMeldezeit(2));
            assertEquals(8345, m1.getMeldezeit(3));
            assertEquals(358949, m1.getMeldezeit(4));
        }
    }
}
