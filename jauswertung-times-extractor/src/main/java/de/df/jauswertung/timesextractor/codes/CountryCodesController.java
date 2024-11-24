package de.df.jauswertung.timesextractor.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryCodesController {

    private static final Logger log = LoggerFactory.getLogger(CountryCodesController.class);

    private final List<CountryCodes> codes = new ArrayList<>();

    private CountryCodesController() {
    }

    public static CountryCodesController getInstance() {
        CountryCodesController controller = new CountryCodesController();
        controller.initialize();
        return controller;
    }

    private void initialize() {
        try (InputStream is = this.getClass().getResourceAsStream("/ioc-fifa-iso.csv");
                Reader reader = new InputStreamReader(is)) {
            CsvToBean<CountryCodesDto> cb = new CsvToBeanBuilder<CountryCodesDto>(reader)
                    .withType(CountryCodesDto.class)
                    .withSeparator(';')
                    .build();

            List<CountryCodes> loadedCodes = cb.stream().map(CountryCodesDto::toCountryCodes).toList();

            codes.clear();
            codes.addAll(loadedCodes);
        } catch (IOException e) {
            log.warn("Could not load country codes", e);
        }
    }

    public CountryCodes getByIOCCode(String iocCode) {
        return codes.stream()
                .filter(c -> c.iocCode().equalsIgnoreCase(iocCode))
                .findFirst()
                .orElse(null);
    }
}
