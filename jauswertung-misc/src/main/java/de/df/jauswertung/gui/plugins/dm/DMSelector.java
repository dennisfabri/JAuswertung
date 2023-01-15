package de.df.jauswertung.gui.plugins.dm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.gui.plugins.aselection.AMSelectionPlugin;
import de.df.jauswertung.gui.plugins.aselection.ISelector;
import de.df.jauswertung.util.RandomUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;

public class DMSelector<T extends ASchwimmer> implements ISelector<T> {

    private final int[][] amount = new int[][] { { 0, 0 }, { 0, 0 }, { 1, 1 }, { 2, 2 }, { 1, 1 } };
    private final int[][] candidates = new int[][] { { 0, 0 }, { 0, 0 }, { 1, 1 }, { 5, 5 }, { 5, 5 } };
    private final boolean[][] ag = new boolean[][] { { false, false }, { false, false }, { true, true }, { true, true },
            { true, true } };

    @Override
    public String getName() {
        return "DM";
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void select(AWettkampf<T> wk, AMSelectionPlugin setter) {
        LinkedList<T>[][] swimmers = new LinkedList[wk.getRegelwerk().size()][2];
        for (int x = 0; x < Math.min(wk.getRegelwerk().size(), ag.length); x++) {
            for (int y = 0; y < 2; y++) {
                swimmers[x][y] = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1);
                if (!swimmers[x][y].isEmpty()) {
                    ListIterator<T> li = swimmers[x][y].listIterator();
                    while (li.hasNext()) {
                        T t = li.next();
                        if (t.getAK().hasHLW() && (t.getHLWState() == HLWStates.NICHT_ANGETRETEN)) {
                            li.remove();
                        }
                    }
                    Collections.sort(swimmers[x][y], new SchwimmerMeldepunkteVergleicher<ASchwimmer>(true, 0));
                    while (swimmers[x][y].size() > candidates[x][y]) {
                        swimmers[x][y].removeLast();
                    }
                }
            }
        }

        Random rng = RandomUtils.getRandomNumberGenerator();

        // Disable selection of at least on first
        int ak = -1;
        int sex = -1;

        boolean found = true;
        while (!found) {
            found = false;
            ak = rng.nextInt(Math.min(swimmers.length, ag.length));
            sex = rng.nextInt(2);
            if (ag[ak][sex]) {
                found = true;
                setter.getSelection().setValue(swimmers[ak][sex].getFirst(), true);
                swimmers[ak][sex].removeFirst();
            }
        }

        for (int x = 0; x < Math.min(wk.getRegelwerk().size(), ag.length); x++) {
            for (int y = 0; y < 2; y++) {
                if (ag[x][y] && !swimmers[x][y].isEmpty()) {
                    int select = amount[x][y];
                    if ((x == ak) && (y == sex)) {
                        select--;
                    }
                    select(swimmers[x][y], setter, select, rng);
                }
            }
        }
    }

    private static <T extends ASchwimmer> void select(LinkedList<T> swimmer, AMSelectionPlugin setter, int select,
            Random rng) {
        Collections.shuffle(swimmer, rng);
        for (int x = 0; x < Math.min(swimmer.size(), select); x++) {
            setter.getSelection().setValue(swimmer.get(x), true);
        }
    }
}