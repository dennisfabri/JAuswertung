package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;

/**
 * Scoring groups as they are used in real competitions: AK 17/18 and AK Offen, later also AK 15/16. DLRG 2019 ships
 * such a group; the later rulebooks have none, so the test adds it as the organizers do.
 */
class WertungsgruppenRegelwerkeTest {

    private static final Set<String> SENIOREN = Set.of("AK 15/16", "AK 17/18", "AK Offen");

    static Stream<String> regelwerke() {
        List<String> result = new ArrayList<>();
        for (int jahr : new int[] { 2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026 }) {
            result.add("DLRG " + jahr + ".rwe");
            result.add("DLRG " + jahr + ".rwm");
        }
        return result.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regelwerke")
    void wertungsgruppeIstGueltig(String datei) {
        Regelwerk regelwerk = regelwerk(datei);

        assertTrue(regelwerk.isValid());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("regelwerke")
    void wertungsgruppeWirdZusammengefasst(String datei) {
        AWettkampf<ASchwimmer> wk = wettkampf(regelwerk(datei));
        Wertungsgruppe wg = wk.getRegelwerk().getWertungsgruppen()[0];
        int mitglieder = (int) Stream.of(wk.getRegelwerk().getAks())
                .filter(ak -> wg.getName().equals(ak.getWertungsgruppe())).count();

        AWettkampf<ASchwimmer> merged = CompetitionUtils.generateWertungsgruppenwettkampf(wk);

        Altersklasse ak = Stream.of(merged.getRegelwerk().getAks()).filter(a -> a.getName().equals(wg.getName()))
                .findFirst().orElseThrow();
        assertEquals(2 * mitglieder, SearchUtils.getSchwimmer(merged, ak).size());
        if (wg.isProtokollMitEinzelwertung()) {
            AWettkampf<ASchwimmer> einzel = ResultUtils.generateEinzelwertungswettkampf(wk, wg.getName(), false);
            assertNotNull(einzel);
            assertEquals(ak.getDiszAnzahl(), einzel.getRegelwerk().size());
        }
    }

    private static Regelwerk regelwerk(String datei) {
        Regelwerk regelwerk = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/" + datei);
        assertNotNull(regelwerk, datei);
        if (regelwerk.getWertungsgruppen().length == 0) {
            Wertungsgruppe senioren = new Wertungsgruppe("Senioren");
            senioren.setProtokollMitEinzelwertung(true);
            senioren.setProtokollMitMehrkampfwertung(true);
            regelwerk.setWertungsgruppen(new Wertungsgruppe[] { senioren });
            for (Altersklasse ak : regelwerk.getAks()) {
                if (SENIOREN.contains(ak.getName())) {
                    ak.setWertungsgruppe(senioren.getName());
                }
            }
        }
        return regelwerk;
    }

    /** One swimmer per member age group and gender. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static AWettkampf<ASchwimmer> wettkampf(Regelwerk regelwerk) {
        AWettkampf wk = regelwerk.isEinzel() ? new EinzelWettkampf(regelwerk, new Strafen())
                : new MannschaftWettkampf(regelwerk, new Strafen());
        for (int x = 0; x < regelwerk.size(); x++) {
            if (regelwerk.getAk(x).getWertungsgruppe() == null) {
                continue;
            }
            for (boolean maennlich : new boolean[] { false, true }) {
                ASchwimmer s;
                if (wk instanceof EinzelWettkampf ewk) {
                    Teilnehmer t = ewk.createTeilnehmer("", "Nachname", "Vorname", 2000, maennlich, "Gliederung", x,
                            "");
                    s = t;
                } else {
                    Mannschaft m = ((MannschaftWettkampf) wk).createMannschaft("Team", maennlich, "Gliederung", x, "");
                    s = m;
                }
                wk.addSchwimmer(s);
                for (int d = 0; d < regelwerk.getAk(x).getDiszAnzahl(); d++) {
                    s.setZeit(d, 6000 + 100 * d);
                }
            }
        }
        return wk;
    }
}
