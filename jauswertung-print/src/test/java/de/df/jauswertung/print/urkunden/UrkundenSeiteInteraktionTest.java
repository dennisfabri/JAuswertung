package de.df.jauswertung.print.urkunden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UrkundenSeiteInteraktionTest {

    private final UrkundenSeite seite = new UrkundenSeite();
    private final UrkundenSeiteInteraktion interaktion = new UrkundenSeiteInteraktion(seite);
    private final List<Rectangle> created = new ArrayList<>();
    private final List<Point> popups = new ArrayList<>();
    private UrkundenFeld feld;

    @BeforeEach
    void setUp() {
        seite.setSize(500, 500);
        seite.setEditing(true);
        interaktion.setNewFieldListener(created::add);
        interaktion.setPopupListener((x, y) -> popups.add(new Point(x, y)));
        feld = seite.addFeld(new Rectangle(100, 100, 100, 50), "Text", new Font("Dialog", Font.PLAIN, 12),
                SwingConstants.CENTER);
    }

    private void press(int x, int y, int modifiers) {
        interaktion.mousePressed(event(MouseEvent.MOUSE_PRESSED, x, y, modifiers, false));
    }

    private void drag(int x, int y) {
        interaktion.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, x, y, InputEvent.BUTTON1_DOWN_MASK, false));
    }

    private void release(int x, int y) {
        interaktion.mouseReleased(event(MouseEvent.MOUSE_RELEASED, x, y, 0, false));
    }

    private MouseEvent event(int id, int x, int y, int modifiers, boolean popup) {
        int mods = id == MouseEvent.MOUSE_PRESSED ? modifiers | InputEvent.BUTTON1_DOWN_MASK : modifiers;
        return new MouseEvent(seite, id, 0, mods, x, y, 1, popup, MouseEvent.BUTTON1);
    }

    @Test
    void clickSelectsTheFieldUnderTheMouse() {
        press(120, 120, 0);
        release(120, 120);

        assertEquals(List.of(feld), seite.getSelection());
    }

    @Test
    void clickOnEmptySpaceClearsTheSelection() {
        seite.select(feld, false);

        press(400, 400, 0);
        release(400, 400);

        assertTrue(seite.getSelection().isEmpty());
    }

    @Test
    void controlClickAddsToTheSelection() {
        UrkundenFeld other = seite.addFeld(new Rectangle(300, 300, 50, 50), "B", feld.getFont(),
                SwingConstants.CENTER);
        press(120, 120, 0);
        release(120, 120);

        press(310, 310, InputEvent.CTRL_DOWN_MASK);
        release(310, 310);

        assertEquals(List.of(feld, other), seite.getSelection());
    }

    @Test
    void draggingMovesTheSelectedFieldByTheMouseDistance() {
        press(120, 120, 0);
        drag(150, 130);
        release(150, 130);

        assertEquals(new Rectangle(130, 110, 100, 50), feld.getBounds());
    }

    @Test
    void movingSnapsToTheGrid() {
        seite.setGridSize(10);
        seite.setGridEnabled(true);

        press(120, 120, 0);
        drag(127, 123);
        release(127, 123);

        assertEquals(new Rectangle(110, 100, 100, 50), feld.getBounds());
    }

    @Test
    void draggingTheSouthEastHandleResizesTheField() {
        seite.select(feld, false);

        press(200, 150, 0);
        drag(240, 190);
        release(240, 190);

        assertEquals(new Rectangle(100, 100, 140, 90), feld.getBounds());
    }

    @Test
    void draggingTheNorthWestHandleMovesTheCornerAndKeepsTheOppositeOne() {
        seite.select(feld, false);

        press(100, 100, 0);
        drag(90, 80);
        release(90, 80);

        assertEquals(new Rectangle(90, 80, 110, 70), feld.getBounds());
    }

    @Test
    void aFieldCannotBeResizedBelowAMinimumSize() {
        seite.select(feld, false);

        press(200, 150, 0);
        drag(0, 0);
        release(0, 0);

        assertEquals(new Rectangle(100, 100, 10, 10), feld.getBounds());
    }

    @Test
    void rubberBandSelectsFieldsItTouches() {
        UrkundenFeld other = seite.addFeld(new Rectangle(300, 300, 50, 50), "B", feld.getFont(),
                SwingConstants.CENTER);

        press(50, 50, 0);
        drag(130, 130);
        release(130, 130);

        assertEquals(List.of(feld), seite.getSelection());
        assertTrue(!seite.getSelection().contains(other));
    }

    @Test
    void inAddModeDraggingReportsTheNewRectangleInsteadOfSelecting() {
        interaktion.setAddMode(true);

        press(300, 300, 0);
        drag(360, 340);
        release(360, 340);

        assertEquals(List.of(new Rectangle(300, 300, 60, 40)), created);
        assertTrue(seite.getSelection().isEmpty());
        assertEquals(new Rectangle(100, 100, 100, 50), feld.getBounds());
    }

    @Test
    void deleteKeyRemovesTheSelectedFields() {
        seite.select(feld, false);

        interaktion.keyPressed(new KeyEvent(seite, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DELETE,
                KeyEvent.CHAR_UNDEFINED));

        assertTrue(seite.getFelder().isEmpty());
    }

    @Test
    void popupTriggerReportsThePositionAndSelectsTheFieldUnderIt() {
        interaktion.mousePressed(event(MouseEvent.MOUSE_PRESSED, 120, 120, 0, true));

        assertEquals(List.of(new Point(120, 120)), popups);
        assertEquals(List.of(feld), seite.getSelection());
    }
}
