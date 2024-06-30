package de.df.jauswertung.gui.veranstaltung;

public class Veranstaltungswertung {

    public static void start() {
        new JVeranstaltungswertung().setVisible(true);
    }

    public static void start(String filename) {
        new JVeranstaltungswertung(filename).setVisible(true);
    }
}