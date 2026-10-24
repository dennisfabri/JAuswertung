package de.df.jauswertung.gui.veranstaltung;

import java.awt.Font;

import javax.swing.JFrame;

import de.df.jauswertung.daten.veranstaltung.Veranstaltung;

/**
 * What the printers of the Veranstaltungswertung print from.
 */
interface Ausgabekontext {

    /**
     * A copy of the event, so it can be printed outside of the EDT.
     */
    Veranstaltung getVeranstaltung();

    /**
     * The font chosen for printing, or {@code null} for the default font.
     */
    Font getSelectedFont();

    JFrame getWindow();
}
