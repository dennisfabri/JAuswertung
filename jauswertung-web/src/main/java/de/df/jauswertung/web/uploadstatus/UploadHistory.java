package de.df.jauswertung.web.uploadstatus;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Remembers the outcome of the last uploads to one system; written by the upload thread, read by the UI.
 */
public final class UploadHistory {

    private static final int FAILURES_IN_A_ROW_FOR_ERROR = 3;

    private final Deque<Optional<UploadFailure>> uploads = new ArrayDeque<>();

    public synchronized void recordSuccess() {
        record(Optional.empty());
    }

    public synchronized void recordFailure(UploadFailure failure) {
        record(Optional.of(failure));
    }

    private void record(Optional<UploadFailure> upload) {
        uploads.addLast(upload);
        if (uploads.size() > FAILURES_IN_A_ROW_FOR_ERROR) {
            uploads.removeFirst();
        }
    }

    public synchronized void clear() {
        uploads.clear();
    }

    public synchronized UploadStatus getStatus() {
        int failuresInARow = 0;
        UploadFailure lastFailure = null;
        for (Optional<UploadFailure> upload : uploads) {
            if (upload.isPresent()) {
                failuresInARow++;
                lastFailure = upload.get();
            } else {
                failuresInARow = 0;
                lastFailure = null;
            }
        }
        return new UploadStatus(toState(failuresInARow), failuresInARow, uploads.size(), lastFailure);
    }

    private static UploadState toState(int failuresInARow) {
        if (failuresInARow >= FAILURES_IN_A_ROW_FOR_ERROR) {
            return UploadState.ERROR;
        }
        return failuresInARow > 0 ? UploadState.WARNING : UploadState.OK;
    }
}
