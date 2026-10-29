package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimesServerAddressTest {

    @Test
    void usesTheDefaultPortWithoutAPort() {
        assertEquals(Optional.of(new TimesServerAddress("localhost", 1999)), TimesServerAddress.parse("localhost"));
    }

    @Test
    void usesTheGivenPort() {
        assertEquals(Optional.of(new TimesServerAddress("10.0.0.5", 2000)), TimesServerAddress.parse(" 10.0.0.5:2000 "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", ":2000", "host:", "host:abc", "host:0", "host:65536" })
    void rejectsAnInvalidAddress(String address) {
        assertEquals(Optional.empty(), TimesServerAddress.parse(address));
    }

    @Test
    void pointsToTheLegacyHeats() {
        assertEquals("http://timing:2000/legacy/heats.xml", new TimesServerAddress("timing", 2000).heatsUrl());
    }
}
