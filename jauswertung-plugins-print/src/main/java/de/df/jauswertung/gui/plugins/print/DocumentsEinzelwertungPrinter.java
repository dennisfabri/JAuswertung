/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.print.Printable;
import java.util.LinkedList;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.util.GraphUtils;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class DocumentsEinzelwertungPrinter extends APerDisciplinePrinter {

    public DocumentsEinzelwertungPrinter(IPluginManager window, CorePlugin plugin) {
        super(window, plugin, false);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("DocumentsEinzelwertung");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected LinkedList<Printable> getPrintable(boolean[][] selected, AWettkampf wk, int qualification) {
        LinkedList<Printable> ll = new LinkedList<Printable>();
        Printable p = GraphUtils.getPrintable(wk, selected, true);
        if (p != null) {
            ll.add(p);
        }
        return ll;
    }
}