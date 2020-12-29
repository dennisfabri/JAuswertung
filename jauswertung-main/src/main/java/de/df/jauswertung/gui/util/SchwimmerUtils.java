/*
 * einzelTools.java Created on 21. Juni 2001, 13:58
 */

package de.df.jauswertung.gui.util;

/**
 * @author Dennis Mueller
 */

import java.awt.Toolkit;
import java.awt.Window;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.print.Zieleinlaufkarte;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.FormelDLRG;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jauswertung.util.valueobjects.Startkarte;
import de.df.jauswertung.util.valueobjects.ZWStartkarte;
import de.df.jauswertung.util.vergleicher.SchwimmerAKVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerInfoStartnummernVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerStartnummernVergleicher;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.util.StringTools;

/**
 * Enthaelt Methoden fuer die Einzelauswertung
 */
public final class SchwimmerUtils {

    private SchwimmerUtils() {
        // Not used
    }

    public static <T extends ASchwimmer> boolean hasSeverePenalty(T s, int disz) {
        if (!s.isDisciplineChosen(disz)) {
            return true;
        }

        Strafe strafe1 = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
        if (strafe1 != null) {
            switch (strafe1.getArt()) {
            case AUSSCHLUSS:
            case DISQUALIFIKATION:
            case NICHT_ANGETRETEN:
                return true;
            default:
                break;
            }
        }

        Strafe strafe2 = s.getAkkumulierteStrafe(disz);
        if (strafe2 != null) {
            switch (strafe2.getArt()) {
            case AUSSCHLUSS:
            case DISQUALIFIKATION:
            case NICHT_ANGETRETEN:
                return true;
            default:
                break;
            }
        }
        return false;
    }

    public static <T extends ASchwimmer> boolean hasCompleteTime(T s, int disz) {
        if (hasSeverePenalty(s, disz)) {
            return true;
        }
        return (s.getZeit(disz) > 0);
    }

    public static int ermittleJahrgang(int jahrgang) {
        if (jahrgang <= 0) {
            return 0;
        }
        if ((jahrgang > 0) && (jahrgang < 100)) {
            jahrgang += 1900;
            int jahr = Calendar.getInstance().get(Calendar.YEAR);
            while (jahrgang + 100 < jahr) {
                jahrgang += 100;
            }
        }
        return jahrgang;
    }

    public static String getShortPenaltyString(Strafe strafe) {
        if ((strafe == null) || (strafe.getArt() == Strafarten.NICHTS)) {
            return "-";
        }
        if (strafe.getArt() == Strafarten.AUSSCHLUSS) {
            return I18n.get("DebarmentShort");
        }
        if (strafe.getArt() == Strafarten.DISQUALIFIKATION) {
            return I18n.get("DisqualifiedShort");
        }
        if (strafe.getArt() == Strafarten.NICHT_ANGETRETEN) {
            return I18n.get("DidNotStartShort");
        }
        if (strafe.getStrafpunkte() == 0) {
            return "-";
        }

        return "" + strafe.getStrafpunkte();
    }

    public static <T extends ASchwimmer> LinkedList<T> sortiereSchwimmerSN(final LinkedList<T> teilies) {
        if (teilies == null) {
            return null;
        }
        LinkedList<T> ergebnis = new LinkedList<T>(teilies);
        Collections.sort(ergebnis, new SchwimmerStartnummernVergleicher<T>());
        return ergebnis;
    }

