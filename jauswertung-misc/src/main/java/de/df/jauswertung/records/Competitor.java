package de.df.jauswertung.records;

public class Competitor {

    public final int Id;
    public final String First;
    public final String Second;
    public final int ClubId;
    public final boolean Team;
    public final boolean Male;

    public Competitor(int id, String first, String second, int clubId, boolean team, boolean male) {
        Id = id;
        Team = team;
        First = first;
        Second = second;
        ClubId = clubId;
        Male = male;
    }
}
