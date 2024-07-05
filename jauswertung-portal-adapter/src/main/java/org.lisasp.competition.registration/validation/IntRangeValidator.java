package org.lisasp.competition.registration.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntRangeValidator {
    public static boolean isValid(Integer min, Integer max, RangeValidationType validationType) {
        if (min == null && max == null) {
            if (RangeValidationType.REQUIRE_NONE != validationType) {
                return false;
            }
        } else if (min == null || max == null) {
            if (RangeValidationType.REQUIRE_BOTH == validationType) {
                return false;
            }
        } else {
            if (min > max) {
                return false;
            }
        }

        if (min != null && min <= 0) {
            return false;
        }

        if (max != null && max <= 0) {
            return false;
        }

        return true;
    }

    public static void validate(Integer min, Integer max, RangeValidationType validationType) {
        if (!isValid(min, max, validationType)) {
            throw new RegistrationValidationException("invalid int range " + min + " - " + max + " for validation type " + validationType);
        }
    }
}
