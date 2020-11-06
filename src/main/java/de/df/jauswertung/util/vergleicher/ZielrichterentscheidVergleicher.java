/**
 * 
 */
package de.df.jauswertung.util.vergleicher;

import java.io.Serializable;
import java.util.Comparator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.util.LaufInfo;
import de.df.jauswertung.gui.util.SchwimmerUtils;

public final class ZielrichterentscheidVergleicher<T extends ASchwimmer> implements Comparator<Zielrichterentscheid<T>>, Serializable {

    @Override
    public int compare(Zielrichterentscheid<T> ze1, Zielrichterentscheid<T> ze2) {
        ASchwimmer s1 = ze1.getSchwimmer().getFirst();
        ASchwimmer s2 = ze2.getSchwimmer().getFirst();
        LaufInfo l1 = SchwimmerUtils.getLaufInfo(s1.getWettkampf(), s1, ze1.getDisziplin());
        LaufInfo l2 = SchwimmerUtils.getLaufInfo(s2.getWettkampf(), s2, ze2.getDisziplin());

        String lauf1 = l1.getLauf();
        String lauf2 = l2.getLauf();

        if (Character.isLetter(lauf1.charAt(lauf1.length() - 1)) != Character.isLetter(lauf2.charAt(lauf2.length() - 1))) {
            if (Character.isLetter(lauf1.charAt(lauf1.length() - 1))) {
                lauf1 = lauf1.substring(0, lauf1.length() - 1);
            } else {
                lauf2 = lauf2.substring(0, lauf2.length() - 1);
            }
        }

        while (lauf1.length() < lauf2.length()) {
            lauf1 = "0" + lauf1;
        }
        while (lauf2.length() < lauf1.length()) {
            lauf2 = "0" + lauf2;
        }

        return lauf1.compareTo(lauf2);
    }
}