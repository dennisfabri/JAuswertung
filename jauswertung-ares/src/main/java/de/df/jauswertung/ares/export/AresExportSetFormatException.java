package de.df.jauswertung.ares.export;

import java.nio.file.Path;

import lombok.Getter;

@Getter
public final class AresExportSetFormatException extends Exception {

    private final Path file;

    public AresExportSetFormatException(Path file, String message) {
        super(message);
        this.file = file;
    }

    public AresExportSetFormatException(Path file, Throwable cause) {
        super(cause);
        this.file = file;
    }
}
