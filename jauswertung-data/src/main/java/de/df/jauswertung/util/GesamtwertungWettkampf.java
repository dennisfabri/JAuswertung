package de.df.jauswertung.util;

import static java.util.Arrays.stream;

import java.io.Serial;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.FormelMedaillen;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

public class GesamtwertungWettkampf extends AWettkampf<GesamtwertungSchwimmer> {

    public boolean doesNotHaveAnyPoints() {
        for (SchwimmerResult<ASchwimmer>[] r : results) {
            if (stream(r).anyMatch(x -> x.getPoints() > 0.005)) {
                return false;
            }
        }
        return true;
    }

    static final class GesamtwertungSchwimmerComparator implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            if ((o1 != null) && (o2 != null)) {
                if ((o1 instanceof GesamtwertungSchwimmer sd1) && (o2 instanceof GesamtwertungSchwimmer sd2)) {
                    if ((sd1.getAkkumulierteStrafe(0).getArt() == Strafarten.NICHT_ANGETRETEN)
                            && (sd2.getAkkumulierteStrafe(0).getArt() == Strafarten.NICHT_ANGETRETEN)) {
                        return 0;
                    }
                    if (sd1.getAkkumulierteStrafe(0).getArt() == Strafarten.NICHT_ANGETRETEN) {
                        return 1;
                    }
                    if (sd2.getAkkumulierteStrafe(0).getArt() == Strafarten.NICHT_ANGETRETEN) {
                        return -1;
                    }
                    return (int) ((sd2.getPunkte() - sd1.getPunkte()) * 100);
                }
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            return -1;

        }
    }

    @Serial
    private static final long serialVersionUID = 2735741444352316840L;

    @SuppressWarnings("rawtypes")
    private AWettkampf wk;
    private Regelwerk aks;

    private final SchwimmerResult<ASchwimmer>[][] results;

    private GroupEvaluationMode modus;

    /** Creates a new instance of Gesamtwertung */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GesamtwertungWettkampf(AWettkampf wettkampf) {
        super(AltersklassenUtils.generateGesamtwertungAKs(), new Strafen());
        if (wettkampf == null) {
            throw new NullPointerException();
        }

        wk = Utils.copy(wettkampf);
        for (ASchwimmer s : (Iterable<ASchwimmer>) wk.getSchwimmer()) {
            if (s.isAusserKonkurrenz()
                    || (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).getArt() == Strafarten.AUSSCHLUSS)
                    || (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF)
                            .getArt() == Strafarten.DISQUALIFIKATION)) {
                wk.removeSchwimmer(s);
            }
        }
        aks = wk.getRegelwerk();
        modus = aks.getGesamtwertungsmodus();
        if (modus == null) {
            modus = GroupEvaluationMode.Best;
        }
        scalePoints();

        results = new SchwimmerResult[2 * aks.size()][0];
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                results[x * 2 + y] = ResultCalculator.getResults(wk, aks.getAk(x), y == 1);
                // Wenn ohne Blocken gewertet wird, wird nur der beste Schwimmer
                // uebernommen.
                if (modus == GroupEvaluationMode.BestWithoutBlocking) {
                    boolean changed = false;
                    Hashtable<String, String> glds = new Hashtable<>();
                    for (int z = 0; z < results[x * 2 + y].length; z++) {
                        SchwimmerResult<ASchwimmer> sr = results[x * 2 + y][z];
                        boolean found = glds.containsKey(sr.getSchwimmer().getGliederungMitQGliederung());
                        if (found) {
                            wk.removeSchwimmer(sr.getSchwimmer());
                            changed = true;
                        } else {
                            String gld = sr.getSchwimmer().getGliederungMitQGliederung();
                            glds.put(gld, gld);
                        }
                    }
                    if (changed) {
                        results[x * 2 + y] = ResultCalculator.getResults(wk, aks.getAk(x), y == 1);
                    }
                }
            }
        }

        berechneMannschaften();
    }

    private void scalePoints() {
        switch (wk.getRegelwerk().getGesamtwertungSkalieren()) {
        case MEDAILLEN:
            scalePointsMedaillen();
            break;
        case INTERNATIONAL:
            scalePointsInternational();
            break;
        case INTERNATIONAL_PER_DISCIPLINE:
            scalePointsInternationalPerDiscipline();
            break;
        case ANZAHL_DISZIPLINEN:
        case KEINER:
            // Do Nothing
            break;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void scalePointsInternational() {
        Regelwerk aks2 = Utils.copy(aks);
        aks2.setFormelID(FormelILS.ID);
        for (int x = 0; x < aks2.size(); x++) {
            aks2.getAk(x).setDiszAnzahl(1);
            aks2.getAk(x).setDisciplineChoiceAllowed(false);
            aks2.getAk(x).setHLW(false);
        }
        MannschaftWettkampf copywk = new MannschaftWettkampf(aks2, wk.getStrafen());

        for (int a = 0; a < aks.size(); a++) {
            for (int b = 0; b < 2; b++) {
                Altersklasse ak = aks.getAk(a);
                boolean male = (b == 1);
                SchwimmerResult<ASchwimmer>[] result = ResultCalculator.getResults(wk, ak, male);

                ak = copywk.getRegelwerk().getAk(a);
                for (SchwimmerResult<ASchwimmer> aResult : result) {
                    ASchwimmer s = aResult.getSchwimmer();
                    if (!aResult.hasKeineWertung()) {
                        Mannschaft m = copywk.createMannschaft(s.getName(), male, s.getGliederungMitQGliederung(), a,
                                "");
                        if (aResult.getPlace() < 0) {
                            m.addStrafe(0, Strafe.DISQUALIFIKATION);
                        } else {
                            m.setZeit(0, aResult.getPlace());
                        }
                        switch (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).getArt()) {
                        case DISQUALIFIKATION:
                        case AUSSCHLUSS:
                        case NICHT_ANGETRETEN:
                            m.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                                    s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF));
                            break;
                        case NICHTS:
                        case STRAFPUNKTE:
                            break;
                        }
                        copywk.addSchwimmer(m);
                    }
                }
            }
        }
        wk = copywk;
        aks = aks2;
    }

    @SuppressWarnings({ "unchecked" })
    private void scalePointsMedaillen() {
        Regelwerk aks2 = Utils.copy(aks);
        aks2.setFormelID(FormelMedaillen.ID);
        for (int x = 0; x < aks2.size(); x++) {
            aks2.getAk(x).setDiszAnzahl(1);
            aks2.getAk(x).setDisciplineChoiceAllowed(false);
            aks2.getAk(x).setHLW(false);
        }
        MannschaftWettkampf copywk = new MannschaftWettkampf(aks2, wk.getStrafen());

        for (int a = 0; a < aks.size(); a++) {
            for (int b = 0; b < 2; b++) {
                Altersklasse ak = aks.getAk(a);
                boolean male = (b == 1);
                SchwimmerResult<ASchwimmer>[] result = ResultCalculator.getResults(wk, ak, male);

                ak = copywk.getRegelwerk().getAk(a);
                for (SchwimmerResult<ASchwimmer> aResult : result) {
                    ASchwimmer s = aResult.getSchwimmer();
                    Mannschaft m = copywk.createMannschaft(s.getName(), male, s.getGliederungMitQGliederung(), a, "");
                    if (aResult.getPlace() < 0) {
                        m.addStrafe(0, Strafe.DISQUALIFIKATION);
                    } else {
                        m.setZeit(0, aResult.getPlace());
                    }
                    switch (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).getArt()) {
                    case DISQUALIFIKATION:
                    case AUSSCHLUSS:
                    case NICHT_ANGETRETEN:
                        m.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                                s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF));
                        break;
                    case NICHTS:
                    case STRAFPUNKTE:
                        break;
                    }
                    copywk.addSchwimmer(m);
                }
            }
        }
        wk = copywk;
        aks = aks2;
    }

    @SuppressWarnings({ "unchecked" })
    private void scalePointsInternationalPerDiscipline() {
        Regelwerk aks2 = Utils.copy(aks);
        aks2.setFormelID(FormelILS.ID);
        for (int x = 0; x < aks2.size(); x++) {
            aks2.getAk(x).setHLW(false);
        }

        MannschaftWettkampf copywk = new MannschaftWettkampf(aks2, wk.getStrafen());

        for (int a = 0; a < aks.size(); a++) {
            for (int b = 0; b < 2; b++) {
                boolean male = (b == 1);

                Altersklasse ak = aks.getAk(a);
                SchwimmerResult<ASchwimmer>[] result = ResultCalculator.getResults(wk, ak, male);
                ak = copywk.getRegelwerk().getAk(a);

                for (SchwimmerResult<ASchwimmer> aResult : result) {
                    ASchwimmer s = aResult.getSchwimmer();
                    Mannschaft m = copywk.createMannschaft(s.getName(), male, s.getGliederungMitQGliederung(), a, "");
                    switch (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).getArt()) {
                    case DISQUALIFIKATION:
                    case AUSSCHLUSS:
                    case NICHT_ANGETRETEN:
                        m.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                                s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF));
                        break;
                    case NICHTS:
                    case STRAFPUNKTE:
                        break;
                    }
                    for (int j = 0; j < ak.getDiszAnzahl(); j++) {
                        m.setDisciplineChoice(j, s.isDisciplineChosen(j));

                        int rank = aResult.getResults()[j].getRank();
                        if (rank < 0) {
                            m.addStrafe(j, Strafe.DISQUALIFIKATION);
                        } else {
                            m.setZeit(j, rank);
                            switch (s.getAkkumulierteStrafe(j).getArt()) {
                            case DISQUALIFIKATION:
                            case AUSSCHLUSS:
                            case NICHT_ANGETRETEN:
                                m.addStrafe(j, s.getAkkumulierteStrafe(j));
                                break;
                            case NICHTS:
                            case STRAFPUNKTE:
                                break;
                            }
                        }
                    }
                    copywk.addSchwimmer(m);
                }
            }
        }

        wk = copywk;
        aks = aks2;
    }

    @SuppressWarnings({ "unchecked" })
    private void berechneMannschaften() {
        if (!aks.hasGesamtwertung()) {
            return;
        }
        LinkedList<String> gliederungen = wk.getGliederungenMitQGliederung();
        for (String s : gliederungen) {
            GesamtwertungSchwimmer gs = berechneGliederung(s);
            addSchwimmer(gs);
        }
    }

    @SuppressWarnings({ "fallthrough", "rawtypes" })
    private GesamtwertungSchwimmer berechneAltersklasse(String g, int aknummer, boolean male) {
        Altersklasse ak = aks.getAk(aknummer);
        GesamtwertungSchwimmer gs = new GesamtwertungSchwimmer(this, g, aks.size());
        if (!ak.getGesamtwertung(male)) {
            return gs;
        }

        int disz = ak.getDiszAnzahl();
        if (ak.hasHLW()) {
            disz++;
        }
        double[] points = new double[disz];

        int found = 0;
        if ((results[aknummer * 2 + (male ? 1 : 0)] != null) && (results[aknummer * 2 + (male ? 1 : 0)].length > 0)) {
            for (SchwimmerResult value : results[aknummer * 2 + (male ? 1 : 0)]) {
                if (value.getSchwimmer().getGliederungMitQGliederung().equals(g)
                        && !value.getSchwimmer().isAusserKonkurrenz()) {
                    switch (modus) {
                    case BestWithoutBlocking:
                    case Best:
                        if (found < 1) {
                            points[0] += value.getPoints();
                        }
                        break;
                    case Best4:
                        if (found < 4) {
                            points[0] += value.getPoints();
                        }
                        break;
                    case All:
                        points[0] += value.getPoints();
                        break;
                    case BestInDiscipline:
                        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                            double p = value.getResults()[x].getPoints();
                            if (p > points[x])
                                points[x] = p;
                        }
                        if (ak.hasHLW() && points[ak.getDiszAnzahl()] < value.getSchwimmer().getHLWPunkte()) {
                            points[ak.getDiszAnzahl()] = value.getSchwimmer().getHLWPunkte();
                        }
                        break;
                    }
                    found++;
                }
            }
        }
        if (found == 0 && aks.isGesamtwertungHart()) {
            return null;
        }

        for (double point : points) {
            gs.addPunkte(aknummer, male, point);
        }
        if (aks.getGesamtwertungSkalieren() == Skalierungsmodus.ANZAHL_DISZIPLINEN) {
            gs.setPunkte(aknummer, male, gs.getPunkte() / disz);
        }
        return gs;
    }

    private GesamtwertungSchwimmer berechneGliederung(String g) {
        GesamtwertungSchwimmer gs = new GesamtwertungSchwimmer(this, g, aks.size());
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                GesamtwertungSchwimmer temp = berechneAltersklasse(g, x, y == 1);
                if (temp == null) {
                    gs.nichtAngetreten();
                } else {
                    gs.addPunkte(x, y == 1, temp.getPunkte());
                }
            }
        }
        return gs;
    }

    public GesamtwertungSchwimmer[] getResult() {
        LinkedList<GesamtwertungSchwimmer> s = getSchwimmer();
        GesamtwertungSchwimmer[] gs = s.toArray(new GesamtwertungSchwimmer[0]);
        Arrays.sort(gs, new GesamtwertungSchwimmerComparator());
        return gs;
    }

    @Override
    public boolean isEinzel() {
        return false;
    }
}