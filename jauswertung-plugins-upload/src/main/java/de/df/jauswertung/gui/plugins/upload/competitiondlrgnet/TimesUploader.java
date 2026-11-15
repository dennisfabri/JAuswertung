package de.df.jauswertung.gui.plugins.upload.competitiondlrgnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.web.uploadstatus.UploadFailure;
import de.df.jauswertung.web.uploadstatus.UploadHistory;
import de.df.jauswertung.web.uploadstatus.UploadStatus;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TimesUploader {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TimesUploader.class);

    private final String serverUrl;
    private final UploadHistory history = new UploadHistory();

    private boolean active = false;

    private String lastUploadId = "";
    private int lastUploadIndex = -1;
    private String lastUploadedJson = "";

    public TimesUploader(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void uploadJson(String uploadId, int index, AWettkampf<?> wk) {
        if (!active) {
            return;
        }

        TimesExtractor extractor = new TimesExtractor();
        JAuswertungCompetition competition = extractor.getZeiten(wk);

        uploadJson(uploadId, index, competition);
    }

    public void uploadJson(String uploadId, int index, JAuswertungCompetition results) {
        if (!active) {
            return;
        }
        String body;
        try {
            body = toJson(results);
        } catch (JsonProcessingException e) {
            log.error("Error converting results to JSON: {}", e.getMessage(), e);
            history.recordFailure(UploadFailure.preparation());
            return;
        }
        if (hasChanges(uploadId, index, body)) {
            upload(uploadId, index, body);
        }
    }

    private void upload(String uploadId, int index, String body) {
        int statusCode;
        try {
            statusCode = uploadJson(uploadId, index, body);
        } catch (IOException ex) {
            log.error("Error uploading results to web: {}", ex.getMessage(), ex);
            history.recordFailure(UploadFailure.connection());
            return;
        } catch (InterruptedException ix) {
            Thread.currentThread().interrupt();
            return;
        }
        if (isErrorCode(statusCode)) {
            log.error("Error uploading results to web. Response code: {}", statusCode);
            history.recordFailure(UploadFailure.httpStatus(statusCode));
            return;
        }
        markAsUploaded(uploadId, index, body);
        history.recordSuccess();
    }

    private void markAsUploaded(String uploadId, int index, String json) {
        lastUploadId = uploadId;
        lastUploadIndex = index;
        lastUploadedJson = json;
    }

    private boolean hasChanges(String uploadId, int index, String body) {
        if (nothingChanged(uploadId, index, body)) {
            log.info("No changes detected. Skipping upload.");
            return false;
        }
        return true;
    }

    private int uploadJson(String uploadId, int index, String body) throws IOException, InterruptedException {
        log.info("Uploading results to web.");
        String url = String.join("/", serverUrl, "rest", "result", "import", "jauswertung", uploadId, "" + index);
        log.debug("Uploading results to URL: {}", url);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(2)).header("Content-Type", "application/json").PUT(
                HttpRequest.BodyPublishers.ofString(body)).build();
        try (HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(
                Duration.ofSeconds(20)).build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Upload response with code {}:\n {}", response.statusCode(), response.body());
            return response.statusCode();
        }
    }

    private boolean isErrorCode(int code) {
        return code < 200 || code >= 300;
    }

    private static String toJson(JAuswertungCompetition results) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(results);
    }

    private boolean nothingChanged(String uploadId, int index, String body) {
        return uploadId.equals(lastUploadId) && index == lastUploadIndex && body.equals(lastUploadedJson);
    }

    public void uploadJson(AWettkampf<?> wk) {
        if (!active) {
            return;
        }

        String uploadId = wk.getStringProperty(PropertyConstants.UPLOAD_ID, "");
        int index = wk.getIntegerProperty(PropertyConstants.UPLOAD_INDEX, 0);
        if (uploadId.isBlank()) {
            log.warn("No upload id found in competition.");
            return;
        }
        if (index < 0) {
            log.warn("Invalid upload index found in competition.");
            return;
        }
        uploadJson(uploadId, index, wk);
    }

    public void setActive(boolean active) {
        this.active = active;
        history.clear();
    }

    public UploadStatus getUploadStatus() {
        return history.getStatus();
    }
}
