package de.df.jauswertung.web.uploadstatus;

/**
 * @param failuresInARow failed uploads at the end of the {@code attempts} last uploads
 * @param lastFailure    failure of the last upload, {@code null} if it succeeded
 */
public record UploadStatus(UploadState state, int failuresInARow, int attempts, UploadFailure lastFailure) {
}
