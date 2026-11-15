package de.df.jauswertung.web.iscupload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.web.uploadstatus.UploadFailure;
import de.df.jauswertung.web.uploadstatus.UploadState;
import de.df.jauswertung.web.uploadstatus.UploadStatus;

class ResultUploaderTest {

    private ResultUploader uploader;

    private IExporter exporter;
    private ISCUploadCredentialRepository repository;
    private SimpleHttpClient httpClient;
    private AWettkampf<?> wk;

    @BeforeEach
    void prepare() throws Exception {
        exporter = mock(IExporter.class);
        given(exporter.canExport(any())).willReturn(true);
        given(exporter.export(any())).willReturn("test");

        repository = mock(ISCUploadCredentialRepository.class);
        given(repository.getCredentials(any(), any())).willReturn("12345678");

        httpClient = mock(SimpleHttpClient.class);
        given(httpClient.put(any(), any())).willReturn(200);

        wk = mock(AWettkampf.class);
        given(wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER)).willReturn("1234567");
        given(wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID)).willReturn("42");

        uploader = new ResultUploader(exporter, repository, httpClient);
        uploader.setActive(true);
    }

    @Test
    void withoutCompetitionNothingIsUploaded() {
        uploader.uploadResultsToISC(null);

        verifyNoMoreInteractions(exporter);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(httpClient);
        assertEquals(0, uploader.getUploadStatus().attempts());
    }

    @Test
    void successfulUploadIsRecorded() {
        uploader.uploadResultsToISC(wk);

        assertEquals(new UploadStatus(UploadState.OK, 0, 1, null), uploader.getUploadStatus());
    }

    @Test
    void rejectedUploadIsRecordedWithStatusCode() throws Exception {
        given(httpClient.put(any(), any())).willReturn(503);

        uploader.uploadResultsToISC(wk);

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 1, UploadFailure.httpStatus(503)),
                uploader.getUploadStatus());
    }

    @Test
    void unreachableServerIsRecorded() throws Exception {
        given(httpClient.put(any(), any())).willThrow(new IOException("unreachable"));

        uploader.uploadResultsToISC(wk);

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 1, UploadFailure.connection()),
                uploader.getUploadStatus());
    }

    @Test
    void failedExportIsRecorded() throws Exception {
        given(exporter.export(any())).willThrow(new IOException("export"));

        uploader.uploadResultsToISC(wk);

        assertEquals(new UploadStatus(UploadState.WARNING, 1, 1, UploadFailure.preparation()),
                uploader.getUploadStatus());
    }

    @Test
    void unchangedResultsAreNotUploadedAgain() {
        uploader.uploadResultsToISC(wk);
        uploader.setDirtyFlag();
        uploader.uploadResultsToISC(wk);

        assertEquals(1, uploader.getUploadStatus().attempts());
    }

    @Test
    void failedUploadIsRetriedWithoutChanges() throws Exception {
        given(httpClient.put(any(), any())).willReturn(503);

        uploader.uploadResultsToISC(wk);
        uploader.uploadResultsToISC(wk);
        uploader.uploadResultsToISC(wk);

        assertEquals(UploadState.ERROR, uploader.getUploadStatus().state());
    }
}
