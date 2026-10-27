package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;

class AresfileTimesSourceTest {

    @TempDir
    Path dir;

    private static TimesSourceException failure(String file) {
        return assertThrows(TimesSourceException.class, () -> AresfileTimesSource.load(file));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  " })
    void noFileIsNotSet(String file) {
        assertEquals(Reason.FILE_NOT_SET, failure(file).getReason());
    }

    @Test
    void aMissingFileIsNotFoundAndNamed() {
        String file = dir.resolve("results.txt").toString();
        TimesSourceException e = failure(file);
        assertEquals(Reason.FILE_NOT_FOUND, e.getReason());
        assertEquals(file, e.getSource());
    }

    @Test
    void aFolderIsUnreadable() {
        assertEquals(Reason.FILE_UNREADABLE, failure(dir.toString()).getReason());
    }
}
