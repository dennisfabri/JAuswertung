/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.doping.DopingSelection;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class DopinglistenPrinter extends ASelectionlistPrinter {

    public DopinglistenPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, I18n.get("Dopingkontrolle"), new DopingSelection());
    }
}