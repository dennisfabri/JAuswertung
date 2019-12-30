/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Fabri
 * @date 29.05.2009
 */
public class FormelILSOutdoor<T extends ASchwimmer> extends FormelILS<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "ILS2004OUTDOOR";

    @Override
    public String getID() {
        return ID;
    }

    /**
     * 
     */
    public FormelILSOutdoor() {
        super();
    }

    @Override
    public String getName() {
        return "ILS Regelwerk - Outdoor (ohne Finals)";
    }

    @Override
    public String getDescription() {
        return "Diese Punktevergabe entspricht der Punktevergabe für Outdoorwettkämpfe der ILS ohne Vor- und Zwischenläufe";
    }

    @Override
    public DataType getDataType() {
        return DataType.RANK;
    }
}