package de.df.jauswertung.io.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SpreadsheetUtils {

    public static String toSheetName(String name) {
        if (name == null) {
            throw new NullPointerException("Name must not be null.");
        }
        String erg = name.trim();
        erg = erg.replace('/', '-');
        erg = erg.replace('\\', '-');
        erg = erg.replace('[', '(');
        erg = erg.replace(']', ')');
        erg = erg.replace('*', '-');
        erg = erg.replace('?', '-');
        erg = erg.replace(':', '-');
        if (erg.length() > 31) {
            erg = erg.substring(0, 31);
        }
        return erg.trim();
    }

    public static short guessMaximumWidth(String[] s) {
        BufferedImage bi = new BufferedImage(50, 50, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = bi.getGraphics();
        try {
            Font arial = new Font("Arial", Font.PLAIN, 10);
            g.setFont(arial);
        } catch (Exception e) {
            try {
                // Conservative quessing
                Font arial = new Font("Courier", Font.PLAIN, 10);
                g.setFont(arial);
            } catch (Exception ex) {
                // Arial and Courier cannot be used. Stick to default font.
            }
        }
        FontMetrics fm = g.getFontMetrics();
        short max = 0;
        for (String value : s) {
            max = (short) Math.max(max, fm.stringWidth(value));
        }
        return (short) (max * 45 + 300);
    }

}
