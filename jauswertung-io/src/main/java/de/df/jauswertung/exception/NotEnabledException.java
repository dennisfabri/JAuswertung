/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.exception;

/**
 * @author Dennis Mueller
 * @date 08.01.2005
 */
public class NotEnabledException extends RuntimeException {

    private static final long serialVersionUID = 2320779078523041573L;

    public NotEnabledException() {
        super();
    }

    public NotEnabledException(String message) {
        super(message);
    }
}
