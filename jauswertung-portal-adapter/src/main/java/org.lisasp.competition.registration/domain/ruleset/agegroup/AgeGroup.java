package org.lisasp.competition.registration.domain.ruleset.agegroup;

import java.util.ArrayList;
import java.util.Collection;

import lombok.*;
import org.lisasp.competition.registration.domain.ruleset.agegroup.discipline.Discipline;
import org.lisasp.competition.registration.validation.RegistrationValidationException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.extern.jackson.Jacksonized;

@JsonInclude(value = Include.ALWAYS)
@Data
@NoArgsConstructor
@Jacksonized
@Builder
public class AgeGroup {
    @NonNull
    private String name;
    @NonNull
    private AgeGroupType type;
    @NonNull
    @Builder.Default
    private AgeLimitConfiguration ageLimits = AgeLimitConfiguration.none();
    @NonNull
    @Builder.Default
    private DisciplineSelectionConfiguration disciplineSelectionConfiguration = new DisciplineSelectionConfiguration(1,
            1);
    @NonNull
    @Builder.Default
    private TeamMemberConfiguration teamMemberConfiguration = TeamMemberConfiguration.builder()
            .registrationRequired(false).build();
    private Integer maxNumberOfParticipantsPerOrganization;
    @NonNull
    @Builder.Default
    private Collection<Discipline> disciplines = new ArrayList<>();
    @NonNull
    private AdmissionConditionType admissionConditionType;

    @Builder
    public AgeGroup(@NonNull String name, @NonNull AgeGroupType type, @NonNull AgeLimitConfiguration ageLimits,
            @NonNull DisciplineSelectionConfiguration disciplineSelectionConfiguration,
            @NonNull TeamMemberConfiguration teamMemberConfiguration, Integer maxNumberOfParticipantsPerOrganization,
            @NonNull Collection<Discipline> disciplines, @NonNull AdmissionConditionType admissionConditionType) {
        this.name = name;
        this.type = type;
        this.ageLimits = ageLimits;
        this.disciplineSelectionConfiguration = disciplineSelectionConfiguration;
        this.teamMemberConfiguration = teamMemberConfiguration;
        this.maxNumberOfParticipantsPerOrganization = maxNumberOfParticipantsPerOrganization;
        this.disciplines = disciplines;
        this.admissionConditionType = admissionConditionType;
        this.validate();
    }

    private void validate() throws RegistrationValidationException {
        AgeGroupValidator.validate(this);
    }
}
