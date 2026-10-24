package de.df.jauswertung.ares.export;

import java.nio.file.Path;
import java.util.List;

public record AresExportSet(List<Path> competitions, Path directory) {

    public AresExportSet {
        competitions = List.copyOf(competitions);
    }
}
