package de.df.jauswertung.gui.plugins.print;

enum Unterschriftsart {
    KEINE("Unterschrift.Keine"), LEER("Unterschrift.Leer"), MIT_AUSHANGZEITEN("Unterschrift.MitAushangzeiten");

    private final String i18nKey;

    Unterschriftsart(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    String getI18nKey() {
        return i18nKey;
    }
}
