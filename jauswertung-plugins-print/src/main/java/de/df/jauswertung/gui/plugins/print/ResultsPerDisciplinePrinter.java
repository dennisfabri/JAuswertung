/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.print.Printable;
import java.io.OutputStream;
import java.util.LinkedList;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ResultsPerDisciplinePrinter extends APerDisciplinePrinter {

    public ResultsPerDisciplinePrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, true);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("Einzelwertung");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LinkedList<Printable> getPrintable(boolean[][] selected, AWettkampf wk, int qualification) {
        return PrintUtils.getFullResultsPrintable(selected, wk, true, PrintUtils.printPointsInDisciplineResults, qualification);
    }

    @Override
    protected void export(AWettkampf wk, OutputStream os) {
    }

    @Override
    protected boolean hasExport() {
        return true;
    }
}