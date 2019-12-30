package de.df.jauswertung.gui.plugins.zulassung;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.util.HashedCounter;

public class Zulassung<T extends ASchwimmer> {

    private final class ProtokollUndMeldepunkteComparator implements Comparator<T> {

        private final int _meldeindex;

        public ProtokollUndMeldepunkteComparator(int index) {
            _meldeindex = index;
        }

        @Override
        public int compare(T o1, T o2) {
            if (o1.hasMeldungMitProtokoll(_meldeindex) != o2.hasMeldungMitProtokoll(_meldeindex)) {
                if (o1.hasMeldungMitProtokoll(_meldeindex)) {
                    return -1;
                }
                return 1;
            }
            return (int) Math.round((o2.getMeldepunkte(_meldeindex) - o1.getMeldepunkte(_meldeindex)) * 100);
        }
    }

    private final class ProtokollQGldUndMeldepunkteComparator implements Comparator<T> {

        private final int _meldeindex;

        public ProtokollQGldUndMeldepunkteComparator(int index) {
            _meldeindex = index;
        }

        @Override
        public int compare(T o1, T o2) {
            if (o1.hasMeldungMitProtokoll(_meldeindex) != o2.hasMeldungMitProtokoll(_meldeindex)) {
                if (o1.hasMeldungMitProtokoll(_meldeindex)) {
                    return -1;
                }
                return 1;
            }
            int v = o1.getQualifikationsebene().toLowerCase().compareTo(o2.getQualifikationsebene().toLowerCase());
            if (v != 0) {
                return (int) Math.signum(v);
            }
            v = (int) Math.round((o2.getMeldepunkte(_meldeindex) - o1.getMeldepunkte(_meldeindex)) * 100);
            if (v < 0) {
                return -1;
            }
            if (v > 0) {
                return 1;
            }
            return 0;
        }
    }

    private final class MeldepunkteComparator implements Comparator<T> {

        private final int _meldeindex;

        public MeldepunkteComparator(int index) {
            _meldeindex = index;
        }

        @Override
        public int compare(T o1, T o2) {
            return (int) Math.round((o2.getMeldepunkte(_meldeindex) - o1.getMeldepunkte(_meldeindex)) * 100);
        }
    }

    private final class QGldUndMeldepunkteComparator implements Comparator<T> {

        private final int _meldeindex;

        public QGldUndMeldepunkteComparator(int index) {
            _meldeindex = index;
        }

        @Override
        public int compare(T o1, T o2) {
            int v = o1.getQualifikationsebene().toLowerCase().compareTo(o2.getQualifikationsebene().toLowerCase());
            if (v != 0) {
                return v * 2;
            }
            v = (int) Math.round((o2.getMeldepunkte(_meldeindex) - o1.getMeldepunkte(_meldeindex)) * 100);
            if (v < 0) {
                return -1;
            }
            if (v > 0) {
                return 1;
            }
            return 0;
        }
    }

    private LinkedList<T>[][]    direkt                                  = null;
    private LinkedList<T>[][]    gesetzt                                 = null;
    private LinkedList<T>[][]    punkte                                  = null;
    private LinkedList<T>[][]    nichtzugelassen                         = null;
    private LinkedList<T>[][]    gesperrt                                = null;
    private LinkedList<T>[][]    nachruecker                             = null;

    private LinkedList<T>[][]    zugelassen                              = null;

    private final AWettkampf<T>  wk;
    private final Regelwerk      aks;

    private int[][]              limits                                  = null;
    private int[][]              direct                                  = null;
    private int[][]              hopers                                  = null;
    private boolean              preferProtocol                          = false;
    private int                  meldeindex                              = 0;

    private static final boolean removeDisabledBeforeDirectQualification = false;

