/*
 * Created on 19.10.2005
 */
package de.df.jauswertung.gui.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.util.format.StartnumberFormatManager;

public class SchwimmerListCellRenderer implements ListCellRenderer<ASchwimmer> {

    private final DefaultListCellRenderer parent;

    private JLabel                        none;

    private JLabel                        l;

    public SchwimmerListCellRenderer() {
        parent = new DefaultListCellRenderer();

        none = new JLabel(I18n.get("None"));
        none.setOpaque(true);

        l = new JLabel();
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 1, 1, 1));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Component getListCellRendererComponent(JList list, ASchwimmer value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return getListRenderData(value, c.getBackground(), c.getForeground());
    }

    public Component getListRenderData(ASchwimmer swimmer, Color b, Color f) {
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
        l.setText(I18n.get("SwimmerID", swimmer.getName(), StartnumberFormatManager.format(swimmer), swimmer.getAK().getName(),
                I18n.geschlechtToString(swimmer), swimmer.getGliederungMitQGliederung()));

        return l;
    }
}