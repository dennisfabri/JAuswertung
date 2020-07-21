package de.df.jauswertung.gui.plugins.upload.dto;

import java.util.Collections;
import java.util.List;

public class ResultsDto {
    private final CompetitionType type;
    private final List<Individual> individuals;
    private final List<Team> teams;

    public ResultsDto(CompetitionType type, List<Individual> individuals, List<Team> teams) {
        super();
        this.type = type;
        this.individuals = Collections.synchronizedList(individuals);
        this.teams = Collections.synchronizedList(teams);
    }

    public CompetitionType getType() {
        return type;
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public List<Team> getTeams() {
        return teams;
    }
}
