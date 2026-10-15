package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.layout.FormLayoutUtils;

/**
 * A table for printing made of labels, so that it can be placed in a {@code ComponentListPrintable2} together with other
 * components.
 */
public final class PseudoTable {

    public static final int NO_HIDDEN_COLUMN = -1;

    private PseudoTable() {
    }

    public static JComponent create(ExtendedTableModel etm, Font font, int hiddenColumn) {
        StringBuilder columns = new StringBuilder("0dlu");
        for (int y = 0; y < etm.getColumnCount(); y++) {
            columns.append(y == hiddenColumn ? ",0dlu" : ",fill:default:grow").append(",0dlu");
        }
        JPanel p = new JPanel(
                new FormLayout(columns.toString(), FormLayoutUtils.createLayoutString(etm.getRowCount() + 1, 1, 0))) {
            @Override
            public void setFont(Font f) {
                super.setFont(f);
                if (f != null) {
                    for (Component c : getComponents()) {
                        c.setFont(f);
                    }
                }
            }

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                if (bg != null) {
                    for (Component c : getComponents()) {
                        c.setBackground(bg);
                    }
                }
            }

            @Override
            public void setForeground(Color fg) {
                super.setForeground(fg);
                if (fg != null) {
                    for (Component c : getComponents()) {
                        c.setForeground(fg);
                    }
                }
            }
        };
        p.setBackground(Color.WHITE);

        Color marker = JPrintTable.getRowMarkerColor();

        for (int y = 0; y < etm.getColumnCount(); y++) {
            JLabel l1 = new JLabel(etm.getColumnName(y));
            l1.setHorizontalAlignment(SwingConstants.CENTER);
            l1.setOpaque(true);
            l1.setBackground(marker);
            l1.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
            p.add(l1, CC.xy(2 + 2 * y, 2));
        }

        for (int x = 0; x < etm.getRowCount(); x++) {
            Color background = x % 2 != 0 ? marker : Color.WHITE;
            for (int y = 0; y < etm.getColumnCount(); y++) {
                JLabel l1 = new JLabel(etm.getValueAt(x, y).toString());
                l1.setHorizontalAlignment(etm.getColumnAlignment(y));
                l1.setOpaque(true);
                l1.setBackground(background);
                p.add(l1, CC.xy(2 + 2 * y, 4 + 2 * x));

                JLabel l2 = new JLabel(" ");
                l2.setOpaque(true);
                l2.setBackground(background);
                p.add(l2, CC.xy(2 + 2 * y, 4 + 2 * x - 1));
            }
        }

        p.setFont(font);
        p.setForeground(Color.BLACK);

        p.setBorder(new LineBorder(Color.BLACK, 1));
        return p;
    }
}
