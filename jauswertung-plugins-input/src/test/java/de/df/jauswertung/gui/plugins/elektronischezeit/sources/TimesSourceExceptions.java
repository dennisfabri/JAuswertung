package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;

/** Builds each kind of failure for tests outside this package. */
public final class TimesSourceExceptions {

    private TimesSourceExceptions() {
    }

    public static TimesSourceException of(Reason reason, String source, int port, int httpStatus) {
        return switch (reason) {
            case INVALID_ADDRESS -> TimesSourceException.invalidAddress(source);
            case FILE_NOT_SET, FILE_NOT_FOUND, FILE_UNREADABLE -> TimesSourceException.file(reason, source, null);
            case WRONG_SERVER -> TimesSourceException.wrongServer(new TimesServerAddress(source, port), httpStatus);
            default -> TimesSourceException.server(reason, new TimesServerAddress(source, port), null);
        };
    }
}
