package de.df.jauswertung.gui.plugins.upload.dto;

import java.util.List;

public class Team {
    private final String name;
    private final List<Individual> members;
    private final SexTeam sex;
    private final String agegroup;
    private final List<Result> results;

    public Team(String name, List<Individual> members, SexTeam sex, String agegroup, List<Result> results) {
        super();
        this.name = name;
        this.members = members;
        this.sex = sex;
        this.agegroup = agegroup;
        this.results = results;
    }

    public String getName() {
        return name;
    }

    public List<Individual> getMembers() {
        return members;
    }

    public SexTeam getSex() {
        return sex;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public List<Result> getResults() {
        return results;
    }
}
