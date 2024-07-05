package org.lisasp.competition.registration.domain.ruleset.templates;

import java.util.Collection;

import org.lisasp.competition.base.api.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RulesetTemplateService {
    private final RulesetTemplateRepository repository;

    public Collection<String> retrieveAllTemplateNames() {
        return repository.findAll()
                .stream()
                .map(RulesetTemplate::getName)
                .toList();
    }

    public RulesetTemplate findByName(String templateName) throws NotFoundException {
        return repository.findByName(templateName);
    }

}
