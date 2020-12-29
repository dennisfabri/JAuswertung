package de.df.jauswertung.gui.veranstaltung;

public class Veranstaltungswertung {

    /**
     * @param args
     */
    public static void start() {
        new JVeranstaltungswertung().setVisible(true);
    }

    public static void start(String name) {
        new JVeranstaltungswertung(name).setVisible(true);
    }
}