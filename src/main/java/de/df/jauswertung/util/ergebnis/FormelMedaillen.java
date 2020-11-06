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

/**
 * @author Dennis Fabri
 * @date 10.06.2007
 */
public class FormelMedaillen<T extends ASchwimmer> implements Formel<T> {

    @SuppressWarnings("rawtypes")
    static final class ILSComparator implements Comparator<SchwimmerData>, Serializable {
        @Override
        public int compare(SchwimmerData sd1, SchwimmerData sd2) {
            if (sd1.getStrafart() != sd2.getStrafart()) {
                if ((sd1.getStrafart() == Strafarten.DISQUALIFIKATION) || (sd1.getStrafart() == Strafarten.AUSSCHLUSS)
                        || (sd1.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                    return 1;
                }
                if ((sd2.getStrafart() == Strafarten.DISQUALIFIKATION) || (sd2.getStrafart() == Strafarten.AUSSCHLUSS)
                        || (sd2.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                    return -1;
                }
            }
            return sd1.getTime() - sd2.getTime();
        }
    }

    public static final String ID = "MEDAILLEN";

    @Override
    public String getID() {
        return ID;
    }

    /**
     * 
     */
    public FormelMedaillen() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.daten.regelwerk.Formel#getFormel()
     */
    @Override
    public String getFormel() {
        return "Platz 1: 3 Punkte, Platz 2: 2 Punkte, Platz 3: 1 Punkt";
    }

    @Override
    public String toString() {
        return getFormel();
    }

    @Override
    public String getName() {
        return "Medaillenwertung";
    }

    @Override
    public String getDescription() {
        return "Diese Wertung vergibt Punkte entsprechend der gewonnenen Medaillen. Zusatzwertungspunkte werden nicht berücksichtigt.";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d, Hashtable<String, Zielrichterentscheid<T>> zes) {
        SchwimmerData[] sold = swimmer;
        Arrays.sort(sold, new ILSComparator());

        // LinkedList<SchwimmerData> zero = new LinkedList<SchwimmerData>();
        LinkedList<SchwimmerData> sd = new LinkedList<SchwimmerData>();
        LinkedList<SchwimmerData> others = new LinkedList<SchwimmerData>();
        for (SchwimmerData aSwimmer : swimmer) {
            if ((aSwimmer.getTime() == 0) || (aSwimmer.getStrafart() == Strafarten.AUSSCHLUSS) || (aSwimmer.getStrafart() == Strafarten.DISQUALIFIKATION)
                    || (aSwimmer.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                // zero.addLast(aSwimmer);
                aSwimmer.setRank(-1);
                aSwimmer.setPoints(0.0);
            } else {
                if (aSwimmer.getSchwimmer().isAusserKonkurrenz()) {
                    others.addLast(aSwimmer);
                } else {
                    sd.addLast(aSwimmer);
                }
            }
        }
        SchwimmerData[] other = others.toArray(new SchwimmerData[others.size()]);
        Arrays.sort(other, new ILSComparator());

        swimmer = sd.toArray(new SchwimmerData[sd.size()]);
        Arrays.sort(swimmer, new ILSComparator());

        double oldResults = Double.MIN_VALUE;
        int disCounter = 0;
        int pos = 1;
        for (int x = 0; x < swimmer.length; x++) {
            if (swimmer[x].getTime() > oldResults) {
                for (int y = pos - 1; y < x; y++) {
                    if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE) || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                        swimmer[y].setPoints(getPoints(pos, x - pos + 1 - disCounter));
                    }
                }
                disCounter = 0;
                pos = (x + 1);
                oldResults = swimmer[x].getTime() + 0.005;
            }
            swimmer[x].setRank(pos);
            if ((swimmer[x].getStrafart() == Strafarten.NICHT_ANGETRETEN) || (swimmer[x].getStrafart() == Strafarten.DISQUALIFIKATION)
                    || (swimmer[x].getStrafart() == Strafarten.AUSSCHLUSS)) {
                disCounter++;
            }
        }
        for (int y = pos - 1; y < swimmer.length; y++) {
            if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE) || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                swimmer[y].setPoints(getPoints(pos, swimmer.length - pos + 1 - disCounter));
            }
        }

        // Set point for "ausser konkurrenz" swimmers
        for (SchwimmerData anOther : other) {
            if (anOther.getTime() > 0) {
                int y = 0;
                while ((y < swimmer.length) && (swimmer[y].getTime() < anOther.getTime()) && (swimmer[y].getTime() > 0)) {
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

    @SuppressWarnings({ "fallthrough", "rawtypes" })
    @Override
    public SchwimmerResult<T>[] toResults(SchwimmerResult<T>[] results, AWettkampf<T> wk, Altersklasse ak, Hashtable<String, Zielrichterentscheid<T>> zes,
            boolean zw) {
        for (SchwimmerResult result : results) {
            SchwimmerData[] daten = result.getResults();
            int dnf = 0;
            int na = 0;
            double[] ps = new double[daten.length];
            for (int y = 0; y < daten.length; y++) {
                ps[y] = daten[y].getPoints();
                switch (daten[y].getStrafe().getArt()) {
                case AUSSCHLUSS:
                    result.setKeineWertung(true);
                case DISQUALIFIKATION:
                    dnf++;
                    break;
                case NICHT_ANGETRETEN:
                    dnf++;
                    na++;
                    break;
                default:
                }
            }
            if (na == result.getSchwimmer().getDisciplineChoiceCount() && (!ak.hasHLW() || result.getSchwimmer().getHLWState() == HLWStates.NICHT_ANGETRETEN
                    || result.getSchwimmer().getHLWState() == HLWStates.NICHT_ANGETRETEN)) {
                result.setKeineWertung(true);
            }

            double points = 0;
            double count = result.getSchwimmer().getDisciplineChoiceCount();
            if ((ak.getMinimalChosenDisciplines() <= count) && (ak.getMaximalChosenDisciplines() >= count)) {
                // not enough or too many disciplines lead to disqualification
                Arrays.sort(ps);
                for (int y = 1; y <= ak.getUsedDisciplines(); y++) {
                    points += ps[ps.length - y];
                }
                if (ak.hasHLW() && (!result.getSchwimmer().isAusgeschlossen())) {
                    if (zw) {
                        if (result.getSchwimmer().getHLWState() == HLWStates.NICHT_ANGETRETEN) {
                            points = 0;
                        } else {
                            // points +=
                            // results[x].getSchwimmer().getHLWPunkte();
                        }
                    }
                }
            } else {
                result.setKeineWertung(true);
            }
            result.setPoints(points);
            result.setDnf(dnf);
            if (result.hasKeineWertung()) {
                result.setPoints(-1);
            }
        }
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
        return results;
    }

    private double getPoints(int rank, int amount) {
        double sum = 0;
        for (int x = rank; x < rank + amount; x++) {
            sum += getPoints(x);
        }
        return sum / amount;
    }

    private static double getPoints(int rank) {
        switch (rank) {
        case 1:
            return 3;
        case 2:
            return 2;
        case 3:
            return 1;
        default:
            return 0;
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.TIME;
    }
}