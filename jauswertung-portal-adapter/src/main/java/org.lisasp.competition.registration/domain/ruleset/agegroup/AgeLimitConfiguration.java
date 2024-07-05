package org.lisasp.competition.registration.domain.ruleset.agegroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class AgeLimitConfiguration {
    @JsonInclude(value = Include.ALWAYS)
    private Integer minAge;
    @JsonInclude(value = Include.ALWAYS)
    private Integer maxAge;
    /**
     * Nur bei Mannschaften
     */
    private Integer minAgeSum;
    /**
     * Nur bei Mannschaften
     */
    private Integer maxAgeSum;

    public static AgeLimitConfiguration none() {
        return new AgeLimitConfiguration();
    }

    public static AgeLimitConfiguration teamConfiguration(Integer minAge, Integer maxAge, Integer minAgeSum, Integer maxAgeSum) {
        final AgeLimitConfiguration ageLimitConfiguration = new AgeLimitConfiguration();
        ageLimitConfiguration.minAge = minAge;
        ageLimitConfiguration.maxAge = maxAge;
        ageLimitConfiguration.minAgeSum = minAgeSum;
        ageLimitConfiguration.maxAgeSum = maxAgeSum;
        return ageLimitConfiguration;
    }

    public static AgeLimitConfiguration singleConfiguration(Integer minAge, Integer maxAge) {
        final AgeLimitConfiguration ageLimitConfiguration = new AgeLimitConfiguration();
        ageLimitConfiguration.minAge = minAge;
        ageLimitConfiguration.maxAge = maxAge;
        return ageLimitConfiguration;
    }
}
