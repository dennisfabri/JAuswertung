package de.df.jauswertung.util.ergebnis;

import java.io.Serializable;
import java.util.*;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;

public class FormelILSFinals<T extends ASchwimmer> extends FormelILS<T> {

    class ComparatorT implements Comparator<SchwimmerData<T>> {

        private final Map<Integer, SchwimmerInfo<T>> hsi;

        public ComparatorT(Map<Integer, SchwimmerInfo<T>> si) {
            hsi = si;
        }

        @Override
        public int compare(SchwimmerData<T> t1, SchwimmerData<T> t2) {
            SchwimmerInfo<T> si1 = hsi.get(t1.getSchwimmer().getStartnummer());
            SchwimmerInfo<T> si2 = hsi.get(t2.getSchwimmer().getStartnummer());

            if (si1 == null && si2 == null) {
                return 0;
            }
            if (si1 == null) {
                return 1;
            }
            if (si2 == null) {
                return -1;
            }

            return si1.compareTo(si2);
        }

    }

    class ComparatorT2 implements Comparator<SchwimmerData<T>> {

        private final Map<Integer, SchwimmerInfo<T>> hsi;
        private final ILSComparator c = new ILSComparator();

        public ComparatorT2(Map<Integer, SchwimmerInfo<T>> si) {
            hsi = si;
        }

        @Override
        public int compare(SchwimmerData<T> t1, SchwimmerData<T> t2) {
            SchwimmerInfo<T> si1 = hsi.get(t1.getSchwimmer().getStartnummer());
            SchwimmerInfo<T> si2 = hsi.get(t2.getSchwimmer().getStartnummer());

            if (si1 == null && si2 == null) {
                return 0;
            }
            if (si1 == null) {
                return 1;
            }
            if (si2 == null) {
                return -1;
            }

            int i = si1.compareTo(si2);
            if (i != 0) {
                return i;
            }
            return c.compare(t1, t2);
        }
    }

    @SuppressWarnings("rawtypes")
    static final class ILSComparator implements Comparator<SchwimmerData>, Serializable {

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

    public static final String ID = "ILS2004Finals";

    @Override
    public String getID() {
        return ID;
    }

    public FormelILSFinals() {
        super();
    }

    @Override
    public String getName() {
        return "ILS Regelwerk - Indoor (mit Finals)";
    }

    @Override
    public String getDescription() {
        return "Diese Punktevergabe entspricht der Punktevergabe für Indoorwettkämpfe der ILS mit Finals";
    }

    private void setPointsDirect(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d,
            boolean isQualified, boolean isFinal, int round) {
        T s = swimmer[0].getSchwimmer();
        Altersklasse ak = s.getAK();
        boolean male = s.isMaennlich();
        int disz = 0;
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            if (ak.getDisziplin(x, male) == d) {
                disz = x;
                break;
            }
        }

        int size = SearchUtils.getSchwimmer(wk, ak, male, disz).size();
        @SuppressWarnings("unchecked")
        SchwimmerInfo<T>[] infos = new SchwimmerInfo[size];

        for (int i = 0; i < swimmer.length; i++) {
            SchwimmerData<T> sd = swimmer[i];

            SchwimmerInfo<T> si = new SchwimmerInfo<>();
            si.schwimmer = SearchUtils.getSchwimmer(wk, swimmer[i].getSchwimmer());
            si.platz = 1; // Um den Vergleicher nicht zu verwirren...
            si.zeit = sd.getTime();
            si.strafe = sd.getStrafe();
            si.runde = round; // (isFinal && isQualified) ? 1 : 0;
            si.isFinal = isFinal;

            infos[i] = si;

            si.lauf = Integer.MAX_VALUE;

            if (wk.getLaufliste() != null && wk.getLaufliste().getLaufliste() != null) {
                LinkedList<Lauf<T>> ll = wk.getLaufliste().getLaufliste();
                for (int l = 0; l < ll.size(); l++) {
                    Lauf<T> lauf = ll.get(ll.size() - 1 - l);
                    for (int j = 0; j < lauf.getBahnen(); j++) {
                        T lt = lauf.getSchwimmer(j);
                        if (lt != null) {
                            if (lt.getStartnummer() == si.schwimmer.getStartnummer()) {
                                si.lauf = l;
                            }
                        }
                    }
                }
            }
        }

