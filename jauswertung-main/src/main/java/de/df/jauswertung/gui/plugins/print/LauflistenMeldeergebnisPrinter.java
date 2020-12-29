/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class LauflistenMeldeergebnisPrinter extends ALauflistenPrinter {

    public LauflistenMeldeergebnisPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, true, true);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("Registrationresult");
    }
}