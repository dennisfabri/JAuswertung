package de.df.jauswertung.dp.displaytool.data;

import java.io.Serializable;
import java.util.*;

public class Competition implements Serializable {

    private static final long serialVersionUID = 5644244152748847085L;

    private List<Competitor>  competitors      = new ArrayList<>();

    private String            name;

    public Competition(String name) {
        this.name = name;

        competitors.add(new Competitor("Team 1", 1234.56, 200));
        competitors.add(new Competitor("Team 2", 1134.56, 869));
        competitors.add(new Competitor("Team 3", 1033.56, 847));
        competitors.add(new Competitor("Team 4", 1032.56, 859));
        competitors.add(new Competitor("Team 5", 1031.56, 584));
        competitors.add(new Competitor("Team 6", 1030.56, 585));
        competitors.add(new Competitor("Team 7", 1029.56));
        competitors.add(new Competitor("Team 8", 1028.56));
        competitors.add(new Competitor("Team 9", 1027.56));
        competitors.add(new Competitor("Team 10", 1026.56));
        competitors.add(new Competitor("Team 11", 1025.56));
        competitors.add(new Competitor("Team 12", 1024.56));
        competitors.add(new Competitor("Team 13", 1023.56));
        competitors.add(new Competitor("Team 14", 1022.56));
        competitors.add(new Competitor("Team 15", 1021.56));
        competitors.add(new Competitor("Team 16", 1020.56));
        competitors.add(new Competitor("Team 17", 1019.56));
        competitors.add(new Competitor("Team 188888888888", 1018.56));
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
