package de.df.jauswertung.gui.plugins.startpass;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.gui.plugins.aselection.ISelection;

public class StartpassSelection implements ISelection {

    @Override
    public boolean getValue(ASchwimmer s) {
        return s.getStartunterlagen() == Startunterlagen.PRUEFEN;
    }

    @Override
    public void setValue(ASchwimmer s, boolean value) {
        s.setStartunterlagen(value ? Startunterlagen.PRUEFEN : Startunterlagen.NICHT_PRUEFEN);
    }

}
