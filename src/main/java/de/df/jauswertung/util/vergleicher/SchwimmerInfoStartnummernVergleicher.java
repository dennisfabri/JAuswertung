package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;

import de.df.jauswertung.gui.util.SchwimmerInfo;

public class SchwimmerInfoStartnummernVergleicher implements java.util.Comparator<SchwimmerInfo>, Serializable {

    @Override
    public int compare(SchwimmerInfo o1, SchwimmerInfo o2) {
        return o1.getStartnummer() - o2.getStartnummer();
    }
}