/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.veranstaltung;

import javax.swing.JPanel;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
interface Printer {
    JPanel getPanel();

    String getName();
}
