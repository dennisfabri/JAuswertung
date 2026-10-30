package de.df.jauswertung.util;

import java.util.EnumSet;
import java.util.prefs.Preferences;

public enum BetaFeature {

    HIDDEN_FORMULAS(Area.FEATURES, "HiddenFormulas"),
    DSM(Area.FEATURES, "DSM"),
    SWITCH_AGE_GROUPS(Area.FEATURES, "SwitchAgeGroups"),
    DUMMY_TIMES_SOURCE(Area.FEATURES, "DummyTimesSource"),
    EDT_CHECKS(Area.DIAGNOSTICS, "EdtChecks"),
    VERBOSE_PLUGIN_MANAGER(Area.DIAGNOSTICS, "VerbosePluginManager"),
    LOG_EXCEPTIONS(Area.DIAGNOSTICS, "LogExceptions"),
    SYSTEM_INFORMATION(Area.DIAGNOSTICS, "SystemInformation"),
    FAST_UPLOAD(Area.DIAGNOSTICS, "FastUpload");

    public enum Area {
        FEATURES, DIAGNOSTICS
    }

    private final Area area;
    private final String key;

    BetaFeature(Area area, String key) {
        this.area = area;
        this.key = key;
    }

    public Area getArea() {
        return area;
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return node().getBoolean(key, false);
    }

    public void setEnabled(boolean enabled) {
        node().putBoolean(key, enabled);
    }

    public static EnumSet<BetaFeature> inArea(Area area) {
        EnumSet<BetaFeature> result = EnumSet.noneOf(BetaFeature.class);
        for (BetaFeature feature : values()) {
            if (feature.area == area) {
                result.add(feature);
            }
        }
        return result;
    }

    private static Preferences node() {
        return Utils.getPreferences().node("beta");
    }
}
