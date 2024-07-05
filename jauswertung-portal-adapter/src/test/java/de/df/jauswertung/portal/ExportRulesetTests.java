package de.df.jauswertung.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.lisasp.competition.registration.JsonObjectMapper;
import org.lisasp.competition.registration.domain.ruleset.templates.RulesetTemplate;

public class ExportRulesetTests {
    @Test
    void einzel2024Test() throws IOException {
        ExportRuleset exportRuleset = new ExportRuleset();
        String filename = "./src/test/resources/rulesets/DLRG 2024.rwe";
        RulesetTemplate actual = exportRuleset.toRuleSetTemplate(filename);
        RulesetTemplate expected = JsonObjectMapper.getInstance().readValue(Path.of("./src/test/resources/rulesets/DLRG-2024-Individual.json").toUri().toURL(), RulesetTemplate.class);
        System.out.println(JsonObjectMapper.getInstance().writeValueAsString(actual));
        assertEquals(expected, actual);
    }

    @Test
    void mannschaft2024Test() throws IOException {
        ExportRuleset exportRuleset = new ExportRuleset();
        String filename = "./src/test/resources/rulesets/DLRG 2024.rwm";
        RulesetTemplate actual = exportRuleset.toRuleSetTemplate(filename);
        RulesetTemplate expected = JsonObjectMapper.getInstance().readValue(Path.of("./src/test/resources/rulesets/DLRG-2024-Team.json").toUri().toURL(), RulesetTemplate.class);
        System.out.println(JsonObjectMapper.getInstance().writeValueAsString(actual));
        assertEquals(expected, actual);
    }
}
