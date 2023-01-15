/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public class FormelFeuerwehr2005<T extends ASchwimmer> implements Formel<T> {

    @SuppressWarnings("rawtypes")
    static double getTime(SchwimmerData sd1, Disziplin d) {
        if ((sd1.getStrafart() == Strafarten.DISQUALIFIKATION) || (sd1.getStrafart() == Strafarten.AUSSCHLUSS)
                || (sd1.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
            return 0;
        }
        int time1 = sd1.getTime();
        if (time1 <= 0) {
            return 0;
        }
        if (sd1.getSchwimmer() instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) sd1.getSchwimmer();
            int jahrgang = t.getJahrgang();
            if ((jahrgang > 0) && (d.getRec() > 0)) {
                int base = t.getWettkampf().getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION,
                        Calendar.getInstance().get(Calendar.YEAR));
                double alter = base - jahrgang;
                if (alter > 28.99) {
                    time1 -= 100.0 * (alter - 29.0) / (1.0 * d.getRec());
                }
            }
        }
        return time1;
    }

    @SuppressWarnings("rawtypes")
    private static final class FWComparator implements Comparator<SchwimmerData>, Serializable {

        private Disziplin d;

        public FWComparator(Disziplin d) {
            this.d = d;
        }

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
            int time1 = (int) (getTime(sd1, d) * 100);
            int time2 = (int) (getTime(sd2, d) * 100);

            if (time1 == time2) {
                return 0;
            }
            if (time1 == 0) {
                return 1;
            }
            if (time2 == 0) {
                return -1;
            }
            return time1 - time2;
        }
    }

    public static final String ID = "Feuerwehr2006";

    @Override
    public String getID() {
        return ID;
    }

    /**
     * 
     */
    public FormelFeuerwehr2005() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.daten.regelwerk.Formel#getFormel()
     */
    @Override
    public String getFormel() {
        return "Platz 1: 1 Punkt, Platz 2: 2 Punkte, ..., Platz 10: 10 Punkte";
    }

    @Override
    public String toString() {
        return getFormel();
    }

    @Override
    public String getName() {
        return "Feuerwehr 2005";
    }

    @Override
    public String getDescription() {
        return "Regelwerk der Berufsfeuerwehr";
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
            return 1;
        case 2:
            return 2;
        case 3:
            return 3;
        case 4:
            return 4;
        case 5:
            return 5;
        case 6:
            return 6;
        case 7:
            return 7;
        case 8:
            return 8;
        case 9:
            return 9;
        default:
            return 10;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d,
            Hashtable<String, Zielrichterentscheid<T>> zes) {
        if (swimmer.length == 0) {
            return;
        }
        Arrays.sort(swimmer, new FWComparator(d));
        for (SchwimmerData aSwimmer1 : swimmer) {
            aSwimmer1.setPoints(10.0);
            // swimmer[x].setPoints(getTime(swimmer[x], d));
        }
        double oldResult = getTime(swimmer[0], d) + 0.005;
        int pos = 0;
        LinkedList<SchwimmerData> stored = new LinkedList<>();
        for (SchwimmerData aSwimmer : swimmer) {
            if (getTime(aSwimmer, d) <= ASchwimmer.TIME_EPSILON) {
                break;
            }
            boolean exit = false;
            switch (aSwimmer.getStrafart()) {
            case AUSSCHLUSS:
            case NICHT_ANGETRETEN:
            case DISQUALIFIKATION:
                exit = true;
                break;
            default:
                break;
            }
            if (exit) {
                break;
            }
            if (oldResult < getTime(aSwimmer, d)) {
                pos++;
                oldResult = getTime(aSwimmer, d) + 0.005;
                double points = getPoints(pos, stored.size());
                for (SchwimmerData data : stored) {
                    data.setRank(pos);
                    data.setPoints(points);
                }
                pos += stored.size() - 1;
                stored.clear();
                stored.addLast(aSwimmer);
            } else {
                stored.addLast(aSwimmer);
            }
        }

        pos++;
        double points = getPoints(pos, stored.size());
        for (SchwimmerData data : stored) {
            data.setRank(pos);
            data.setPoints(points);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public SchwimmerResult<T>[] toResults(SchwimmerResult<T>[] results, AWettkampf<T> wk, Altersklasse ak,
            Hashtable<String, Zielrichterentscheid<T>> zes,
            boolean zw) {
        for (SchwimmerResult result : results) {
            SchwimmerData[] daten = result.getResults();
            double points = 0;
            for (int y = 0; y < daten.length; y++) {
                points += daten[y].getPoints();
            }
            if (ak.hasHLW()) {
                points += result.getSchwimmer().getHLWPunkte();
            }
            result.setPoints(points);
        }
        Arrays.sort(results, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if ((o1 instanceof SchwimmerResult) && (o2 instanceof SchwimmerResult)) {
                    SchwimmerResult sd1 = (SchwimmerResult) o1;
                    SchwimmerResult sd2 = (SchwimmerResult) o2;
                    return (int) ((sd1.getPoints() - sd2.getPoints()) * 100);
                }
                return 0;
            }
        });
        double oldResults = Double.MIN_VALUE;
        int pos = 0;
        for (int x = 0; x < results.length; x++) {
            if (results[x].getPoints() > oldResults) {
                pos = (x + 1);
                oldResults = results[x].getPoints() + 0.005;
            }
            results[x].setRank(pos);
        }
        return results;
    }

    @Override
    public DataType getDataType() {
        return DataType.TIME;
    }
}