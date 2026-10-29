package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import org.lisasp.alphatiming.legacy.api.Heat;

public interface ITimesSource {
    public Heat[] getHeats() throws TimesSourceException;
}
