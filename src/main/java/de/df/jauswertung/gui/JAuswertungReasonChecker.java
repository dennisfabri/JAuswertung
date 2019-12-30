/*
 * Created on 31.10.2005
 */
package de.df.jauswertung.gui;

import de.df.jutils.plugin.UpdateEvent;

public class JAuswertungReasonChecker implements UpdateEvent.ReasonChecker {

    public JAuswertungReasonChecker() {
        // Nothing to do
    }

    @Override
    public long check(long updatereason) {
        // Set the constants that are also affected
        if ((updatereason & UpdateEventConstants.REASON_NEW_LOAD_WK) > 0) {
            updatereason = updatereason | UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_PROPERTIES_CHANGED
                    | UpdateEventConstants.REASON_ZW_LIST_CHANGED | UpdateEventConstants.REASON_LAUF_LIST_CHANGED | UpdateEventConstants.REASON_REFEREES_CHANGED
                    | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;
        }
        if ((updatereason & UpdateEventConstants.REASON_NEW_WK) > 0) {
            updatereason = updatereason | UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_PROPERTIES_CHANGED
                    | UpdateEventConstants.REASON_ZW_LIST_CHANGED | UpdateEventConstants.REASON_LAUF_LIST_CHANGED | UpdateEventConstants.REASON_REFEREES_CHANGED
                    | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;
        }
        if ((updatereason & UpdateEventConstants.REASON_TEAMASSIGNMENT_CHANGED) > 0) {
            updatereason = updatereason | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED;
        }

        return updatereason;
    }
}