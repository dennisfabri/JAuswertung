package de.df.jauswertung.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;

public final class BestTimeUtils {

    /**
     * The fastest time of a discipline and the swimmers who swam it, ordered by name.
     */
    public record BestTime<T extends ASchwimmer>(int time, List<T> swimmers) {
    }

    private BestTimeUtils() {
        // Hide
    }

    /**
     * The fastest time of a discipline in a heat-based competition, taken from all of its rounds. Times with a penalty
     * and swimmers with a penalty on the whole competition are ignored.
     *
     * @return the best time or null if nobody has a valid time
     */
    public static <T extends ASchwimmer> BestTime<T> getBestTimeOverAllRounds(AWettkampf<T> wk, int ageGroup,
            boolean male, int discipline) {
        int rounds = wk.getRegelwerk().getAk(ageGroup).getDisziplin(discipline, male).getRunden().length + 1;
        int best = Integer.MAX_VALUE;
        List<T> fastest = new ArrayList<>();
        for (T s : SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(ageGroup), male)) {
            if (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
                continue;
            }
            int time = Integer.MAX_VALUE;
            for (int round = 0; round < rounds; round++) {
                String id = OWDisziplin.getId(ageGroup, male, discipline, round);
                int zeit = s.getZeit(id);
                if (zeit > 0 && !s.getAkkumulierteStrafe(id).isStrafe()) {
                    time = Math.min(time, zeit);
                }
            }
            if (time < best) {
                best = time;
                fastest.clear();
            }
            if (time == best && time < Integer.MAX_VALUE) {
                fastest.add(s);
            }
        }
        if (fastest.isEmpty()) {
            return null;
        }
        // Equal times have no defined order, so they are listed alphabetically
        fastest.sort(Comparator.comparing(ASchwimmer::getName, Collator.getInstance()));
        return new BestTime<>(best, fastest);
    }
}
