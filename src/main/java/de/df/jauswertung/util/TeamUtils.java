package de.df.jauswertung.util;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.util.valueobjects.Team;

public class TeamUtils {

    public static <T extends ASchwimmer> int[][][] getTeammembersStatus(AWettkampf<T> wk, String organisation) {
        int[][][] result = new int[wk.getRegelwerk().size()][2][4];
        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[x].length; y++) {
                for (int z = 0; z < result[x][y].length; z++) {
                    result[x][y][z] = 0;
                }
            }
        }
        if (wk instanceof MannschaftWettkampf) {
            MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
            for (Mannschaft s : mwk.getSchwimmer()) {
                if (organisation != null && !s.getGliederungMitQGliederung().equals(organisation)) {
                    continue;
                }
                int[] trm = new int[3];
                trm[0] = 0;
                trm[1] = 0;
                trm[2] = 0;
                for (int x = 0; x < s.getMaxMembers(); x++) {
                    int tr = -1;
                    Mannschaftsmitglied mm = s.getMannschaftsmitglied(x);
                    if (mm.isComplete()) {
                        tr = 2;
                    } else if (mm.HasName()) {
                        tr = 1;
                    } else {
                        tr = 0;
                    }
                    trm[tr]++;
                }
                if (trm[2] >= s.getMinMembers()) {
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][1]++;
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][2]++;
                } else if (trm[1] + trm[2] >= s.getMinMembers()) {
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][1]++;
                } else {
                    result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][0]++;
                }
                result[s.getAKNummer()][s.isMaennlich() ? 1 : 0][3]++;
            }
        }
        return result;
    }

    public static boolean checkBarcode(String code) {
        try {
            Team.FromCode(code);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }
}