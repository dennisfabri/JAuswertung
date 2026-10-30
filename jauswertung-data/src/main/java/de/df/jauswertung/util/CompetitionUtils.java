/*
 * Created on 16.03.2006
 */
package de.df.jauswertung.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jauswertung.util.vergleicher.SchwimmerAKVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerGliederungVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerStartnummernVergleicher;

public final class CompetitionUtils {

    private CompetitionUtils() {
        // Hide
    }

    @SuppressWarnings("rawtypes")
    public final static SchwimmerStartnummernVergleicher VERGLEICHER_STARTNUMMER = new SchwimmerStartnummernVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerAKVergleicher VERGLEICHER_ALTERSKLASSE = new SchwimmerAKVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerGliederungVergleicher VERGLEICHER_GLIEDERUNG = new SchwimmerGliederungVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerMeldepunkteVergleicher VERGLEICHER_MELDEPUNKTE = new SchwimmerMeldepunkteVergleicher();
    @SuppressWarnings("rawtypes")
    public final static SchwimmerNameVergleicher VERGLEICHER_NAME = new SchwimmerNameVergleicher();

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
        LinkedList<T> swimmer = SearchUtils.getSchwimmer(w, gliederungen.toArray(new String[gliederungen.size()]),
                true);
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
        Hashtable<String, int[]> medaille = new Hashtable<>();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                SchwimmerResult<T>[] results = ResultCalculator.getResults(wk, aks.getAk(x), y == 1);
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
        LinkedList<Medal> medals = new LinkedList<>();
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

    static class MedalTableModel extends AbstractTableModel {

        private static String[] titles = new String[] { I18n.get("Rank"), I18n.get("Organisation"),
                "  " + I18n.get("Gold") + "  ",
                "  " + I18n.get("Silver") + "  ", "  " + I18n.get("Bronze") + "  " };

        private final Medal[] medals;

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
        private int[] medals;
        private int points;
        private int rank;

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

    public static <T extends ASchwimmer> AWettkampf<T> generateWertungsgruppenwettkampf(AWettkampf<T> wk) {
        return generateWertungsgruppenwettkampf(wk, false);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateWertungsgruppenwettkampf(AWettkampf<T> wk,
            boolean nurMehrkampf) {
        AWettkampf<T> wkx = Utils.copy(wk);
        Wertungsgruppe[] wgs = wkx.getRegelwerk().getWertungsgruppen(nurMehrkampf);
        if (wgs.length <= 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        LinkedList<T>[] swimmers = new LinkedList[wgs.length];
        for (int x = 0; x < swimmers.length; x++) {
            swimmers[x] = new LinkedList<>();
        }

        wkx.disableUpdates();
        try {
            for (Altersklasse ak : wkx.getRegelwerk().getAks()) {
                if ((ak.getWertungsgruppe() == null)
                        || (wkx.getRegelwerk().getWertungsgruppe(ak.getWertungsgruppe()) == null)) {
                    wkx.removeSchwimmer(SearchUtils.getSchwimmer(wkx, ak));
                }
            }
        } finally {
            wkx.enableUpdates();
        }

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

                wg.applyTo(ak);
                ak.setMehrkampfwertung(true);
                ak.setWertungsgruppe(null);

                for (T t : swimmers[x]) {
                    t.setAKNummer(pos, false);
                }
            }
        }

        return wkx;
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
        LinkedList<T> notchanged = new LinkedList<>();
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

    public static <T extends ASchwimmer> int[][] getQOrganizationAmounts(AWettkampf<T> wk) {
        Hashtable<String, String> orgs = new Hashtable<>();
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

    public static <T extends ASchwimmer> int[] getStarter(T s, int akNummer, boolean male, int discipline, int round) {
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