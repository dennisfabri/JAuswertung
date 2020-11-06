package de.df.jauswertung.gui.plugins.upload.dto;

import java.util.List;
import java.util.stream.Collectors;

public class ResultsDto {
    private final CompetitionType type;
    private final List<Individual> individuals;
    private final List<Team> teams;

    public ResultsDto(CompetitionType type, List<Individual> individuals, List<Team> teams) {
        super();
        this.type = type;
        this.individuals = individuals.stream().collect(Collectors.toUnmodifiableList());
        this.teams = teams.stream().collect(Collectors.toUnmodifiableList());
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
