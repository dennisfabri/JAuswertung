/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;

import javax.swing.JPanel;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.ZielrichterentscheidUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.print.ComponentPackingPrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.util.StringTools;

public final class ZielrichterentscheidPrintable<T extends ASchwimmer> extends ComponentPackingPrintable {

    public ZielrichterentscheidPrintable(AWettkampf<T> wk) {
        super(5, -1, getPanels(wk));
    }

    private static <T extends ASchwimmer> Component[] getPanels(AWettkampf<T> wk) {
        if (wk == null) {
            return null;
        }

        LinkedList<Zielrichterentscheid<T>>[] zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk);
        if (zes[0].isEmpty()) {
            return null;
        }

        LinkedList<Component> components = new LinkedList<Component>();
        for (Zielrichterentscheid<T> ze : zes[0]) {
            components.addLast(getPanel(ze));
        }
        return components.toArray(new Component[components.size()]);
    }

    static <T extends ASchwimmer> JPanel getPanel(Zielrichterentscheid<T> ze) {
        SimpleFormBuilder sfb = new SimpleFormBuilder();
        sfb.setFont(PrintManager.getFont());

        T s = ze.getSchwimmer().getFirst();
        Altersklasse ak = s.getAK();
        int disziplinNr = ze.getDisziplin();
        String disziplin = ak.getDisziplin(disziplinNr, true).getName();
        int zeit = ze.getZeit();

        LaufInfo li = SchwimmerUtils.getLaufInfo(s.getWettkampf(), s, ze.getDisziplin());

        sfb.add(I18n.get("Heat") + ":", li.getLauf());
        sfb.add(I18n.get("Discipline") + ":", disziplin);
        sfb.add(I18n.get("Time") + ":", StringTools.zeitString(zeit));

        sfb.addSeparator(I18n.get("Order"));

        int x = 0;
        for (ASchwimmer t : ze.getSchwimmer()) {
            li = SchwimmerUtils.getLaufInfo(t.getWettkampf(), t, ze.getDisziplin());

            x++;
            sfb.add(I18n.get("ZielrichterentscheidOrder", x, t.getName(), t.getGliederung(), li.getBahn()));
        }

        JPanel panel = sfb.getPanel();
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);
        return panel;
    }
}