package de.df.jauswertung.timesextractor.codes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CountryCodesDto {

    private static final String DEFAULT_ALPHA2 = "DE";
    private static final Map<String, String> ALPHA3_TO_ALPHA2 = buildAlpha3ToAlpha2();

    @CsvBindByName(column = "Name")
    public String name;
    @CsvBindByName(column = "IOC")
    public String iocCode;
    @CsvBindByName(column = "FIFA")
    public String fifaCode;
    @CsvBindByName(column = "ISO")
    public String iso3Code;

    private static Map<String, String> buildAlpha3ToAlpha2() {
        Map<String, String> map = new HashMap<>();
        for (String alpha2 : Locale.getISOCountries()) {
            map.put(new Locale.Builder().setRegion(alpha2).build().getISO3Country(), alpha2);
        }
        return map;
    }

    public CountryCodes toCountryCodes() {
        String alpha2 = iso3Code == null ? DEFAULT_ALPHA2
                : ALPHA3_TO_ALPHA2.getOrDefault(iso3Code.toUpperCase(Locale.ROOT), DEFAULT_ALPHA2);
        return new CountryCodes(name, iocCode, alpha2, iso3Code);
    }
}
