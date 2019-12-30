package de.df.jauswertung.gui.plugins.aselection;

import de.df.jauswertung.daten.*;

public interface ISelector<T extends ASchwimmer> {
    String getName();

    void select(AWettkampf<T> wk, AMSelectionPlugin setter);
}