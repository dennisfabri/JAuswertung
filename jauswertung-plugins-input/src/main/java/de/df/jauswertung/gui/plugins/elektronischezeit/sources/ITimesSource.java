package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import de.df.jauswertung.gui.plugins.elektronischezeit.IETStrategy;
import de.dm.ares.data.Heat;

public interface ITimesSource {
    public Heat[] getHeats();
}
