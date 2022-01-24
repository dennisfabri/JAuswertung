package de.df.jauswertung.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class AuthKeyUtils {

    private Path generateFilename(String edvnumber, String competitionId) throws IOException {
        String userhome = SystemUtils.getUserHome().getCanonicalPath();
        new File(Paths.get(userhome, ".JAuswertungHome", "ISCAuth").toFile().getAbsolutePath()).mkdirs();
        return Paths.get(userhome, ".JAuswertungHome", "ISCAuth", edvnumber + "x" + competitionId + ".authkey");
    }

    public void writeAuthKey(String edvnumber, String competitionId, String authkey) {
        try {
            if (edvnumber == null || competitionId == null) {
                return;
            }
            edvnumber = edvnumber.trim();
            competitionId = edvnumber.trim();
            if (edvnumber.length() < 5 || competitionId.isEmpty()) {
                return;
            }

            Path path = generateFilename(edvnumber, competitionId);
            if (authkey == null || authkey.trim().length() == 0) {
                path.toFile().delete();
            } else {
                try (PrintWriter writer = new PrintWriter(path.toFile(), StandardCharsets.UTF_8)) {
                    writer.append(authkey.trim());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String readAuthKey(String edvnumber, String competitionId) {
        try {
            if (edvnumber == null || competitionId == null) {
                return "";
            }
            edvnumber = edvnumber.trim();
            competitionId = edvnumber.trim();
            if (edvnumber.length() < 5 || competitionId.isEmpty()) {
                return "";
            }

            Path path = generateFilename(edvnumber, competitionId);
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
