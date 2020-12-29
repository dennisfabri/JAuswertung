/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;

/**
 * @author Dennis Fabri
 * @date 10.06.2007
 */
public class FormelILS<T extends ASchwimmer> implements Formel<T> {

    @SuppressWarnings("rawtypes")
    private static final class ILSComparator implements Comparator<SchwimmerData>, Serializable {

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
            return sd1.getTime() - sd2.getTime();
        }
    }

    public static final String ID = "ILS2004";

    private final Comparator<SchwimmerData> comparator;
    
    @Override
    public String getID() {
        return ID;
    }

    public FormelILS() {
        this(new ILSComparator());
    }
    
    protected FormelILS(Comparator<SchwimmerData> comparator) {
        this.comparator = comparator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.daten.regelwerk.Formel#getFormel()
     */
    @Override
    public String getFormel() {
        return "Platz 1: 20 Punkte, Platz 2: 18 Punkte, ..., Platz 16: 1 Punkt";
    }

    @Override
    public String toString() {
        return getFormel();
    }

    @Override
    public String getName() {
        return "ILS Regelwerk - Indoor (ohne Finals)";
    }

    @Override
    public String getDescription() {
        return "Diese Punktevergabe entspricht der Punktevergabe f�r Indoorwettk�mpfe der ILS ohne Finals";
    }

    private static final double Epsilon = 0.005; 
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d,
            Hashtable<String, Zielrichterentscheid<T>> zes) {
        if (swimmer.length == 0) {
            return;
        }

        SchwimmerData[] sold = swimmer;
        Arrays.sort(sold, comparator);

        LinkedList<SchwimmerData> sd = new LinkedList<SchwimmerData>();
        LinkedList<SchwimmerData> others = new LinkedList<SchwimmerData>();
        for (SchwimmerData aSwimmer : swimmer) {
            if ((aSwimmer.getTime() == 0) || (aSwimmer.getStrafart() == Strafarten.AUSSCHLUSS)
                    || (aSwimmer.getStrafart() == Strafarten.DISQUALIFIKATION)
                    || (aSwimmer.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                aSwimmer.setRank(-1);
                aSwimmer.setPoints(0.0);
                aSwimmer.setTime(0);
            } else {
                if (aSwimmer.getSchwimmer().isAusserKonkurrenz()) {
                    others.addLast(aSwimmer);
                } else {
                    sd.addLast(aSwimmer);
                }
            }
        }
        SchwimmerData[] other = others.toArray(new SchwimmerData[others.size()]);
        Arrays.sort(other, comparator);

        swimmer = sd.toArray(new SchwimmerData[sd.size()]);
        Arrays.sort(swimmer, comparator);

        double oldResults = Double.MIN_VALUE;
        int disCounter = 0;
        int pos = 1;
        for (int x = 0; x < swimmer.length; x++) {
            if (Math.abs(swimmer[x].getTime() - oldResults) > Epsilon) {
                for (int y = pos - 1; y < x; y++) {
                    if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE)
                            || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                        swimmer[y].setPoints(getPoints(swimmer[y].getTime(), d.getRec(), pos, x - pos + 1 - disCounter, Strafe.NICHTS));
                    }
                }
                disCounter = 0;
                pos = (x + 1);
                oldResults = swimmer[x].getTime();
            }
            swimmer[x].setRank(pos);
            if ((swimmer[x].getStrafart() == Strafarten.NICHT_ANGETRETEN)
                    || (swimmer[x].getStrafart() == Strafarten.DISQUALIFIKATION)
                    || (swimmer[x].getStrafart() == Strafarten.AUSSCHLUSS)) {
                disCounter++;
                swimmer[x].setTime(0);
            }
            if (swimmer[x].isWithdraw()) {
                swimmer[x].setRank(-1);
            }
        }
        for (int y = pos - 1; y < swimmer.length; y++) {
            if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE)
                    || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                swimmer[y].setPoints(getPoints(swimmer[y].getTime(), d.getRec(), pos, swimmer.length - pos + 1 - disCounter, Strafe.NICHTS));
            }
        }

        // Set point for "ausser konkurrenz" swimmers
        for (SchwimmerData anOther : other) {
            if (anOther.getTime() > 0) {
                int y = 0;
                while ((y < swimmer.length) && (swimmer[y].getTime() < anOther.getTime())
                        && (swimmer[y].getTime() > 0)) {
                    y++;
                }
                if (y == swimmer.length) {
                    if (y == 0) {
                        anOther.setPoints(getPoints(1));
                    } else {
                        anOther.setPoints(getPoints(swimmer[y - 1].getRank() + 1));
                    }
                } else {
                    if (swimmer[y].getTime() > anOther.getTime()) {
                        anOther.setPoints(getPoints(swimmer[y].getRank()));
                    } else {
                        anOther.setPoints(swimmer[y].getPoints());
                    }
                }
            } else {
                anOther.setPoints(0);
            }
            anOther.setRank(-1);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public SchwimmerResult<T>[] toResults(SchwimmerResult<T>[] results, AWettkampf<T> wk, Altersklasse ak,
            Hashtable<String, Zielrichterentscheid<T>> zes, boolean zw) {

        boolean isRealFinal = wk.getBooleanProperty("isFinal", false) && wk.getBooleanProperty("isQualified", false);

        boolean oneDiscipline = ak.getDiszAnzahl() == 1 && !ak.hasHLW();

        for (SchwimmerResult result : results) {
            SchwimmerData[] daten = result.getResults();
            int dnf = 0;
            int dns = 0;
            double[] ps = new double[daten.length];
            for (int y = 0; y < daten.length; y++) {
                ps[y] = daten[y].getPoints();
                switch (daten[y].getStrafe().getArt()) {
                case AUSSCHLUSS:
                    result.setKeineWertung(true);
                    dnf++;
                    break;
                case DISQUALIFIKATION:
                    dnf++;
                    break;
                case NICHT_ANGETRETEN:
                    dnf++;
                    dns++;
                    break;
                default:
                }
            }

            double points = 0;
            int count = result.getSchwimmer().getDisciplineChoiceCount();
            boolean isOk = (ak.getMinimalChosenDisciplines() <= count) && (ak.getMaximalChosenDisciplines() >= count)
                    && (count > dns);
            if (isOk || isRealFinal) {
                // not enough or too many disciplines lead to disqualification
                Arrays.sort(ps);
                for (int y = 1; y <= ak.getUsedDisciplines(); y++) {
                    points += ps[ps.length - y];
                }
                if (ak.hasHLW() && (!result.getSchwimmer().isAusgeschlossen())) {
                    if (zw) {
                        if (result.getSchwimmer().getHLWState() == HLWStates.NICHT_ANGETRETEN) {
                            points = 0;
                            result.setKeineWertung(true);
                        } else {
                            points += result.getSchwimmer().getHLWPunkte();
                        }
                    }
                }
            } else {
                result.setKeineWertung(true);
            }
            result.setPoints(points);
            result.setDnf(dnf);
            if (result.hasKeineWertung()) {
                result.setPoints(0);
                result.setRank(-1);
            }
        }

        if (oneDiscipline) {
            for (int x = 0; x < results.length; x++) {
                results[x].setRank(results[x].getResults()[0].getRank());
                results[x].setKeineWertung(results[x].getPlace() <= 0);
            }

            Arrays.sort(results, new Comparator<SchwimmerResult>() {
                @Override
                public int compare(SchwimmerResult sd1, SchwimmerResult sd2) {
                    if (sd1.getSchwimmer().isAusserKonkurrenz() == sd2.getSchwimmer().isAusserKonkurrenz()) {
                        int points = (int) ((sd2.getPoints() - sd1.getPoints()) * 100);
                        if (points != 0) {
                            return points;
                        }
                        int rank = sd1.getPlace() - sd2.getPlace();
                        if (rank != 0) {
                            if (sd1.getPlace() <= 0) {
                                return 1;
                            }
                            if (sd2.getPlace() <= 0) {
                                return -1;
                            }
                            return rank;
                        }
                        return sd1.getDnf() - sd2.getDnf();
                    }
                    if (sd1.getSchwimmer().isAusserKonkurrenz()) {
                        return 1;
                    }
                    if (sd2.getSchwimmer().isAusserKonkurrenz()) {
                        return -1;
                    }
                    if (sd1.hasKeineWertung() != sd2.hasKeineWertung()) {
                        if (sd1.hasKeineWertung()) {
                            return 1;
                        }
                        return -1;
                    }
                    return 0;
                }
            });

        } else {
            Arrays.sort(results, new Comparator<SchwimmerResult>() {
                @Override
                public int compare(SchwimmerResult sd1, SchwimmerResult sd2) {
                    if (sd1.getSchwimmer().isAusserKonkurrenz() == sd2.getSchwimmer().isAusserKonkurrenz()) {
                        int points = (int) ((sd2.getPoints() - sd1.getPoints()) * 100);
                        if (points != 0) {
                            return points;
                        }
                        return sd1.getDnf() - sd2.getDnf();
                    }
                    if (sd1.getSchwimmer().isAusserKonkurrenz()) {
                        return 1;
                    }
                    if (sd2.getSchwimmer().isAusserKonkurrenz()) {
                        return -1;
                    }
                    if (sd1.hasKeineWertung() != sd2.hasKeineWertung()) {
                        if (sd1.hasKeineWertung()) {
                            return 1;
                        }
                        return -1;
                    }
                    return 0;
                }
            });
            double oldResults = Double.MAX_VALUE;
            int pos = 0;
            for (int x = 0; x < results.length; x++) {
                if (results[x].getPoints() < oldResults) {
                    pos = (x + 1);
                    oldResults = results[x].getPoints() - 0.005;
                }
                results[x].setRank(pos);
            }
        }
        return results;
    }

    protected double getPoints(int time, int rec, int rank, int amount, Strafe s) {
        switch (s.getArt()) {
        case AUSSCHLUSS:
            return 0;
        case DISQUALIFIKATION:
            return getPoints(rank + amount - 1);
        case NICHT_ANGETRETEN:
            return 0;
        case NICHTS:
            break;
        case STRAFPUNKTE:
            break;
        }
        return getPoints(rank);
    }

    protected static final int[] POINTS = new int[] { 20, 18, 16, 14, 13, 12, 11, 10, 8, 7, 6, 5, 4, 3, 2, 1 };

    protected double getPoints(int rank) {
        if ((rank > 0) && (rank <= POINTS.length)) {
            return POINTS[rank - 1];
        }
        return 0;
    }

    @Override
    public DataType getDataType() {
        return DataType.TIME;
    }
}