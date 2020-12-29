/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Fabri
 * @date 29.05.2009
 */
public class FormelILSOutdoorFinals<T extends ASchwimmer> extends FormelILSFinals<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "ILS2004OUTDOORFinals";

    @Override
    public String getID() {
        return ID;
    }

    /**
     * 
     */
    public FormelILSOutdoorFinals() {
        super();
    }

    @Override
    public String getName() {
        return "ILS Regelwerk (Beta) - Outdoor (mit Finals)";
    }

    @Override
    public String getDescription() {
        return "Diese Punktevergabe entspricht der Punktevergabe für Outdoorwettkämpfe der ILS mit Finals";
    }

    @Override
    public DataType getDataType() {
        return DataType.RANK;
    }
}