package de.df.jauswertung.misc;

import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SearchUtils;

public class LimitsCheck {

    public LimitsCheck() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        AWettkampf<?> wk = InputManager.ladeWettkampf("DEM2019_Mannschaft_mixed.wk");

        boolean result = checkRecords(wk, 0);
    }

    static <T extends ASchwimmer> boolean checkRecords(AWettkampf<T> wk, int index) {
        TimelimitsContainer tc = wk.getTimelimits();

        int akmin = 0;
        int akmax = wk.getRegelwerk().size();
        if ((index >= 0) && (index < wk.getRegelwerk().size())) {
            akmin = index;
            akmax = index + 1;
        }
        for (int x = akmin; x < akmax; x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<T> swimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    for (ASchwimmer s : swimmer) {
                        if (wk.isHeatBased()) {
                            OWDisziplin<T>[] dx = wk.getLauflisteOW().getDisziplinen();
                            for (OWDisziplin<T> d : dx) {
                                if (s.hasInput(d.Id)) {
                                    if (tc.isBrokenBy(s.getZeit(d.Id), s.getAkkumulierteStrafe(d.Id), ak.getDisziplin(d.disziplin, d.maennlich).getName(), s,
                                            wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), d.round)) {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            Disziplin d = ak.getDisziplin(z, y == 1);
                            if (s.hasInput(z)) {
                                if (tc.isBrokenBy(s.getZeit(z), s.getAkkumulierteStrafe(z), d.getName(), s,
                                        wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), 0)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

}
