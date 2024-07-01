package org.lisasp.competition.registration.domain.ruleset.agegroup.discipline;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.lisasp.competition.base.api.type.Gender;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Discipline {
    private Gender gender;
    private int index = 0;
    private String name;
    private RatingType ratingType;
    private Integer numberOfTeamMembers;
}
