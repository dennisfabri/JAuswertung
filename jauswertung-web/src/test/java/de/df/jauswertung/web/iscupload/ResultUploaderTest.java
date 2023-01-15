package de.df.jauswertung.web.iscupload;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.NoMoreInteractions;

class ResultUploaderTest {

    private ResultUploader uploader;

    private IExporter exporter;
    private ISCUploadCredentialRepository repository;
    private SimpleHttpClient httpClient;

    @BeforeEach
    void prepare() throws Exception {
        exporter = mock(IExporter.class);
        given(exporter.canExport(any())).willReturn(true);
        given(exporter.export(any())).willReturn("test");

        repository = mock(ISCUploadCredentialRepository.class);
        given(repository.getCredentials(any(), any())).willReturn("12345678");
        // given(repository.putCredentials(any())).willReturn("test");

        httpClient = mock(SimpleHttpClient.class);
        given(httpClient.put(any(), any())).willReturn(true);

        uploader = new ResultUploader(exporter, repository, httpClient);
    }

    @Test
    void test() {
        uploader.uploadResultsToISC(null);

        verifyNoMoreInteractions(exporter);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(httpClient);
        // verify(exporter, times(0)).canExport(any());
    }
}