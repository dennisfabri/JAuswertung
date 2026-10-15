package de.df.jauswertung.gui.plugins.check;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import de.df.jutils.gui.border.BorderUtils;

class DisciplineListRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -4064957849985467566L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
        label.setBorder(BorderUtils.createSpaceBorder(2));
        return label;
    }
}
