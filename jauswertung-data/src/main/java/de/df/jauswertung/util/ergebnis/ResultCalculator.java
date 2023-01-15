/*
 * Created on 15.02.2005
 */
package de.df.jauswertung.util.ergebnis;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.util.SearchUtils;

/**
 * @author Fabri
 */
public final class ResultCalculator {

    private ResultCalculator() {
        // Not used
    }

    public static <T extends ASchwimmer> SchwimmerResult<T>[] getResults(AWettkampf<T> wk, Altersklasse ak,
            boolean maennlich) {
        return getResults(wk, ak, maennlich, null, true);
    }

    public static <T extends ASchwimmer> SchwimmerResult<T>[] getResults(AWettkampf<T> wk, Altersklasse ak,
            boolean maennlich, boolean[] select, boolean zw) {
        return getResults(wk, SearchUtils.getSchwimmer(wk, ak, maennlich), ak, maennlich, select, zw);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> SchwimmerResult<T>[] getResults(AWettkampf<T> wk, LinkedList<T> swimmers,
            Altersklasse ak, boolean maennlich,
            boolean[] select, boolean zw) {
        if (select == null) {
            select = new boolean[ak.getDiszAnzahl()];
            Arrays.fill(select, true);
        }
        Formel<T> formel = FormelManager.getInstance().get(wk.getRegelwerk().getFormelID());

        wk.check();
        LinkedList<Zielrichterentscheid<T>> zes = new LinkedList<>(
                wk.getZielrichterentscheide());

        ListIterator<Zielrichterentscheid<T>> li = zes.listIterator();
        Hashtable<String, Zielrichterentscheid<T>> zetable = new Hashtable<>();
        while (li.hasNext()) {
            Zielrichterentscheid<T> ze = li.next();
            if (ze.isValid()) {
                LinkedList<T> zeSchwimmer = ze.getSchwimmer();
                ASchwimmer t = zeSchwimmer.getFirst();
                LinkedList<T> zSchwimmer = SearchUtils.getSchwimmer(wk, t.getAK(), t.isMaennlich(), ze.getDisziplin(),
                        ze.getZeit());
                if (zSchwimmer.size() == zeSchwimmer.size()) {
                    boolean check = true;
                    for (T s : zSchwimmer) {
                        if (!zeSchwimmer.contains(s)) {
                            check = false;
                            break;
                        }
                    }
                    if (check) {
                        for (ASchwimmer s : zeSchwimmer) {
                            zetable.put(s.getStartnummer() + "x"
                                    + s.getAK().getDisziplin(ze.getDisziplin(), s.isMaennlich()).getName(), ze);
                        }
                    }
                }
            }
        }

        T[] schwimmer = (T[]) swimmers.toArray(new ASchwimmer[swimmers.size()]);
        SchwimmerData<T>[][] ergebnisse = new SchwimmerData[ak.getDiszAnzahl()][0];
        for (int x = 0; x < ergebnisse.length; x++) {
            ergebnisse[x] = toResult(wk, schwimmer, ak.getDisziplin(x, maennlich), x, formel, select, zetable);
        }

        SchwimmerResult<T>[] results = new SchwimmerResult[schwimmer.length];
        for (int x = 0; x < schwimmer.length; x++) {
            results[x] = new SchwimmerResult<>(schwimmer[x]);
            for (SchwimmerData<T>[] anErgebnisse : ergebnisse) {
                results[x].add(anErgebnisse[x]);
            }
        }
        return formel.toResults(results, wk, ak, zetable, zw);
    }

    private static <T extends ASchwimmer> SchwimmerData<T>[] toResult(AWettkampf<T> wk, T[] swimmer,
            Disziplin disziplin, int nummer, Formel<T> f,
            boolean[] select, Hashtable<String, Zielrichterentscheid<T>> zes) {
        @SuppressWarnings("unchecked")
        SchwimmerData<T>[] schwimmer = new SchwimmerData[swimmer.length];
        @SuppressWarnings("unchecked")
        SchwimmerData<T>[] copy = new SchwimmerData[swimmer.length];

        for (int y = 0; y < schwimmer.length; y++) {
            schwimmer[y] = new SchwimmerData<>(swimmer[y], nummer, select[nummer]);
            copy[y] = schwimmer[y];
        }

        f.setPoints(wk, copy, disziplin, zes);
        return schwimmer;
    }
}