        Hashtable<Integer, SchwimmerInfo<T>> hsi = new Hashtable<>();
        for (SchwimmerInfo<T> sx : infos) {
            hsi.put(sx.schwimmer.getStartnummer(), sx);
        }
        ComparatorT2 comp = new ComparatorT2(hsi);
        Arrays.sort(swimmer, comp);

        ArrayList<ArrayList<SchwimmerData<T>>> xxl = new ArrayList<>();
        ArrayList<SchwimmerData<T>> current = null;
        SchwimmerData<T> sdx = null;

        for (SchwimmerData<T> tSchwimmerData : swimmer) {
            if (sdx == null || comp.compare(sdx, tSchwimmerData) != 0) {
                current = new ArrayList<>();
                xxl.add(current);
                sdx = tSchwimmerData;
                current.add(sdx);
            } else {
                current.add(tSchwimmerData);
            }
        }

        OWDisziplin<T> owDisziplin= wk.getLauflisteOW().getDisziplin(OWDisziplin.getId(s.getAKNummer(), male, disz, round));
        int heatSize = wk.getIntegerProperty(PropertyConstants.HEATS_LANES, 6);
        if (owDisziplin != null) {
            heatSize = owDisziplin.getBahnen();
        }
        //int heatSize = wk.getLauflisteOW().getMaximaleAnzahlBahnen();

        int rank = 1;
        for (ArrayList<SchwimmerData<T>> asd : xxl) {
            int amount = asd.size();
            for (SchwimmerData<T> sd : asd) {
                sd.setRank(rank);
                sd.setPoints(getPoints(sd.getTime(), d.getRec(), rank, amount, sd.getStrafe(), heatSize));
                switch (sd.getStrafart()) {
                case AUSSCHLUSS:
                case DISQUALIFIKATION:
                case NICHT_ANGETRETEN:
                    if ((!isQualified && !isFinal) || round == 0) {
                        sd.setRank(-1);
                        sd.setPoints(0);
                    }
                    sd.setTime(0);
                    break;
                default:
                    break;
                }
            }
            rank += amount;
        }
    }

    /** ----------------- */

