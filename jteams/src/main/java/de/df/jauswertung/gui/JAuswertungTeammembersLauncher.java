package de.df.jauswertung.gui;

import de.df.jutils.plugin.JPanelContainer;

public final class JAuswertungTeammembersLauncher {

    private JAuswertungTeammembersLauncher() {
    }

    public static void main(String[] args) {
        PluginApplicationLauncher.launch("JTeams", "plugins-mmm", "JAuswertungTeammembersWindow", JPanelContainer.SINGLE,
                args);
    }
}
