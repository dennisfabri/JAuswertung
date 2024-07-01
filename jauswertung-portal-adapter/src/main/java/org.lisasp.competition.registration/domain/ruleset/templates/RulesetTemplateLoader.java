package org.lisasp.competition.registration.domain.ruleset.templates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import org.lisasp.competition.registration.JsonObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RulesetTemplateLoader {

    private final RulesetTemplateFilesPath registrationTemplateFilesPath;
    private final ObjectMapper objectMapper = JsonObjectMapper.getInstance();

    /**
     * Loads all registration templates from the specified file path.
     *
     * @return a collection of registration templates
     * @throws RulesetTemplateException if an error occurs while loading the
     *                                       templates
     */
    Collection<RulesetTemplate> loadAllRegistrationTemplates() throws RulesetTemplateException {
        try (var stream = Files.walk(registrationTemplateFilesPath.getValue())) {
            final var templates = stream
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> path.toFile().getName().endsWith(".json"))
                    .map(this::loadSingleRegistrationTemplate)
                    .collect(Collectors.toSet());
            if (templates.isEmpty()) {
                throw new IllegalStateException(
                        "No registration templates found in path " + registrationTemplateFilesPath);
            }
            return templates;
        } catch (IOException e) {
            throw new RulesetTemplateException(
                    "Failed to access registration template path " + registrationTemplateFilesPath, e);
        } catch (RulesetTemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new RulesetTemplateException("Failed to load registration templates", e);
        }
    }

    private RulesetTemplate loadSingleRegistrationTemplate(Path path) throws RulesetTemplateException {
        try {
            return objectMapper.readValue(path.toUri().toURL(), RulesetTemplate.class);
        } catch (IOException e) {
            throw new RulesetTemplateException("Failed to read registration template " + path, e);
        }
    }
}
