/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public class FormelDLRG<T extends ASchwimmer> extends FormelDLRG2007<T> {

    @SuppressWarnings("hiding")
    public static final String ID = "DLRG1999";

    @Override
    public String getID() {
        return FormelDLRG.ID;
    }

    @Override
    public String getName() {
        return "DLRG Regelwerk 1999";
    }

    @Override
    public String getDescription() {
        return "Diese Formel entspricht dem Regelwerk der DLRG von 1999";
    }

    public FormelDLRG() {
        super();
    }
}