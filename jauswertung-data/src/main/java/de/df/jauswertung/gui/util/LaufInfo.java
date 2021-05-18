/*
 * LaufInfo.java Created on 12. November 2001, 10:40
 */

package de.df.jauswertung.gui.util;

/**
 * @author Dennis Fabri
 * @version
 */
public class LaufInfo {

    private final String lauf;
    private final String bahn;

    /** Creates new LaufInfo */
    public LaufInfo() {
        lauf = "        ";
        bahn = "        ";
    }

    public LaufInfo(String iLauf, int iBahn) {
        lauf = iLauf;
        bahn = "" + iBahn;
    }

    /**
     * @return Returns the bahn.
     */
    public String getBahn() {
        return bahn;
    }

    /**
     * @return Returns the lauf.
     */
    public String getLauf() {
        return lauf;
    }
}
