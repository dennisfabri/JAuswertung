/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.io.Serializable;
import java.util.Comparator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;

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

    @SuppressWarnings("rawtypes")
    private static final class ILSComparatorSERC implements Comparator<SchwimmerData>, Serializable {

        private static final long serialVersionUID = -7952635514437923875L;

        @Override
        public int compare(SchwimmerData sd1, SchwimmerData sd2) {
            if (sd1.getStrafart() != sd2.getStrafart()) {
                if ((sd1.getStrafart() == Strafarten.AUSSCHLUSS)) {
                    return 1;
                }
                if ((sd2.getStrafart() == Strafarten.AUSSCHLUSS)) {
                    return -1;
                }
                if ((sd1.getStrafart() == Strafarten.DISQUALIFIKATION)) {
                    return 1;
                }
                if ((sd2.getStrafart() == Strafarten.DISQUALIFIKATION)) {
                    return -1;
                }
                if ((sd1.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                    return 1;
                }
                if ((sd2.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                    return -1;
                }
            }
            if (sd1.isWithdraw() != sd2.isWithdraw()) {
                return sd1.isWithdraw() ? 1 : -1;
            }
            return -(sd1.getTime() - sd2.getTime());
        }
    }

    public FormelILSSERC() {
        super(new ILSComparatorSERC());
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