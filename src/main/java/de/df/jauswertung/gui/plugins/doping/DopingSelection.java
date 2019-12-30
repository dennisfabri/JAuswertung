package de.df.jauswertung.gui.plugins.doping;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.plugins.aselection.ISelection;

public class DopingSelection implements ISelection {

    @Override
    public boolean getValue(ASchwimmer s) {
        return s.hasDopingkontrolle();
    }

    @Override
    public void setValue(ASchwimmer s, boolean value) {
        s.setDopingkontrolle(value);
    }

}
