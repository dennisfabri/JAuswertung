/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.print.Printable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.RecorderPrintable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.print.PrintManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class LauflistenRecorderPrinter extends ALauflistenPrinter {

    public LauflistenRecorderPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, false, false);
    }

    @Override
    public String getName() {
        return I18n.get("Recorder-Laufliste");
    }

    @Override
    protected <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, String header, boolean withComments, boolean withDisciplines, boolean withTimes) {
        Printable p = new RecorderPrintable<T>(wk, false, false, false, !PrintUtils.printOmitOrganisationForTeams);
        return PrintManager.getFinalPrintable(PrintManager.getHeaderPrintable(p, getName()), wk.getLastChangedDate(),
                true, getName());
    }
}