/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.util.Comparator;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Fabri
 * @date 29.05.2009
 */
public class FormelILSSERC<T extends ASchwimmer> extends FormelILS<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "ILS2004SERC";

    @Override
    public String getID() {
        return ID;
    }

    private static final class ILSComparatorSERC<T extends ASchwimmer>
            implements Comparator<SchwimmerData<T>> {

        @Override
        public int compare(SchwimmerData<T> sd1, SchwimmerData<T> sd2) {
            return -(sd1.getTime() - sd2.getTime());
        }
    }

    public FormelILSSERC() {
        super(new ILSComparatorSERC<>());
    }

    @Override
    public String getName() {
        return "ILS Regelwerk - SERC (ohne Finals)";
    }

    @Override
    public String getDescription() {
        return "Diese Punktevergabe entspricht der Punktevergabe für SERC der ILS ohne Vor- und Zwischenläufe";
    }

    @Override
    public DataType getDataType() {
        return DataType.RANK;
    }
}