    public static LinkedList<SchwimmerInfo> sortiereSchwimmerInfo(final LinkedList<SchwimmerInfo> si) {
        if (si == null) {
            return null;
        }
        if (si.size() == 0) {
            return si;
        }

        LinkedList<SchwimmerInfo> ergebnis = new LinkedList<SchwimmerInfo>(si);
        // Collections.sort(ergebnis);
        Collections.sort(ergebnis, new SchwimmerInfoStartnummernVergleicher());
        return ergebnis;
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> LinkedList<SchwimmerInfo>[] toInfo(final AWettkampf<T> wk) {
        int anzahl = 0;
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<T> ll = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);
                if ((ll != null) && (ll.size() > 0)) {
                    anzahl++;
                }
            }
        }

        LinkedList<SchwimmerInfo>[] ergebnis = new LinkedList[anzahl];
        int zahl = 0;
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<T> ll = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);
                if ((ll != null) && (ll.size() > 0)) {
                    ergebnis[zahl] = new LinkedList<SchwimmerInfo>();
                    ListIterator<T> li = ll.listIterator();
                    do {
                        T s = li.next();
                        ergebnis[zahl].addLast(getSchwimmerInfo(wk, s));
                    } while (li.hasNext());
                    ergebnis[zahl] = sortiereSchwimmerInfo(ergebnis[zahl]);
                    zahl++;
                }
            }
        }
        return ergebnis;
    }

    public static <T extends ASchwimmer> LaufInfo getLaufInfo(AWettkampf<T> wk, T s, int disz) {
        LinkedList<Lauf<T>> laufliste = wk.getLaufliste().getLaufliste();
        if (laufliste == null) {
            return null;
        }
        if (laufliste.size() == 0) {
            return null;
        }
        if (disz >= s.getAK().getDiszAnzahl()) {
            return null;
        }

        ListIterator<Lauf<T>> li = laufliste.listIterator();
        do {
            Lauf<T> l = li.next();
            for (int x = 0; x < l.getBahnen(); x++) {
                T temp = l.getSchwimmer(x);
                if ((temp != null) && (s.equals(temp))) {
                    int d = l.getDisznummer(x);
                    if (disz == d) {
                        return new LaufInfo(l.getName(), x + 1);
                    }
                }
            }
        } while (li.hasNext());
        return null;
    }

    public static <T extends ASchwimmer> ZWInfo[] getZWInfo(AWettkampf<T> wk, ASchwimmer s) {
        if (wk.getHLWListe().isEmpty()) {
            return null;
        }

        ZWInfo[] zw = new ZWInfo[s.getMaximaleHLW()];
        int index = 0;

        for (int i = 0; i < wk.getHLWListe().getLauflistenCount(); i++) {
            LinkedList<HLWLauf<T>> laufliste = wk.getHLWListe().getLaufliste(i);
            ListIterator<HLWLauf<T>> li = laufliste.listIterator();
            while (li.hasNext()) {
                HLWLauf<T> l = li.next();
                for (int x = 0; x < l.getBahnen(); x++) {
                    ASchwimmer temp = l.getSchwimmer(x);
                    if ((temp != null) && (s.equals(temp))) {
                        zw[index] = new ZWInfo(l.getTime().toString(), x + 1);
                        index++;
                    }
                }
            }
        }
        for (int x = index; x < zw.length; x++) {
            zw[x] = new ZWInfo();
        }
        return zw;
    }

    public static <T extends ASchwimmer> SchwimmerInfo getSchwimmerInfo(AWettkampf<T> wk, T s) {
        String[] li = new String[s.getAK().getDiszAnzahl()];
        String[] bi = new String[s.getAK().getDiszAnzahl()];

        for (int x = 0; x < li.length; x++) {
            li[x] = "-";
            bi[x] = "-";
            try {
                LaufInfo laufinfo = getLaufInfo(wk, s, x);
                if (laufinfo != null) {
                    li[x] = laufinfo.getLauf();
                    bi[x] = laufinfo.getBahn();
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }

        return new SchwimmerInfo(s, li, bi);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ASchwimmer> LinkedList<Startkarte> toStartkarten(AWettkampf<T>[] wks, int perpage, boolean includeEmptyLanes, boolean allheats,
            int minheat, int maxheat) {
        if (wks == null) {
            return null;
        }
        int maxLanes = 0;
        for (AWettkampf wk : wks) {
            Laufliste<T> ll = wk.getLaufliste();
            if (ll == null) {
                return null;
            }
            LinkedList<Lauf<T>> laufliste = ll.getLaufliste();
            int bahnen = ll.getLaufliste().getFirst().getBahnen();
            if (maxLanes < bahnen) {
                maxLanes = bahnen;
            }
        }

        LinkedList<Startkarte>[] listen = new LinkedList[maxLanes];
        for (int x = 0; x < listen.length; x++) {
            listen[x] = new LinkedList<Startkarte>();
        }

        for (AWettkampf wk : wks) {
            Laufliste<T> ll = wk.getLaufliste();
            if (ll == null) {
                continue;
            }
            LinkedList<Lauf<T>> laufliste = ll.getLaufliste();
            if (laufliste == null) {
                continue;
            }
            if (laufliste.size() == 0) {
                continue;
            }

            // Ausdruck ggf begrenzen
            int index = 0;
            int amount = ll.getLaufliste().size();
            if (!allheats) {
                index = minheat;
                amount = maxheat - minheat + 1;
            }
            ListIterator<Lauf<T>> li = laufliste.listIterator(index);

            int roundId = wk.getIntegerProperty("roundId", 0);

            while (li.hasNext() && (amount > 0)) {
                amount--;

                Lauf<T> l = li.next();
                for (int x = 0; x < l.getBahnen(); x++) {
                    T s = l.getSchwimmer(x);
                    if (s != null) {
                        listen[x].addLast(new Startkarte(s, roundId, l.getDisznummer(x), l.getName(), x + 1));
                    } else {
                        if (includeEmptyLanes) {
                            listen[x].addLast(new Startkarte(null, 0, -1, l.getName(), x + 1));
                        }
                    }
                }
            }
        }

        LinkedList<Startkarte> temp = new LinkedList<Startkarte>();
        for (LinkedList<Startkarte> aListen : listen) {
            Startkarte[] sk = aListen.stream().sorted().toArray(Startkarte[]::new);
            for (Startkarte s : sk) {
                temp.add(s);
            }
        }
        return repack(temp, perpage);
    }

    public static <T extends ASchwimmer> LinkedList<Zieleinlaufkarte> toZieleinlauf(AWettkampf<T>[] wks, int perpage, boolean allheats, int minheat,
            int maxheat) {
        if (wks == null || wks.length == 0) {
            return null;
        }

        LinkedList<Zieleinlaufkarte> karten = new LinkedList<Zieleinlaufkarte>();
        for (AWettkampf wk : wks) {
            if ((wk == null) || (wk.getLaufliste() == null) || (wk.getLaufliste().getLaufliste() == null)) {
                continue;
            }

            // Ausdruck ggf begrenzen
            int index = 0;
            int amount = wk.getLaufliste().getLaufliste().size();
            if (!allheats) {
                index = minheat;
                amount = maxheat - minheat + 1;
            }
            ListIterator<Lauf<T>> li = wk.getLaufliste().getLaufliste().listIterator(index);
            while (li.hasNext() && (amount > 0)) {
                amount--;
                karten.addLast(new Zieleinlaufkarte(wk, li.next()));
            }
        }
        LinkedList<Zieleinlaufkarte> temp = new LinkedList<>();
        Zieleinlaufkarte[] sk = karten.stream().sorted(new Comparator<Zieleinlaufkarte>() {

            @Override
            public int compare(Zieleinlaufkarte zk1, Zieleinlaufkarte zk2) {
                return zk1.getEvent() - zk2.getEvent();
            }

        }).toArray(Zieleinlaufkarte[]::new);
        for (Zieleinlaufkarte s : sk) {
            temp.add(s);
        }
        return repack(temp, perpage);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Object> LinkedList<T> repack(LinkedList<T> temp, int perpage) {
        if (perpage <= 1) {
            return temp;
        }

        int size = temp.size();
        int width = size / perpage;
        if (size % perpage > 0) {
            width += 1;
        }
        Object[][] result = new Object[width][perpage];
        int border = size % perpage;
        if (border == 0) {
            border = perpage;
        }
        ListIterator<T> zw = temp.listIterator();
        int x = 0;
        int y = 0;
        while (zw.hasNext()) {
            assert y < perpage;

            result[x][y] = zw.next();
            x++;
            if ((x == result.length - 1) && (y >= border)) {
                x = 0;
                y++;
            }
            if (x == result.length) {
                x = 0;
                y++;
            }
        }

        LinkedList<T> ergebnis = new LinkedList<T>();
        for (x = 0; x < result.length; x++) {
            for (y = 0; y < result[x].length; y++) {
                if (result[x][y] != null) {
                    ergebnis.addLast((T) result[x][y]);
                }
            }
        }
        return ergebnis;
    }

    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkarten(HLWListe<T> ll, int perpage, boolean allheats, int minheat, int maxheat,
            boolean bylane) {
        if (bylane) {
            return toZWStartkartenByLane(ll, perpage, allheats, minheat, maxheat);
        }
        return toZWStartkartenByHeat(ll, perpage, allheats, minheat, maxheat);
    }

    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkartenByHeat(HLWListe<T> ll, int perpage, boolean allheats, int minheat,
            int maxheat) {
        if (ll.isEmpty()) {
            return null;
        }

        LinkedList<HLWLauf<T>> merged = new LinkedList<HLWLauf<T>>();

        {
            ListIterator<LinkedList<HLWLauf<T>>> lli = ll.getIterator();
            while (lli.hasNext()) {
                merged.addAll(lli.next());
            }
        }

        LinkedList<ZWStartkarte<T>> liste = new LinkedList<ZWStartkarte<T>>();

        if (merged.size() == 0) {
            return null;
        }

        int sindex = 0;
        int amount = merged.size();
        if (!allheats) {
            sindex = minheat;
            amount = maxheat - minheat + 1;
        }

        ListIterator<HLWLauf<T>> li = merged.listIterator(sindex);

        Hashtable<Integer, Integer> zaehler = new Hashtable<Integer, Integer>();

        while (li.hasNext() && (amount > 0)) {
            amount--;

            HLWLauf<T> l = li.next();
            for (int x = 0; x < l.getBahnen(); x++) {
                T s = l.getSchwimmer(x);
                if (s != null) {
                    int index = 0;
                    if (zaehler.get(s.getStartnummer()) != null) {
                        index = zaehler.get(s.getStartnummer()) + 1;
                        zaehler.put(s.getStartnummer(), index);
                    } else {
                        zaehler.put(s.getStartnummer(), 0);
                    }

                    liste.addLast(new ZWStartkarte<T>(s, l.getTime(), x + 1, index));
                }
            }
        }

        return repack(liste, perpage);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkartenByLane(HLWListe<T> ll, int perpage, boolean allheats, int minheat,
            int maxheat) {
        if (ll.isEmpty()) {
            return null;
        }

        LinkedList<HLWLauf<T>> merged = new LinkedList<HLWLauf<T>>();

        {
            ListIterator<LinkedList<HLWLauf<T>>> lli = ll.getIterator();
            while (lli.hasNext()) {
                merged.addAll(lli.next());
            }
        }

        int[] first = new int[] { 0, 0 };

        LinkedList<ZWStartkarte<T>>[] listen = new LinkedList[ll.get(first).getBahnen()];
        for (int x = 0; x < listen.length; x++) {
            listen[x] = new LinkedList<ZWStartkarte<T>>();
        }

        if (merged.size() == 0) {
            return null;
        }

        int sindex = 0;
        int amount = merged.size();
        if (!allheats) {
            sindex = minheat;
            amount = maxheat - minheat + 1;
        }

        ListIterator<HLWLauf<T>> li = merged.listIterator(sindex);

        Hashtable<Integer, Integer> zaehler = new Hashtable<Integer, Integer>();

        while (li.hasNext() && (amount > 0)) {
            amount--;

            HLWLauf<T> l = li.next();
            for (int x = 0; x < l.getBahnen(); x++) {
                T s = l.getSchwimmer(x);
                if (s != null) {
                    int index = 0;
                    if (zaehler.get(s.getStartnummer()) != null) {
                        index = zaehler.get(s.getStartnummer()) + 1;
                        zaehler.put(s.getStartnummer(), index);
                    } else {
                        zaehler.put(s.getStartnummer(), 0);
                    }

                    listen[x].addLast(new ZWStartkarte<T>(s, l.getTime(), x + 1, index));
                }
            }
        }

        LinkedList<ZWStartkarte<T>> temp = new LinkedList<ZWStartkarte<T>>();
        for (LinkedList<ZWStartkarte<T>> aListen : listen) {
            temp.addAll(aListen);
        }
        return repack(temp, perpage);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkarten(AWettkampf<T> wk, int perPage) {
        LinkedList<ZWStartkarte<T>> liste = toZWStartkarten(wk);
        if (liste == null) {
            return liste;
        }
        if (perPage <= 1) {
            return liste;
        }

        int size = liste.size();
        int rest = size % perPage;
        int[] sizes = new int[perPage];
        for (int x = 0; x < perPage; x++) {
            sizes[x] = size / perPage;
            if (rest > x) {
                sizes[x]++;
            }
        }

        ZWStartkarte<T>[][] listen = new ZWStartkarte[perPage][size + (rest > 0 ? 1 : 0)];
        ListIterator<ZWStartkarte<T>> li = liste.listIterator();
        for (int x = 0; x < perPage; x++) {
            for (int y = 0; y < sizes[x]; y++) {
                listen[x][y] = li.next();
            }
        }
        liste.clear();
        for (int y = 0; y < sizes[0]; y++) {
            for (int x = 0; x < perPage; x++) {
                if (sizes[x] > y) {
                    liste.addLast(listen[x][y]);
                }
            }
        }
        return liste;
    }

    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkarten(AWettkampf<T> wk) {
        if (!wk.hasSchwimmer()) {
            return null;
        }

        LinkedList<ZWStartkarte<T>> liste = new LinkedList<ZWStartkarte<T>>();

        LinkedList<T> swimmers = wk.getSchwimmer();
        Collections.sort(swimmers, new SchwimmerNameVergleicher<T>());
        Collections.sort(swimmers, new SchwimmerAKVergleicher<T>());
        ListIterator<T> li = swimmers.listIterator();

        while (li.hasNext()) {
            T s = li.next();
            if (s.getAK().hasHLW()) {
                for (int index = 0; index < s.getMaximaleHLW(); index++) {
                    liste.addLast(new ZWStartkarte<T>(s, null, 0, index));
                }
            }
        }
        return liste;
    }

    public static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkartenForChecklist(HLWListe<T> ll, boolean allheats, int minheat, int maxheat,
            boolean byLane) {
        if (byLane) {
            return toZWStartkartenForChecklistByLane(ll, allheats, minheat, maxheat);
        }
        return toZWStartkartenForChecklistByHeat(ll, allheats, minheat, maxheat);
    }

    private static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkartenForChecklistByHeat(HLWListe<T> ll, boolean allheats, int minheat,
            int maxheat) {
        if (ll.isEmpty()) {
            return null;
        }

        LinkedList<HLWLauf<T>> merged = new LinkedList<HLWLauf<T>>();

        {
            LinkedList<HLWLauf<T>> temp = new LinkedList<HLWLauf<T>>();
            ListIterator<LinkedList<HLWLauf<T>>> lli = ll.getIterator();
            while (lli.hasNext()) {
                temp.addAll(lli.next());
            }

            int sindex = 0;
            int amount = temp.size();
            if (!allheats) {
                sindex = minheat;
                amount = maxheat - minheat + 1;
            }

            ListIterator<HLWLauf<T>> xli = temp.listIterator(sindex);
            while (xli.hasNext() && (amount > 0)) {
                merged.addLast(xli.next());
                amount--;
            }
        }

        LinkedList<ZWStartkarte<T>> liste = new LinkedList<ZWStartkarte<T>>();

        ListIterator<HLWLauf<T>> li = merged.listIterator();

        Hashtable<Integer, Integer> zaehler = new Hashtable<Integer, Integer>();

        while (li.hasNext()) {
            HLWLauf<T> l = li.next();
            for (int x = 0; x < l.getBahnen(); x++) {
                T s = l.getSchwimmer(x);
                if (s != null) {
                    int index = 0;
                    if (zaehler.get(s.getStartnummer()) != null) {
                        index = zaehler.get(s.getStartnummer()) + 1;
                        zaehler.put(s.getStartnummer(), index);
                    } else {
                        zaehler.put(s.getStartnummer(), 0);
                    }

                    liste.addLast(new ZWStartkarte<T>(s, l.getTime(), x + 1, index));
                }
            }
        }
        return liste;
    }

    private static <T extends ASchwimmer> LinkedList<ZWStartkarte<T>> toZWStartkartenForChecklistByLane(HLWListe<T> ll, boolean allheats, int minheat,
            int maxheat) {
        if (ll.isEmpty()) {
            return null;
        }

        LinkedList<ZWStartkarte<T>> liste = toZWStartkartenForChecklistByHeat(ll, allheats, minheat, maxheat);

        Collections.sort(liste, new Comparator<ZWStartkarte<T>>() {
            @Override
            public int compare(ZWStartkarte<T> o1, ZWStartkarte<T> o2) {
                int l1 = o1.getBahnindex();
                int l2 = o2.getBahnindex();
                if (l1 < l2) {
                    return -1;
                }
                if (l1 > l2) {
                    return 1;
                }
                return 0;
            }
        });

        return liste;
    }

    /**
     * Ueberprueft die eingegebene Zeit darauf, ob sie den Rec-Wert deutlich
     * unterbietet.
     * 
     * @param s
     * @param disz
     * @return
     */
    public static boolean checkTime(ASchwimmer s, int disz) {
        if (!s.getWettkampf().isDLRGBased()) {
            return true;
        }
        int zeit = s.getZeit(disz);
        int rec = s.getAK().getDisziplin(disz, s.isMaennlich()).getRec();

        if (zeit <= 0) {
            return true;
        }

        return rec <= zeit;
    }

    public static boolean EnableHighPointWarning = true;

    public static boolean checkTimeAndNotify(Window parent, ASchwimmer s, int disz) {
        if (!EnableHighPointWarning) {
            return true;
        }
        boolean result = checkTime(s, disz);
        if (!result) {
            Toolkit.getDefaultToolkit().beep();
            try {
                EnableHighPointWarning = false;
                result = DialogUtils.askAndWarn(parent, I18n.get("PointsVeryHigh"),
                        I18n.get("IsTimeCorrect", s.getName(), StringTools.zeitString(s.getZeit(disz))),
                        I18n.get("IsTimeCorrect.Note", s.getName(), StringTools.zeitString(s.getZeit(disz))));
            } finally {
                EnableHighPointWarning = true;
            }
        }
        return result;
    }

    public static TimeStatus getTimeStatus(String formelID, int zeit, int rec) {
        if (zeit <= 0.005) {
            return TimeStatus.NONE;
        }
        if (formelID.equals(FormelDLRG.ID) || formelID.equals(FormelDLRG2007.ID)) {
            if (zeit < rec) {
                return TimeStatus.FAST;
            } else if (zeit > 3 * rec) {
                return TimeStatus.SLOW;
            }
        }
        return TimeStatus.NORMAL;
    }

    public static <T extends ASchwimmer> TimeStatus getTimeStatus(AWettkampf<T> wk, T s, int x) {
        if (hasSeverePenalty(s, x)) {
            return TimeStatus.NORMAL;
        }
        return getTimeStatus(wk.getRegelwerk().getFormelID(), s.getZeit(x), s.getAK().getDisziplin(x, s.isMaennlich()).getRec());
    }

    public static <T extends ASchwimmer> TimeStatus getRegistrationTimeStatus(AWettkampf<T> wk, T s, int x) {
        if (hasSeverePenalty(s, x)) {
            return TimeStatus.NORMAL;
        }
        return getTimeStatus(wk.getRegelwerk().getFormelID(), s.getMeldezeit(x), s.getAK().getDisziplin(x, s.isMaennlich()).getRec());
    }

    public static <T extends ASchwimmer> int getTeilgenommeneDisziplinenAnzahl(T s) {
        int count = 0;
        for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
            if (hasCompleteTime(s, x) && s.isDisciplineChosen(x) && (s.getAkkumulierteStrafe(x).getArt() != Strafarten.NICHT_ANGETRETEN)) {
                count++;
            }
        }
        return count;
    }

}