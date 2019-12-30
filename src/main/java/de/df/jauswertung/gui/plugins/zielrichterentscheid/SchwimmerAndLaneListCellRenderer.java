/*
 * Created on 19.10.2005
 */
package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.format.StartnumberFormatManager;

class SchwimmerAndLaneListCellRenderer implements ListCellRenderer {

    private ListCellRenderer parent;

    private JLabel           none;

    private JLabel           l;

    public SchwimmerAndLaneListCellRenderer() {
        this(null);
    }

    public SchwimmerAndLaneListCellRenderer(ListCellRenderer p) {
        parent = p;
        if (parent == null) {
            parent = new DefaultListCellRenderer();
        }

        none = new JLabel(I18n.get("None"));
        none.setOpaque(true);

        l = new JLabel();
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 1, 1, 1));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!(value instanceof Object[])) {
            return c;
        }

        Object[] array = (Object[]) value;
        return getListRenderData((ASchwimmer) array[0], array[1].toString(), c.getBackground(), c.getForeground());
    }

    public Component getListRenderData(ASchwimmer swimmer, String bahn, Color b, Color f) {
        if (swimmer == null) {
            none.setForeground(f);
            none.setBackground(b);
            return none;
        }
        l.setForeground(f);
        l.setBackground(b);
        if (swimmer instanceof Teilnehmer) {
            l.setIcon(IconManager.getBigIcon("person"));
        } else {
            l.setIcon(IconManager.getBigIcon("team"));
        }
        l.setText(I18n.get("SwimmerLane", swimmer.getName(), StartnumberFormatManager.format(swimmer), bahn, swimmer.getAK(), I18n.geschlechtToString(swimmer),
                swimmer.getGliederungMitQGliederung()));

        return l;
    }
}