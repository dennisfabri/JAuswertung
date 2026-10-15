package de.df.jauswertung.print.urkunden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

class UrkundenSeiteTest {

    private final UrkundenSeite seite = new UrkundenSeite();

    private UrkundenFeld feld(int x, int y, int width, int height) {
        return seite.addFeld(new Rectangle(x, y, width, height), "Text", new Font("Dialog", Font.PLAIN, 12),
                SwingConstants.CENTER);
    }

    @Test
    void addedFieldsAreKeptInOrder() {
        UrkundenFeld first = feld(10, 10, 100, 20);
        UrkundenFeld second = feld(10, 40, 100, 20);

        assertEquals(List.of(first, second), seite.getFelder());
    }

    @Test
    void fieldAtReturnsTopmostFieldContainingThePoint() {
        UrkundenFeld below = feld(0, 0, 100, 100);
        UrkundenFeld above = feld(50, 50, 100, 100);

        assertSame(above, seite.getFeldAt(60, 60));
        assertSame(below, seite.getFeldAt(10, 10));
        assertNull(seite.getFeldAt(300, 300));
    }

    @Test
    void removingFieldsAlsoRemovesThemFromTheSelection() {
        UrkundenFeld a = feld(0, 0, 100, 20);
        UrkundenFeld b = feld(0, 30, 100, 20);
        seite.select(a, false);
        seite.select(b, true);

        seite.removeSelected();

        assertTrue(seite.getFelder().isEmpty());
        assertTrue(seite.getSelection().isEmpty());
    }

    @Test
    void selectWithoutExtendReplacesTheSelection() {
        UrkundenFeld a = feld(0, 0, 100, 20);
        UrkundenFeld b = feld(0, 30, 100, 20);

        seite.select(a, false);
        seite.select(b, false);

        assertEquals(List.of(b), seite.getSelection());
    }

    @Test
    void selectWithExtendTogglesTheField() {
        UrkundenFeld a = feld(0, 0, 100, 20);
        UrkundenFeld b = feld(0, 30, 100, 20);

        seite.select(a, false);
        seite.select(b, true);
        seite.select(a, true);

        assertEquals(List.of(b), seite.getSelection());
    }

    @Test
    void snapRoundsToTheGridWhenEnabled() {
        seite.setGridSize(10);
        seite.setGridEnabled(true);
        assertEquals(20, seite.snap(16));
        assertEquals(10, seite.snap(14));

        seite.setGridEnabled(false);
        assertEquals(16, seite.snap(16));
    }

    @Test
    void textIsPaintedInsideTheFieldOnly() {
        seite.setSize(200, 100);
        seite.addFeld(new Rectangle(20, 20, 100, 40), "WWWW", new Font("Dialog", Font.BOLD, 20), SwingConstants.LEFT);

        BufferedImage image = paint();

        assertTrue(hasInk(image, new Rectangle(20, 20, 100, 40)));
        assertTrue(!hasInk(image, new Rectangle(130, 0, 70, 100)));
        assertTrue(!hasInk(image, new Rectangle(0, 0, 20, 100)));
    }

    @Test
    void alignmentMovesTheTextWithinTheField() {
        seite.setSize(300, 60);
        UrkundenFeld feld = seite.addFeld(new Rectangle(0, 0, 300, 60), "WW", new Font("Dialog", Font.BOLD, 20),
                SwingConstants.LEFT);

        int left = firstInkColumn(paint());
        feld.setAlignment(SwingConstants.CENTER);
        int center = firstInkColumn(paint());
        feld.setAlignment(SwingConstants.RIGHT);
        int right = firstInkColumn(paint());

        assertTrue(left < center, "left " + left + " center " + center);
        assertTrue(center < right, "center " + center + " right " + right);
    }

    @Test
    void multipleLinesAreStackedAndCenteredVertically() {
        seite.setSize(200, 200);
        seite.addFeld(new Rectangle(0, 0, 200, 200), "I\nI\nI", new Font("Dialog", Font.BOLD, 20),
                SwingConstants.CENTER);

        BufferedImage image = paint();

        assertTrue(hasInk(image, new Rectangle(0, 60, 200, 80)));
        assertTrue(!hasInk(image, new Rectangle(0, 0, 200, 40)));
        assertTrue(!hasInk(image, new Rectangle(0, 160, 200, 40)));
    }

    private BufferedImage paint() {
        BufferedImage image = new BufferedImage(seite.getWidth(), seite.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        seite.paintContent(g);
        g.dispose();
        return image;
    }

    private static boolean hasInk(BufferedImage image, Rectangle area) {
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int firstInkColumn(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            if (hasInk(image, new Rectangle(x, 0, 1, image.getHeight()))) {
                return x;
            }
        }
        return -1;
    }
}