    @SuppressWarnings({ "unchecked", "null" })
    @Override
    public void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d,
            Hashtable<String, Zielrichterentscheid<T>> zes) {
        if (swimmer.length == 0) {
            return;
        }
        if (wk.getLauflisteOW().isEmpty() && wk.getLaufliste() != null && !wk.getLaufliste().isEmpty()) {
            setPointsDirect(wk, swimmer, d, wk.getBooleanProperty("isQualified"), wk.getBooleanProperty("isFinal"),
                    wk.getIntegerProperty("round"));
            return;
        }

        T s = swimmer[0].getSchwimmer();
        Altersklasse ak = s.getAK();
        boolean male = s.isMaennlich();
        int disz = 0;
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            if (ak.getDisziplin(x, male) == d) {
                disz = x;
                break;
            }
        }

        int size = SearchUtils.getSchwimmer(wk, ak, male, disz).size();
        SchwimmerInfo<T>[] infos = new SchwimmerInfo[size];

        int rounds = d.getRunden().length + 1;

        for (int x = 0; x < rounds; x++) {
            OWSelection t = new OWSelection(ak, s.getAKNummer(), s.isMaennlich(), disz, x);
            OWDisziplin<T> dx = wk.getLauflisteOW().getDisziplin(t);
            if (dx == null) {
                for (SchwimmerData<T> sd : swimmer) {
                    sd.setTime(0);
                    sd.overridePenalty(Strafe.NICHTS);
                    sd.setRank(-1);
                    sd.setPoints(0);
                }
                return;
            }
        }

        ArrayList<SchwimmerResult<T>[]> results = new ArrayList<>();
        for (int x = 0; x < rounds; x++) {
            OWSelection t = new OWSelection(ak, s.getAKNummer(), s.isMaennlich(), disz, x);
            AWettkampf<T> wkx = ResultUtils.createCompetitionFor(wk, t);
            if (!t.isFinal) {
                wkx.getRegelwerk().setFormelID(FormelILS.ID);
            }
            SchwimmerResult<T>[] r = ResultCalculator.getResults(wkx, wkx.getRegelwerk().getAk(s.getAKNummer()), male);
            for (int i = 0; i < r.length; i++) {

                SchwimmerData<T> sd = r[i].getResults()[0];

                SchwimmerInfo<T> si = new SchwimmerInfo<>();
                si.schwimmer = SearchUtils.getSchwimmer(wk, r[i].getSchwimmer());
                si.platz = sd.getRank();
                si.zeit = sd.getTime();
                si.strafe = sd.getStrafe();
                si.runde = x;
                si.isFinal = x + 1 == r.length;

                if (x == 0) {
                    infos[i] = si;
                } else {
                    for (int k = 0; k < infos.length; k++) {
                        if (infos[k].schwimmer.getStartnummer() == si.schwimmer.getStartnummer()) {
                            infos[k] = si;
                        }
                    }
                }

                si.lauf = Integer.MAX_VALUE;

                LinkedList<Lauf<T>> ll = wkx.getLaufliste().getLaufliste();
                for (int l = 0; l < ll.size(); l++) {
                    Lauf<T> lauf = ll.get(ll.size() - 1 - l);
                    for (int j = 0; j < lauf.getBahnen(); j++) {
                        T lt = lauf.getSchwimmer(j);
                        if (lt != null) {
                            if (lt.getStartnummer() == si.schwimmer.getStartnummer()) {
                                si.lauf = l;
                            }
                        }
                    }
                }
            }
            results.add(r);
        }

        if (results.isEmpty()) {
            return;
        }

        Map<Integer, SchwimmerInfo<T>> hsi = new HashMap<>();
        for (SchwimmerInfo<T> sx : infos) {
            hsi.put(sx.schwimmer.getStartnummer(), sx);
        }
        ComparatorT comp = new ComparatorT(hsi);
        Arrays.sort(swimmer, comp);

        List<List<SchwimmerData<T>>> xxl = new ArrayList<>();
        List<SchwimmerData<T>> current = null;
        SchwimmerData<T> sdx = null;

        for (SchwimmerData<T> tSchwimmerData : swimmer) {
            if (sdx == null || comp.compare(sdx, tSchwimmerData) != 0) {
                current = new ArrayList<>();
                xxl.add(current);
                sdx = tSchwimmerData;
                current.add(sdx);
            } else {
                current.add(tSchwimmerData);
            }
        }


        int round = 0;
        for (int x = 0; x < rounds; x++) {
            OWSelection t = new OWSelection(ak, s.getAKNummer(), s.isMaennlich(), disz, x);
            OWDisziplin<T> dx = wk.getLauflisteOW().getDisziplin(t);
            if (dx != null) {
                round = x;
            }
        }

        OWDisziplin<T> owDisziplin= wk.getLauflisteOW().getDisziplin(OWDisziplin.getId(s.getAKNummer(), male, disz, round));
        int heatSize = wk.getIntegerProperty(PropertyConstants.HEATS_LANES, 6);
        if (owDisziplin != null) {
            heatSize = owDisziplin.getBahnen();
        }
        //int heatSize = wk.getLauflisteOW().getMaximaleAnzahlBahnen();

        int rank = 1;
        for (List<SchwimmerData<T>> asd : xxl) {
            int amount = asd.size();
            for (SchwimmerData<T> sd : asd) {
                SchwimmerInfo<T> si = hsi.get(sd.getSchwimmer().getStartnummer());
                if (si != null) {
                    sd.setTime(si.zeit);
                    sd.overridePenalty(si.strafe);
                    sd.setRank(rank);
                    sd.setPoints(getPoints(si.zeit, d.getRec(), rank, amount, si.strafe, heatSize));
                    switch (sd.getStrafart()) {
                    case AUSSCHLUSS:
                    case DISQUALIFIKATION:
                    case NICHT_ANGETRETEN:
                        if (si.runde > 0) {
                            // sd.overridePenalty(Strafe.NICHTS);
                            // sd.setPoints(0);
                            sd.setTime(0);
                        } else {
                            sd.setRank(-1);
                            sd.setPoints(0);
                            sd.setTime(0);
                        }
                        break;
                    default:
                        break;
                    }
                    if (sd.getTime() == 0) {
                        if (rounds <= 1) {
                            sd.setRank(-1);
                            sd.setPoints(0);
                        }
                    }
                } else {
                    sd.setTime(0);
                    sd.overridePenalty(Strafe.NICHTS);
                    sd.setRank(-1);
                    sd.setPoints(0);
                }
            }
            rank += amount;
        }
    }
}