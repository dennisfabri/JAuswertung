package org.lisasp.competition.registration.domain.ruleset.agegroup;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DisciplineSelectionConfiguration {
    @NonNull
    private Integer minNumberOfDisciplines;
    @NonNull
    private Integer maxNumberOfDisciplines;
}
