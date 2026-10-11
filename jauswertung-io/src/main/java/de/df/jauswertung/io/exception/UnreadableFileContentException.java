package de.df.jauswertung.io.exception;

import java.io.IOException;
import java.io.Serial;

/**
 * The content of a file cannot be read, for example because migrating an older format fails. The cause is the
 * original error.
 */
public class UnreadableFileContentException extends IOException {

    @Serial
    private static final long serialVersionUID = 4417385208346193041L;

    public UnreadableFileContentException(Throwable cause) {
        super("File content cannot be read: " + cause, cause);
    }
}
