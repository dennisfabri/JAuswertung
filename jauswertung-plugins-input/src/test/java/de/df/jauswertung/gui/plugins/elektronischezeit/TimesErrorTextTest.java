package de.df.jauswertung.gui.plugins.elektronischezeit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceExceptions;

class TimesErrorTextTest {

    private static final Set<Reason> WITH_PORT = EnumSet.of(Reason.NOT_RUNNING, Reason.NO_ANSWER, Reason.CONNECTION_FAILED, Reason.WRONG_SERVER,
                                                            Reason.UNREADABLE_DATA);

    @ParameterizedTest
    @EnumSource(Reason.class)
    void everyReasonHasItsOwnTextWithTheSourceAndTheHintToTheLog(Reason reason) {
        String text = JElektronischeZeit.describe(TimesSourceExceptions.of(reason, "zeitnahme", 19999, 404));
        assertAll(() -> assertFalse(text.contains("Error.Times"), text),
                  () -> assertTrue(reason == Reason.FILE_NOT_SET || text.contains("zeitnahme"), text),
                  () -> assertTrue(!WITH_PORT.contains(reason) || text.contains("19999"), text),
                  () -> assertTrue(reason != Reason.WRONG_SERVER || text.contains("HTTP 404"), text),
                  () -> assertTrue(text.endsWith("Details stehen in der Protokolldatei."), text));
    }
}
