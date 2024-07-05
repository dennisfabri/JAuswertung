package de.df.jauswertung.util.ergebnis;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;

public class FormelILS<T extends ASchwimmer> implements Formel<T> {

    private static final class ILSComparator<T extends ASchwimmer>
            implements Comparator<SchwimmerData<T>>, Serializable {

        @Serial
        private static final long serialVersionUID = -7952635514437923875L;

        @Override
        public int compare(SchwimmerData<T> sd1, SchwimmerData<T> sd2) {
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
            if (sd1.getStrafart() != Strafarten.NICHTS && sd2.getStrafart() != Strafarten.NICHTS) {
                return 0;
            }
            boolean zeroed1 = false;
            boolean zeroed2 = false;
            if (sd1.getStrafart() != Strafarten.NICHTS) {
                zeroed1 = true;
            }
            if (sd2.getStrafart() != Strafarten.NICHTS) {
                zeroed2 = true;
            }
            if (sd1.getTime() == 0) {
                zeroed1 = true;
            }
            if (sd2.getTime() == 0) {
                zeroed2 = true;
            }

            if (zeroed1 && zeroed2) {
                return 0;
            }
            if (zeroed1) {
                return 1;
            }
            if (zeroed2) {
                return -1;
            }
            return sd1.getTime() - sd2.getTime();
        }
    }

    public static final String ID = "ILS2004";

    private final Comparator<SchwimmerData<T>> comparator;

    @Override
    public String getID() {
        return ID;
    }

    public FormelILS() {
        this(new ILSComparator<>());
    }

    protected FormelILS(Comparator<SchwimmerData<T>> comparator) {
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
        return "Diese Punktevergabe entspricht der Punktevergabe für Indoorwettkämpfe der ILS ohne Finals";
    }

    private static final double Epsilon = 0.005;

