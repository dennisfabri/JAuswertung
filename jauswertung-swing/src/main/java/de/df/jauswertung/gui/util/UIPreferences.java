package de.df.jauswertung.gui.util;

import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.UIPerformanceMode;

public final class UIPreferences {

    private UIPreferences() {
        // Not used
    }

    public static UIPerformanceMode getUIPerformanceMode() {
        return switch (Utils.getPreferences().getInt("UIPerformanceMode", UIPerformanceMode.Default.value)) {
            case 1 -> UIPerformanceMode.Software;
            case 2 -> UIPerformanceMode.OpenGL;
            default -> UIPerformanceMode.Default;
        };
    }

    public static void setUIPerformanceMode(UIPerformanceMode mode) {
        Utils.getPreferences().putInt("UIPerformanceMode", mode.value);
    }
}
