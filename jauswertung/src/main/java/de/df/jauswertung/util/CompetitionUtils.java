/*
 * Created on 16.03.2006
 */
package de.df.jauswertung.util;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.ResultUtils;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jauswertung.util.vergleicher.SchwimmerAKVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerGliederungVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerStartnummernVergleicher;
import de.df.jutils.gui.util.EDTUtils;

public final class CompetitionUtils {

    private CompetitionUtils() {
        // Hide
    }

    @SuppressWarnings("rawtypes")
    public final static SchwimmerStartnummernVergleicher VERGLEICHER_STARTNUMMER  = new SchwimmerStartnummernVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerAKVergleicher           VERGLEICHER_ALTERSKLASSE = new SchwimmerAKVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerGliederungVergleicher   VERGLEICHER_GLIEDERUNG   = new SchwimmerGliederungVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerMeldepunkteVergleicher  VERGLEICHER_MELDEPUNKTE  = new SchwimmerMeldepunkteVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerNameVergleicher         VERGLEICHER_NAME         = new SchwimmerNameVergleicher();

    public static <T extends ASchwimmer> AWettkampf<T> convertResultsToMeldung(AWettkampf<T> wk, boolean alltimes) {
        wk = Utils.copy(wk);
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                JResultTable result = JResultTable.getResultTable(wk, aks.getAk(x), y == 1, false, true, 0);
                for (int z = 0; z < result.getRowCount(); z++) {
                    ASchwimmer s = result.getSchwimmer(z);
                    s.setMeldepunkte(0, result.getPunkte(z));
                    s.setMeldungMitProtokoll(0, true);
                    for (int w = 0; w < s.getAK().getDiszAnzahl(); w++) {
                        Strafe str = s.getAkkumulierteStrafe(w);
                        switch (str.getArt()) {
                        case AUSSCHLUSS:
                        case DISQUALIFIKATION:
                        case NICHT_ANGETRETEN:
                            if (alltimes) {
                                s.setMeldezeit(w, s.getZeit(w));
                            } else {
                                s.setMeldezeit(w, 0);
                            }
                            break;
                        case NICHTS:
                        case STRAFPUNKTE:
                            if (alltimes || str.getStrafpunkte() <= 0) {
                                s.setMeldezeit(w, s.getZeit(w));
                            } else {
                                s.setMeldezeit(w, 0);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return wk;
    }

    public static <T extends ASchwimmer> AWettkampf<T> getFilteredInstance(AWettkampf<T> wk) {
        Filter f = wk.getCurrentFilter();
        if (f.getGliederungen() == null) {
            return wk;
        }
        String[] names = f.getGliederungen();
        AWettkampf<T> w = Utils.copy(wk);

        w.disableUpdates();

        LinkedList<String> gliederungen = w.getGliederungenMitQGliederung();
        ListIterator<String> li = gliederungen.listIterator();
        while (li.hasNext()) {
            String s = li.next();
            for (String name : names) {
                if (s.equals(name)) {
                    li.remove();
                    break;
                }
            }
        }
        LinkedList<T> swimmer = SearchUtils.getSchwimmer(w, gliederungen.toArray(new String[gliederungen.size()]), true);
        if (swimmer.isEmpty()) {
            return w;
        }
        w.removeSchwimmer(swimmer);

        w.enableUpdates();

        w.setFilter(wk.getFilter());
        w.setCurrentFilterIndex(wk.getCurrentFilterIndex());
        return w;
    }

    public static <T extends ASchwimmer> void minimizeCompetition(AWettkampf<T> wk) {
        if (!wk.hasSchwimmer()) {
            return;
        }
        int min = wk.getRegelwerk().size();
        for (int x = wk.getRegelwerk().size() - 1; x >= 0; x--) {
            if (SearchUtils.hasSchwimmer(wk, wk.getRegelwerk().getAk(x))) {
                break;
            }
            min--;
        }
        wk.getRegelwerk().setSize(min);
    }

    public static <T extends ASchwimmer> TableModel generateMedaillenspiegel(AWettkampf<T> wk) {
        if (!wk.hasSchwimmer()) {
            return null;
        }
        Regelwerk aks = wk.getRegelwerk();
        Hashtable<String, int[]> medaille = new Hashtable<String, int[]>();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                SchwimmerResult<T>[] results = ResultUtils.getResults(wk, aks.getAk(x), y == 1);
                for (SchwimmerResult<T> result : results) {
                    if (result.getPoints() < 0.005) {
                        break;
                    }
                    if ((result.getPlace() > 3) || (result.getPlace() <= 0)) {
                        break;
                    }
                    if (result.getSchwimmer().isAusserKonkurrenz()) {
                        break;
                    }

                    int index = result.getPlace() - 1;
                    String gliederung = result.getSchwimmer().getGliederung();
                    int[] values = medaille.get(gliederung);
                    if (values == null) {
                        values = new int[3];
                        for (int k = 0; k < 3; k++) {
                            values[k] = 0;
                        }
                        medaille.put(gliederung, values);
                    }
                    values[index]++;
                }
            }
        }
        LinkedList<Medal> medals = new LinkedList<Medal>();
        Enumeration<String> li = medaille.keys();
        while (li.hasMoreElements()) {
            String name = li.nextElement();
            int[] values = medaille.get(name);
            if (values != null) {
                medals.add(new Medal(name, values));
            }
        }
        Collections.sort(medals);

        int x = 0;
        int points = 0;
        int offset = 1;
        ListIterator<Medal> lm = medals.listIterator();
        while (lm.hasNext()) {
            Medal m = lm.next();
            if (points != m.getPoints()) {
                x += offset;
                points = m.getPoints();
                offset = 1;
                m.setRank(x);
            } else {
                offset++;
                m.setRank(-1);
            }
        }
        return new MedalTableModel(medals.toArray(new Medal[medals.size()]));
    }

    @SuppressWarnings("serial")
    static class MedalTableModel extends AbstractTableModel {

        private static String[] titles = new String[] { I18n.get("Rank"), I18n.get("Organisation"), "  " + I18n.get("Gold") + "  ",
                "  " + I18n.get("Silver") + "  ", "  " + I18n.get("Bronze") + "  " };

        private final Medal[]   medals;

        @Override
        public String getColumnName(int column) {
            return titles[column];
        }

        public MedalTableModel(Medal[] medals) {
            this.medals = medals;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return medals.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                int rank = medals[rowIndex].getRank();
                if (rank <= 0) {
                    return "";
                }
                return rank;
            case 1:
                return medals[rowIndex].getName();
            case 2:
                return medals[rowIndex].getMedals(0);
            case 3:
                return medals[rowIndex].getMedals(1);
            case 4:
                return medals[rowIndex].getMedals(2);
            default:
                return null;
            }
        }
    }

    static class Medal implements Comparable<Medal> {

        private String name;
        private int[]  medals;
        private int    points;
        private int    rank;

        public Medal(String name, int[] medals) {
            this.name = name;
            this.medals = medals;
            points = 0;
            rank = 0;
            for (int x = 0; x < medals.length; x++) {
                points += (medals.length - x) * medals[x];
            }
        }

        public int getMedals(int x) {
            return medals[x];
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Medal) {
                return compareTo((Medal) obj) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (name + points + rank).hashCode();
        }

        @Override
        public int compareTo(Medal o) {
            for (int x = 0; x < medals.length; x++) {
                if (medals[x] != o.medals[x]) {
                    return o.medals[x] - medals[x];
                }
            }
            return 0;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    private static class Result implements Comparable<Result> {

        private final double punkte;
        private final int    time;
        private final Strafe strafe;

        public Result(double p, int t, Strafe s) {
            punkte = p;
            time = t;
            strafe = s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Result) {
                return compareTo((Result) obj) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {

            return Double.valueOf(punkte).hashCode();
        }

        @Override
        public int compareTo(Result o) {
            return (int) ((o.punkte - punkte) * 100);
        }

        public int getZeit() {
            return time;
        }

        public Strafe getStrafe() {
            return strafe;
        }
    }

    /**
     * Erstellt anhand der "Bemerkungen" der Schwimmer zusammengehörende
     * Mannschaften. Die Zeiten der Mannschaften entsprechen der der durch
     * schnittszeit der besten <code>min</code> Schwimmer.
     * 
     * @param wk
     * @param min
     * @param max
     * @return
     */
    public static <T extends ASchwimmer> MannschaftWettkampf generateTeamCompetition(AWettkampf<T> wk, int min, int max) {
        if (min <= 0) {
            throw new IllegalArgumentException("Min must not be lower than or equal to 0.");
        }
        if (max < min) {
            throw new IllegalArgumentException("Min must not be higher than max.");
        }

        Hashtable<String, LinkedList<T>> teams = new Hashtable<String, LinkedList<T>>();
        {
            ListIterator<T> li = wk.getSchwimmer().listIterator();
            while (li.hasNext()) {
                T t = li.next();
                if (t.getBemerkung().length() > 0) {
                    String team = t.getBemerkung();

                    LinkedList<T> ll = teams.get(team);
                    if (ll == null) {
                        ll = new LinkedList<T>();
                        teams.put(team, ll);
                    }
                    ll.addLast(t);
                }
            }
        }

        MannschaftWettkampf mwk = new MannschaftWettkampf(wk.getRegelwerk(), wk.getStrafen());

        Enumeration<String> en = teams.keys();
        while (en.hasMoreElements()) {
            String team = en.nextElement();
            LinkedList<T> ll = teams.get(team);
            if ((ll.size() >= min) && (ll.size() <= max)) {
                T tn = ll.getFirst();

                int index = team.indexOf("|");
                if (index > 0) {
                    team = team.substring(0, index).trim();
                }

                int aknummer = tn.getAKNummer();

                ListIterator<T> li = ll.listIterator();
                while (li.hasNext()) {
                    T t = li.next();
                    if (t.getAKNummer() > aknummer) {
                        aknummer = t.getAKNummer();
                    }
                }

                String[] disziplinen = tn.getAK().getDisziplinenNamen();
                Mannschaft m = mwk.createMannschaft(team, tn.isMaennlich(), tn.getGliederung(), aknummer, "");
                li = ll.listIterator();
                while (li.hasNext()) {
                    T t = li.next();
                    if (t.isMaennlich()) {
                        m.setMaennlich(true);
                    }
                    boolean penalty = false;
                    if (t.getAK().getDiszAnzahl() != disziplinen.length) {
                        penalty = true;
                    } else {
                        for (int x = 0; x < t.getAK().getDiszAnzahl(); x++) {
                            if (!disziplinen[x].equals(t.getAK().getDisziplin(x, true).getName())) {
                                penalty = true;
                            }
                        }
                    }
                    if (penalty) {
                        if (m.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF).size() == 0) {
                            m.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF, new Strafe(I18n.get("MixedAgegroups"), "GA", Strafarten.DISQUALIFIKATION, 0));
                        }
                    }
                }

                Altersklasse ak = wk.getRegelwerk().getAk(aknummer);

                for (int i = 0; i < ak.getDiszAnzahl(); i++) {
                    LinkedList<Result> times = new LinkedList<Result>();
                    SchwimmerResult<T>[] result = ResultUtils.getResults(wk, ll, ak, m.isMaennlich(), null, true);
                    for (SchwimmerResult<T> sr : result) {
                        SchwimmerData<T> sd = sr.getResults()[i];
                        T t = sr.getSchwimmer();
                        if ((t.isDisciplineChosen(i)) && (t.getZeit(i) > 0)) {
                            Strafe s = t.getAkkumulierteStrafe(i);
                            switch (s.getArt()) {
                            case NICHT_ANGETRETEN:
                            case DISQUALIFIKATION:
                            case AUSSCHLUSS:
                                // Time will not be included
                                break;
                            case STRAFPUNKTE:
                                times.addLast(new Result(sd.getPoints(), t.getZeit(i), s));
                                break;
                            default:
                            case NICHTS:
                                times.addLast(new Result(sd.getPoints(), t.getZeit(i), s));
                                break;
                            }
                        }
                    }

                    if (times.size() >= min) {
                        Collections.sort(times);
                        ListIterator<Result> time = times.listIterator();
                        int counter = 0;
                        while (counter < min) {
                            Result r = time.next();
                            int zeit = r.getZeit();
                            m.setZeit(i, m.getZeit(i) + zeit);
                            m.addStrafe(i, r.getStrafe());
                            counter++;
                        }
                        m.setZeit(i, (int) Math.round((double) m.getZeit(i) / (double) min));
                    } else {
                        m.addStrafe(i, new Strafe(I18n.get("NotEnoughValidTimes"), "GZ", Strafarten.NICHT_ANGETRETEN, 0));
                    }
                }

                mwk.addSchwimmer(m);

            }
        }
        return mwk;
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateWertungsgruppenwettkampf(AWettkampf<T> wk) {
        return generateWertungsgruppenwettkampf(wk, false);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateWertungsgruppenwettkampf(AWettkampf<T> wk, boolean nurMehrkampf) {
        AWettkampf<T> wkx = Utils.copy(wk);
        Wertungsgruppe[] wgs = wkx.getRegelwerk().getWertungsgruppen(nurMehrkampf);
        if (wgs.length <= 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        LinkedList<T>[] swimmers = new LinkedList[wgs.length];
        for (int x = 0; x < swimmers.length; x++) {
            swimmers[x] = new LinkedList<T>();
        }

        wk.disableUpdates();
        for (Altersklasse ak : wkx.getRegelwerk().getAks()) {
            if ((ak.getWertungsgruppe() == null) || (wkx.getRegelwerk().getWertungsgruppe(ak.getWertungsgruppe()) == null)) {
                wkx.removeSchwimmer(SearchUtils.getSchwimmer(wkx, ak));
            }
        }
        wk.enableUpdates();

        int[] indizes = new int[wgs.length];
        for (int x = 0; x < wgs.length; x++) {
            indizes[x] = -1;
            Wertungsgruppe wg = wgs[x];
            for (int y = 0; y < wkx.getRegelwerk().size(); y++) {
                Altersklasse ak = wkx.getRegelwerk().getAk(y);
                if (wg.getName().equals(ak.getWertungsgruppe())) {
                    if (indizes[x] < 0) {
                        indizes[x] = y;
                    }
                    swimmers[x].addAll(SearchUtils.getSchwimmer(wkx, ak));
                }
            }
        }

        for (int x = 0; x < wgs.length; x++) {
            if (!swimmers[x].isEmpty()) {
                int pos = indizes[x];
                Altersklasse ak = wkx.getRegelwerk().getAk(pos);
                Wertungsgruppe wg = wgs[x];

                ak.setName(wg.getName());
                ak.setEinzelwertung(wg.isProtokollMitEinzelwertung());
                ak.setEinzelwertungHlw(wg.isEinzelwertungHlw());
                ak.setMehrkampfwertung(true);
                ak.setStrafeIstDisqualifikation(wg.isStrafeIstDisqualifikation());
                ak.setWertungsgruppe(null);

                for (T t : swimmers[x]) {
                    t.setAKNummer(pos, false);
                }
            }
        }

        return wkx;
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, int akn, boolean maennlich, boolean removeEmpty) {
        return generateEinzelwertungswettkampf(wk, akn, true, maennlich, removeEmpty);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, int akn, boolean removeEmpty) {
        return generateEinzelwertungswettkampf(wk, akn, false, true, removeEmpty);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, String wgname, boolean removeEmpty) {
        wk = Utils.copy(wk);
        Wertungsgruppe wg = wk.getRegelwerk().getWertungsgruppe(wgname);
        LinkedList<T> teilnehmer = new LinkedList<T>();
        int index = -1;
        wk.disableUpdates();
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            if (wg.getName().equals(ak.getWertungsgruppe())) {
                LinkedList<T> temp = SearchUtils.getSchwimmer(wk, ak);
                teilnehmer.addAll(temp);
                if (index < 0) {
                    index = x;
                    ak.setEinzelwertung(wg.isProtokollMitEinzelwertung());
                    ak.setEinzelwertungHlw(wg.isEinzelwertungHlw());
                    ak.setStrafeIstDisqualifikation(wg.isStrafeIstDisqualifikation());
                    ak.setName(wg.getName());
                } else {
                    for (T t : temp) {
                        t.setAKNummer(index, false);
                    }
                }
            } else {
                for (T t : SearchUtils.getSchwimmer(wk, ak)) {
                    wk.removeSchwimmer(t);
                }
            }
        }
        wk.enableUpdates();
        if (index < 0) {
            return null;
        }
        return generateEinzelwertungswettkampf(wk, index, false, true, removeEmpty);
    }

    private static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, int akn, boolean filtersex, boolean maennlich,
            boolean removeEmpty) {
        return EDTUtils.executeOnEDTwithReturn(() -> generateEinzelwertungswettkampfI(wk, akn, filtersex, maennlich, removeEmpty));
    }

    @SuppressWarnings({ "unchecked", "null" })
    private static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampfI(AWettkampf<T> wk, int akn, boolean filtersex, boolean maennlich,
            boolean removeEmpty) {
        if (wk == null) {
            throw new NullPointerException();
        }
        AWettkampf<T> temp = wk;
        boolean einzel = temp instanceof EinzelWettkampf;
        Altersklasse ak = wk.getRegelwerk().getAk(akn);
        if (!ak.hasEinzelwertung()) {
            return null;
        }
        int anzahl = ak.getDiszAnzahl();
        Regelwerk aks = new Regelwerk(ak.getDiszAnzahl(), einzel, wk.getRegelwerk().getFormelID());
        for (int x = 0; x < anzahl; x++) {
            Altersklasse a = aks.getAk(x);
            a.setMemberCounts(ak.getMinMembers(), ak.getMaxMembers());
            a.setName(ak.getName());
            a.setDiszAnzahl(1);
            a.setDisziplin(ak.getDisziplin(x, true), 0, true);
            a.setDisziplin(ak.getDisziplin(x, false), 0, false);
            a.setStrafeIstDisqualifikation(ak.isStrafeIstDisqualifikation());
            a.setHLW(ak.hasHLW() && ak.isEinzelwertungHlw());
        }
        @SuppressWarnings("rawtypes")
        AWettkampf w = null;
        EinzelWettkampf ewk = null;
        MannschaftWettkampf mwk = null;
        if (einzel) {
            w = new EinzelWettkampf(aks, wk.getStrafen());
            ewk = (EinzelWettkampf) w;
        } else {
            w = new MannschaftWettkampf(aks, wk.getStrafen());
            mwk = (MannschaftWettkampf) w;
        }
        int min = 0;
        int max = 2;
        if (filtersex) {
            min = (maennlich ? 1 : 0);
            max = (maennlich ? 2 : 1);
        }

        for (int z = min; z < max; z++) {
            OWLaufliste<T> llow = wk.getLauflisteOW();
            OWLaufliste<T> llNeu = w.getLauflisteOW();
            if (llow != null && llNeu != null) {
                for (OWDisziplin<T> owd : llow.getDisziplinen()) {
                    if (owd.akNummer == akn && owd.maennlich == (z == 1)) {
                        OWDisziplin<T> neu = llNeu.addDisziplin(owd.disziplin, z == 1, 0, owd.round);
                        for (OWLauf<T> lauf : owd.laeufe) {
                            OWLauf<T> laufNeu = new OWLauf<T>(w, neu.Id, lauf, false);
                            neu.laeufe.add(laufNeu);
                        }
                    }
                }
            }
        }

        w.disableUpdates();
        for (int z = min; z < max; z++) {
            JResultTable jrt = JResultTable.getResultTable(wk, ak, z == 1, false, true, 0);
            for (int x = 0; x < jrt.getRowCount(); x++) {
                ASchwimmer s = jrt.getSchwimmer(x);
                if ((!removeEmpty) || (!jrt.getResult(x).hasKeineWertung())) {
                    boolean include = true;
                    if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                        if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                            include = false;
                        }
                    }
                    for (int y = 0; y < anzahl; y++) {
                        if (include) {
                            if (s.isDisciplineChosen(y)) {
                                ASchwimmer neu = null;
                                if (einzel) {
                                    Teilnehmer t = (Teilnehmer) s;
                                    neu = ewk.createTeilnehmer(t.getNachname(), t.getVorname(), t.getJahrgang(), t.isMaennlich(), t.getGliederung(), y,
                                            t.getBemerkung());
                                } else {
                                    Mannschaft t = (Mannschaft) s;
                                    neu = mwk.createMannschaft(t.getName(), t.isMaennlich(), t.getGliederung(), y, t.getBemerkung());
                                    Mannschaft mneu = (Mannschaft) neu;
                                    for (int i = 0; i < t.getMaxMembers(); i++) {
                                        t.getMannschaftsmitglied(i).copyTo(mneu.getMannschaftsmitglied(i));
                                    }
                                }

                                neu.setAusserKonkurrenz(s.isAusserKonkurrenz());
                                neu.setZeit(0, s.getZeit(y));
                                neu.setStrafen(0, s.getStrafen(y));
                                neu.setStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF, s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));
                                neu.setStarter(0, s.getStarter(y));
                                neu.setBemerkung("" + s.getStartnummer());// StartnumberFormatManager.format(s));
                                neu.setQualifikationsebene(s.getQualifikationsebene());
                                neu.setQualifikation(s.getQualifikation());

                                w.addSchwimmer(neu);

                                if (wk.isHeatBased()) {
                                    OWLaufliste<T> llow = wk.getLauflisteOW();
                                    OWLaufliste<T> llNeu = w.getLauflisteOW();

                                    for (OWDisziplin<T> owd : llow.getDisziplinen()) {
                                        if (owd.akNummer == akn && owd.maennlich == s.isMaennlich() && owd.disziplin == y) {

                                            Eingabe e1 = s.getEingabe(owd.Id);
                                            if (e1 != null) {
                                                Eingabe e = neu.getEingabe(OWDisziplin.getId(y, s.isMaennlich(), 0, owd.round), true);
                                                e.setStarter(e1.getStarter());
                                                e.setZeit(e1.getZeit());
                                                for (Strafe str : e1.getStrafen()) {
                                                    e.addStrafe(str);
                                                }
                                                OWDisziplin<T> OWDneu = llNeu.getDisziplin(owd.disziplin, z == 1, 0, owd.round);

                                                boolean found = false;

                                                for (OWLauf<T> lauf : owd.laeufe) {
                                                    int lane = lauf.GetSchwimmerIndex(s.getStartnummer());
                                                    if (lane >= 0) {
                                                        for (OWLauf<T> laufNeu : OWDneu.getLaeufe()) {
                                                            if (laufNeu.getLaufnummer() == lauf.getLaufnummer()
                                                                    && laufNeu.getLaufbuchstabe() == lauf.getLaufbuchstabe()) {
                                                                found = true;
                                                                laufNeu.setSchwimmer(lane, (T) neu);
                                                            }
                                                        }
                                                    }
                                                }
                                                if (found) {
                                                    OWDneu.addSchwimmer((T) neu);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        w.enableUpdates();
        if (!w.hasSchwimmer()) {
            return null;
        }

        LinkedList<Zielrichterentscheid<T>> neuezes = new LinkedList<Zielrichterentscheid<T>>();
        LinkedList<Zielrichterentscheid<T>> zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk)[0];
        ListIterator<Zielrichterentscheid<T>> li = zes.listIterator();
        while (li.hasNext()) {
            Zielrichterentscheid<T> ze = li.next();
            Zielrichterentscheid<T> neu = new Zielrichterentscheid<T>();
            for (T t : ze.getSchwimmer()) {
                if (t.getAKNummer() == akn) {
                    LinkedList<T> ll = SearchUtils.getSchwimmer(w, w.getRegelwerk().getAk(ze.getDisziplin()), t.isMaennlich(), "" + t.getStartnummer());
                    if (ll.size() == 1) {
                        neu.addSchwimmer(ll.getFirst());
                    }
                }
            }
            if (neu.isValid()) {
                neuezes.addLast(neu);
            }
        }
        w.setZielrichterentscheide(neuezes);

        wk.copyProperties(w);
        // w.setProperty(PropertyConstants.URKUNDE, Utils.copy(wk.getProperty(PropertyConstants.URKUNDE)));
        // w.setProperty(PropertyConstants.URKUNDE_EINZELWERTUNG, Utils.copy(wk.getProperty(PropertyConstants.URKUNDE_EINZELWERTUNG)));

        w.setFilter(wk.getFilter());
        w.setCurrentFilterIndex(wk.getCurrentFilterIndex());

        w.changedNow();

        // OutputManager.speichereWettkampf("D:\\test-w2.wk", w);

        return w;
    }

    public static <T extends ASchwimmer> boolean hasEinzelwertungswettkampf(AWettkampf<T> wk, int akn, boolean removeEmpty) {
        if (wk == null) {
            return false;
        }
        Altersklasse ak = wk.getRegelwerk().getAk(akn);
        if (!ak.hasEinzelwertung()) {
            return false;
        }
        int anzahl = ak.getDiszAnzahl();
        if (removeEmpty) {
            for (int z = 0; z < 2; z++) {
                JResultTable jrt = JResultTable.getResultTable(wk, ak, z == 1, false, true, 0);
                for (int x = 0; x < jrt.getRowCount(); x++) {
                    ASchwimmer s = jrt.getSchwimmer(x);
                    if (!jrt.getResult(x).hasKeineWertung()) {
                        for (int y = 0; y < anzahl; y++) {
                            if (s.isDisciplineChosen(y)) {
                                boolean include = true;
                                if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                                    if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                                        include = false;
                                    }
                                }
                                if (include) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int z = 0; z < 2; z++) {
                LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, ak, z == 1);
                for (T s : swimmers) {
                    for (int y = 0; y < anzahl; y++) {
                        if (s.isDisciplineChosen(y)) {
                            boolean include = true;
                            if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                                if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                                    include = false;
                                }
                            }
                            if (include) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public static <T extends ASchwimmer> AWettkampf<T> createCompetitionWithCompleteDisciplines(AWettkampf<T> orig) {
        AWettkampf<T> wk = Utils.copy(orig);
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<T> swimmers = null;
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    if (!wk.isDisciplineComplete(z, x, y == 1)) {
                        if (swimmers == null) {
                            swimmers = SearchUtils.getSchwimmer(wk, ak, y == 1);
                        }
                        for (T s : swimmers) {
                            s.setZeit(z, 0);
                            s.setStrafen(z, null);
                        }
                    }
                }
                if (ak.hasHLW() && !wk.isHlwComplete(x, y == 1)) {
                    if (swimmers == null) {
                        swimmers = SearchUtils.getSchwimmer(wk, ak, y == 1);
                    }
                    for (T s : swimmers) {
                        for (int i = 0; i < s.getMaximaleHLW(); i++) {
                            s.setHLWPunkte(i, 0);
                            s.setHLWState(i, HLWStates.NOT_ENTERED);
                        }
                    }
                }
            }
        }
        return wk;
    }

    public static <T extends ASchwimmer> LinkedList<T> switchAGs(AWettkampf<T> wk) {
        LinkedList<T> notchanged = new LinkedList<T>();
        ListIterator<T> li = wk.getSchwimmer().listIterator();
        Regelwerk aks = wk.getRegelwerk();
        while (li.hasNext()) {
            T t = li.next();
            String neu = t.getBemerkung();
            String alt = t.getAK().getName();
            int x = aks.getIndex(neu);
            if (x >= 0) {
                t.setAKNummer(x, true);
                t.setBemerkung(alt);
            } else {
                notchanged.addLast(t);
            }
        }
        if (notchanged.isEmpty()) {
            return null;
        }
        return notchanged;
    }

    public static <T extends ASchwimmer> int[][] getSchwimmerAmounts(AWettkampf<T> wk) {
        int[][] amounts = new int[wk.getRegelwerk().size()][2];
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                amounts[x][y] = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1).size();
            }
        }
        return amounts;
    }

    public static <T extends ASchwimmer> int[][] getOrganizationAmounts(AWettkampf<T> wk) {
        Hashtable<String, String> orgs = new Hashtable<String, String>();
        int[][] amounts = new int[wk.getRegelwerk().size()][2];
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                orgs.clear();
                LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1);
                for (T t : swimmers) {
                    String key = t.getGliederungMitQGliederung();
                    if (!orgs.containsKey(key)) {
                        orgs.put(key, key);
                    }
                }
                amounts[x][y] = orgs.size();
            }
        }
        return amounts;
    }

    public static <T extends ASchwimmer> int[][] getQOrganizationAmounts(AWettkampf<T> wk) {
        Hashtable<String, String> orgs = new Hashtable<String, String>();
        int[][] amounts = new int[wk.getRegelwerk().size()][2];
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                orgs.clear();
                LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1);
                for (T t : swimmers) {
                    String key = t.getQualifikationsebene().toLowerCase();
                    if (key != null && key.length() > 0) {
                        if (!orgs.containsKey(key)) {
                            orgs.put(key, key);
                        }
                    }
                }
                amounts[x][y] = orgs.size();
            }
        }
        return amounts;
    }

    public static <T extends ASchwimmer> boolean isDisciplineFinished(AWettkampf<T> wk, String id) {
        OWLaufliste<T> ll = wk.getLauflisteOW();
        OWDisziplin<T> d = ll.getDisziplin(id);
        if (d == null) {
            return false;
        }
        LinkedList<T> swimmers = d.getSchwimmer();

        Altersklasse ak = wk.getRegelwerk().getAk(d.akNummer);
        if (swimmers == null) {
            swimmers = SearchUtils.getSchwimmer(wk, ak, d.maennlich, d.disziplin);
        }
        for (T t : swimmers) {
            if (!t.isFinished(id)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(AWettkampf<T> cwk, OWSelection t) {
        if (cwk == null) {
            return null;
        }
        AWettkampf<T> wk = Utils.copy(cwk);
        Altersklasse akx = wk.getRegelwerk().getAk(t.akNummer);
        if (!akx.isEinzelwertungHlw()) {
            akx.setHLW(false);
        }
        Disziplin disziplin = akx.getDisziplin(t.discipline, t.male);

        // Remove disciplines before participants for performance reasons
        for (OWDisziplin<T> dx : wk.getLauflisteOW().getDisziplinen()) {
            if (!dx.Id.equals(t.getId())) {
                wk.getLauflisteOW().removeDiscipline(t.getId());
            }
        }

        OWDisziplin<T> d = cwk.getLauflisteOW().getDisziplin(t);

        int qualifiedPerHeat = wk.getIntegerProperty("qualifiedPerHeat", 0);
        if (!t.isFinal && disziplin.getRunden().length > t.round) {
            int amount = disziplin.getRunden()[t.round];
            if (d == null || d.getLaeufe() == null) {
                qualifiedPerHeat = 0;
            } else {
                int heats = d.getLaeufe().size();
                qualifiedPerHeat = amount / heats;
            }
        }

        wk.getLaufliste().clear();
        wk.getLaufliste().resetAll();
        wk.getLaufliste().setIsFinal(cwk.isFinal(t));
        wk.setProperty("isQualified", t.round > 0);
        wk.setProperty("qualifiedPerHeat", qualifiedPerHeat);
        wk.setProperty("isFinal", t.isFinal);
        wk.setProperty("round", t.round);
        wk.setProperty("roundId", disziplin.getRundenId(t.round));
        wk.setProperty(PropertyConstants.HEATS_EMPTY_LIST, false);

        if (t.round < 0) {
            throw new IllegalArgumentException("round must be at least 0.");
        } else if (t.round == 0) {
            wk.disableUpdates();
            for (T s : new ArrayList<T>(wk.getSchwimmer())) {
                if (s.getAKNummer() != t.akNummer) {
                    wk.removeSchwimmer(s);
                } else if (s.isMaennlich() != t.male) {
                    wk.removeSchwimmer(s);
                } else if (!s.isDisciplineChosen(t.discipline)) {
                    wk.removeSchwimmer(s);
                }
            }
            wk.enableUpdates();
        } else if (d != null) {
            wk.disableUpdates();
            HashSet<Integer> sn = new HashSet<Integer>(d.getSchwimmer().stream().map(s -> s.getStartnummer()).collect(Collectors.toList()));
            for (T s : wk.getSchwimmer()) {
                if (!sn.contains(s.getStartnummer())) {
                    wk.removeSchwimmer(s);
                }
            }
            wk.enableUpdates();
        } else {
            OWSelection t1 = new OWSelection(cwk.getRegelwerk().getAk(t.akNummer), t.akNummer, t.male, t.discipline, t.round - 1, false);
            AWettkampf<T> wk1a = createCompetitionFor(cwk, t1);

            wk.disableUpdates();

            wk1a.getRegelwerk().getAk(t.akNummer).setEinzelwertung(true);
            wk1a.getRegelwerk().getAk(t.akNummer).setChosenDisciplines(1, 1, 1);
            wk1a.getRegelwerk().setFormelID(FormelILS.ID);
            AWettkampf<T> wk1 = generateEinzelwertungswettkampf(wk1a, t.akNummer, true);
            if (wk1 == null) {
                return null;
            }
            int qualified = cwk.getRegelwerk().getAk(t.akNummer).getDisziplin(t.discipline, t.male).getRunden()[t1.round];
            SchwimmerResult<T>[] results = ResultUtils.getResults(wk1, wk1.getRegelwerk().getAk(0), t.male);

            HashSet<Integer> sn = new HashSet<Integer>();
            for (SchwimmerResult<T> r : results) {
                SchwimmerData<T> rs = r.getResults()[0];
                if (rs.getRank() >= 0 && rs.getRank() <= qualified) {
                    int snx = Integer.parseInt(r.getSchwimmer().getBemerkung());
                    sn.add(snx);
                }
            }
            for (T s : wk.getSchwimmer()) {
                if (!sn.contains(s.getStartnummer())) {
                    wk.removeSchwimmer(s);
                }
            }

            wk.enableUpdates();
        }

        Regelwerk r = wk.getRegelwerk();
        Altersklasse ak = r.getAk(t.akNummer);
        ak.setDisciplineChoiceAllowed(false);

        if (t.round == 0) {
            for (T s : wk.getSchwimmer()) {
                s.setMeldezeit(0, s.getMeldezeit(t.discipline));
                s.setStarter(0, s.getStarter(t.discipline));
            }
        } else {
            String id = OWDisziplin.getId(t.akNummer, t.male, t.discipline, t.round - 1);
            for (T s : wk.getSchwimmer()) {
                s.setMeldezeit(0, s.getZeit(id));
                s.setStarter(0, getStarter(s, t.akNummer, t.male, t.discipline, t.round));
            }
        }

        for (int x = 0; x < r.size(); x++) {
            if (x != t.akNummer) {
                r.getAk(x).setDiszAnzahl(0);
                r.getAk(x).setHLW(false);
            }
        }

        if (ak.getDiszAnzahl() > 0) {
            if (t.discipline > 0) {
                ak.setDisziplin(ak.getDisziplin(t.discipline, false), 0, false);
                ak.setDisziplin(ak.getDisziplin(t.discipline, true), 0, true);
            }
            ak.setDiszAnzahl(1);
        }
        for (T tx : wk.getSchwimmer()) {
            tx.updateAK(t.akNummer, true);
        }

        for (T s : new ArrayList<T>(wk.getSchwimmer())) {
            int zeit = 0;
            LinkedList<Strafe> strafen = new LinkedList<Strafe>();
            if (d != null) {
                T sx = SearchUtils.getSchwimmer(wk, s.getStartnummer());
                zeit = sx.getZeit(d.Id);
                strafen = sx.getStrafen(d.Id);
            }
            s.setBemerkung("" + s.getStartnummer());
            s.clear();
            s.setZeit(0, zeit);
            s.setStrafen(0, strafen);
        }

        if (d != null && !d.isEmpty()) {
            wk.setProperty(HEATS_LANES, d.getBahnen());
            Laufliste<T> ll = wk.getLaufliste();
            int x = 0;
            for (OWLauf<T> owl : d.laeufe) {
                Lauf<T> l = ll.add(x);
                l.setLaufnummer(owl.getLaufnummer());
                l.setLaufbuchstabe(owl.getLaufbuchstabe());
                for (int y = 0; y < owl.getBahnen(); y++) {
                    T tx = owl.getSchwimmer(y);
                    if (tx != null) {
                        l.setSchwimmer(SearchUtils.getSchwimmer(wk, tx.getStartnummer()), 0, y);
                    }
                }
                x++;
            }
        }

        wk.getLauflisteOW().clear();

        return wk;
    }

    private static <T extends ASchwimmer> int[] getStarter(T s, int akNummer, boolean male, int discipline, int round) {
        if (round == 0) {
            return s.getStarter(discipline);
        }
        String id2 = OWDisziplin.getId(akNummer, male, discipline, round);
        int[] sx = s.getStarter(id2);
        if (sx == null || sx.length == 0) {
            return getStarter(s, akNummer, male, discipline, round - 1);
        }
        for (int x = 0; x < sx.length; x++) {
            if (sx[x] > 0) {
                return sx;
            }
        }
        return getStarter(s, akNummer, male, discipline, round - 1);
    }
}