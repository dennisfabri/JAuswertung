/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.startpass;

import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.gui.plugins.aselection.AMSelectionPlugin;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MStartpassPlugin extends AMSelectionPlugin {

    public MStartpassPlugin() {
        super("Startpasskontrolle", PropertyConstants.SUK_REGISTERED_POINTS_INDEX, new StartpassSelection());
    }
}