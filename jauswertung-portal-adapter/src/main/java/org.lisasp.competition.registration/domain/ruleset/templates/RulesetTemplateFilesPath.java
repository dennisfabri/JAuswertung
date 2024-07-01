package org.lisasp.competition.registration.domain.ruleset.templates;

import java.nio.file.Path;

import lombok.NonNull;
import lombok.Value;

@Value
public class RulesetTemplateFilesPath {
    @NonNull
    private final Path value;
}
