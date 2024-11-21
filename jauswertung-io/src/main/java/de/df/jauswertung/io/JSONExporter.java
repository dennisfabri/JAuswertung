package de.df.jauswertung.io;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jutils.util.Feedback;

public class JSONExporter extends EmptyExporter {

    private static final Logger log = LoggerFactory.getLogger(JSONExporter.class);

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case TIMES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "json" };
    }

    @Override
    public <T extends ASchwimmer> boolean zeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        JAuswertungCompetition competition = new TimesExtractor().getZeiten(wk);
        try {
            objectMapper.writeValue(name, competition);
            return true;
        } catch (IOException ex) {
            log.debug("Konnte Daten nicht schreiben", ex);
            return false;
        } catch (RuntimeException ex) {
            log.debug("Unerwarteter Fehler", ex);
            return false;
        }
    }
}
