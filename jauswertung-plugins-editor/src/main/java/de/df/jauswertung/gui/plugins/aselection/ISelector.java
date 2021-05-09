package de.df.jauswertung.gui.plugins.aselection;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;

public interface ISelector<T extends ASchwimmer> {
    String getName();

    void select(AWettkampf<T> wk, AMSelectionPlugin setter);
}