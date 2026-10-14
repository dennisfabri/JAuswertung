package de.df.jauswertung.gui;

import java.awt.Toolkit;

import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.JPanelContainer;

public final class JAuswertungLauncher {

    private JAuswertungLauncher() {
    }

    public static void main(String[] args) {
        boolean smallScreen = Toolkit.getDefaultToolkit().getScreenSize().height < 1000;
        boolean singleDisplayMode = Utils.getPreferences().getBoolean("WindowMode",
                smallScreen ? JPanelContainer.SINGLE : JPanelContainer.DOUBLE);
        PluginApplicationLauncher.launch("JAuswertung", "plugins", "JAuswertungMainWindow", singleDisplayMode, args);
    }
}
