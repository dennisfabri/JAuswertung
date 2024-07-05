package org.lisasp.competition.registration.domain.ruleset.agegroup;

import java.util.Collection;

import org.lisasp.competition.registration.domain.ruleset.agegroup.discipline.Discipline;
import org.lisasp.competition.registration.domain.ruleset.agegroup.discipline.RatingType;
import org.lisasp.competition.registration.validation.IntRangeValidator;
import org.lisasp.competition.registration.validation.RangeValidationType;
import org.lisasp.competition.registration.validation.RegistrationValidationException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AgeGroupValidator {
    public static void validate(AgeGroup ageGroup) throws RegistrationValidationException {
        validateAgeLimits(ageGroup);
        validateDisciplineSelectionConfiguration(ageGroup);
        validateTeamMemberConfiguration(ageGroup);
        validateDisciplines(ageGroup);
    }

    private static void validateDisciplines(AgeGroup ageGroup) {
        final Collection<Discipline> disciplines = ageGroup.getDisciplines();
        for (Discipline discipline : disciplines) {
            validateDiscipline(ageGroup.getType(), discipline);
        }
    }

    private static void validateDiscipline(AgeGroupType ageGroupType, Discipline discipline) {
        if (AgeGroupType.TEAM == ageGroupType) {
            if (discipline.getNumberOfTeamMembers() == null || discipline.getNumberOfTeamMembers() <= 0) {
                throw new RegistrationValidationException("numberOfTeamMembers must be > 0");
            }
        } else {
            if (discipline.getNumberOfTeamMembers() != null) {
                throw new RegistrationValidationException("numberOfTeamMembers must be null for ageGroup type " + ageGroupType);
            }
        }
    }

    private static void validateTeamMemberConfiguration(AgeGroup ageGroup) {
        TeamMemberConfiguration teamMemberConfiguration = ageGroup.getTeamMemberConfiguration();
        if (AgeGroupType.TEAM == ageGroup.getType()) {
            IntRangeValidator.validate(teamMemberConfiguration.getMinNumberOfTeamMembers(),
                    teamMemberConfiguration.getMaxNumberOfTeamMembers(), RangeValidationType.REQUIRE_BOTH);
        }
    }

    private static void validateDisciplineSelectionConfiguration(AgeGroup ageGroup) {
        DisciplineSelectionConfiguration disciplineSelectionConfiguration = ageGroup
                .getDisciplineSelectionConfiguration();
        IntRangeValidator.validate(disciplineSelectionConfiguration.getMinNumberOfDisciplines(),
                disciplineSelectionConfiguration.getMaxNumberOfDisciplines(), RangeValidationType.REQUIRE_BOTH);
    }

    private static void validateAgeLimits(AgeGroup ageGroup) {
        final AgeLimitConfiguration ageLimitConfiguration = ageGroup.getAgeLimits();
        IntRangeValidator.validate(null, null, RangeValidationType.REQUIRE_NONE);

        if (AgeGroupType.TEAM == ageGroup.getType()) {
            IntRangeValidator.validate(ageLimitConfiguration.getMinAgeSum(), ageLimitConfiguration.getMaxAgeSum(),
                    RangeValidationType.REQUIRE_NONE);
        }
    }
}
