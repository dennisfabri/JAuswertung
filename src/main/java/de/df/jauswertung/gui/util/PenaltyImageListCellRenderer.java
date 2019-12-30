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

import de.df.jauswertung.daten.regelwerk.Strafe;

public class PenaltyImageListCellRenderer<T> implements ListCellRenderer<T> {

    @SuppressWarnings("rawtypes")
    private ListCellRenderer parent     = new DefaultListCellRenderer();

    private JLabel           none;
    private JLabel           image;

    private boolean          alwaysDisq = false;

    public PenaltyImageListCellRenderer() {
        this(false);
    }

    public PenaltyImageListCellRenderer(boolean alwaysDisq) {
        none = new JLabel(I18n.get("None"));
        none.setOpaque(true);

        image = new JLabel();
        image.setOpaque(true);
        image.setBorder(new EmptyBorder(1, 1, 1, 1));

        this.alwaysDisq = alwaysDisq;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        @SuppressWarnings("unchecked")
        Component c = parent.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
        if (!(value instanceof Strafe)) {
            value = null;
        }
        return getListRenderData((Strafe) value, c.getBackground(), c.getForeground());
    }

    private Component getListRenderData(Strafe strafe, Color b, Color f) {
        if (strafe == null) {
            none.setForeground(f);
            none.setBackground(b);
            return none;
        }
        if (alwaysDisq) {
            strafe = new Strafe(strafe, true);
        }
        image.setForeground(f);
        image.setBackground(b);
        switch (strafe.getArt()) {
        case AUSSCHLUSS:
            image.setIcon(IconManager.getBigIcon("ausschluss"));
            image.setText(I18n.get("PenaltyDebarment", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            break;
        case NICHT_ANGETRETEN:
            image.setIcon(IconManager.getBigIcon("didnotstart"));
            image.setText(I18n.get("PenaltyDidNotStart", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            break;
        case DISQUALIFIKATION:
            image.setIcon(IconManager.getBigIcon("disqualification"));
            image.setText(I18n.get("PenaltyDisqualification", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            break;
        case STRAFPUNKTE:
            if (strafe.getStrafpunkte() > 0) {
                image.setIcon(IconManager.getBigIcon("penaltypoints"));
                image.setText(I18n.get("PenaltyPoints", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            } else {
                image.setIcon(IconManager.getBigIcon("nopoints"));
                image.setText(I18n.get("PenaltyPoints", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            }
            break;
        default:
        case NICHTS:
            image.setIcon(IconManager.getBigIcon("nopoints"));
            image.setText(I18n.get("PenaltyNothing", strafe.getName(), strafe.getShortname(), strafe.getStrafpunkte()));
            break;
        }
        return image;
    }
}