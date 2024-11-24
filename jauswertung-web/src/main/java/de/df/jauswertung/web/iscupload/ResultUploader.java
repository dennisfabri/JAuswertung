package de.df.jauswertung.web.iscupload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;

public class ResultUploader {

    private static final Logger log = LoggerFactory.getLogger(ResultUploader.class);

    private static final String URL = "https://dlrg.net/service.php?doc=apps/liveergebnisse&modus=upload&edvnummer={edvnummer}&wkid={wkid}&auth={authkey}";

    private final ISCUploadCredentialRepository authKeys;
    private final IExporter exporter;
    private final SimpleHttpClient httpClient;

    private String lastUpload = "";

    private volatile boolean isActive = false;
    private volatile boolean isDirty = true;

    public ResultUploader() {
        this(new CSVResultExporter(), new ISCUploadCredentialRepository(), new SimpleHttpClient());
    }

    public ResultUploader(IExporter exporter, ISCUploadCredentialRepository authKeys, SimpleHttpClient httpClient) {
        this.exporter = exporter;
        this.authKeys = authKeys;
        this.httpClient = httpClient;
    }

    public void uploadResultsToISC(AWettkampf<?> wk) {
        if (!isDirty) {
            log.debug("Upload - not dirty");
            return;
        }

        try {
            if (wk == null) {
                log.debug("Upload - no competition");
                return;
            }

            String edvNumber = wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER);
            String competitionId = wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID);

            if (edvNumber == null || edvNumber.trim().length() < 5) {
                log.debug("Upload - edvNumber not configured");
                return;
            }
            if (competitionId == null || competitionId.trim().isEmpty()) {
                log.debug("Upload - competitionId not configured");
                return;
            }
            String authkey = authKeys.getCredentials(edvNumber, competitionId);
            if (authkey == null || authkey.trim().length() < 5) {
                log.debug("Upload - authkey not configured");
                return;
            }

            String url = URL.replace("{edvnummer}", edvNumber).replace("{wkid}", competitionId).replace("{authkey}",
                    authkey);

            if (!exporter.canExport(wk)) {
                log.debug("Upload - No Data available");
                return;
            }

            String resultsAsCsv = exporter.export(wk);
            if (resultsAsCsv == null || resultsAsCsv.isEmpty()) {
                log.debug("Upload - Export not generated");
                return;
            }

            if (lastUpload.equals(resultsAsCsv)) {
                log.debug("Upload - No changes detected");
                isDirty = false;
                return;
            }
            if (httpClient.put(url, resultsAsCsv)) {
                lastUpload = resultsAsCsv;
                isDirty = false;
            }
        } catch (Exception ex) {
            log.warn("Upload to ISC failed.", ex);
        }
    }

    public void setDirtyFlag() {
        isDirty = true;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
