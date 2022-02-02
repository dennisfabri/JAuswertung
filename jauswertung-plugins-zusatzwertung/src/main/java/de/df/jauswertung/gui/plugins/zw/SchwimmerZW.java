/*
 * Created on 27.06.2005
 */
package de.df.jauswertung.gui.plugins.zw;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.renderer.ListRenderDataProvider;
import de.df.jutils.gui.util.EDTUtils;

class SchwimmerZW<T extends ASchwimmer> implements ListRenderDataProvider, Comparable<SchwimmerZW<T>> {

    private final T swimmer;
    private int     count;

    private JLabel  l;

    public SchwimmerZW() {
        this(null, 0);
    }

    public SchwimmerZW(T s, int offen) {
        swimmer = s;
        updateCount(offen);

        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                initLabel();
            }
        });
    }

    public void updateCount(int amount) {
        count = amount;
    }

    public void decreaseCount() {
        count--;
    }

    public void increaseCount() {
        count++;
    }

    public T getSchwimmer() {
        return swimmer;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Component getListRenderData(Color f, Color b) {
        if (swimmer == null) {
            l.setIcon(null);
            l.setText(null);
            l.setForeground(f);
            l.setBackground(b);
            return l;
        }
        l.setForeground(f);
        l.setBackground(b);
        if (swimmer instanceof Teilnehmer) {
            l.setIcon(IconManager.getBigIcon("person"));
        } else {
            l.setIcon(IconManager.getBigIcon("team"));
        }
        if (swimmer instanceof Mannschaft) {
            l.setText(I18n.get("SwimmerInZWqueueTeam", swimmer.getName(), count, StartnumberFormatManager.format(swimmer), swimmer.getAK().getName(),
                    I18n.geschlechtToString(swimmer), swimmer.getGliederung()));
        } else {
            l.setText(I18n.get("SwimmerInZWqueueSingle", swimmer.getName(), count, StartnumberFormatManager.format(swimmer), swimmer.getAK().getName(),
                    I18n.geschlechtToString(swimmer), swimmer.getGliederung()));
        }

        return l;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SchwimmerZW) {
            return compareTo((SchwimmerZW<T>) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return swimmer.getStartnummer();
    }

    @Override
    public int compareTo(SchwimmerZW<T> sd) {
        if ((swimmer == null) && (sd.getSchwimmer() == null)) {
            return 0;
        }
        if (swimmer == null) {
            return -1;
        }
        if (sd.getSchwimmer() == null) {
            return 1;
        }
        int diff = swimmer.getAKNummer() - sd.swimmer.getAKNummer();
        if (diff != 0) {
            return diff;
        }
        diff = (swimmer.isMaennlich() ? 1 : 0) - (sd.swimmer.isMaennlich() ? 1 : 0);
        if (diff != 0) {
            return diff;
        }
        diff = swimmer.getName().compareToIgnoreCase(sd.swimmer.getName());
        if (diff != 0) {
            return diff;
        }

        return swimmer.getStartnummer() - sd.swimmer.getStartnummer();
    }

    void initLabel() {
        l = new JLabel();
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 1, 1, 1));
    }
}