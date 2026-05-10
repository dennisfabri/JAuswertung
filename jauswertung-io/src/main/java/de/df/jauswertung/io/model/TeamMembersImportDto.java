package de.df.jauswertung.io.model;

import de.df.jauswertung.util.valueobjects.Teammember;

import java.util.Hashtable;

public record TeamMembersImportDto(Hashtable<String, Teammember> teamMembers) {
    public TeamMembersImportDto() {
        this(new Hashtable<>());
    }
}
