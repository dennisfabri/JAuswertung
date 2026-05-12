package de.df.jauswertung.io.model;

import de.df.jauswertung.io.value.TeamWithStarters;

import java.util.List;
import java.util.Set;

public record StartersImportDto(List<TeamWithStarters> teams, Set<AgegroupGenderDisciplineRound> importSelection) {
    public StartersImportDto() {
        this(List.of());
    }

    public StartersImportDto(List<TeamWithStarters> teams) {
        this(teams, null);
    }

    public StartersImportDto with(Set<AgegroupGenderDisciplineRound> selectedCells) {
        return new StartersImportDto(this.teams, selectedCells);
    }
}