    /** ----------------- */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d,
            Hashtable<String, Zielrichterentscheid<T>> zes) {
        if (swimmer.length == 0) {
            return;
        }

        SchwimmerData<T>[] sold = swimmer;
        Arrays.sort(sold, comparator);

        LinkedList<SchwimmerData<T>> sd = new LinkedList<>();
        LinkedList<SchwimmerData<T>> others = new LinkedList<>();
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
        SchwimmerData<T>[] other = others.toArray(new SchwimmerData[0]);
        Arrays.sort(other, comparator);

        swimmer = sd.toArray(new SchwimmerData[0]);
        Arrays.sort(swimmer, comparator);

        int heatSize = wk.getIntegerProperty(PropertyConstants.HEATS_LANES, 8);

        double oldResults = Double.MIN_VALUE;
        int disCounter = 0;
        int pos = 1;
        for (int x = 0; x < swimmer.length; x++) {
            if (Math.abs(swimmer[x].getTime() - oldResults) > Epsilon) {
                for (int y = pos - 1; y < x; y++) {
                    if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE)
                            || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                        swimmer[y].setPoints(getPoints(swimmer[y].getTime(), d.getRec(), pos, x - pos + 1 - disCounter,
                                Strafe.NICHTS, heatSize));
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
                // swimmer[x].setRank(-1);
            }
        }
        for (int y = pos - 1; y < swimmer.length; y++) {
            if ((swimmer[y].getStrafart() == Strafarten.STRAFPUNKTE)
                    || (swimmer[y].getStrafart() == Strafarten.NICHTS)) {
                swimmer[y].setPoints(getPoints(swimmer[y].getTime(), d.getRec(), pos,
                        swimmer.length - pos + 1 - disCounter, Strafe.NICHTS, heatSize));
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

            ASchwimmer s = result.getSchwimmer();

            double points = 0;
            int count = 0;
            int chosenAmount = s.getDisciplineChoiceCount();
            if (s.getAK().isDisciplineChoiceAllowed()) {
                for (int i = 0; i < s.getAK().getDiszAnzahl(); i++) {
                    if (s.isDisciplineChosen(i)) {
                        Eingabe eingabe = s.getEingabe(OWDisziplin.getId(s.getAKNummer(), s.isMaennlich(), i, 0));
                        if (eingabe == null || eingabe.getStrafen().stream()
                                .noneMatch(str -> str.getArt() == Strafarten.NICHT_ANGETRETEN)) {
                            count++;
                        }
                    }
                }
            } else {
                count = s.getAK().getDiszAnzahl();
            }
            boolean isOk = (ak.getMinimalChosenDisciplines() <= count)
                    && (ak.getMaximalChosenDisciplines() >= chosenAmount);
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

            Arrays.sort(results, (Comparator<SchwimmerResult>) (sd1, sd2) -> {
                if (sd1.getSchwimmer().isAusserKonkurrenz() != sd2.getSchwimmer().isAusserKonkurrenz()) {
                    if (sd1.getSchwimmer().isAusserKonkurrenz()) {
                        return 1;
                    }
                    if (sd2.getSchwimmer().isAusserKonkurrenz()) {
                        return -1;
                    }
                }

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
                if (sd1.getDnf() != sd2.getDnf()) {
                    return sd1.getDnf() - sd2.getDnf();
                }
                if (sd1.hasKeineWertung() != sd2.hasKeineWertung()) {
                    if (sd1.hasKeineWertung()) {
                        return 1;
                    }
                    return -1;
                }
                return 0;
            });
        } else {
            Arrays.sort(results, (sd1, sd2) -> {
                if (sd1.getSchwimmer().isAusserKonkurrenz() != sd2.getSchwimmer().isAusserKonkurrenz()) {
                    if (sd1.getSchwimmer().isAusserKonkurrenz()) {
                        return 1;
                    }
                    if (sd2.getSchwimmer().isAusserKonkurrenz()) {
                        return -1;
                    }
                }

                int points = (int) ((sd2.getPoints() - sd1.getPoints()) * 100);
                if (points != 0) {
                    return points;
                }
                if (sd1.getDnf() != sd1.getDnf()) {
                    return sd1.getDnf() - sd2.getDnf();
                }

                if (sd1.hasKeineWertung() != sd2.hasKeineWertung()) {
                    if (sd1.hasKeineWertung()) {
                        return 1;
                    }
                    return -1;
                }
                return 0;
            });
            double oldResults = Double.MAX_VALUE;
            int pos = 0;
            for (int x = 0; x < results.length; x++) {
                if (results[x].getPoints() < oldResults) {
                    pos = (x + 1);
                    oldResults = results[x].getPoints() - 0.005;
                }
                if (!results[x].hasKeineWertung()) {
                    results[x].setRank(pos);
                }
            }
        }
        return results;
    }

    protected double getPoints(int time, int rec, int rank, int amount, Strafe s, int heatSize) {
        // heatSize = 8;
        double points = switch (s.getArt()) {
            case AUSSCHLUSS, NICHT_ANGETRETEN -> 0;
            case DISQUALIFIKATION -> {
                if (rank < 0 || rank > 16) {
                    yield 0;
                }
                int lastRankInHeat = heatSize;
                while (rank > lastRankInHeat) {
                    lastRankInHeat += heatSize;
                }
                yield getPoints(lastRankInHeat);
            }
            default -> getPoints(rank);
        };
        return points;
    }

    protected static final int[] POINTS = new int[] { 20, 18, 16, 14, 13, 12, 11, 10, 8, 7, 6, 5, 4, 3, 2, 1 };

    protected double getPoints(int rank) {
        return getPoints(rank, 1);
    }

    protected double getPoints(int rank, int amount) {
        if (!((rank > 0) && (rank <= POINTS.length))) {
            return 0;
        }
        double sum = 0;
        for (int x = rank - 1; x < Math.min(rank + amount - 1, POINTS.length); x++) {
            sum += POINTS[x];
        }
        return Math.round(100.0 * sum / amount) / 100.0;
    }

    @Override
    public DataType getDataType() {
        return DataType.TIME;
    }
}