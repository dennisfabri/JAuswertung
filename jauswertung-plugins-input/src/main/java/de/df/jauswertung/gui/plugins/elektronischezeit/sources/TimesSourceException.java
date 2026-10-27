package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import lombok.Getter;

/** Why the times could not be loaded; the window that shows the times builds the message from this data. */
@Getter
public final class TimesSourceException extends Exception {

    private static final long serialVersionUID = 1L;

    public enum Reason {
        INVALID_ADDRESS,
        UNKNOWN_HOST,
        NOT_RUNNING,
        NO_ANSWER,
        CONNECTION_FAILED,
        WRONG_SERVER,
        UNREADABLE_DATA,
        FILE_NOT_SET,
        FILE_NOT_FOUND,
        FILE_UNREADABLE
    }

    private final Reason reason;
    /** The address, host or file the times were loaded from. */
    private final String source;
    /** The port of the server; 0 for a file or an invalid address. */
    private final int port;
    /** The HTTP status the server answered with; 0 for every other reason. */
    private final int httpStatus;

    private TimesSourceException(Reason reason, String source, int port, int httpStatus, Throwable cause) {
        super(reason + " " + source + (port > 0 ? ":" + port : "") + (httpStatus > 0 ? " (HTTP " + httpStatus + ")" : ""), cause);
        this.reason = reason;
        this.source = source;
        this.port = port;
        this.httpStatus = httpStatus;
    }

    static TimesSourceException invalidAddress(String address) {
        return new TimesSourceException(Reason.INVALID_ADDRESS, address, 0, 0, null);
    }

    static TimesSourceException server(Reason reason, TimesServerAddress server, Throwable cause) {
        return new TimesSourceException(reason, server.host(), server.port(), 0, cause);
    }

    static TimesSourceException wrongServer(TimesServerAddress server, int httpStatus) {
        return new TimesSourceException(Reason.WRONG_SERVER, server.host(), server.port(), httpStatus, null);
    }

    static TimesSourceException file(Reason reason, String file, Throwable cause) {
        return new TimesSourceException(reason, file, 0, 0, cause);
    }
}
