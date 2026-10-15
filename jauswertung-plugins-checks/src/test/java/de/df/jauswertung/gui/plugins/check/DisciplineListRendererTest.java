package de.df.jauswertung.gui.plugins.check;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;

import org.junit.jupiter.api.Test;

import de.df.jutils.gui.border.BorderUtils;

class DisciplineListRendererTest {

    private final JList<String> list = new JList<>(new String[] { "50m Retten", "100m Kombi" });
    private final DisciplineListRenderer renderer = new DisciplineListRenderer();

    private JLabel render(String value, int index, boolean selected) {
        Component c = renderer.getListCellRendererComponent(list, value, index, selected, false);
        return (JLabel) c;
    }

    @Test
    void theValueIsShownAsText() {
        assertEquals("50m Retten", render("50m Retten", 0, false).getText());
    }

    @Test
    void nothingIsShownForNoValue() {
        assertEquals("", render(null, 0, false).getText());
    }

    @Test
    void entriesHaveASpaceBorder() {
        JLabel label = render("50m Retten", 0, false);

        assertEquals(BorderUtils.createSpaceBorder(2).getBorderInsets(label), label.getBorder().getBorderInsets(label));
    }

    @Test
    void theBorderStaysTheSameWhenTheEntryHasFocus() {
        JLabel label = renderer.getListCellRendererComponent(list, "50m Retten", 0, false, true) instanceof JLabel l ? l
                : null;

        assertEquals(BorderUtils.createSpaceBorder(2).getBorderInsets(label), label.getBorder().getBorderInsets(label));
    }

    @Test
    void selectedEntriesUseTheSelectionColors() {
        list.setSelectionBackground(new Color(10, 20, 30));
        list.setSelectionForeground(new Color(200, 210, 220));

        JLabel label = render("50m Retten", 0, true);

        assertEquals(new Color(10, 20, 30), label.getBackground());
        assertEquals(new Color(200, 210, 220), label.getForeground());
    }

    @Test
    void otherEntriesUseTheListColors() {
        list.setBackground(new Color(1, 2, 3));
        list.setForeground(new Color(4, 5, 6));

        JLabel label = render("50m Retten", 0, false);

        assertEquals(new Color(1, 2, 3), label.getBackground());
        assertEquals(new Color(4, 5, 6), label.getForeground());
    }
}
