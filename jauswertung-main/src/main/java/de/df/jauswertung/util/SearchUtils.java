/*
 * Created on 27.03.2004
 */
package de.df.jauswertung.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;

/**
 * @author Dennis Fabri
 * @date 27.03.2004
 */
public final class SearchUtils {

    private SearchUtils() {
        // Hide Constructor
    }

    public static <T extends ASchwimmer> LinkedList<T> search(String sn, String name, double points, String bemerkung, String gliederung, int ak, int sex,
            int ausserk, AWettkampf<T> wk) {
        return search(sn, name, points, bemerkung, gliederung, "", ak, sex, ausserk, wk);
    }

    public static <T extends ASchwimmer> LinkedList<T> search(String sn, String name, double points, String bemerkung, String gliederung, String quali, int ak,
            int sex, int ausserk, AWettkampf<T> wk) {
        if (wk == null) {
            return new LinkedList<T>();
        }

        bemerkung = bemerkung.toLowerCase();
        name = name.toLowerCase();
        gliederung = gliederung.toLowerCase();
        quali = quali.toLowerCase();

        LinkedList<T> menge = null;
        String akname = null;
        if ((ak >= wk.getRegelwerk().size()) || (ak < 0)) {
            menge = wk.getSchwimmer();
            akname = "";
        } else {
            menge = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(ak), false);
            menge.addAll(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(ak), true));
            Regelwerk r = wk.getRegelwerk();
            akname = r.getAk(ak).toString();
        }
        return search(sn, name, points, bemerkung, gliederung, quali, akname, sex, ausserk, menge);
    }

    @SuppressWarnings("fallthrough")
    private static <T extends ASchwimmer> LinkedList<T> search(String sn, String name, double points, String bemerkung, String gliederung, String quali,
            String ak, int sex, int ausserk, LinkedList<T> menge) {

        bemerkung = bemerkung.toLowerCase();
        name = name.toLowerCase();
        gliederung = gliederung.toLowerCase();
        ak = ak.toLowerCase();
        quali = quali.toLowerCase();

        ListIterator<T> li = menge.listIterator();
        while (li.hasNext()) {
            ASchwimmer s = li.next();
            if ((("" + s.getStartnummer()).indexOf(sn) == -1) || (s.getName().toLowerCase().indexOf(name) == -1)
                    || (s.getBemerkung().toLowerCase().indexOf(bemerkung) == -1) || (s.getGliederung().toLowerCase().indexOf(gliederung) == -1)
                    || (s.getQualifikationsebene().toLowerCase().indexOf(quali) == -1) || (s.getAK().getName().toLowerCase().indexOf(ak) == -1)
                    || (points > s.getMeldepunkte(0))) {
                li.remove();
            } else {
                switch (sex) {
                default:
                    if (s.isMaennlich() != (sex == 1)) {
                        li.remove();
                        break;
                    }
                case 2:
                    switch (ausserk) {
                    default:
                        if (s.isAusserKonkurrenz() != (1 == ausserk)) {
                            li.remove();
                            break;
                        }
                    case 2:
                        break;
                    }
                    break;
                }
            }
        }
        return menge;
    }

    private static <T extends ASchwimmer> LinkedList<T> startGetSchwimmer(AWettkampf<T> wk) {
        LinkedList<T> suchen;
        synchronized (wk) {
            suchen = new LinkedList<T>(wk.getSchwimmer());
        }
        return suchen;
    }

    private static <T extends ASchwimmer> void filtereName(LinkedList<T> suchen, String n) {
        if (n == null) {
            return;
        }
        if (suchen.size() == 0) {
            return;
        }
        synchronized (suchen) {
            ListIterator<T> li = suchen.listIterator();
            while (li.hasNext()) {
                T jetzt = li.next();
                if (!n.equals(jetzt.getName())) {
                    li.remove();
                }
            }
        }
    }

    public static <T extends ASchwimmer> void filterAusserKonkurrenz(LinkedList<T> suchen) {
        synchronized (suchen) {
            ListIterator<T> li = suchen.listIterator();
            while (li.hasNext()) {
                T jetzt = li.next();
                if (jetzt.isAusserKonkurrenz()) {
                    li.remove();
                }
            }
        }
    }

    public static <T extends ASchwimmer> void filtereMeldepunkte(LinkedList<T> suchen, int amount, int meldeindex) {
        if (amount <= 0) {
            suchen.clear();
            return;
        }
        if (suchen.size() <= amount) {
            return;
        }
        synchronized (suchen) {
            Collections.sort(suchen, new SchwimmerMeldepunkteVergleicher<T>(meldeindex));
            ListIterator<T> li = suchen.listIterator();
            double points = Double.MAX_VALUE;
            for (int x = 0; x < amount; x++) {
                T s = li.next();
                points = s.getMeldepunkte(0) - 0.001;
            }
            while (li.hasNext()) {
                T jetzt = li.next();
                if (jetzt.getMeldepunkte(0) < points) {
                    li.remove();
                }
            }
        }
    }

    private static <T extends ASchwimmer> void filtereGliederungen(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.size() == 0) {
            return;
        }
        synchronized (suchen) {
            ListIterator<T> li = suchen.listIterator();
            while (li.hasNext()) {
                T jetzt = li.next();
                boolean found = false;
                for (String aN : n) {
                    if (jetzt.getGliederung().equals(aN)) {
                        found = true;
                        break;
                    }
                    if (jetzt.getGliederungMitQGliederung().equals(aN)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    li.remove();
                }
            }
        }
    }

    private static <T extends ASchwimmer> void filtereGliederungenMitQGld(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.size() == 0) {
            return;
        }
        synchronized (suchen) {
            ListIterator<T> li = suchen.listIterator();
            while (li.hasNext()) {
                T jetzt = li.next();
                boolean found = false;
                for (String aN : n) {
                    if (jetzt.getGliederungMitQGliederung().equals(aN)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    li.remove();
                }
            }
        }
    }

    private static <T extends ASchwimmer> void filtereQGldGliederungen(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.size() == 0) {
            return;
        }
        synchronized (suchen) {
            ListIterator<T> li = suchen.listIterator();
            while (li.hasNext()) {
                T jetzt = li.next();
                boolean found = false;
                for (String aN : n) {
                    if (jetzt.getQualifikationsebene().equals(aN)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    li.remove();
                }
            }
        }
    }

    private static <T extends ASchwimmer> void filtereGeschlecht(LinkedList<T> suchen, boolean maennlich) {
        if (suchen.size() == 0) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (jetzt.isMaennlich() != maennlich) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereDisziplin(LinkedList<T> suchen, int d) {
        if (suchen.size() == 0) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (!jetzt.isDisciplineChosen(d)) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereAK(LinkedList<T> suchen, Altersklasse ak) {
        if (suchen.size() == 0) {
            return;
        }
        if (ak == null) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (ak != jetzt.getAK()) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereAKs(LinkedList<T> suchen, LinkedList<Altersklasse> aks) {
        if (suchen.size() == 0) {
            return;
        }
        if (aks == null) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (!aks.contains(jetzt.getAK())) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereStartnummer(LinkedList<T> suchen, int nummer) {
        if (suchen == null) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (jetzt.getStartnummer() != nummer) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereBemerkung(LinkedList<T> suchen, String bemerkung) {
        if (suchen == null) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if (!jetzt.getBemerkung().equals(bemerkung)) {
                li.remove();
            }
        }
    }

    private static <T extends ASchwimmer> void filtereZeit(LinkedList<T> suchen, int disziplin, int zeit) {
        if (suchen == null) {
            return;
        }
        ListIterator<T> li = suchen.listIterator();
        while (li.hasNext()) {
            T jetzt = li.next();
            if ((!jetzt.isDisciplineChosen(disziplin)) || (jetzt.getZeit(disziplin) != zeit)) {
                li.remove();
            }
        }
    }

    public static <T extends ASchwimmer> T getSchwimmer(AWettkampf<T> wk, ASchwimmer s) {
        return getSchwimmer(wk, s.getStartnummer());
    }

    /**
     * Liefert den Schwimmer mit der entsprechenden Startnummer.
     * 
     * @param wk
     *            Wettkampf
     * @param startnummer
     *            Die zu suchende Startnummer
     * @return Liefert den gefundenen Schwimmer oder null
     */
    public static <T extends ASchwimmer> T getSchwimmer(AWettkampf<T> wk, int startnummer) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereStartnummer(suchen, startnummer);
        if (suchen.size() == 0) {
            return null;
        }
        return suchen.getFirst();
    }

    /**
     * Liefert die Schwimmer mit dem gesuchten Namen.
     * 
     * @param wk
     *            Wettkampf
     * @param name
     *            Name
     * @return Liefert eine Liste der gesuchten Schwimmer.
     */
    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, String name) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereName(suchen, name);
        return suchen;
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, Altersklasse ak) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, Startgruppe sg) {
        return !getSchwimmer(wk, sg).isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, Startgruppe sg, boolean male) {
        return !getSchwimmer(wk, sg, male).isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, String... gliederung) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereGliederungen(suchen, gliederung);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmerForQGld(AWettkampf<T> wk, String... gliederung) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereQGldGliederungen(suchen, gliederung);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(LinkedList<T> wk, Altersklasse ak) {
        LinkedList<T> suchen = new LinkedList<T>(wk);
        filtereAK(suchen, ak);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(LinkedList<T> wk, int sn) {
        LinkedList<T> suchen = new LinkedList<T>(wk);
        filtereStartnummer(suchen, sn);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean maennlich) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, maennlich);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean maennlich, int disziplin) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, maennlich);
        filtereDisziplin(suchen, disziplin);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean maennlich) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, maennlich);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean maennlich, int disziplin) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, maennlich);
        filtereDisziplin(suchen, disziplin);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, boolean maennlich) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereGeschlecht(suchen, maennlich);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Altersklasse ak) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, String[] gliederungen, boolean mustFitQGld) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        if (mustFitQGld) {
            filtereGliederungenMitQGld(suchen, gliederungen);
        } else {
            filtereGliederungen(suchen, gliederungen);
        }
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmerForQGld(AWettkampf<T> wk, String... qgliederung) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereQGldGliederungen(suchen, qgliederung);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean male, String bemerkung) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, male);
        filtereBemerkung(suchen, bemerkung);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Altersklasse ak, boolean male, int disziplin, int zeit) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAK(suchen, ak);
        filtereGeschlecht(suchen, male);
        filtereZeit(suchen, disziplin, zeit);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Startgruppe sg, boolean male) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAKs(suchen, wk.getRegelwerk().getAKsForStartgroup(sg));
        filtereGeschlecht(suchen, male);
        return suchen;
    }

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Startgruppe sg) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereAKs(suchen, wk.getRegelwerk().getAKsForStartgroup(sg));
        return suchen;
    }
}