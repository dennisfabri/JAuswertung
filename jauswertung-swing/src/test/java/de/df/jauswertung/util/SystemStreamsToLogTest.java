package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SystemStreamsToLogTest {

    private final ByteArrayOutputStream originalBytes = new ByteArrayOutputStream();
    private final PrintStream original = new PrintStream(originalBytes, true, StandardCharsets.UTF_8);
    private final List<String> lines = new ArrayList<>();

    @Test
    void completedLinesAreLoggedAndNotWrittenToTheOriginalStream() {
        PrintStream stream = SystemStreamsToLog.wrap(original, lines::add);

        stream.println("first");
        stream.print("second\r\n");
        stream.print("incomplete");

        assertEquals(List.of("first", "second"), lines);
        assertEquals("", originalBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void umlautsSurvive() {
        PrintStream stream = SystemStreamsToLog.wrap(original, lines::add);

        stream.println("Größe äöü");

        assertEquals(List.of("Größe äöü"), lines);
    }

    @Test
    void outputWrittenWhileLoggingGoesToTheOriginalStreamAndIsNotLoggedAgain() {
        PrintStream[] stream = new PrintStream[1];
        stream[0] = SystemStreamsToLog.wrap(original, line -> {
            lines.add(line);
            stream[0].println("echo of " + line);
        });

        stream[0].println("message");

        assertEquals(List.of("message"), lines);
        assertEquals("echo of message" + System.lineSeparator(), originalBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void outputWrittenToTheOtherStreamWhileLoggingIsNotLoggedEither() {
        ByteArrayOutputStream otherBytes = new ByteArrayOutputStream();
        PrintStream otherOriginal = new PrintStream(otherBytes, true, StandardCharsets.UTF_8);
        List<String> otherLines = new ArrayList<>();
        PrintStream other = SystemStreamsToLog.wrap(otherOriginal, otherLines::add);
        PrintStream stream = SystemStreamsToLog.wrap(original, line -> {
            lines.add(line);
            other.println("appender wrote: " + line);
        });

        stream.println("an error");

        assertEquals(List.of("an error"), lines);
        assertTrue(otherLines.isEmpty(), otherLines.toString());
        assertEquals("appender wrote: an error" + System.lineSeparator(), otherBytes.toString(StandardCharsets.UTF_8));
    }
}
