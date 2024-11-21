package de.df.jauswertung.misc.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class TimesUploader {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TimesUploader.class);

    private final String serverUrl;

    public TimesUploader(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void uploadJson(String uploadId, int index, AWettkampf<?> wk) {
        TimesExtractor extractor = new TimesExtractor();
        JAuswertungCompetition competition = extractor.getZeiten(wk);

        uploadJson(uploadId, index, competition);
    }

    public void uploadJson(String uploadId, int index, JAuswertungCompetition results) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String body = mapper.writeValueAsString(results);
            log.info("Uploading results to web.");
            // log.info(body);
            Files.writeString(Path.of(uploadId + "-" + index + ".json"), body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.join("/",
                            new String[] {
                                    serverUrl,
                                    "rest", "result", "import",
                                    "jauswertung", uploadId, "" + index
                            })))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            try (
                    HttpClient client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .connectTimeout(Duration.ofSeconds(20))
                            .build()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.statusCode());
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException iox) {
            iox.printStackTrace();
        }
    }

}
