package de.df.jauswertung.gui.util;

import java.awt.*;
import java.util.*;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.print.Zieleinlaufkarte;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.FormelDLRG;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jauswertung.util.valueobjects.Startkarte;
import de.df.jauswertung.util.vergleicher.SchwimmerInfoStartnummernVergleicher;
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
        if (isSeverePenalty(strafe1)) {
            return isSeverePenalty(strafe1);
        }

        Strafe strafe2 = s.getAkkumulierteStrafe(disz);
        return isSeverePenalty(strafe2);
    }

    private static boolean isSeverePenalty(Strafe strafe1) {
        if (strafe1 == null) {
            return false;
        }
        return switch (strafe1.getArt()) {
        case AUSSCHLUSS, DISQUALIFIKATION, NICHT_ANGETRETEN -> true;
        default -> false;
        };
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
        if (jahrgang < 100) {
            jahrgang += 1900;
            int jahr = Calendar.getInstance().get(Calendar.YEAR);
            while (jahrgang + 100 < jahr) {
                jahrgang += 100;
            }
        }
        return jahrgang;
    }

    public static LinkedList<SchwimmerInfo> sortiereSchwimmerInfo(final LinkedList<SchwimmerInfo> si) {
        if (si == null) {
            return null;
        }
        if (si.isEmpty()) {
            return si;
        }

        LinkedList<SchwimmerInfo> ergebnis = new LinkedList<>(si);
        // Collections.sort(ergebnis);
        ergebnis.sort(new SchwimmerInfoStartnummernVergleicher());
        return ergebnis;
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> LinkedList<SchwimmerInfo>[] toInfo(final AWettkampf<T> wk) {
        int anzahl = 0;
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<T> ll = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);
                if ((ll != null) && !ll.isEmpty()) {
                    anzahl++;
                }
            }
        }

        LinkedList<SchwimmerInfo>[] ergebnis = new LinkedList[anzahl];
        int zahl = 0;
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                LinkedList<T> ll = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);
                if ((ll != null) && !ll.isEmpty()) {
                    ergebnis[zahl] = new LinkedList<>();
                    for (T s : ll) {
                        ergebnis[zahl].addLast(getSchwimmerInfo(wk, s));
                    }
                    ergebnis[zahl] = sortiereSchwimmerInfo(ergebnis[zahl]);
                    zahl++;
                }
            }
        }
        return ergebnis;
    }

    private static <T extends ASchwimmer> SchwimmerInfo getSchwimmerInfo(AWettkampf<T> wk, T s) {
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

    public static <T extends ASchwimmer> LaufInfo getLaufInfo(AWettkampf<T> wk, T s, int disz) {
        LinkedList<Lauf<T>> laufliste = wk.getLaufliste().getLaufliste();
        if (laufliste == null) {
            return null;
        }
        if (laufliste.isEmpty()) {
            return null;
        }
        if (disz >= s.getAK().getDiszAnzahl()) {
            return null;
        }

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        for (Lauf<T> l : laufliste) {
            for (int x = 0; x < l.getBahnen(); x++) {
                T temp = l.getSchwimmer(x);
                if ((temp != null) && (s.getStartnummer() == temp.getStartnummer()) && disz == l.getDisznummer(x)) {
                    return new LaufInfo(l.getName(scheme), x + 1);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ASchwimmer> LinkedList<Startkarte> toStartkarten(AWettkampf<T>[] wks, int perpage,
            boolean includeEmptyLanes, boolean allheats,
            int minheat, int maxheat) {
        if (wks == null) {
            return null;
        }
        int maxLanes = 0;
        for (AWettkampf<T> wk : wks) {
            Laufliste<T> ll = wk.getLaufliste();
            if (ll == null) {
                return null;
            }
            int bahnen = ll.getLaufliste().getFirst().getBahnen();
            if (maxLanes < bahnen) {
                maxLanes = bahnen;
            }
        }

        LinkedList<Startkarte>[] listen = new LinkedList[maxLanes];
        for (int x = 0; x < listen.length; x++) {
            listen[x] = new LinkedList<>();
        }

        for (AWettkampf<T> wk : wks) {
            Laufliste<T> ll = wk.getLaufliste();
            if (ll == null) {
                continue;
            }
            LinkedList<Lauf<T>> laufliste = ll.getLaufliste();
            if (laufliste == null || laufliste.isEmpty()) {
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
                        listen[x].addLast(new Startkarte(s, roundId, l, x + 1));
                    } else {
                        if (includeEmptyLanes) {
                            listen[x].addLast(new Startkarte(l, roundId, x + 1));
                        }
                    }
                }
            }
        }

        LinkedList<Startkarte> temp = new LinkedList<>();
        for (LinkedList<Startkarte> aListen : listen) {
            Startkarte[] sk = aListen.stream().sorted().toArray(Startkarte[]::new);
            temp.addAll(Arrays.asList(sk));
        }
        return repack(temp, perpage);
    }

    public static <T extends ASchwimmer> LinkedList<Zieleinlaufkarte> toZieleinlauf(AWettkampf<T>[] wks, int perpage,
            boolean allheats, int minheat,
            int maxheat) {
        if (wks == null || wks.length == 0) {
            return null;
        }

        LinkedList<Zieleinlaufkarte> karten = new LinkedList<>();
        for (AWettkampf<T> wk : wks) {
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
        Zieleinlaufkarte[] sk = karten.stream().sorted(Comparator.comparingInt(Zieleinlaufkarte::getEvent))
                .toArray(Zieleinlaufkarte[]::new);
        Collections.addAll(temp, sk);
        return repack(temp, perpage);
    }

    @SuppressWarnings("unchecked")
    private static <T> LinkedList<T> repack(LinkedList<T> temp, int perpage) {
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

        LinkedList<T> ergebnis = new LinkedList<>();
        for (x = 0; x < result.length; x++) {
            for (y = 0; y < result[x].length; y++) {
                if (result[x][y] != null) {
                    ergebnis.addLast((T) result[x][y]);
                }
            }
        }
        return ergebnis;
    }


    /**
     * Ueberprueft die eingegebene Zeit darauf, ob sie den Rec-Wert deutlich
     * unterbietet.
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

    public static boolean EnableHighTimesWarning = false;

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
        if (rec <= 0.005) {
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
        return getTimeStatus(wk.getRegelwerk().getFormelID(), s.getZeit(x),
                s.getAK().getDisziplin(x, s.isMaennlich()).getRec());
    }

    public static <T extends ASchwimmer> TimeStatus getRegistrationTimeStatus(AWettkampf<T> wk, T s, int x) {
        if (hasSeverePenalty(s, x)) {
            return TimeStatus.NORMAL;
        }
        return getTimeStatus(wk.getRegelwerk().getFormelID(), s.getMeldezeit(x),
                s.getAK().getDisziplin(x, s.isMaennlich()).getRec());
    }
}