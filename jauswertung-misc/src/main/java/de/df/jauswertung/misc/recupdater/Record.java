package de.df.jauswertung.misc.recupdater;

import java.io.Serializable;

import de.df.jauswertung.misc.times.Time;

public class Record implements Serializable {

    private String competition;
    private String agegroup;
    private boolean male;
    private String discipline;
    private int time;
    private String name;
    private boolean team;

    private boolean changed;
    private boolean matched;

    public Record(String competition, String agegroup, boolean male, String discipline, int time, String name,
            boolean team) {
        setAgegroup(agegroup);
        setMale(male);
        setDiscipline(discipline);
        setTeam(team);
        update(competition, time, name);
        setChanged(false);
        setMatched(false);
    }

    public boolean update(String newCompetition, int newTime, String newName) {
        setMatched(true);
        if (newTime <= 0) {
            return false;
        }
        if (newTime >= this.time && this.time > 0 && !this.name.equalsIgnoreCase("geschätzt")) {
            return false;
        }
        if (discipline.equals("200 m Super Lifesaver") && male == false && agegroup.equals("AK 15/16")) {
            System.out.println("X -> "+newName+ " - "+ newTime + " < "+ time);
        }
        setCompetition(newCompetition);
        setTime(newTime);
        setName(newName);

        setChanged(true);
        return true;
    }

    public String getCompetition() {
        return competition;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public boolean isMale() {
        return male;
    }

    public String getDiscipline() {
        return discipline;
    }

    public int getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public boolean isTeam() {
        return team;
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isMatched() {
        return matched;
    }

    private void setCompetition(String competition) {
        this.competition = competition;
    }

    private void setAgegroup(String agegroup) {
        this.agegroup = agegroup;
    }

    private void setMale(boolean male) {
        this.male = male;
    }

    private void setDiscipline(String discripline) {
        this.discipline = discripline;
    }

    private void setTime(int time) {
        this.time = time;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    @Override
    public String toString() {
        return "\"" + competition + "\",\"" + agegroup + "\"," + male + ",\"" + discipline + "\"," + time + ",\"" + name
                + "\"," + team + "," + matched + ","
                + changed;
    }

    public boolean fits(Record other) {
        if (other.male != male) {
            return false;
        }
        if (other.team != team) {
            return false;
        }
        if (!fitsAgegroup(other.agegroup)) {
            return false;
        }
        if (!fitsDiscipline(other.discipline)) {
            return false;
        }
        return true;
    }

    public boolean fits(Time other) {
        if (other.hasRealPenalty()) {
            return false;
        }
        if (other.male() != male) {
            return false;
        }
        if (other.team() != team) {
            return false;
        }
        if (!fitsAgegroup(other.agegroup())) {
            return false;
        }
        if (!fitsDiscipline(other.discipline())) {
            return false;
        }
        return true;
    }

    private boolean fitsAgegroup(String ag) {
        if (ag.equalsIgnoreCase("open")) {
            ag = "ak offen";
        }
        return agegroup.equalsIgnoreCase(ag);
    }

    String[][] matchings = new String[][] { { "50m Retten", "50 m Retten einer Puppe" },
            { "50m Retten mit Flossen", "50 m Retten einer Puppe mit Flossen" },
            { "100m Retten mit Flossen", "100 m Retten einer Puppe mit Flossen" },
            { "100m Lifesaver", "100 m Retten mit Flossen und Gurtretter" },
            { "200m Super-Lifesaver", "200 m Super Lifesaver" },
            { "100m Retten einer Puppe mit Flossen und Gurtretter", "100 m Retten mit Flossen und Gurtretter" },
            { "200m Obstacle Swim", "200 m Hindernisschwimmen" }, { "50m Manikin Carry", "50 m Retten einer Puppe" },
            { "100m Manikin Carry with Fins", "100 m Retten einer Puppe mit Flossen" },
            { "100m Manikin Tow with Fins", "100 m Retten mit Flossen und Gurtretter" },
            { "100m Rescue Medley (new)", "100 m Kombinierte Rettungsübung" },
            { "4x25m Manikin Relay", "4 x 25 m Puppenstaffel" },
            { "4x50m Obstacle Relay", "4 x 50 m Hindernisstaffel" },
            { "4x50m Medley Relay", "4 x 50 m Gurtretterstaffel" },
            { "4x50m Lifesaving Relay", "4 x 50 m Rettungsstaffel" }, };

    private boolean fitsDiscipline(String d) {
        if (discipline.equalsIgnoreCase(d)) {
            return true;
        }
        String d1 = d;
        for (int x = 0; x < matchings.length; x++) {
            if (d1.equals(matchings[x][0])) {
                d1 = matchings[x][1];
            }
        }
        d1 = d1.replace("4*25m", "4 x 25 m").replace("4*50m", "4 x 50 m").replace("25m", "25 m").replace("50m", "50 m")
                .replace("100m", "100 m").replace("200m",
                        "200 m");
        return discipline.equalsIgnoreCase(d1);
    }
}
