/*
 * Created on 31.10.2005
 */
package de.df.jauswertung.gui;

import de.df.jutils.plugin.UpdateEvent;

public final class UpdateEventConstants {

    public static final UpdateEvent EVERYTHING_CHANGED;

    public static final long        NOTHING                             = 0;

    public static final long        REASON_NEW_WK                       = bitmask(0);
    public static final long        REASON_NEW_TN                       = bitmask(1);
    public static final long        REASON_AKS_CHANGED                  = bitmask(2);
    public static final long        REASON_PROPERTIES_CHANGED           = bitmask(3);
    public static final long        REASON_ZW_LIST_CHANGED              = bitmask(4);
    public static final long        REASON_LAUF_LIST_CHANGED            = bitmask(5);
    public static final long        REASON_LOAD_WK                      = bitmask(6);
    public static final long        REASON_REFEREES_CHANGED             = bitmask(7);
    public static final long        REASON_POINTS_CHANGED               = bitmask(8);
    public static final long        REASON_TEAMASSIGNMENT_CHANGED       = bitmask(9);
    public static final long        REASON_GLIEDERUNG_CHANGED           = bitmask(10);
    public static final long        REASON_SWIMMER_CHANGED              = bitmask(11);
    public static final long        REASON_SWIMMER_DELETED              = bitmask(12);
    public static final long        REASON_FILENAME_CHANGED             = bitmask(13);
    public static final long        REASON_STARTUP                      = bitmask(14);
    public static final long        REASON_APEAL                        = bitmask(15);
    public static final long        REASON_PENALTY                      = bitmask(16);
    public static final long        REASON_FILTERS_CHANGED              = bitmask(17);
    public static final long        REASON_FILTER_SELECTION             = bitmask(18);
    public static final long        REASON_MELDEZEITEN_CHANGED          = bitmask(19);
    public static final long        REASON_ZIELRICHTERENTSCHEID_CHANGED = bitmask(20);
    public static final long        REASON_TEAMMEMBERS_EXPORTID_CHANGED = bitmask(21);
    public static final long        REASON_TIMELIMITS_CHANGED           = bitmask(22);

    public static final int         USED_REASON_BITS                    = 23;
    public static final long        REASON_EVERYTHING_CHANGED;

    public static final long        REASON_NEW_LOAD_WK;

    static {
        REASON_EVERYTHING_CHANGED = everythingChanged();
        REASON_NEW_LOAD_WK = UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_LOAD_WK;
        EVERYTHING_CHANGED = new UpdateEvent("", REASON_EVERYTHING_CHANGED, null);
    }

    private static long everythingChanged() {
        return ((long) Math.pow(2, UpdateEventConstants.USED_REASON_BITS)) - 1 - UpdateEventConstants.REASON_STARTUP;
    }

    public static long bitmask(int index) {
        return (long) Math.pow(2, index);
    }

    private UpdateEventConstants() {
        // Hide
    }
}