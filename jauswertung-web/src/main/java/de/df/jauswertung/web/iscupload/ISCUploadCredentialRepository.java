package de.df.jauswertung.web.iscupload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class ISCUploadCredentialRepository {

    public void putCredentials(ISCUploadCredentials credentials) {
        try {
            if (!credentials.isValid()) {
                return;
            }

            ensureDirectoryExists();

            Path path = generateFilename(credentials);
            if (credentials.getAuthKey().isEmpty()) {
                Files.deleteIfExists(path);
            } else {
                try (PrintWriter writer = new PrintWriter(path.toFile(), StandardCharsets.UTF_8)) {
                    writer.append(credentials.getAuthKey().trim());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getCredentials(String edvnumber, String competitionId) {
        try {
            if (edvnumber == null || competitionId == null) {
                return "";
            }
            edvnumber = edvnumber.trim();
            competitionId = competitionId.trim();
            if (edvnumber.length() < 5 || competitionId.isEmpty()) {
                return "";
            }
            return new String(Files.readAllBytes(generateFilename(edvnumber, competitionId)), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private void ensureDirectoryExists() throws IOException {
        String userhome = SystemUtils.getUserHome().getCanonicalPath();
        new File(Paths.get(userhome, ".JAuswertungHome", "ISCAuth").toFile().getAbsolutePath()).mkdirs();
    }

    private Path generateFilename(String edvnumber, String wkId) throws IOException {
        String userhome = SystemUtils.getUserHome().getCanonicalPath();
        return Paths.get(userhome, ".JAuswertungHome", "ISCAuth",
                String.format("%sx%s.authkey", edvnumber, wkId));
    }

    private Path generateFilename(ISCUploadCredentials credentials) throws IOException {
        return generateFilename(credentials.getEdvnumber(), credentials.getWkId());
    }
}
