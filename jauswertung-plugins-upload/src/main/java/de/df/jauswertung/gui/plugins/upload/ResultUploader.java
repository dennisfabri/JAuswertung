package de.df.jauswertung.gui.plugins.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.util.AuthKeyUtils;

public class ResultUploader {

    private static Logger log = LoggerFactory.getLogger(ResultUploader.class);

    private static final String URL = "https://dlrg.net/service.php?doc=apps/liveergebnisse&modus=upload&edvnummer={edvnummer}&wkid={wkid}&auth={authkey}";
    
    private AuthKeyUtils authkeys = new AuthKeyUtils();

    private String lastUpload = "";

    private volatile boolean isActive = false;
    private volatile boolean isDirty = true;

    public void uploadResultsToISC(AWettkampf<?> wk) {
        log.info("Upload");
        if (!isActive) {
            log.info("Upload - not active");
            return;
        }
        if (!isDirty) {
            log.info("Upload - not dirty");
            return;
        }

        try {
            String edvnumber = wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER);
            String competitionId = wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID);

            if (edvnumber == null || edvnumber.trim().length() < 5) {
                return;
            }
            if (competitionId == null || competitionId.trim().length() < 1) {
                return;
            }
            String authkey = authkeys.readAuthKey(edvnumber, competitionId);
            if (authkey == null || authkey.trim().length() < 5) {
                return;
            }

            String url = URL.replace("{edvnummer}", edvnumber).replace("{wkid}", competitionId).replace("{authkey}",
                    authkey);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                boolean ok = ExportManager.export("CSV", bos, ImportExportTypes.RESULTS, wk, null);
                if (ok) {
                    String resultsAsCsv = IOUtils.toString(bos.toByteArray(), "Cp1252");

                    if (lastUpload.equals(resultsAsCsv)) {
                        isDirty = false;
                        return;
                    }
                    if (sendViaHttp(url, resultsAsCsv)) {
                        lastUpload = resultsAsCsv;
                        isDirty = false;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Upload to ISC failed.", ex);
        }
    }

    private boolean sendViaHttp(String url, String resultsAsCsv) throws IOException {
        HttpPut putRequest = new HttpPut(url);
        putRequest.setEntity(new BasicHttpEntity(
                new ByteArrayInputStream(resultsAsCsv.getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_PLAIN));

        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(putRequest)) {
            log.debug("Upload with response code {}: {}", response.getCode(), response.getReasonPhrase());
            return isValid(response.getCode());
        }
    }

    private boolean isValid(int returnCode) {
        return 200 <= returnCode && returnCode < 300;
    }

    public void setDirtyFlag() {
        isDirty = true;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
