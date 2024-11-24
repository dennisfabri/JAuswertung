package de.df.jauswertung.timesextractor.codes;

import com.neovisionaries.i18n.CountryCode;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class CountryCodesDto {

    private static final Logger log = LoggerFactory.getLogger(CountryCodesDto.class);

    @CsvBindByName(column = "Name")
    public String name;
    @CsvBindByName(column = "IOC")
    public String iocCode;
    @CsvBindByName(column = "FIFA")
    public String fifaCode;
    @CsvBindByName(column = "ISO")
    public String iso3Code;

    public CountryCodes toCountryCodes() {
        String alpha2 = "DE";
        try {
            alpha2 = CountryCode.getByCode(iso3Code, false).getAlpha2();
        } catch (RuntimeException re) {
            log.info("Could not find alpha2 code for {}", iso3Code, re);
        }
        return new CountryCodes(name, iocCode, alpha2, iso3Code);
    }
}
