package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.util.BetaFeature.Area;

class BetaFeatureTest {

    private static final BetaFeature FEATURE = BetaFeature.DSM;

    private final Preferences node = Utils.getPreferences().node("beta");
    private String saved;

    @BeforeEach
    void save() {
        saved = node.get(FEATURE.getKey(), null);
        node.remove(FEATURE.getKey());
    }

    @AfterEach
    void restore() {
        if (saved == null) {
            node.remove(FEATURE.getKey());
        } else {
            node.put(FEATURE.getKey(), saved);
        }
    }

    @Test
    void isDisabledWhenNeverSet() {
        assertFalse(FEATURE.isEnabled());
    }

    @Test
    void enabledStateIsStoredInTheBetaNode() {
        FEATURE.setEnabled(true);

        assertTrue(FEATURE.isEnabled());
        assertTrue(node.getBoolean(FEATURE.getKey(), false));

        FEATURE.setEnabled(false);

        assertFalse(FEATURE.isEnabled());
    }

    @Test
    void featuresAndDiagnosticsAreSeparateAreas() {
        assertEquals(EnumSet.of(BetaFeature.HIDDEN_FORMULAS, BetaFeature.DSM, BetaFeature.SWITCH_AGE_GROUPS,
                BetaFeature.DUMMY_TIMES_SOURCE), BetaFeature.inArea(Area.FEATURES));
        assertEquals(EnumSet.of(BetaFeature.EDT_CHECKS, BetaFeature.VERBOSE_PLUGIN_MANAGER, BetaFeature.LOG_EXCEPTIONS,
                BetaFeature.SYSTEM_INFORMATION, BetaFeature.FAST_UPLOAD), BetaFeature.inArea(Area.DIAGNOSTICS));
    }
}
