package de.df.jauswertung.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Font;
import java.awt.Rectangle;
import java.util.Hashtable;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.print.graphics.Dimension;
import de.df.jauswertung.print.graphics.Point;
import de.df.jauswertung.print.urkunden.UrkundenFeld;
import de.df.jauswertung.print.urkunden.UrkundenSeite;

class GraphUtilsTest {

    private static Hashtable<String, Object> cell(Object fontstyle, Object fontsize, Object alignment, String text) {
        Hashtable<String, Object> ht = new Hashtable<>();
        ht.put("offset", new Point(10, 20));
        ht.put("size", new Dimension(300, 40));
        ht.put("fontname", "Dialog");
        ht.put("fontstyle", fontstyle);
        ht.put("fontsize", fontsize);
        ht.put("alignment", alignment);
        ht.put("text", text);
        return ht;
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, Object>[] cells(Hashtable<String, Object>... cells) {
        return cells;
    }

    @Test
    void populateCreatesFieldsFromStoredCells() {
        UrkundenSeite seite = new UrkundenSeite();

        GraphUtils.populateGraph(seite, cells(cell(Font.BOLD, 18, SwingConstants.RIGHT, "Hallo")), null, null);

        UrkundenFeld feld = seite.getFelder().get(0);
        assertEquals(new Rectangle(10, 20, 300, 40), feld.getBounds());
        assertEquals("Hallo", feld.getText());
        assertEquals(new Font("Dialog", Font.BOLD, 18), feld.getFont());
        assertEquals(SwingConstants.RIGHT, feld.getAlignment());
    }

    @Test
    void populateAcceptsNumbersStoredAsText() {
        UrkundenSeite seite = new UrkundenSeite();

        GraphUtils.populateGraph(seite, cells(cell("1", "14", "2", "Alt")), null, null);

        UrkundenFeld feld = seite.getFelder().get(0);
        assertEquals(new Font("Dialog", Font.BOLD, 14), feld.getFont());
        assertEquals(SwingConstants.LEFT, feld.getAlignment());
    }

    @Test
    void populateReplacesPlaceholdersCaseInsensitive() {
        UrkundenSeite seite = new UrkundenSeite();

        GraphUtils.populateGraph(seite, cells(cell(Font.PLAIN, 12, SwingConstants.CENTER, "Platz <PLATZ> für <name>")),
                new String[] { "<platz>", "<name>" }, new String[] { "3", "Max" });

        assertEquals("Platz 3 für Max", seite.getFelder().get(0).getText());
    }

    @Test
    void populateFallsBackToDefaultsForUnreadableValues() {
        UrkundenSeite seite = new UrkundenSeite();

        GraphUtils.populateGraph(seite, cells(cell("x", "y", "z", "T")), null, null);

        UrkundenFeld feld = seite.getFelder().get(0);
        assertEquals(Font.PLAIN, feld.getFont().getStyle());
        assertEquals(12, feld.getFont().getSize());
        assertEquals(SwingConstants.CENTER, feld.getAlignment());
    }

    @Test
    void collectReturnsStoredCellFormat() {
        UrkundenSeite seite = new UrkundenSeite();
        seite.addFeld(new Rectangle(5, 6, 70, 80), "Text", new Font("Dialog", Font.ITALIC, 20), SwingConstants.LEFT);

        Hashtable<String, Object> ht = GraphUtils.collectGraph(seite)[0];

        Point offset = (Point) ht.get("offset");
        Dimension size = (Dimension) ht.get("size");
        assertEquals(5, offset.x);
        assertEquals(6, offset.y);
        assertEquals(70, size.width);
        assertEquals(80, size.height);
        assertEquals("Dialog", ht.get("fontname"));
        assertEquals(Font.ITALIC, ht.get("fontstyle"));
        assertEquals(20, Integer.parseInt(ht.get("fontsize").toString()));
        assertEquals(SwingConstants.LEFT, ht.get("alignment"));
        assertEquals("Text", ht.get("text"));
    }

    @Test
    void collectOfAnEmptyPageIsNull() {
        assertNull(GraphUtils.collectGraph(new UrkundenSeite()));
    }

    @Test
    void collectedCellsCanBeLoadedAgain() {
        UrkundenSeite original = new UrkundenSeite();
        original.addFeld(new Rectangle(1, 2, 300, 40), "A\nB", new Font("Dialog", Font.BOLD + Font.ITALIC, 11),
                SwingConstants.RIGHT);
        UrkundenSeite copy = new UrkundenSeite();

        GraphUtils.populateGraph(copy, GraphUtils.collectGraph(original), null, null);

        UrkundenFeld a = original.getFelder().get(0);
        UrkundenFeld b = copy.getFelder().get(0);
        assertEquals(a.getBounds(), b.getBounds());
        assertEquals(a.getText(), b.getText());
        assertEquals(a.getFont(), b.getFont());
        assertEquals(a.getAlignment(), b.getAlignment());
    }

    @Test
    void newFieldWithNegativeSizeIsNormalizedAndTooSmallFieldsGetADefaultSize() {
        UrkundenSeite seite = new UrkundenSeite();

        GraphUtils.addTextfield(seite, 100, 100, -50, -30, "T");
        GraphUtils.addTextfield(seite, 0, 0, 3, 3, "T");

        assertEquals(new Rectangle(50, 70, 50, 30), seite.getFelder().get(0).getBounds());
        assertEquals(new Rectangle(0, 0, 100, 100), seite.getFelder().get(1).getBounds());
    }
}
