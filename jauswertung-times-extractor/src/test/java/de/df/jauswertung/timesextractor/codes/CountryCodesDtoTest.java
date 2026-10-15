package de.df.jauswertung.timesextractor.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountryCodesDtoTest {

    private static String alpha2(String iso3Code) {
        CountryCodesDto dto = new CountryCodesDto();
        dto.setName("Name");
        dto.setIocCode("IOC");
        dto.setIso3Code(iso3Code);
        return dto.toCountryCodes().alpha2Code();
    }

    @Test
    void knownIso3CodesAreMappedToAlpha2() {
        assertEquals("DE", alpha2("DEU"));
        assertEquals("DZ", alpha2("DZA"));
        assertEquals("AX", alpha2("ALA"));
        assertEquals("AF", alpha2("AFG"));
        assertEquals("US", alpha2("USA"));
    }

    @Test
    void lookupIsCaseInsensitive() {
        assertEquals("FR", alpha2("fra"));
    }

    @Test
    void unknownOrMissingCodesFallBackToGermany() {
        assertEquals("DE", alpha2("XXX"));
        assertEquals("DE", alpha2(""));
        assertEquals("DE", alpha2(null));
    }

    @Test
    void allEntriesOfTheBundledCsvHaveAnAlpha2Code() {
        CountryCodesController controller = CountryCodesController.getInstance();
        assertEquals("DE", controller.getByIOCCode("GER").alpha2Code());
        assertEquals("AL", controller.getByIOCCode("ALB").alpha2Code());
    }
}
