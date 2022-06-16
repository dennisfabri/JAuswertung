package de.df.jauswertung.io.value;

public class TeamWithStarters {
    private final int startnumber;
    private final String discipline;
    private final int round;
    private final int[] starters;

    public TeamWithStarters(int startnumber, String discipline, int round, int... starters) {
        this.startnumber = startnumber;
        this.discipline = discipline;
        this.round = round;
        this.starters = starters;        
    }

    public int getStartnumber() {
        return startnumber;
    }

    public String getDiscipline() {
        return discipline;
    }
    
    public int getRound() {
        return round;
    }

    public int[] getStarters() {
        return starters;
    }
}
