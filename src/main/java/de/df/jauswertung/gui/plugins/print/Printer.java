/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import javax.swing.JPanel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
public interface Printer {
    JPanel getPanel();

    String getName();

    <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk);
}
