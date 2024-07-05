package org.lisasp.competition.registration.domain.ruleset.agegroup;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamMemberConfiguration {
    @NonNull
    private Boolean registrationRequired;
    private Integer minNumberOfTeamMembers;
    private Integer maxNumberOfTeamMembers;
}
