/*
 * LaufInfo.java Created on 12. November 2001, 10:40
 */

package de.df.jauswertung.gui.util;

/**
 * @author Dennis Mueller
 * @version
 */
public class ZWInfo {

    private final String zeit;
    private final String bahn;

    /** Creates new LaufInfo */
    public ZWInfo() {
        zeit = "        ";
        bahn = "        ";
    }

    public ZWInfo(String iZeit, int iBahn) {
        zeit = iZeit;
        bahn = "" + iBahn;
    }

    /**
     * @return Returns the bahn.
     */
    public String getBahn() {
        return bahn;
    }

    /**
     * @return Returns the time.
     */
    public String getZeit() {
        return zeit;
    }
}
