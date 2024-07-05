package de.df.jauswertung.print.util;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import de.df.jauswertung.gui.util.IconManager;

public final class ComponentUtils {

    private static final Icon IMAGE_BOX = IconManager.getImageIcon("box");

    private ComponentUtils() {
        // Hide constructor
    }

    public static JComponent createCheckBox(String text) {
        return createCheckBox(text, SwingConstants.LEFT);
    }

    public static JComponent createCheckBox(String text, int align) {
        JLabel l = new JLabel(text, IMAGE_BOX, align);
        l.setBackground(Color.WHITE);
        return l;
    }
}