    @SuppressWarnings("unchecked")
    public Zulassung(AWettkampf<T> wk) {
        this.wk = wk;
        aks = wk.getRegelwerk();

        direkt = new LinkedList[aks.size()][2];
        gesetzt = new LinkedList[aks.size()][2];
        punkte = new LinkedList[aks.size()][2];
        nichtzugelassen = new LinkedList[aks.size()][2];
        gesperrt = new LinkedList[aks.size()][2];
        zugelassen = new LinkedList[aks.size()][2];
        nachruecker = new LinkedList[aks.size()][2];
    }

    public void execute() {
        wk.setZulassungslimits(limits);
        wk.setDirektqualifizierte(direct);
        wk.setNachruecker(hopers);
        wk.setProperty(PropertyConstants.PREFER_PROTOCOL, preferProtocol);
        wk.setProperty(PropertyConstants.ZULASSUNG_REGISTERED_POINTS_INDEX, meldeindex);

        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                for (T t : direkt[x][y]) {
                    if (!t.getQualifikation().isManual()) {
                        t.setQualifikation(Qualifikation.DIREKT);
                    }
                }
                for (T t : punkte[x][y]) {
                    if (!t.getQualifikation().isManual()) {
                        t.setQualifikation(Qualifikation.QUALIFIZIERT);
                    }
                }
                for (T t : nichtzugelassen[x][y]) {
                    if (!t.getQualifikation().isManual()) {
                        t.setQualifikation(Qualifikation.NICHT_QUALIFIZIERT);
                    }
                }
                for (T t : nachruecker[x][y]) {
                    if (!t.getQualifikation().isManual()) {
                        t.setQualifikation(Qualifikation.NACHRUECKER);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void calculate(int[][] _limits, int[][] _direct, int[][] _hopers, boolean _keep, boolean _preferProtocol, int _meldeindex) {
        this.limits = _limits;
        this.direct = _direct;
        this.preferProtocol = _preferProtocol;
        this.meldeindex = _meldeindex;
        this.hopers = _hopers;

        direkt = new LinkedList[aks.size()][2];
        gesetzt = new LinkedList[aks.size()][2];
        punkte = new LinkedList[aks.size()][2];
        nichtzugelassen = new LinkedList[aks.size()][2];
        gesperrt = new LinkedList[aks.size()][2];
        zugelassen = new LinkedList[aks.size()][2];
        nachruecker = new LinkedList[aks.size()][2];

        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<T> schwimmer = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);

                direkt[x][y] = new LinkedList<T>();
                gesetzt[x][y] = new LinkedList<T>();
                gesperrt[x][y] = new LinkedList<T>();
                punkte[x][y] = new LinkedList<T>();
                nichtzugelassen[x][y] = new LinkedList<T>();
                nachruecker[x][y] = new LinkedList<T>();

                zugelassen[x][y] = new LinkedList<T>();

                HashedCounter directs = new HashedCounter();

                {
                    // "Auﬂer konkurrenz" aussortieren
                    ListIterator<T> li = schwimmer.listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        if (t.isAusserKonkurrenz()) {
                            nichtzugelassen[x][y].add(t);
                            li.remove();
                        }
                    }
                }

                // Vorsortieren und bereits belegte Pl‰tze vergeben
                if (_keep) {
                    ListIterator<T> li = schwimmer.listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        switch (t.getQualifikation()) {
                        case DIREKT:
                            directs.inc(t.getQualifikationsebene());
                            direkt[x][y].add(t);
                            li.remove();
                            break;
                        case GESETZT:
                            gesetzt[x][y].add(t);
                            li.remove();
                            break;
                        case GESPERRT:
                            if (removeDisabledBeforeDirectQualification) {
                                gesperrt[x][y].add(t);
                                li.remove();
                            }
                            break;
                        case NACHRUECKER:
                            // Nothing to do
                            break;
                        case NICHT_QUALIFIZIERT:
                            // Nothing to do
                            break;
                        case OFFEN:
                            // Nothing to do
                            break;
                        case QUALIFIZIERT:
                            punkte[x][y].add(t);
                            li.remove();
                            break;
                        }
                    }
                } else {
                    ListIterator<T> li = schwimmer.listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        switch (t.getQualifikation()) {
                        case GESETZT:
                            gesetzt[x][y].add(t);
                            li.remove();
                            break;
                        case GESPERRT:
                            if (removeDisabledBeforeDirectQualification) {
                                gesperrt[x][y].add(t);
                                li.remove();
                            }
                            break;
                        default:
                            // Nothing to do
                            break;
                        }
                    }
                }

