/*
 * Created on 15.06.2007
 */
package de.df.jauswertung.gui.util;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;

public class PenaltyShortTextListRenderer extends DefaultListCellRenderer {

    public PenaltyShortTextListRenderer() {
        // Nothing to do
    }

    private static final long serialVersionUID = 4675647196125434845L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        if (value instanceof Strafe) {
            Strafe s = (Strafe) value;
            value = PenaltyUtils.getPenaltyShortText(s, null);
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}