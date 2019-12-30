package de.df.jauswertung.gui.plugins.aselection;

import de.df.jauswertung.daten.ASchwimmer;

public interface ISelection {

    public void setValue(ASchwimmer s, boolean value);

    public boolean getValue(ASchwimmer s);

}
