package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import org.lisasp.alphatiming.legacy.api.AresResultFile;
import org.lisasp.alphatiming.legacy.api.Heat;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;

public class AresfileTimesSource implements ITimesSource {

    @Override
    public Heat[] getHeats() throws TimesSourceException {
        return load(SourcesConfig.getAresfile());
    }

    static Heat[] load(String file) throws TimesSourceException {
        if (file.isBlank()) {
            throw TimesSourceException.file(Reason.FILE_NOT_SET, file, null);
        }
        try {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                throw TimesSourceException.file(Reason.FILE_NOT_FOUND, file, null);
            }
            return AresResultFile.read(path);
        } catch (InvalidPathException e) {
            throw TimesSourceException.file(Reason.FILE_NOT_FOUND, file, e);
        } catch (IOException | RuntimeException e) {
            throw TimesSourceException.file(Reason.FILE_UNREADABLE, file, e);
        }
    }
}
