package de.df.jauswertung.gui.plugins.properties;

import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.gui.util.I18n;

import static java.util.Arrays.stream;

class HeatsNumberingDisplay {
    private final String value;
    private final HeatsNumberingScheme scheme;

    HeatsNumberingDisplay(HeatsNumberingScheme scheme) {
        this.scheme = scheme;
        this.value = I18n.get("HeatsNumberingScheme." + scheme.getValue());
    }

    public static HeatsNumberingDisplay[] values() {
        return stream(HeatsNumberingScheme.values())
                .map(HeatsNumberingDisplay::new)
                .toArray(HeatsNumberingDisplay[]::new);
    }

    public HeatsNumberingScheme getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return value;
    }
}
