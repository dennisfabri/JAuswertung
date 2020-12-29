package de.df.jauswertung.util;

import java.util.Comparator;

import de.df.jauswertung.daten.regelwerk.Startgruppe;

public final class StartgroupsComparator implements Comparator<Startgruppe> {
    @Override
    public int compare(Startgruppe o1, Startgruppe o2) {
        return o1.getName().compareTo(o2.getName());
    }
}