package de.df.jauswertung.daten.laufliste;

import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
public enum Reihenfolge {
    Zufall(0, true), GleicheGliederungGegeneinander(1, true),
    GleicheGliederungVerteilen(2, true),
    Meldepunkte(3, true), Meldezeiten(4, false), ZufallJeDisziplin(5, false),
    ILSPool(6, false), ILSPoolVorlauf(7, false), ILSOpenWater(8, false), ILSOpenWaterVorlauf(9, false),
    Regelwerk(99, true);

    private final int value;
    private final boolean rotatable;

    Reihenfolge(int v, boolean rotatable) {
        value = v;
        this.rotatable = rotatable;
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
