package de.df.jauswertung.gui.veranstaltung;

import de.df.jauswertung.gui.PluginApplicationLauncher;
import de.df.jutils.plugin.JPanelContainer;

public final class VeranstaltungswertungLauncher {

    static final String CORE = "de.df.jauswertung.veranstaltung.core";
    static final String SCHRIFTART = "de.df.jauswertung.veranstaltung.schriftart";

    private VeranstaltungswertungLauncher() {
    }

    public static void main(String[] args) {
        PluginApplicationLauncher.launch("Veranstaltungswertung", "plugins-veranstaltung", "Veranstaltungswertung",
                JPanelContainer.SINGLE, args);
    }
}
