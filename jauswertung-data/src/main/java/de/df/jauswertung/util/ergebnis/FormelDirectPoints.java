/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007.DLRGComparator;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007.RankComparator;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007.RankComparator2;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public class FormelDirectPoints<T extends ASchwimmer> implements Formel<T> {

    public static final String ID = "DirectPoints";

    @Override
    public String getID() {
        return FormelDirectPoints.ID;
    }

    @Override
    public String getName() {
        return "Direkte Punktevergabe";
    }

    @Override
    public String getDescription() {
        return "Direkte Punktevergabe";
    }

    public FormelDirectPoints() {
        super();
    }

    @Override
    public String getFormel() {
        return "-";
    }

    @Override
    public DataType getDataType() {
        return DataType.TIME;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d, Hashtable<String, Zielrichterentscheid<T>> zes) {
        SchwimmerData[] sold = swimmer;
        Arrays.sort(sold, new DLRGComparator());

        LinkedList<SchwimmerData> sd = new LinkedList<SchwimmerData>();

        for (SchwimmerData aSwimmer2 : swimmer) {
            if ((aSwimmer2.getTime() == 0) || (aSwimmer2.getStrafart() == Strafarten.AUSSCHLUSS) || (aSwimmer2.getStrafart() == Strafarten.DISQUALIFIKATION)
                    || (aSwimmer2.getStrafart() == Strafarten.NICHT_ANGETRETEN)) {
                aSwimmer2.setRank(-1);
                aSwimmer2.setPoints(0.0);
            } else {
                sd.addLast(aSwimmer2);
            }
        }
        swimmer = sd.toArray(new SchwimmerData[sd.size()]);
        Arrays.sort(swimmer, new DLRGComparator());

        for (SchwimmerData aSwimmer1 : swimmer) {
            Strafe s = aSwimmer1.getStrafe();
            Strafe s2 = new Strafe(aSwimmer1.getSchwimmer().getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF),
                    aSwimmer1.getSchwimmer().getAK().isStrafeIstDisqualifikation());
            switch (s2.getArt()) {
            case AUSSCHLUSS:
            case DISQUALIFIKATION:
                s = s.join(s2);
                break;
            case NICHT_ANGETRETEN:
            case NICHTS:
            case STRAFPUNKTE:
                break;
            }
            aSwimmer1.setPoints(getResult(aSwimmer1.getTime(), d.getRec(), s.getArt(), s.getStrafpunkte()));
        }
        Arrays.sort(swimmer, new DLRGComparator());
        double oldResults = Double.MAX_VALUE;
        int pos = 0;
        for (int x = 0; x < swimmer.length; x++) {
            if (swimmer[x].getPoints() < oldResults) {
                pos = (x + 1);
                oldResults = swimmer[x].getPoints() - 0.005;
            }
            swimmer[x].setRank(pos);
        }

        // Update results based on "Zielrichterentscheid"
        Hashtable<Integer, LinkedList<SchwimmerData>> table = new Hashtable<Integer, LinkedList<SchwimmerData>>();
        for (SchwimmerData aSwimmer : swimmer) {
            if (aSwimmer.getRank() > 0) {
                LinkedList<SchwimmerData> liste = table.get(aSwimmer.getRank());
                if (liste == null) {
                    liste = new LinkedList<SchwimmerData>();
                    table.put(aSwimmer.getRank(), liste);
                }
                liste.addLast(aSwimmer);
            }
        }

        for (LinkedList<SchwimmerData> liste : table.values()) {
            if (liste.size() > 1) {
                Zielrichterentscheid<T> ze = zes.get(liste.getFirst().getSchwimmer().getStartnummer() + "x" + d.getName());

                if (ze != null) {
                    SchwimmerData[] temp = liste.toArray(new SchwimmerData[liste.size()]);
                    LinkedList<SchwimmerData> ergebnis = new LinkedList<SchwimmerData>();
                    for (T s : ze.getSchwimmer()) {
                        for (SchwimmerData aTemp : temp) {
                            if (aTemp.getSchwimmer().equals(s)) {
                                ergebnis.addLast(aTemp);
                            }
                        }
                    }

                    int x = 0;
                    ListIterator<SchwimmerData> li = ergebnis.listIterator();
                    while (li.hasNext()) {
                        SchwimmerData sdx = li.next();
                        sdx.setRank(sdx.getRank() + x);
                        x++;
                    }
                }
            }
        }
        Arrays.sort(swimmer, new RankComparator());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public SchwimmerResult<T>[] toResults(SchwimmerResult<T>[] results, AWettkampf<T> wk, Altersklasse ak, Hashtable<String, Zielrichterentscheid<T>> zes,
            boolean zw) {
        for (SchwimmerResult result1 : results) {
            SchwimmerData[] daten = result1.getResults();
            int dnf = 0;
            int na = 0;
            double[] ps = new double[daten.length];
            for (int y = 0; y < daten.length; y++) {
                ps[y] = daten[y].getPoints();
                switch (daten[y].getStrafe().getArt()) {
                case AUSSCHLUSS:
                    result1.setKeineWertung(true);
                    dnf++;
                    break;
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

            if (na == result1.getSchwimmer().getDisciplineChoiceCount()) {
                if (!ak.hasHLW() || result1.getSchwimmer().getHLWState() == HLWStates.NICHT_ANGETRETEN
                        || result1.getSchwimmer().getHLWState() == HLWStates.NOT_ENTERED) {
                    result1.setKeineWertung(true);
                }
            }

            ASchwimmer s = result1.getSchwimmer();
            double points = 0;
            double count = 0;
            if (s.getAK().isDisciplineChoiceAllowed()) {
                for (int i = 0; i < s.getAK().getDiszAnzahl(); i++) {
                    if (s.isDisciplineChosen(i)) {
                        if (s.getAkkumulierteStrafe(i).getArt() != Strafarten.NICHT_ANGETRETEN) {
                            count++;
                        }
                    }
                }
            } else {
                count = s.getAK().getDiszAnzahl();
            }
            if ((ak.getMinimalChosenDisciplines() <= count) && (ak.getMaximalChosenDisciplines() >= count)) {
                // not enough or too much disciplines lead to disqualification
                Arrays.sort(ps);
                for (int y = 1; y <= ak.getUsedDisciplines(); y++) {
                    points += ps[ps.length - y];
                }
                if (ak.hasHLW() && (!result1.getSchwimmer().isAusgeschlossen())) {
                    if (zw) {
                        if (result1.getSchwimmer().getHLWState() != HLWStates.NICHT_ANGETRETEN) {
                            points += result1.getSchwimmer().getHLWPunkte();
                        }
                    }
                }
            } else {
                result1.setKeineWertung(true);
            }
            result1.setPoints(points);
            result1.setDnf(dnf);
            switch (result1.getStrafe().getArt()) {
            case AUSSCHLUSS:
                result1.setPoints(-0.0002);
                break;
            case DISQUALIFIKATION:
                result1.setPoints(0);
                break;
            case NICHT_ANGETRETEN:
                result1.setPoints(0);
                break;
            case NICHTS:
                break;
            case STRAFPUNKTE:
                result1.setPoints(Math.max(points - result1.getStrafe().getStrafpunkte(), 0));
                break;
            }
            if (result1.hasKeineWertung()) {
                result1.setPoints(-0.0001);
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

        if (ak.getDiszAnzahl() == 1) {
            // Update results based on "Zielrichterentscheid"
            Hashtable<Integer, LinkedList<SchwimmerResult>> table = new Hashtable<Integer, LinkedList<SchwimmerResult>>();
            for (SchwimmerResult result : results) {
                if (result.getPlace() > 0) {
                    LinkedList<SchwimmerResult> liste = table.get(result.getPlace());
                    if (liste == null) {
                        liste = new LinkedList<SchwimmerResult>();
                        table.put(result.getPlace(), liste);
                    }
                    liste.addLast(result);
                }
            }

            for (LinkedList<SchwimmerResult> liste : table.values()) {
                if (liste.size() > 1) {
                    Zielrichterentscheid<T> ze = zes.get(liste.getFirst().getSchwimmer().getStartnummer() + "x"
                            + ak.getDisziplin(0, liste.getFirst().getSchwimmer().isMaennlich()).getName());

                    if (ze != null) {
                        SchwimmerResult[] temp = liste.toArray(new SchwimmerResult[liste.size()]);
                        LinkedList<SchwimmerResult> ergebnis = new LinkedList<SchwimmerResult>();
                        for (T s : ze.getSchwimmer()) {
                            for (SchwimmerResult aTemp : temp) {
                                if (aTemp.getSchwimmer().equals(s)) {
                                    ergebnis.addLast(aTemp);
                                }
                            }
                        }

                        int x = 0;
                        ListIterator<SchwimmerResult> li = ergebnis.listIterator();
                        while (li.hasNext()) {
                            SchwimmerResult sdx = li.next();
                            sdx.setRank(sdx.getPlace() + x);
                            x++;
                        }
                    }
                }
            }
            Arrays.sort(results, new RankComparator2());
        }

        return results;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.daten.regelwerk.Formel#getResult(double, double,
     * double)
     */
    double getResult(int seconds, int rec, Strafarten misc, int strafe) {
        if (seconds == 0) {
            return 0.0;
        }
        if ((misc != Strafarten.STRAFPUNKTE) && (misc != Strafarten.NICHTS)) {
            return 0.0;
        }

        double ergebnis = seconds;

        ergebnis = ergebnis / 100.0 - strafe;
        if (ergebnis <= 0.0) {
            ergebnis = 0.0;
        }
        return ergebnis;
    }
}