package de.df.jauswertung.web.uploadstatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UploadHistoryTest {

    private static final UploadFailure CONNECTION = UploadFailure.connection();
    private static final UploadFailure HTTP_500 = UploadFailure.httpStatus(500);

    private final UploadHistory history = new UploadHistory();

    @Test
    void withoutUploadsTheStateIsOk() {
        assertEquals(new UploadStatus(UploadState.OK, 0, 0, null), history.getStatus());
    }

    @Test
    void successfulUploadsAreOk() {
        history.recordSuccess();
        history.recordSuccess();

        assertEquals(new UploadStatus(UploadState.OK, 0, 2, null), history.getStatus());
    }

    @Test
    void failedLastUploadIsAWarning() {
        history.recordSuccess();
        history.recordFailure(HTTP_500);

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 2, HTTP_500), history.getStatus());
    }

    @Test
    void successAfterAFailureIsOk() {
        history.recordFailure(HTTP_500);
        history.recordSuccess();

        assertEquals(new UploadStatus(UploadState.OK, 0, 2, null), history.getStatus());
    }

    @Test
    void twoFailuresInARowAreAWarning() {
        history.recordSuccess();
        history.recordFailure(HTTP_500);
        history.recordFailure(CONNECTION);

        assertEquals(new UploadStatus(UploadState.WARNING, 2, 3, CONNECTION), history.getStatus());
    }

    @Test
    void lastThreeUploadsFailedIsAnError() {
        history.recordSuccess();
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);
        history.recordFailure(CONNECTION);

        assertEquals(new UploadStatus(UploadState.ERROR, 3, 3, CONNECTION), history.getStatus());
    }

    @Test
    void successAfterThreeFailuresIsOk() {
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);
        history.recordSuccess();

        assertEquals(new UploadStatus(UploadState.OK, 0, 3, null), history.getStatus());
    }

    @Test
    void failureAfterASuccessStartsAnewWithAWarning() {
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);
        history.recordSuccess();
        history.recordFailure(CONNECTION);

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 3, CONNECTION), history.getStatus());
    }

    @Test
    void clearForgetsAllUploads() {
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);
        history.recordFailure(HTTP_500);

        history.clear();

        assertEquals(new UploadStatus(UploadState.OK, 0, 0, null), history.getStatus());
    }
}
