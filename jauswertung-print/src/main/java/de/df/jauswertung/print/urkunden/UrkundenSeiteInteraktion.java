package de.df.jauswertung.print.urkunden;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class UrkundenSeiteInteraktion extends MouseAdapter {

    private static final int MIN_SIZE = 10;
    private static final int PREVIEW_ALPHA = 60;

    private final UrkundenSeite seite;

    private boolean addMode = false;
    private Consumer<Rectangle> newFieldListener = r -> {
    };
    private BiConsumer<Integer, Integer> popupListener = (x, y) -> {
    };

    private int startX;
    private int startY;
    private int currentX;
    private int currentY;
    private int resizeHandle = -1;
    private boolean moving = false;
    private boolean rubberBand = false;
    private boolean creating = false;
    private final Map<UrkundenFeld, Rectangle> origins = new IdentityHashMap<>();

    private JScrollPane editor;
    private UrkundenFeld edited;

    public UrkundenSeiteInteraktion(UrkundenSeite seite) {
        this.seite = seite;
        seite.addMouseListener(this);
        seite.addMouseMotionListener(this);
        seite.setFocusable(true);
        seite.setOverlay(this);
        seite.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                UrkundenSeiteInteraktion.this.keyPressed(e);
            }
        });
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
    }

    public void setNewFieldListener(Consumer<Rectangle> newFieldListener) {
        this.newFieldListener = newFieldListener;
    }

    public void setPopupListener(BiConsumer<Integer, Integer> popupListener) {
        this.popupListener = popupListener;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            seite.removeSelected();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        commitEdit();
        seite.requestFocusInWindow();
        startX = e.getX();
        startY = e.getY();
        currentX = startX;
        currentY = startY;

        if (e.isPopupTrigger()) {
            UrkundenFeld feld = seite.getFeldAt(startX, startY);
            if (feld != null && !seite.getSelection().contains(feld)) {
                seite.select(feld, false);
            }
            popupListener.accept(startX, startY);
            return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (addMode) {
            creating = true;
            return;
        }

        resizeHandle = handleAt(startX, startY);
        if (resizeHandle >= 0) {
            rememberOrigins();
            return;
        }

        UrkundenFeld feld = seite.getFeldAt(startX, startY);
        if (feld == null) {
            if (!e.isControlDown() && !e.isShiftDown()) {
                seite.clearSelection();
            }
            rubberBand = true;
            return;
        }
        boolean extend = e.isControlDown() || e.isShiftDown();
        if (!seite.getSelection().contains(feld) || extend) {
            seite.select(feld, extend);
        }
        if (seite.getSelection().contains(feld)) {
            moving = true;
            rememberOrigins();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentX = e.getX();
        currentY = e.getY();
        if (resizeHandle >= 0) {
            resize();
        } else if (moving) {
            move();
        } else if (rubberBand || creating) {
            seite.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentX = e.getX();
        currentY = e.getY();
        if (rubberBand) {
            selectInRubberBand();
        } else if (creating) {
            newFieldListener.accept(dragRectangle());
        }
        if (e.isPopupTrigger() && !moving && resizeHandle < 0) {
            popupListener.accept(currentX, currentY);
        }
        resizeHandle = -1;
        moving = false;
        rubberBand = false;
        creating = false;
        origins.clear();
        seite.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            UrkundenFeld feld = seite.getFeldAt(e.getX(), e.getY());
            if (feld != null) {
                startEdit(feld);
            }
        }
    }

    public void paintOverlay(Graphics g) {
        if (!rubberBand && !creating) {
            return;
        }
        Rectangle r = dragRectangle();
        g.setColor(new Color(0x1E, 0x64, 0xC8, PREVIEW_ALPHA));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(new Color(0x1E, 0x64, 0xC8));
        g.drawRect(r.x, r.y, r.width, r.height);
    }

    private Rectangle dragRectangle() {
        int x = Math.min(startX, currentX);
        int y = Math.min(startY, currentY);
        return new Rectangle(x, y, Math.abs(currentX - startX), Math.abs(currentY - startY));
    }

    private int handleAt(int x, int y) {
        for (UrkundenFeld feld : seite.getSelection()) {
            Rectangle[] handles = UrkundenSeite.handlesOf(feld.getBounds());
            for (int i = 0; i < handles.length; i++) {
                if (handles[i].contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void rememberOrigins() {
        origins.clear();
        for (UrkundenFeld feld : seite.getSelection()) {
            origins.put(feld, new Rectangle(feld.getBounds()));
        }
    }

    private void move() {
        int dx = currentX - startX;
        int dy = currentY - startY;
        for (Map.Entry<UrkundenFeld, Rectangle> entry : origins.entrySet()) {
            Rectangle o = entry.getValue();
            entry.getKey().setBounds(new Rectangle(seite.snap(o.x + dx), seite.snap(o.y + dy), o.width, o.height));
        }
        seite.repaint();
    }

    private void resize() {
        int dx = currentX - startX;
        int dy = currentY - startY;
        boolean north = resizeHandle == 0 || resizeHandle == 1 || resizeHandle == 2;
        boolean east = resizeHandle == 2 || resizeHandle == 3 || resizeHandle == 4;
        boolean south = resizeHandle == 4 || resizeHandle == 5 || resizeHandle == 6;
        boolean west = resizeHandle == 6 || resizeHandle == 7 || resizeHandle == 0;
        for (Map.Entry<UrkundenFeld, Rectangle> entry : origins.entrySet()) {
            Rectangle o = entry.getValue();
            int left = o.x;
            int top = o.y;
            int right = o.x + o.width;
            int bottom = o.y + o.height;
            if (west) {
                left = Math.min(seite.snap(o.x + dx), right - MIN_SIZE);
            }
            if (east) {
                right = Math.max(seite.snap(o.x + o.width + dx), left + MIN_SIZE);
            }
            if (north) {
                top = Math.min(seite.snap(o.y + dy), bottom - MIN_SIZE);
            }
            if (south) {
                bottom = Math.max(seite.snap(o.y + o.height + dy), top + MIN_SIZE);
            }
            entry.getKey().setBounds(new Rectangle(left, top, right - left, bottom - top));
        }
        seite.repaint();
    }

    private void selectInRubberBand() {
        Rectangle band = dragRectangle();
        List<UrkundenFeld> hit = new ArrayList<>(seite.getSelection());
        for (UrkundenFeld feld : seite.getFelder()) {
            if (feld.getBounds().intersects(band) && !hit.contains(feld)) {
                hit.add(feld);
            }
        }
        seite.selectAll(hit);
    }

    private void startEdit(UrkundenFeld feld) {
        commitEdit();
        edited = feld;
        JTextArea area = new JTextArea(feld.getText());
        area.setFont(feld.getFont());
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitEdit();
            }
        });
        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEdit();
                }
            }
        });
        editor = new JScrollPane(area);
        editor.setBounds(feld.getBounds());
        seite.setLayout(null);
        seite.add(editor);
        seite.revalidate();
        area.requestFocusInWindow();
        area.selectAll();
    }

    private void commitEdit() {
        if (editor == null) {
            return;
        }
        JTextArea area = (JTextArea) editor.getViewport().getView();
        edited.setText(area.getText());
        cancelEdit();
    }

    private void cancelEdit() {
        if (editor == null) {
            return;
        }
        seite.remove(editor);
        editor = null;
        edited = null;
        seite.requestFocusInWindow();
        seite.repaint();
    }
}
