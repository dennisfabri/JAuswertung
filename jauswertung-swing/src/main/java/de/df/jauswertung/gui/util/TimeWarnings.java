package de.df.jauswertung.gui.util;

import java.awt.Toolkit;
import java.awt.Window;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.util.SchwimmerUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.util.StringTools;

/**
 * Asks the user to confirm implausible times; the check itself is {@link SchwimmerUtils#checkTime(ASchwimmer, int)}.
 */
public final class TimeWarnings {

    public static boolean EnableHighPointWarning = true;

    public static boolean EnableHighTimesWarning = false;

    private TimeWarnings() {
        // Not used
    }

    public static boolean checkTimeAndNotify(Window parent, ASchwimmer s, int disz) {
        if (!EnableHighPointWarning) {
            return true;
        }
        boolean result = SchwimmerUtils.checkTime(s, disz);
        if (!result) {
            Toolkit.getDefaultToolkit().beep();
            try {
                EnableHighPointWarning = false;
                result = DialogUtils.askAndWarn(parent, I18n.get("PointsVeryHigh"),
                                                I18n.get("IsTimeCorrect", s.getName(), StringTools.zeitString(s.getZeit(disz))),
                                                I18n.get("IsTimeCorrect.Note", s.getName(), StringTools.zeitString(s.getZeit(disz))));
            } finally {
                EnableHighPointWarning = true;
            }
        }
        return result;
    }
}
