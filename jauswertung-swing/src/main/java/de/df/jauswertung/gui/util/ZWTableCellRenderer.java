/**
 * 
 */
package de.df.jauswertung.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;

public final class ZWTableCellRenderer extends AlignmentCellRenderer {

    public static boolean compressLists = true;

    private boolean border = true;

    public ZWTableCellRenderer() {
        this(true);
    }

    public ZWTableCellRenderer(boolean border) {
        super(new int[] { SwingConstants.RIGHT, SwingConstants.LEFT },
                Utils.getPreferences().getBoolean("HHListLeftAlign", false) ? SwingConstants.LEFT
                        : SwingConstants.CENTER);
        this.border = border;
    }

    public String[] getTableRenderData(ASchwimmer swimmer) {
        String[] result = getTableRenderDataI(swimmer);
        for (int x = 0; x < result.length; x++) {
            result[x] = result[x].replace("  ", " ");
        }
        if (compressLists) {
            while ((result.length > 2) && (result[0].indexOf(result[1]) >= 0)) {
                String[] r = new String[result.length - 1];
                r[0] = result[0];
                System.arraycopy(result, 2, r, 1, result.length - 2);
                result = r;
            }
        }
        return result;
    }

    private String[] getTableRenderDataI(ASchwimmer swimmer) {
        if (swimmer == null) {
            return new String[] { " ", " " };
        }
        return new String[] {
                I18n.get("ZWTableLine1", swimmer.getName(), swimmer.getGliederung(), swimmer.getAK().getName(),
                        I18n.geschlechtToShortString(swimmer),
                        StartnumberFormatManager.format(swimmer)),
                I18n.get("ZWTableLine2", swimmer.getName(), swimmer.getGliederung(), swimmer.getAK().getName(),
                        I18n.geschlechtToShortString(swimmer),
                        StartnumberFormatManager.format(swimmer)),
                I18n.get("ZWTableLine3", swimmer.getName(), swimmer.getGliederung(), swimmer.getAK().getName(),
                        I18n.geschlechtToShortString(swimmer),
                        StartnumberFormatManager.format(swimmer)) };
    }

    @Override
    public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        ASchwimmer s = null;
        if (value instanceof ASchwimmer) {
            s = (ASchwimmer) value;
            value = getTableRenderData(s);
        }

        Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);

        if (c instanceof JComponent) {
            JComponent jc = ((JComponent) c);
            jc.setOpaque(true);
            if (!isSelected) {
                jc.setBackground(JTableUtils.getEvenDefault());
            }
            if (border) {
                jc.setBorder(new CompoundBorder(
                        new ExtendedLineBorder(Color.BLACK, 0, 0, (row + 1 < t.getRowCount() ? 1 : 0),
                                (column + 1 < t.getColumnCount() ? 1 : 0)),
                        new EmptyBorder(0, 1, 0, 1)));
            }
            jc.setOpaque(true);
        }
        if (!isSelected) {
            if (s != null) {
                if (s.isMaennlich()) {
                    c.setBackground(JTableUtils.getOddDefault());
                } else {
                    c.setBackground(JTableUtils.getEvenDefault());
                }
            }
        }

        int low = 0;
        int right = 0;
        if (t.getShowHorizontalLines()) {
            if (column + 1 < t.getColumnCount()) {
                right = 1;
            }
        }
        if (t.getShowVerticalLines()) {
            if (row + 1 < t.getRowCount()) {
                low = 1;
            }
        }
        right = 0;
        low = 0;

        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setSize(c.getWidth() + right, c.getHeight() + low);
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(0, 0, low, right));

        c = p;

        return c;
    }
}