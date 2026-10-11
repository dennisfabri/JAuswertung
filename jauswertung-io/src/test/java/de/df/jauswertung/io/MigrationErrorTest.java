package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.io.exception.UnreadableFileContentException;

class MigrationErrorTest {

    @Test
    void failingMigrationIsTheCauseOfTheReportedError() {
        // Version 2 runs migration step 3, which fails without a heat list
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <EinzelWettkampf version="2.0">
                </EinzelWettkampf>
                """;

        UnreadableFileContentException e = assertThrows(UnreadableFileContentException.class,
                () -> IOUtils.fromXML(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));

        assertTrue(causes(e).anyMatch(cause -> Stream.of(cause.getStackTrace())
                .anyMatch(frame -> frame.getMethodName().equals("migrator3")
                        && frame.getClassName().endsWith("Laufliste"))),
                "Cause should be the failing migration step");
    }

    private static Stream<Throwable> causes(Throwable t) {
        return Stream.iterate(t, c -> c != null, Throwable::getCause);
    }
}
