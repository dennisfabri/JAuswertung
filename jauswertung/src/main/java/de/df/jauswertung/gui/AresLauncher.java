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
        JAresWriter writer = new JAresWriter();
        writer.setVisible(true);
        if (args != null && args.length > 1 && "-open".equalsIgnoreCase(args[0]) && args[1] != null
                && !args[1].isBlank()) {
            writer.openExportSet(args[1].trim());
        }
    }
}