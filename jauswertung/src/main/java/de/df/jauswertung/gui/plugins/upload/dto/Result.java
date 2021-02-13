package de.df.jauswertung.gui.plugins.upload.dto;

public class Result {
    private final String discipline;
    private final int timeInMilliseconds;
    private final String penalty;
    private final int event;
    private final int heat;
    private final int lane;
    private final int round;

    public Result(String discipline, int timeInMilliseconds, String penalty, int event, int heat, int lane, int round) {
        super();
        this.discipline = discipline;
        this.timeInMilliseconds = timeInMilliseconds;
        this.penalty = penalty;
        this.event = event;
        this.heat = heat;
        this.lane = lane;
        this.round = round;
    }

    public String getDiscipline() {
        return discipline;
    }

    public int getTimeInMilliseconds() {
        return timeInMilliseconds;
    }

    public String getPenalty() {
        return penalty;
    }

    public int getEvent() {
        return event;
    }

    public int getHeat() {
        return heat;
    }

    public int getLane() {
        return lane;
    }

    public int getRound() {
        return round;
    }
}
