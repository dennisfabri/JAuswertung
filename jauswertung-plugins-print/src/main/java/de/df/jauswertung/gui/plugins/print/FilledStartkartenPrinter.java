/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.print.Printable;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.StartkartenPrintable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.print.PageMode;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class FilledStartkartenPrinter extends AFilledKartenPrinter {

    public FilledStartkartenPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, I18n.get("Startkarten"));
    }

    @Override
    public String getName() {
        return I18n.get("FilledStartkarten");
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Printable getPrintable(AWettkampf[] wk, PageMode mode, boolean printEmptyCards, boolean allheats,
            int minheat, int maxheat) {
        return new StartkartenPrintable(wk, mode, printEmptyCards, allheats, minheat, maxheat);
    }
}