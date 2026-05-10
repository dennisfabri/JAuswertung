package de.df.jauswertung.io.model;

import de.df.jauswertung.io.value.TeamWithStarters;

import java.util.List;

public record StartersImportDto(List<TeamWithStarters> teams) {
    public StartersImportDto() {
        this(List.of());
    }
}
