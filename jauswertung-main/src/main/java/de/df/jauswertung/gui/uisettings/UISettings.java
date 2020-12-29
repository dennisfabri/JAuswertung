package de.df.jauswertung.gui.uisettings;

import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.UIPerformanceMode;

public class UISettings {

    public static void main(String[] args) {
        DesignInit.init(true, UIPerformanceMode.Software);
        new UIModeWindow().setVisible(true);
    }
}
