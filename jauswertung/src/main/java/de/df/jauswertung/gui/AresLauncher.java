package de.df.jauswertung.gui;

import de.df.jauswertung.ares.gui.JAresWriter;
import de.df.jauswertung.util.DefaultInit;
import de.df.jutils.gui.util.DesignInit;

public final class AresLauncher {

    private AresLauncher() {
        // Nothing to do
    }

    public static void main(String[] args) {
        DefaultInit.init();
        DesignInit.init();
        new JAresWriter().setVisible(true);
    }
}