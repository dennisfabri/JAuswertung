/*
 * SchwimmerInfo.java Created on 18. Januar 2002, 18:35
 */

package de.df.jauswertung.gui.util;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Mueller
 */

public class SchwimmerInfo implements Comparable<SchwimmerInfo> {

    private final ASchwimmer schwimmer;
    private final String[]   lauf;
    private final String[]   bahn;

    /** Creates a new instance of SchwimmerInfo */
    public SchwimmerInfo(ASchwimmer s, String[] l) {
        schwimmer = s;
        lauf = l;
        bahn = new String[lauf.length];
        for (int x = 0; x < lauf.length; x++) {
            bahn[x] = "";
        }
    }

    public SchwimmerInfo(ASchwimmer s, String[] sLauf, String[] sBahn) {
        schwimmer = s;
        lauf = sLauf;
        bahn = sBahn;
    }

    public boolean isEmpty() {
        return schwimmer == null;
    }

    @Override
    public int compareTo(SchwimmerInfo s) {
        if (s == null) {
            return 1;
        }
        if (s.lauf == null) {
            return 1;
        }
        if (lauf == null) {
            return -1;
        }
        for (int x = 0; x < Math.min(s.lauf.length, lauf.length); x++) {
            if ((s.lauf[x] == null) && (lauf[x] != null)) {
                return 1;
            }
            if ((s.lauf[x] != null) && (lauf[x] == null)) {
                return -1;
            }
            if ((!lauf[x].equals("-")) && (!s.lauf[x].equals("-"))) {
                int zahl = getHeatDifference(s, x);
                if (zahl == 0) {
                    zahl = lauf[x].length() - s.lauf[x].length();
                    if (zahl == 0) {
                        zahl = lauf[x].compareTo(s.lauf[x]);
                    }
                }
                if (zahl != 0) {
                    return zahl;
                }
            } else {
                if ((!lauf[x].equals("-")) || (!s.lauf[x].equals("-"))) {
                    return (lauf[x].equals("-") ? 1 : -1);
                }
            }
        }
        for (int x = 0; x < Math.min(s.lauf.length, lauf.length); x++) {
            if ((!lauf[x].equals("-")) && (!s.lauf[x].equals("-"))) {
                int zahl = getHeatDifference(s, x);
                if (zahl == 0) {
                    zahl = lauf[x].length() - s.lauf[x].length();
                    if (zahl == 0) {
                        zahl = lauf[x].compareTo(s.lauf[x]);
                        if (zahl == 0) {
                            zahl = bahn[x].compareTo(s.bahn[x]);
                        }
                    }
                }
                if (zahl != 0) {
                    return zahl;
                }
            }
        }
        return lauf.length - s.lauf.length;
    }

    public int getStartnummer() {
        return schwimmer.getStartnummer();
    }

    /**
     * @param s
     * @param x
     * @return
     */
    public int getHeatDifference(SchwimmerInfo s, int x) {
        int lauf1 = 0;
        int lauf2 = 0;
        try {
            lauf1 = Integer.parseInt(lauf[x]);
        } catch (RuntimeException re) {
            lauf1 = Integer.parseInt(lauf[x].substring(0, lauf[x].length() - 1));
        }
        try {
            lauf2 = Integer.parseInt(s.lauf[x]);
        } catch (RuntimeException re) {
            lauf2 = Integer.parseInt(s.lauf[x].substring(0, s.lauf[x].length() - 1));
        }
        return lauf1 - lauf2;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SchwimmerInfo) {
            return compareTo((SchwimmerInfo) o) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (schwimmer == null) {
            return 0;
        }
        return schwimmer.hashCode();
    }

    public boolean isSimilar(SchwimmerInfo s) {
        if (s == null) {
            return false;
        }
        if (lauf == null) {
            return false;
        }
        if (s.lauf == null) {
            return false;
        }
        for (int x = 0; x < Math.min(s.lauf.length, lauf.length); x++) {
            if ((s.lauf[x] == null) && (lauf[x] != null)) {
                return false;
            }
            if ((s.lauf[x] != null) && (lauf[x] == null)) {
                return false;
            }
            if ((!lauf[x].equals("-")) && (!s.lauf[x].equals("-"))) {
                int zahl = lauf[x].length() - s.lauf[x].length();
                if (zahl != 0) {
                    return false;
                }
                zahl = lauf[x].compareTo(s.lauf[x]);
                return zahl == 0;
            }
        }
        return (lauf.length - s.lauf.length == 0);
    }

    /**
     * @return Returns the bahn.
     */
    public String getBahn(int i) {
        return bahn[i];
    }

    /**
     * @return Returns the lauf.
     */
    public String getLauf(int i) {
        return lauf[i];
    }

    public int getLaufCount() {
        return lauf.length;
    }

    /**
     * @return Returns the schwimmer.
     */
    public ASchwimmer getSchwimmer() {
        return schwimmer;
    }
}
