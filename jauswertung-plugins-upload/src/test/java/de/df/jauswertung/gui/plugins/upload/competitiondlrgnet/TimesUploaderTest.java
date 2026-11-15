package de.df.jauswertung.gui.plugins.upload.competitiondlrgnet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.web.uploadstatus.UploadFailure;
import de.df.jauswertung.web.uploadstatus.UploadState;
import de.df.jauswertung.web.uploadstatus.UploadStatus;

class TimesUploaderTest {

    private HttpServer server;
    private volatile int responseCode = 200;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(responseCode, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private TimesUploader createUploader(int port) {
        TimesUploader uploader = new TimesUploader("http://localhost:" + port);
        uploader.setActive(true);
        return uploader;
    }

    private TimesUploader createUploader() {
        return createUploader(server.getAddress().getPort());
    }

    @Test
    void successfulUploadIsRecorded() {
        TimesUploader uploader = createUploader();

        uploader.uploadJson("id", 1, new JAuswertungCompetition());

        assertEquals(new UploadStatus(UploadState.OK, 0, 1, null), uploader.getUploadStatus());
    }

    @Test
    void rejectedUploadIsRecordedWithStatusCode() {
        responseCode = 500;
        TimesUploader uploader = createUploader();

        uploader.uploadJson("id", 1, new JAuswertungCompetition());

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 1, UploadFailure.httpStatus(500)),
                uploader.getUploadStatus());
    }

    @Test
    void unreachableServerIsRecorded() throws IOException {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }
        TimesUploader uploader = createUploader(unusedPort);

        uploader.uploadJson("id", 1, new JAuswertungCompetition());

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 1, UploadFailure.connection()),
                uploader.getUploadStatus());
    }

    @Test
    void unchangedResultsAreNotUploadedAgain() {
        TimesUploader uploader = createUploader();

        uploader.uploadJson("id", 1, new JAuswertungCompetition());
        uploader.uploadJson("id", 1, new JAuswertungCompetition());

        assertEquals(1, uploader.getUploadStatus().attempts());
    }

    @Test
    void failedUploadIsRetriedWithoutChanges() {
        responseCode = 500;
        TimesUploader uploader = createUploader();

        uploader.uploadJson("id", 1, new JAuswertungCompetition());
        uploader.uploadJson("id", 1, new JAuswertungCompetition());
        uploader.uploadJson("id", 1, new JAuswertungCompetition());

        assertEquals(UploadState.ERROR, uploader.getUploadStatus().state());
    }
}
