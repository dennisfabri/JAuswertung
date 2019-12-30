package de.df.jauswertung.records;

public class Discipline {
    public final int     Id;
    public final String  Name;
    public final boolean Team;

    public Discipline(int id, String name, boolean team) {
        Id = id;
        Name = name;
        Team = team;
    }
}
