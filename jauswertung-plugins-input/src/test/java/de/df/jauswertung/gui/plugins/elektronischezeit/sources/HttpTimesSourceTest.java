package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lisasp.alphatiming.legacy.api.Heat;
import org.lisasp.alphatiming.legacy.api.LaneStatus;
import org.lisasp.alphatiming.legacy.api.LegacySerialization;

import com.sun.net.httpserver.HttpServer;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;

class HttpTimesSourceTest {

    private static final Duration SHORT = Duration.ofMillis(500);

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private TimesServerAddress serve(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] data = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, data.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        });
        server.start();
        return new TimesServerAddress("localhost", server.getAddress().getPort());
    }

    private static int freePort() throws IOException {
        try (ServerSocket probe = new ServerSocket(0)) {
            return probe.getLocalPort();
        }
    }

    private static TimesSourceException failure(TimesServerAddress address) {
        return assertThrows(TimesSourceException.class, () -> HttpTimesSource.load(address, SHORT, SHORT));
    }

    @Test
    void loadsTheHeatsOfTheServer() throws Exception {
        Heat heat = new Heat("1", 3, 2);
        heat.store(1, 61234, LaneStatus.ResultsOfTheRace);
        Heat[] heats = HttpTimesSource.load(serve(200, LegacySerialization.toXML(new Heat[] { heat })), SHORT, SHORT);
        assertEquals(1, heats.length);
        assertEquals(heat, heats[0]);
    }

    @Test
    void anInvalidAddressNamesTheAddress() {
        TimesSourceException e = assertThrows(TimesSourceException.class, () -> HttpTimesSource.load("host:port", SHORT, SHORT));
        assertEquals(Reason.INVALID_ADDRESS, e.getReason());
        assertEquals("host:port", e.getSource());
    }

    @Test
    void nothingListeningOnThePortIsNotRunning() throws Exception {
        TimesSourceException e = failure(new TimesServerAddress("localhost", freePort()));
        assertEquals(Reason.NOT_RUNNING, e.getReason());
        assertEquals("localhost", e.getSource());
    }

    @Test
    void anUnknownHostIsReportedAsSuch() {
        assertEquals(Reason.UNKNOWN_HOST, failure(new TimesServerAddress("does-not-exist.invalid", 1999)).getReason());
    }

    @Test
    void aServerThatDoesNotAnswerInTimeGivesNoAnswer() throws Exception {
        try (ServerSocket silent = new ServerSocket(0)) {
            TimesSourceException e = failure(new TimesServerAddress("localhost", silent.getLocalPort()));
            assertEquals(Reason.NO_ANSWER, e.getReason());
            assertEquals(silent.getLocalPort(), e.getPort());
        }
    }

    @Test
    void anotherHttpStatusIsAWrongServerWithTheStatus() throws Exception {
        TimesSourceException e = failure(serve(404, "Not found"));
        assertEquals(Reason.WRONG_SERVER, e.getReason());
        assertEquals(404, e.getHttpStatus());
    }

    @Test
    void anAnswerThatIsNoHttpCannotBeToldFromAClosedConnection() throws Exception {
        try (ServerSocket other = new ServerSocket(0)) {
            Thread answering = new Thread(() -> {
                try (Socket s = other.accept()) {
                    s.getInputStream().read(new byte[256]);
                    s.getOutputStream().write("220 mail.example.org ESMTP\r\n".getBytes(StandardCharsets.US_ASCII));
                } catch (IOException ex) {
                    // the test fails on the assertion instead
                }
            });
            answering.start();
            assertEquals(Reason.CONNECTION_FAILED, failure(new TimesServerAddress("localhost", other.getLocalPort())).getReason());
            answering.join();
        }
    }

    @Test
    void aClosedConnectionIsAFailedConnection() throws Exception {
        try (ServerSocket closing = new ServerSocket(0)) {
            Thread closer = new Thread(() -> {
                try (Socket s = closing.accept()) {
                    // closes at once, as AlphaTiming does when too many requests wait
                } catch (IOException ex) {
                    // the test fails on the assertion instead
                }
            });
            closer.start();
            assertEquals(Reason.CONNECTION_FAILED, failure(new TimesServerAddress("localhost", closing.getLocalPort())).getReason());
            closer.join();
        }
    }

    @Test
    void dataThatAreNoHeatsAreUnreadable() throws Exception {
        assertEquals(Reason.UNREADABLE_DATA, failure(serve(200, "<html>Hello</html>")).getReason());
    }
}
