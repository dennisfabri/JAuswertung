package de.df.jauswertung.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.laufliste.Lauf;

public final class ZielrichterentscheidUtils {

    private ZielrichterentscheidUtils() {
        // hide constructor
    }

    public static <T extends ASchwimmer> LinkedList<Zielrichterentscheid<T>> createCopy(
            LinkedList<Zielrichterentscheid<T>> liste) {
        LinkedList<Zielrichterentscheid<T>> zes = new LinkedList<>();
        for (Zielrichterentscheid<T> ze : liste) {
            Zielrichterentscheid<T> neu = new Zielrichterentscheid<>();
            neu.setDisziplin(ze.getDisziplin());
            neu.addSchwimmer(ze.getSchwimmer());

            zes.addLast(neu);
        }
        return zes;
    }

    public static <T extends ASchwimmer> LinkedList<Zielrichterentscheid<T>> generateZielrichterentscheide(
            AWettkampf<T> wk) {
        if (wk == null) {
            throw new NullPointerException("Competition must not be null");
        }
        if ((wk.getLaufliste() == null) || (wk.getLaufliste().getLaufliste() == null)
                || (wk.getLaufliste().getLaufliste().isEmpty())) {
            return null;
        }

        LinkedList<Zielrichterentscheid<T>> result = new LinkedList<>();
        {
            ListIterator<Lauf<T>> li = wk.getLaufliste().getLaufliste().listIterator();
            while (li.hasNext()) {
                Lauf<T> lauf = li.next();
                Hashtable<String, LinkedList<T>> temp = new Hashtable<>();
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    if (lauf.getSchwimmer(x) != null) {
                        T t = lauf.getSchwimmer(x);
                        if (t.getZeit(lauf.getDisznummer(x)) > 0) {
                            String key = "" + lauf.getDisznummer(x) + "x" + t.getZeit(lauf.getDisznummer(x));
                            LinkedList<T> liste = temp.get(key);
                            if (liste == null) {
                                liste = new LinkedList<>();
                                temp.put(key, liste);
                            }
                            liste.addLast(t);
                        }
                    }
                }
                Enumeration<String> keys = temp.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    LinkedList<T> liste = temp.get(key);
                    if (liste.size() > 1) {
                        int disziplin = Integer.parseInt(key.substring(0, key.indexOf("x")));
                        Zielrichterentscheid<T> ze = new Zielrichterentscheid<>();
                        ze.setDisziplin(disziplin);
                        ze.addSchwimmer(liste);

                        result.add(ze);
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    @SuppressWarnings({ "cast", "unchecked" })
    public static <T extends ASchwimmer> LinkedList<Zielrichterentscheid<T>>[] checkZielrichterentscheide(
            AWettkampf<T> wk) {
        if (wk == null) {
            throw new NullPointerException("Competition must not be null");
        }

        LinkedList<Zielrichterentscheid<T>> possible = generateZielrichterentscheide(wk);
        if (possible == null) {
            return (LinkedList<Zielrichterentscheid<T>>[]) new LinkedList[] { new LinkedList<Zielrichterentscheid<T>>(),
                    wk.getZielrichterentscheide() };
        }

        LinkedList<Zielrichterentscheid<T>> existing = wk.getZielrichterentscheide();

        LinkedList<Zielrichterentscheid<T>> valid = new LinkedList<>();
        LinkedList<Zielrichterentscheid<T>> invalid = new LinkedList<>();

        for (Zielrichterentscheid<T> ze : existing) {
            if (possible.contains(ze)) {
                valid.addLast(ze);
            } else {
                invalid.addLast(ze);
            }
        }

        return (LinkedList<Zielrichterentscheid<T>>[]) new LinkedList[] { valid, invalid };
    }
}