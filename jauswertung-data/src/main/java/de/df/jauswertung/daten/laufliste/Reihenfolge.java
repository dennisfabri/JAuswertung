package de.df.jauswertung.daten.laufliste;

import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
public enum Reihenfolge {
    Zufall(0), GleicheGliederungGegeneinander(1),
    GleicheGliederungVerteilen(2),
    Meldepunkte(3), Meldezeiten(4), ZufallJeDisziplin(5),
    ILSPool(6), ILSPoolVorlauf(7), Regelwerk(99);

    private final int value;

    Reihenfolge(int v) {
        value = v;
    }

    public static int length() {
        return values().length;
    }

    public static Reihenfolge fromValue(int value) {
        return stream(Reihenfolge.values()).filter(r -> r.value == value).findAny().orElse(Zufall);
    }

    public boolean equalsValue(int v) {
        return value == v;
    }
}
