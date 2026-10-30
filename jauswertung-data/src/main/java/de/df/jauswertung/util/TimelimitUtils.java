package de.df.jauswertung.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.util.vergleicher.ZeitenVergleicher;

public final class TimelimitUtils {

    /**
     * A time (with its penalty) of a swimmer in a discipline that breaks a set of time limits.
     */
    public record BrokenTimelimit<T extends ASchwimmer>(T swimmer, Altersklasse ageGroup, Disziplin discipline,
            int time, Strafe penalty, Timelimits timelimits) {
    }

    private TimelimitUtils() {
        // Hide
    }

    /**
     * All broken time limits, ordered by set of time limits, age group, sex and discipline.
     *
     * @param ageGroup index of the age group; any other value means all age groups
     */
    public static <T extends ASchwimmer> List<BrokenTimelimit<T>> getBrokenTimelimits(AWettkampf<T> wk,
            int ageGroup) {
        List<BrokenTimelimit<T>> result = new ArrayList<>();
        TimelimitsContainer container = wk.getTimelimits();
        if (container == null || container.isEmpty()) {
            return result;
        }
        int year = wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION);
        int first = 0;
        int last = wk.getRegelwerk().size();
        if ((ageGroup >= 0) && (ageGroup < wk.getRegelwerk().size())) {
            first = ageGroup;
            last = ageGroup + 1;
        }
        for (Timelimits limits : container.getTimelimits()) {
            for (int x = first; x < last; x++) {
                Altersklasse ak = wk.getRegelwerk().getAk(x);
                for (int y = 0; y < 2; y++) {
                    boolean male = y == 1;
                    LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, ak, male);
                    if (wk.isHeatBased()) {
                        for (OWDisziplin<T> round : wk.getLauflisteOW().getDisziplinen()) {
                            Disziplin d = ak.getDisziplin(round.disziplin, male);
                            for (T s : swimmers) {
                                if (s.hasInput(round.getId()) && limits.isBrokenBy(s.getZeit(round.getId()),
                                        s.getAkkumulierteStrafe(round.getId()), d.getName(), s, year, round.round)) {
                                    result.add(new BrokenTimelimit<>(s, ak, d, s.getZeit(round.getId()),
                                            s.getAkkumulierteStrafe(round.getId()), limits));
                                }
                            }
                        }
                    } else {
                        for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                            swimmers.sort(new ZeitenVergleicher(z));
                            Disziplin d = ak.getDisziplin(z, male);
                            for (T s : swimmers) {
                                if (s.hasInput(z) && limits.isBrokenBy(s.getZeit(z), s.getAkkumulierteStrafe(z),
                                        d.getName(), s, year, 0)) {
                                    result.add(new BrokenTimelimit<>(s, ak, d, s.getZeit(z), s.getAkkumulierteStrafe(z),
                                            limits));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static <T extends ASchwimmer> boolean hasBrokenTimelimits(AWettkampf<T> wk, int ageGroup) {
        return !getBrokenTimelimits(wk, ageGroup).isEmpty();
    }
}
