package de.df.jauswertung.misc.upload;

import static de.df.jauswertung.io.InputManager.ladeWettkampf;
import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.timesextractor.TimesExtractor;

public class UploadJsonTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UploadJsonTest.class);

    private static final String BASE_PATH = "src/test/resources/samples/";

    // public static final String SERVER_URL = "http://localhost:9001";
    // public static final String UPLOAD_ID = "ef1a370aeb4748f3a769e7e32b251446";

    public static final String SERVER_URL = "https://dev.lisasp.org";
    public static final String UPLOAD_ID = "0927875d-b3f7-4484-ac02-80803fc27049";

    public static void main(String[] args) throws InterruptedException {
        UploadJsonTest upload = new UploadJsonTest();
        upload.clear(0);
        upload.clear(1);
        upload.clear(2);
        upload.clear(3);

        sleep(10000);

        upload.uploadJson(0, "Einzel 4.wk");
        upload.uploadJson(1, "Mannschaft 4.wk");
        upload.uploadJson(2, "Einzel-Outdoor.wk");
        upload.uploadJson(3, "Mannschaft-Outdoor.wk");
    }

    void clear(int index) {
        TimesExtractor extractor = new TimesExtractor();
        JAuswertungCompetition competition = extractor.getZeiten(null);

        new TimesUploader(SERVER_URL).uploadJson(UPLOAD_ID, index, competition);
    }

    void uploadJson(int index, String name) {
        AWettkampf<?> wk = ladeWettkampf(BASE_PATH + name);

        if (wk == null) {
            throw new NullPointerException("Wettkampf nicht geladen: " + name);
        }

        new TimesUploader(SERVER_URL).uploadJson(UPLOAD_ID, index, wk);
    }

}
