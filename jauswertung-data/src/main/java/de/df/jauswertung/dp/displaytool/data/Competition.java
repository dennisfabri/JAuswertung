package de.df.jauswertung.dp.displaytool.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Competition implements Serializable {

    private static final long serialVersionUID = 5644244152748847085L;

    private List<Competitor> competitors = new ArrayList<>();

    private String name;

    public Competition(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public int size() {
        return competitors.size();
    }

    public void setCompetitors(Competitor[] cs) {
        competitors.clear();
        if (cs != null) {
            competitors.addAll(Arrays.asList(cs));
            sort();
        }
    }

    public Competitor[] getCompetitors() {
        return competitors.toArray(Competitor[]::new);
    }

    public void sort() {
        if (competitors.size() > 0) {
            Collections.sort(competitors);
            int rank = 1;
            int pos = 1;
            Competitor last = competitors.get(0);
            for (Competitor c : competitors) {
                if (Math.abs(last.getSum() - c.getSum()) > 0.005) {
                    rank = pos;
                }
                c.setRank(rank);

                pos++;
            }
        }
    }
}
