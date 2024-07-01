package org.lisasp.competition.registration.domain.ruleset.templates;

import org.lisasp.competition.registration.domain.ruleset.Ruleset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(value = Include.ALWAYS)
@Jacksonized
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
@EqualsAndHashCode()
@ToString
public class RulesetTemplate {
    @NonNull
    private String name;

    @NonNull
    private Ruleset ruleset;

    public RulesetTemplate(String name) {
        this.name = name;
        this.ruleset = new Ruleset();
    }
}