                // Direktqualifizierte ¸bernehmen
                if (direct[x][y] > 0) {
                    if (preferProtocol) {
                        Collections.sort(schwimmer, new ProtokollQGldUndMeldepunkteComparator(meldeindex));
                    } else {
                        Collections.sort(schwimmer, new QGldUndMeldepunkteComparator(meldeindex));
                    }

                    ListIterator<T> li = schwimmer.listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        if (t.getQualifikationsebene().length() > 0) {
                            if (directs.get(t.getQualifikationsebene()) < _direct[x][y]) {
                                directs.inc(t.getQualifikationsebene());
                                direkt[x][y].add(t);
                                li.remove();
                            }
                        }
                    }
                }

                // Gesperrte entfernen
                {
                    ListIterator<T> li = direkt[x][y].listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        if (t.getQualifikation() == Qualifikation.GESPERRT) {
                            gesperrt[x][y].addLast(t);
                            li.remove();
                        }
                    }

                    li = schwimmer.listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        if (t.getQualifikation() == Qualifikation.GESPERRT) {
                            gesperrt[x][y].addLast(t);
                            li.remove();
                        }
                    }

                }

                // Punktqualifizierte berechnen
                {
                    int limit = limits[x][y] - gesetzt[x][y].size() - direkt[x][y].size();
                    // Wenn keine Grenze gesetzt ist, werden alle akzeptiert
                    if (limits[x][y] <= 0) {
                        limit = schwimmer.size();
                    }

                    if (limit > 0) {
                        if (limit >= schwimmer.size()) {
                            punkte[x][y].addAll(schwimmer);
                            schwimmer.clear();
                        } else {
                            if (_preferProtocol) {
                                Collections.sort(schwimmer, new ProtokollUndMeldepunkteComparator(meldeindex));
                            } else {
                                Collections.sort(schwimmer, new MeldepunkteComparator(meldeindex));
                            }

                            for (int i = 0; i < limit; i++) {
                                punkte[x][y].addLast(schwimmer.remove());
                            }
                        }
                    }
                }

                {
                    int limit = hopers[x][y];
                    // Wenn keine Grenze gesetzt ist, wird kein nachr¸cker
                    // markiert

                    if (limit > 0) {
                        if (limit >= schwimmer.size()) {
                            nachruecker[x][y].addAll(schwimmer);
                            schwimmer.clear();
                        } else {
                            if (_preferProtocol) {
                                Collections.sort(schwimmer, new ProtokollUndMeldepunkteComparator(meldeindex));
                            } else {
                                Collections.sort(schwimmer, new MeldepunkteComparator(meldeindex));
                            }

                            for (int i = 0; i < limit; i++) {
                                nachruecker[x][y].addLast(schwimmer.remove());
                            }
                        }
                    }
                }

                nichtzugelassen[x][y].addAll(schwimmer);

                zugelassen[x][y].addAll(gesetzt[x][y]);
                zugelassen[x][y].addAll(direkt[x][y]);
                zugelassen[x][y].addAll(punkte[x][y]);
            }
        }
    }

    public LinkedList<T>[][] getDirekt() {
        return direkt;
    }

    public LinkedList<T>[][] getPunkte() {
        return punkte;
    }

    public LinkedList<T>[][] getGesetzt() {
        return gesetzt;
    }

    public LinkedList<T>[][] getGesperrte() {
        return gesperrt;
    }

    public LinkedList<T>[][] getNichtZugelassene() {
        return nichtzugelassen;
    }

    public LinkedList<T>[][] getZugelassene() {
        return zugelassen;
    }

    public LinkedList<T>[][] getNachruecker() {
        return nachruecker;
    }
}