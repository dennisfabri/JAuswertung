package de.df.jauswertung.util;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.util.StringTools;

public class ZWUtils {

    // public static final String[] ABC = new String[] { "a", "b", "c", "d",
    // "e",
    // "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
    // "s", "t", "u", "v", "w", "x", "y", "z" };
    //
    public static <T extends ASchwimmer> int getZWStartnummer(AWettkampf<T> wk, String sn) {
        if ((sn == null) || (sn.trim().length() == 0)) {
            return -1;
        }
        sn = sn.trim();
        if (StringTools.isInteger(sn)) {
            return Integer.parseInt(sn);
        }
        try {
            return StartnumberFormatManager.convert(wk, sn);
        } catch (Exception ex) {
            // Nothing to do
        }
        if (sn.length() <= 1) {
            return -1;
        }
        char last = sn.toUpperCase().charAt(sn.length() - 1);
        sn = sn.substring(0, sn.length() - 1);
        for (int x = 0; x < StringTools.ABC.length; x++) {
            if (last == StringTools.ABC[x]) {
                if (StringTools.isInteger(sn)) {
                    return StartnumberFormatManager.convert(wk, sn);
                }
                return -1;
            }
        }
        return -1;
    }

    public static <T extends ASchwimmer> int[][][] getHLWStatus(AWettkampf<T> wk) {
        int[][][] result = new int[wk.getRegelwerk().size()][2][5];
        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[x].length; y++) {
                for (int z = 0; z < result[x][y].length; z++) {
                    result[x][y][z] = 0;
                }
            }
        }
        for (T s : wk.getSchwimmer()) {
            if (s.getAK().hasHLW()) {
                for (int x = 0; x < s.getMaximaleHLW(); x++) {
                    HLWStates state = s.getHLWState(x);
                    switch (state) {
                    case NOT_ENTERED:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][0]++;
                        break;
                    case ENTERED:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][1]++;
                        break;
                    case NICHT_ANGETRETEN:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][2]++;
                        break;
                    case DISQALIFIKATION:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][3]++;
                        break;
                    }
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][4]++;
                }
            }
        }
        return result;
    }

    public static <T extends ASchwimmer> int[][][] getHLWStatusKompakt(AWettkampf<T> wk) {
        int[][][] result = new int[wk.getRegelwerk().size()][2][3];
        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[x].length; y++) {
                for (int z = 0; z < result[x][y].length; z++) {
                    result[x][y][z] = 0;
                }
            }
        }
        for (T s : wk.getSchwimmer()) {
            if (s.getAK().hasHLW()) {
                for (int x = 0; x < s.getMaximaleHLW(); x++) {
                    HLWStates state = s.getHLWState(x);
                    switch (state) {
                    case NOT_ENTERED:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][0]++;
                        break;
                    case ENTERED:
                    case NICHT_ANGETRETEN:
                    case DISQALIFIKATION:
                        result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][1]++;
                        break;
                    }
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][2]++;
                }
            }
        }
        return result;
    }

    public static <T extends ASchwimmer> HLWStates getHLWState(AWettkampf<T> wk, String pen) {
        pen = pen.trim();
        if (pen.length() == 0) {
            return HLWStates.NOT_ENTERED;
        }
        Strafen str = wk.getStrafen();
        Strafe s = str.getNichtAngetreten();
        if (pen.equalsIgnoreCase("d") || pen.equalsIgnoreCase(I18n.get("DisqualificationShort"))) {
            s = str.getDisqualifiziert();
        } else if (pen.equalsIgnoreCase("n") || pen.equalsIgnoreCase(I18n.get("DidNotStartShort"))) {
            s = str.getNichtAngetreten();
        } else {
            s = str.getStrafe(pen);
            if (s == null) {
                s = Strafe.NICHTS;
            }
        }
        switch (s.getArt()) {
        case AUSSCHLUSS:
        case DISQUALIFIKATION:
            return HLWStates.DISQALIFIKATION;
        case NICHT_ANGETRETEN:
            return HLWStates.NICHT_ANGETRETEN;
        case NICHTS:
        case STRAFPUNKTE:
            return HLWStates.ENTERED;
        }
        return HLWStates.NOT_ENTERED;
    }

    public static <T extends ASchwimmer> int getZWIndex(AWettkampf<T> wk, String sn) {
        if ((sn == null) || (sn.trim().length() == 0)) {
            return -1;
        }
        sn = sn.trim();
        try {
            StartnumberFormatManager.convert(wk, sn);
            return 0;
        } catch (Exception ex) {
            // Nothing to do
        }
        if (sn.length() <= 1) {
            return -1;
        }
        char last = sn.toUpperCase().charAt(sn.length() - 1);
        sn = sn.substring(0, sn.length() - 1);
        for (int x = 0; x < StringTools.ABC.length; x++) {
            if (last == StringTools.ABC[x]) {
                return x;
            }
        }
        return -1;
    }
}