package org.lisasp.competition.registration.domain.ruleset;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import org.lisasp.competition.registration.domain.ruleset.agegroup.AgeGroup;

import lombok.NonNull;
import lombok.Value;

@Value
public class Ruleset {
    @NonNull
    private Collection<AgeGroup> ageGroups = new ArrayList<>();
}
