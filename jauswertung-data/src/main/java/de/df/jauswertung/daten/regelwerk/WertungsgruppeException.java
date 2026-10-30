package de.df.jauswertung.daten.regelwerk;

/**
 * The age groups of a scoring group differ in a setting that must match, so the scoring group cannot be merged into
 * one age group. The message names the scoring group, the setting and the age groups.
 */
public class WertungsgruppeException extends RuntimeException {

    public WertungsgruppeException(String message) {
        super(message);
    }
}
