package de.df.jauswertung.util.times;

public class Time {

    private final String discipline;
    private final String agegroup;
    private final String sex;
    private final int timeInMillis;
    private final String penalty;
    private final Swimmer[] swimmer;

    public Time(String discipline, String agegroup, String sex, int timeInMillis, String penalty, Swimmer[] swimmer) {
        super();
        this.discipline = discipline;
        this.agegroup = agegroup;
        this.sex = sex;
        this.timeInMillis = timeInMillis;
        this.penalty = penalty;
        this.swimmer = swimmer;
    }

    public String getDiscipline() {
        return discipline;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public String getSex() {
        return sex;
    }

    public int getTimeInMillis() {
        return timeInMillis;
    }

    public String getPenalty() {
        return penalty;
    }

    public Swimmer[] getSwimmer() {
        return swimmer;
    }
}
