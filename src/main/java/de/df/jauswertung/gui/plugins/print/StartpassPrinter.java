/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.startpass.StartpassSelection;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class StartpassPrinter extends ASelectionlistPrinter {

    public StartpassPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, I18n.get("Startunterlagenkontrolle"), new StartpassSelection());
    }
}