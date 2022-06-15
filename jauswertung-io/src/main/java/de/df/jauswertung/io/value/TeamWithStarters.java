package de.df.jauswertung.io.value;

public class TeamWithStarters {
    private final int startnumber;
    private final String discipline;
    private final int[] starters;

    public TeamWithStarters(int startnumber, String discipline, int... starters) {
        this.startnumber = startnumber;
        this.discipline = discipline;
        this.starters = starters;
    }

    public int getStartnumber() {
        return startnumber;
    }

    public String getDiscipline() {
        return discipline;
    }

    public int[] getStarters() {
        return starters;
    }
}
