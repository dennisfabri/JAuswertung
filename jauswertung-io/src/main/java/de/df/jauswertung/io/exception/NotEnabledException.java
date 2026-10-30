/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.io.exception;

/**
 * @author Dennis Fabri
 * @date 08.01.2005
 */
public class NotEnabledException extends RuntimeException {

    public NotEnabledException() {
        super();
    }

    public NotEnabledException(String message) {
        super(message);
    }
}
