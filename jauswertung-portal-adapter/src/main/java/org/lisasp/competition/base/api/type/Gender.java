package org.lisasp.competition.base.api.type;

import java.util.Locale;

public enum Gender {
    Female, Male, Mixed, Unknown;

    public static Gender fromString(String gender) {
        if (gender == null) {
            return null;
        }
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(gender)) {
                return g;
            }
        }
        return switch (gender.toLowerCase(Locale.ROOT)) {
            case "f", "w" -> Gender.Female;
            case "m" -> Gender.Male;
            case "x" -> Gender.Mixed;
            default -> Gender.Unknown;
        };
    }
}
