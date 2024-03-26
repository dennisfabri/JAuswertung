package de.df.jauswertung.daten.laufliste;

import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
public enum Laufeinteilungsmodus {
    Auto(0), Blocks(1), Einteilung(2);

    private final int value;

    Laufeinteilungsmodus(int v) {
        value = v;
    }

    public static Laufeinteilungsmodus fromValue(int value) {
        return stream(Laufeinteilungsmodus.values()).filter(r -> r.value == value).findAny().orElse(Auto);
    }

}
