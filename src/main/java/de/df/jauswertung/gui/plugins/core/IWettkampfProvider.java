package de.df.jauswertung.gui.plugins.core;

import de.df.jauswertung.daten.*;

public interface IWettkampfProvider {
    public <T extends ASchwimmer> AWettkampf<T> getWettkampf();
}
