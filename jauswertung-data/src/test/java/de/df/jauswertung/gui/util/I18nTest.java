package de.df.jauswertung.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class I18nTest {

    @Test
    void readsUmlautsAndSharpS() {
        assertEquals("Außer Konkurrenz", I18n.get("AusserKonkurrenz"));
        assertEquals("Anzahl muss größer als Null sein.", I18n.get("AmountMustBeBiggerThanZero"));
    }

    @Test
    void readsGermanQuotes() {
        assertEquals("Der Rechner „x“ wurde nicht gefunden.\nBitte die Adresse des Zeitnahme-Servers in den Optionen prüfen.",
                I18n.get("Error.Times.UnknownHost", "x"));
    }
}
