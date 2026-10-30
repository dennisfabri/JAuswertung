package de.df.jauswertung.gui.util;

import java.awt.Window;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.WertungsgruppeException;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jutils.gui.util.DialogUtils;

/**
 * Reports a scoring group that cannot be merged (see {@link WertungsgruppeException}) before a printout starts.
 */
public final class WertungsgruppenPruefung {

    private WertungsgruppenPruefung() {
        // Not used
    }

    /**
     * @return {@code false} after showing the error if a scoring group with swimmers cannot be merged
     */
    public static boolean pruefe(Window parent, AWettkampf<?> wk) {
        try {
            CompetitionUtils.checkWertungsgruppen(wk);
            return true;
        } catch (WertungsgruppeException e) {
            DialogUtils.warn(parent, I18n.get("PrintError"), e.getMessage());
            return false;
        }
    }

    /**
     * @return {@code false} after showing the error if the scoring group has swimmers and cannot be merged
     */
    public static boolean pruefe(Window parent, AWettkampf<?> wk, String wgname) {
        try {
            CompetitionUtils.checkWertungsgruppe(wk, wgname);
            return true;
        } catch (WertungsgruppeException e) {
            DialogUtils.warn(parent, I18n.get("PrintError"), e.getMessage());
            return false;
        }
    }
}
