package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.IOException;
import java.nio.file.Path;

import org.lisasp.alphatiming.legacy.api.AresResultFile;
import org.lisasp.alphatiming.legacy.api.Heat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AresfileTimesSource implements ITimesSource {

    @Override
    public Heat[] getHeats() {
        String file = SourcesConfig.getAresfile();
        try {
            return AresResultFile.read(Path.of(file));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read the ARES21 result file {}", file, e);
            return null;
        }
    }
}
