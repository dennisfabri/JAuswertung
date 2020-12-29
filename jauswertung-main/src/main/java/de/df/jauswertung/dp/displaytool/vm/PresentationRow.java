package de.df.jauswertung.dp.displaytool.vm;

import de.df.jauswertung.dp.displaytool.data.Competitor;

public class PresentationRow {

    public final String rank;
    public final String name;
    public final String serc;
    public final String points;

    public PresentationRow(Competitor c) {
        this("" + c.getRank() + ".", c.getName(), c.getSerc() < 0.005 ? "" : String.format("%d", (int) c.getSerc()),
                String.format("%d", (int) c.getSum()));
    }

    public PresentationRow(String rank, String name, String serc, String points) {
        super();
        this.rank = rank;
        this.name = name;
        this.serc = serc;
        this.points = points;
    }
}