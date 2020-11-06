package de.df.jauswertung.gui.plugins.core;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;

public interface IWettkampfProvider {
    public <T extends ASchwimmer> AWettkampf<T> getWettkampf();
}
