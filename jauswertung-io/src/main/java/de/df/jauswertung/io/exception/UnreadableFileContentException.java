package de.df.jauswertung.io.exception;

import java.io.IOException;

/**
 * The content of a file cannot be read, for example because migrating an older format fails. The cause is the
 * original error.
 */
public class UnreadableFileContentException extends IOException {

    public UnreadableFileContentException(Throwable cause) {
        super("File content cannot be read: " + cause, cause);
    }
}
