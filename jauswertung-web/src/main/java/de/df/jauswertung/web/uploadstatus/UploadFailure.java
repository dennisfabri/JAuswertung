package de.df.jauswertung.web.uploadstatus;

/**
 * @param statusCode HTTP status code of the server's answer; only set for {@link UploadFailureReason#HTTP_STATUS}
 */
public record UploadFailure(UploadFailureReason reason, int statusCode) {

    public static UploadFailure preparation() {
        return new UploadFailure(UploadFailureReason.PREPARATION, 0);
    }

    public static UploadFailure connection() {
        return new UploadFailure(UploadFailureReason.CONNECTION, 0);
    }

    public static UploadFailure httpStatus(int statusCode) {
        return new UploadFailure(UploadFailureReason.HTTP_STATUS, statusCode);
    }
}
