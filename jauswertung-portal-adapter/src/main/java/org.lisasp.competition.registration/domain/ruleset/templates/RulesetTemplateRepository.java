package org.lisasp.competition.registration.domain.ruleset.templates;

import java.util.Collection;
import java.util.HashSet;

import org.lisasp.competition.base.api.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RulesetTemplateRepository {
    private static final Collection<RulesetTemplate> TEMPLATES = new HashSet<>();

    private final RulesetTemplateLoader registrationTemplateLoader;

    public void loadTemplates() {
        TEMPLATES.addAll(registrationTemplateLoader.loadAllRegistrationTemplates());
    }

    public Collection<RulesetTemplate> findAll() {
        return TEMPLATES;
    }

    public RulesetTemplate findByName(String templateName) throws NotFoundException {
        return TEMPLATES
                .stream()
                .filter(template -> template.getName().equals(templateName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("RegistrationTemplate", templateName));
    }
    
}
