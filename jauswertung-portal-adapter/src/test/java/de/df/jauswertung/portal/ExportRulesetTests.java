package de.df.jauswertung.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lisasp.competition.registration.JsonObjectMapper;
import org.lisasp.competition.registration.domain.ruleset.templates.RulesetTemplate;

public class ExportRulesetTests {
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = { "DLRG 2024.rwe", "DLRG 2024.rwm", "International - Ocean.rwe", "International - Ocean.rwm",
            "International - Ocean Mixed.rwm", "International - Pool.rwe", "International - Pool.rwm",
            "International - Pool Mixed.rwm" })
    void einzel2024Test(String source) throws IOException {
        ExportRuleset exportRuleset = new ExportRuleset();
        String filename = "./src/test/resources/rulesets/" + source;
        RulesetTemplate actual = exportRuleset.toRuleSetTemplate(filename);
        RulesetTemplate expected = JsonObjectMapper.getInstance()
                .readValue(Path.of(
                        "./src/test/resources/rulesets/" + source + ".json")
                        .toUri()
                        .toURL(), RulesetTemplate.class);
        System.out.println(JsonObjectMapper.getInstance().writeValueAsString(actual));
        assertEquals(JsonObjectMapper.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(expected),
                JsonObjectMapper.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        assertEquals(expected, actual);
    }
}