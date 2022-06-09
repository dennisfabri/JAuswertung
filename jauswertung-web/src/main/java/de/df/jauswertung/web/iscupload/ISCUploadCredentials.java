package de.df.jauswertung.web.iscupload;

import java.util.regex.Pattern;

public final class ISCUploadCredentials {

    private Pattern edvPattern = Pattern.compile("^[0-9]{6,8}$");
    private Pattern integerPattern = Pattern.compile("^[0-9]+$");
    private Pattern authPattern = Pattern.compile("^[0-9a-z]{14}\\.?[0-9]{8}$");

    private final String edvnumber;
    private final String wkId;
    private final String authKey;

    public ISCUploadCredentials(String edvnumber, String wkId, String authKey) {
        this.edvnumber = edvnumber == null ? "" : edvnumber.trim();
        this.wkId = wkId == null ? "" : wkId.trim();
        this.authKey = authKey == null ? "" : authKey.trim();
    }

    public String getEdvnumber() {
        return edvnumber;
    }

    public String getWkId() {
        return wkId;
    }

    public String getAuthKey() {
        return authKey;
    }

    public boolean isValid() {
        return isEDVNumberValid() && isWkIdValid() && isAuthKeyValid();
    }

    private boolean isEDVNumberValid() {
        return edvPattern.matcher(edvnumber).matches();
    }

    private boolean isWkIdValid() {
        if (wkId.isEmpty()) {
            return false;
        }
        return integerPattern.matcher(wkId).matches();
    }

    private boolean isAuthKeyValid() {
        if (authKey.isEmpty()) {
            return true;
        }
        return authPattern.matcher(authKey).matches();
    }
}
