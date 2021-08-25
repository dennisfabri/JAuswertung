package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import de.df.jauswertung.gui.plugins.elektronischezeit.IETStrategy;
import de.df.jauswertung.gui.plugins.elektronischezeit.MElektronischeZeitPlugin;
import de.dm.ares.data.Heat;
import de.dm.ares.file.FileReader;

public class AresfileTimesSource implements ITimesSource {

    @Override
    public Heat[] getHeats() {
        String file = SourcesConfig.getAresfile();
        return FileReader.readHeats(file);
    }
}