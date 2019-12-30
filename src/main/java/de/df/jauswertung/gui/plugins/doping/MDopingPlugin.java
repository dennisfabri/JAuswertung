/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.doping;

import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.gui.plugins.aselection.AMSelectionPlugin;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MDopingPlugin extends AMSelectionPlugin {

    public MDopingPlugin() {
        super("Dopingkontrolle", PropertyConstants.DOPING_REGISTERED_POINTS_INDEX, new DopingSelection());
    }

}