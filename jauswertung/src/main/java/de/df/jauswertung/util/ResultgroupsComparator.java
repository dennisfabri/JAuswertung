package de.df.jauswertung.util;

import java.util.Comparator;

import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;

public final class ResultgroupsComparator implements Comparator<Wertungsgruppe> {
    @Override
    public int compare(Wertungsgruppe o1, Wertungsgruppe o2) {
        return o1.getName().compareTo(o2.getName());
    }
}