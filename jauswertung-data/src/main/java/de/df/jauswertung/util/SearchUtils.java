package de.df.jauswertung.util;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

/**
 * Created 27.03.2004
 *
 * @author Dennis Fabri
 */
public final class SearchUtils {

    private SearchUtils() {
        // Hide Constructor
    }

    public static <T extends ASchwimmer> LinkedList<T> search(String sn,
                                                              String name,
                                                              double points,
                                                              String bemerkung,
                                                              String gliederung,
                                                              String quali,
                                                              int ak,
                                                              GenderMode sex,
                                                              AusserKonkurrenzMode ausserk,
                                                              AWettkampf<T> wk) {
        if (wk == null) {
            return new LinkedList<>();
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

    private static <T extends ASchwimmer> LinkedList<T> search(String sn,
                                                               String name,
                                                               double points,
                                                               String bemerkung,
                                                               String gliederung,
                                                               String quali,
                                                               String ak,
                                                               GenderMode sex,
                                                               AusserKonkurrenzMode ausserk,
                                                               LinkedList<T> menge) {

        bemerkung = bemerkung.toLowerCase();
        name = name.toLowerCase();
        gliederung = gliederung.toLowerCase();
        ak = ak.toLowerCase();
        quali = quali.toLowerCase();

        ListIterator<T> li = menge.listIterator();
        while (li.hasNext()) {
            ASchwimmer s = li.next();
            if ((!("" + s.getStartnummer()).contains(sn)) || (!s.getName().toLowerCase().contains(name)) || (!s.getBemerkung().toLowerCase().contains(bemerkung)) || (!s.getGliederung().toLowerCase().contains(
                    gliederung)) || (!s.getQualifikationsebene().toLowerCase().contains(quali)) || (!s.getAK().getName().toLowerCase().contains(ak)) || (points > s.getMeldepunkte(
                    0))) {
                li.remove();
            } else if (sex != GenderMode.Both && (s.isMaennlich() != (sex == GenderMode.Male))) {
                li.remove();
            } else if (ausserk != AusserKonkurrenzMode.Both) {
                if (s.isAusserKonkurrenz() != (AusserKonkurrenzMode.Yes == ausserk)) {
                    li.remove();
                }
            }
        }
        return menge;
    }

    private static <T extends ASchwimmer> LinkedList<T> startGetSchwimmer(AWettkampf<T> wk) {
        LinkedList<T> suchen;
        suchen = new LinkedList<>(wk.getSchwimmer());
        return suchen;
    }

    private static <T extends ASchwimmer> void filtereName(LinkedList<T> suchen, String n) {
        if (n == null) {
            return;
        }
        if (suchen.isEmpty()) {
            return;
        }
        suchen.removeIf(jetzt -> !n.equals(jetzt.getName()));
    }

    public static <T extends ASchwimmer> void filtereMeldepunkte(LinkedList<T> suchen, int amount, int meldeindex) {
        if (amount <= 0) {
            suchen.clear();
            return;
        }
        if (suchen.size() <= amount) {
            return;
        }
        suchen.sort(new SchwimmerMeldepunkteVergleicher<>(meldeindex));
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

    private static <T extends ASchwimmer> void filtereGliederungen(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.isEmpty()) {
            return;
        }
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

    private static <T extends ASchwimmer> void filtereGliederungenMitQGld(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.isEmpty()) {
            return;
        }
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

    private static <T extends ASchwimmer> void filtereQGldGliederungen(LinkedList<T> suchen, String[] n) {
        if (n == null) {
            return;
        }
        if (suchen.isEmpty()) {
            return;
        }
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

    private static <T extends ASchwimmer> void filtereGeschlecht(LinkedList<T> suchen, boolean maennlich) {
        if (suchen.isEmpty()) {
            return;
        }
        suchen.removeIf(jetzt -> jetzt.isMaennlich() != maennlich);
    }

    private static <T extends ASchwimmer> void filtereDisziplin(LinkedList<T> suchen, int d) {
        if (suchen.isEmpty()) {
            return;
        }
        suchen.removeIf(jetzt -> !jetzt.isDisciplineChosen(d));
    }

    private static <T extends ASchwimmer> void filtereAK(LinkedList<T> suchen, Altersklasse ak) {
        if (suchen.isEmpty()) {
            return;
        }
        if (ak == null) {
            return;
        }
        suchen.removeIf(jetzt -> ak != jetzt.getAK());
    }

    private static <T extends ASchwimmer> void filtereAKs(LinkedList<T> suchen, LinkedList<Altersklasse> aks) {
        if (suchen.isEmpty()) {
            return;
        }
        if (aks == null) {
            return;
        }
        suchen.removeIf(jetzt -> !aks.contains(jetzt.getAK()));
    }

    private static <T extends ASchwimmer> void filtereStartnummer(LinkedList<T> suchen, int nummer) {
        if (suchen == null) {
            return;
        }
        suchen.removeIf(jetzt -> jetzt.getStartnummer() != nummer);
    }

    private static <T extends ASchwimmer> void filtereStartnumbers(LinkedList<T> suchen, Set<Integer> startnumbers) {
        if (suchen == null) {
            return;
        }
        suchen.removeIf(jetzt -> !startnumbers.contains(jetzt.getStartnummer()));
    }

    private static <T extends ASchwimmer> void filtereBemerkung(LinkedList<T> suchen, String bemerkung) {
        if (suchen == null) {
            return;
        }
        suchen.removeIf(jetzt -> !jetzt.getBemerkung().equals(bemerkung));
    }

    private static <T extends ASchwimmer> void filtereZeit(LinkedList<T> suchen, int disziplin, int zeit) {
        if (suchen == null) {
            return;
        }
        suchen.removeIf(jetzt -> (!jetzt.isDisciplineChosen(disziplin)) || (jetzt.getZeit(disziplin) != zeit));
    }

    public static <T extends ASchwimmer> T getSchwimmer(AWettkampf<T> wk, ASchwimmer s) {
        return getSchwimmer(wk, s.getStartnummer());
    }

    /**
     * Liefert den Schwimmer mit der entsprechenden Startnummer.
     *
     * @param wk          Wettkampf
     * @param startnummer Die zu suchende Startnummer
     * @return Liefert den gefundenen Schwimmer oder null
     */
    public static <T extends ASchwimmer> T getSchwimmer(AWettkampf<T> wk, int startnummer) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereStartnummer(suchen, startnummer);
        if (suchen.isEmpty()) {
            return null;
        }
        return suchen.getFirst();
    }

    /**
     * Liefert die Schwimmer mit dem gesuchten Namen.
     *
     * @param wk   Wettkampf
     * @param name Name
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
        LinkedList<T> suchen = new LinkedList<>(wk);
        filtereAK(suchen, ak);
        return !suchen.isEmpty();
    }

    public static <T extends ASchwimmer> boolean hasSchwimmer(LinkedList<T> wk, int sn) {
        LinkedList<T> suchen = new LinkedList<>(wk);
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

    public static <T extends ASchwimmer> LinkedList<T> getSchwimmer(AWettkampf<T> wk, Set<Integer> startnumbers) {
        LinkedList<T> suchen = startGetSchwimmer(wk);
        filtereStartnumbers(suchen, startnumbers);
        return suchen;
    }
}
