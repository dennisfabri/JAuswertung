package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

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

    @Test
    void aLineLoggedDirectlyIsWrittenToTheOriginalStreamAndNotLoggedAgain() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();
        ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
        console.setContext(context);
        console.setEncoder(encoder);
        console.start();
        Logger logger = context.getLogger("SystemStreamsToLogTest.direct");
        logger.setAdditive(false);
        logger.addAppender(console);
        PrintStream systemOut = System.out;
        System.setOut(SystemStreamsToLog.wrap(original, lines::add));
        try {
            logger.info("direct");
        } finally {
            System.setOut(systemOut);
            logger.detachAppender(console);
            console.stop();
        }

        assertEquals("direct" + System.lineSeparator(), originalBytes.toString(StandardCharsets.UTF_8));
        assertTrue(lines.isEmpty(), lines.toString());
    }
}
