package de.df.jauswertung.gui.plugins.upload.competitiondlrgnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
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
        try {
            String body = toJson(results);
            if (hasChanges(uploadId, index, body)) {
                uploadJson(uploadId, index, body);
                markAsUploaded(uploadId, index, body);
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting results to JSON: {}", e.getMessage(), e);
        } catch (IOException ex) {
            log.error("Error uploading results to web: {}", ex.getMessage(), ex);
        }
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

    private void uploadJson(String uploadId, int index, String body) throws IOException {
        try {
            log.info("Uploading results to web.");
            // log.info(body);
            //Files.writeString(Path.of(uploadId + "-" + index + ".json"), body);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(String.join("/",
                                                                                      new String[]{serverUrl, "rest", "result", "import", "jauswertung", uploadId, "" + index}))).timeout(
                    Duration.ofMinutes(2)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            try (HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(
                    Duration.ofSeconds(20)).build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.statusCode());
                System.out.println(response.body());
            }
        } catch (InterruptedException ix) {
            Thread.currentThread().interrupt();
        }
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
    }
}
