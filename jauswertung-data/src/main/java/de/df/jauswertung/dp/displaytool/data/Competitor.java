package de.df.jauswertung.dp.displaytool.data;

import java.io.Serializable;

public class Competitor implements Serializable, Comparable<Competitor> {

    private static final long serialVersionUID = -2493019290606120583L;

    private int rank;
    private String name;
    private double points;
    private double serc;

    public Competitor(String name, double points) {
        this(name, points, 0);
    }

    public Competitor(String name, double points, double serc) {
        this.rank = 1;
        this.name = name;
        this.points = points;
        this.serc = serc;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public double getSerc() {
        return serc;
    }

    public void setSerc(double serc) {
        this.serc = serc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public double getSum() {
        return points + serc;
    }

    public Object[] getRow() {
        return new Object[] { "" + rank, getName(), String.format("%1$,.2f", points), String.format("%1$,.2f", serc),
                String.format("%1$,.2f", points + serc) };
    }

    @Override
    public int compareTo(Competitor o) {
        if (o == null) {
            return 1;
        }
        return (int) Math.round((o.getSum() - getSum()) * 100);
    }

    public int getRank() {
        return rank;
    }

}
