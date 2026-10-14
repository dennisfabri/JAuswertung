package de.df.jauswertung.test.results.indoorfinals.penalties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.test.results.indoorfinals.penalties.IlsScenario.Art;

/**
 * Every kind of penalty in every situation of the ILS formula with finals. The expected results of all rounds and the
 * overall result are in {@code src/test/resources/results/ils-strafen/<situation>/<fall>.csv}; on a mismatch the
 * actual result is written to {@code target/ils-strafen/}.
 */
class IlsStrafenTest {

    private static final Path ERWARTET = Path.of("src/test/resources/results/ils-strafen");
    private static final Path TATSAECHLICH = Path.of("target/ils-strafen");

    /**
     * @param qualifizierte number of swimmers qualifying for the next round, one entry per round before the final
     */
    private record Situation(String name, int schwimmer, int[] qualifizierte, int runde, int startnummer) {
    }

    private static final List<Situation> SITUATIONEN = List.of(
            new Situation("gerades-finale", 8, new int[0], 0, 2),
            new Situation("finale-16", 16, new int[0], 0, 2),
            new Situation("finale-20", 20, new int[0], 0, 2),
            new Situation("vorlauf", 20, new int[] { 16 }, 0, 2),
            new Situation("vorlauf-am-schnitt", 20, new int[] { 16 }, 0, 16),
            new Situation("a-finale", 20, new int[] { 16 }, 1, 2),
            new Situation("b-finale", 20, new int[] { 16 }, 1, 10),
            new Situation("zwischenlauf-ohne-punkte-weiter", 30, new int[] { 24, 16 }, 1, 2),
            new Situation("zwischenlauf-ohne-punkte-ausgeschieden", 30, new int[] { 24, 16 }, 1, 18),
            new Situation("zwischenlauf-mit-punkten-weiter", 20, new int[] { 16, 8 }, 1, 2),
            new Situation("zwischenlauf-mit-punkten-ausgeschieden", 20, new int[] { 16, 8 }, 1, 10),
            new Situation("finale-nach-zwischenlauf", 20, new int[] { 16, 8 }, 2, 2));

    static Stream<Arguments> faelle() {
        Stream<Arguments> jedeStrafeInJederSituation = SITUATIONEN.stream()
                .flatMap(s -> Stream.of(Art.values()).map(art -> fall(s.name(), dateiname(art), s.schwimmer(),
                        s.qualifizierte(), s.runde(), art, s.startnummer())));
        Stream<Arguments> sonderfaelle = Stream.of(
                fall("gerades-finale", "strafpunkte-zwei-teilnehmer", 8, new int[0], 0, Art.STRAFPUNKTE, 2, 5),
                fall("finale-16", "strafpunkte-unter-null", 16, new int[0], 0, Art.STRAFPUNKTE, 15),
                fall("finale-20", "strafpunkte-ohne-punkteplatz", 20, new int[0], 0, Art.STRAFPUNKTE, 18),
                fall("finale-20", "strafpunkte-jeder-zweite", 20, new int[0], 0, Art.STRAFPUNKTE,
                        IntStream.rangeClosed(1, 20).filter(x -> x % 2 == 0).toArray()),
                fall("a-finale", "strafpunkte-zwei-teilnehmer", 20, new int[] { 16 }, 1, Art.STRAFPUNKTE, 2, 5),
                fall("b-finale", "strafpunkte-letzter", 20, new int[] { 16 }, 1, Art.STRAFPUNKTE, 16),
                fall("zwischenlauf-mit-punkten-ausgeschieden", "strafpunkte-letzter", 20, new int[] { 16, 8 }, 1,
                        Art.STRAFPUNKTE, 16));
        return Stream.concat(jedeStrafeInJederSituation, sonderfaelle);
    }

    private static String dateiname(Art art) {
        return art.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static Arguments fall(String situation, String fall, int schwimmer, int[] qualifizierte, int runde, Art art,
            int... startnummern) {
        return Arguments.of(situation, fall, schwimmer, qualifizierte, runde, art, startnummern);
    }

    @ParameterizedTest(name = "{0}/{1}")
    @MethodSource("faelle")
    void ergebnisse(String situation, String fall, int schwimmer, int[] qualifizierte, int runde, Art art,
            int[] startnummern) throws IOException {
        List<String> actual = IlsScenario.mit(schwimmer, qualifizierte).strafe(runde, art, startnummern).schwimme()
                .ergebnisse();

        Path datei = Path.of(situation, fall + ".csv");
        Path tatsaechlich = TATSAECHLICH.resolve(datei);
        Files.createDirectories(tatsaechlich.getParent());
        Files.write(tatsaechlich, actual, StandardCharsets.UTF_8);

        List<String> expected = Files.exists(ERWARTET.resolve(datei))
                ? Files.readAllLines(ERWARTET.resolve(datei), StandardCharsets.UTF_8)
                : List.of();
        assertEquals(String.join("\n", expected), String.join("\n", actual), datei.toString());
        Files.delete(tatsaechlich);
    }
}
