package de.df.jauswertung.test.results.dem24;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.InputManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DEM24Tests {

    @ParameterizedTest
    @ValueSource(strings = { "DEM2024_Einzel_Final_anonym", "DEM2024_Einzel_Vorlauf_anonym",
            "DEM2024_Mannschaft_Vorlauf_Final_anonym", "DEM2024_Mixed_Vorlauf_Final_anonym" })
    void test(String name) throws IOException {
        String[] expected = Files
                .readAllLines(Paths.get("src/test/resources/competitions/dem24/" + name + ".csv"),
                        StandardCharsets.ISO_8859_1)
                .toArray(String[]::new);

        AWettkampf<?> wk = InputManager.ladeWettkampf("src/test/resources/competitions/dem24/" + name + ".wk");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ExportManager.export("CSV", bos, ImportExportTypes.RESULTS, wk, null);
        String[] actual = bos.toString(StandardCharsets.ISO_8859_1).replace("\r\n", "\n").split("\n");
        Files.write(Paths.get("src/test/resources/competitions/dem24/" + name + "_actual.csv"), bos.toByteArray());

        assertArrayEquals(expected, actual);
    }
}
