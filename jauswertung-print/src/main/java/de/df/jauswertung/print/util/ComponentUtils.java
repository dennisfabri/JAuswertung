package de.df.jauswertung.print.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public final class ComponentUtils {

    private static final int BOX_SIZE = 10;

    // Empty square to tick by hand on the printout
    private static final Icon IMAGE_BOX = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawRect(x, y, BOX_SIZE - 1, BOX_SIZE - 1);
        }

        @Override
        public int getIconWidth() {
            return BOX_SIZE;
        }

        @Override
        public int getIconHeight() {
            return BOX_SIZE;
        }
    };

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
