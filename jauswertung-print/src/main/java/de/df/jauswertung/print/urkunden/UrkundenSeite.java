package de.df.jauswertung.print.urkunden;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

public class UrkundenSeite extends JComponent {

    private static final int TEXT_INSET = 2;
    private static final int HANDLE_SIZE = 7;
    private static final Color SELECTION_COLOR = new Color(0x1E64C8);

    private final List<UrkundenFeld> felder = new ArrayList<>();
    private final List<UrkundenFeld> selection = new ArrayList<>();

    private int gridSize = 1;
    private boolean gridEnabled = false;
    private boolean editing = false;
    private UrkundenSeiteInteraktion overlay;

    public UrkundenSeite() {
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    public UrkundenFeld addFeld(Rectangle bounds, String text, Font font, int alignment) {
        UrkundenFeld feld = new UrkundenFeld(new Rectangle(bounds), text, font, alignment);
        felder.add(feld);
        repaint();
        return feld;
    }

    public List<UrkundenFeld> getFelder() {
        return Collections.unmodifiableList(felder);
    }

    public UrkundenFeld getFeldAt(int x, int y) {
        for (int i = felder.size() - 1; i >= 0; i--) {
            if (felder.get(i).getBounds().contains(x, y)) {
                return felder.get(i);
            }
        }
        return null;
    }

    public void clear() {
        felder.clear();
        selection.clear();
        repaint();
    }

    public void removeSelected() {
        felder.removeAll(selection);
        selection.clear();
        repaint();
    }

    public List<UrkundenFeld> getSelection() {
        return Collections.unmodifiableList(selection);
    }

    public void select(UrkundenFeld feld, boolean extend) {
        if (!extend) {
            selection.clear();
            selection.add(feld);
        } else if (!selection.remove(feld)) {
            selection.add(feld);
        }
        repaint();
    }

    public void selectAll(List<UrkundenFeld> neu) {
        selection.clear();
        selection.addAll(neu);
        repaint();
    }

    public void clearSelection() {
        selection.clear();
        repaint();
    }

    public void setGridSize(int gridSize) {
        this.gridSize = Math.max(1, gridSize);
    }

    public void setGridEnabled(boolean gridEnabled) {
        this.gridEnabled = gridEnabled;
    }

    public int snap(int value) {
        if (!gridEnabled) {
            return value;
        }
        return (int) (Math.round((double) value / gridSize) * gridSize);
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        repaint();
    }

    public boolean isEditing() {
        return editing;
    }

    public void setPageSize(Dimension size) {
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            paintContent(g2d);
            if (overlay != null) {
                overlay.paintOverlay(g2d);
            }
        } finally {
            g2d.dispose();
        }
    }

    void setOverlay(UrkundenSeiteInteraktion overlay) {
        this.overlay = overlay;
    }

    public void paintContent(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        for (UrkundenFeld feld : felder) {
            if (editing) {
                paintOutline(g2d, feld);
            }
            paintText(g2d, feld);
        }
        if (editing) {
            for (UrkundenFeld feld : selection) {
                paintHandles(g2d, feld);
            }
        }
    }

    private static void paintOutline(Graphics2D g2d, UrkundenFeld feld) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 4f, 3f }, 0f));
        Rectangle r = feld.getBounds();
        g.drawRect(r.x, r.y, r.width, r.height);
        g.dispose();
    }

    private static void paintText(Graphics2D g2d, UrkundenFeld feld) {
        String text = feld.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Graphics2D g = (Graphics2D) g2d.create();
        try {
            g.setFont(feld.getFont());
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            String[] lines = text.split("\n", -1);
            Rectangle r = feld.getBounds();
            int blockHeight = lines.length * fm.getHeight();
            int baseline = r.y + (r.height - blockHeight) / 2 + fm.getAscent();
            for (String line : lines) {
                g.drawString(line, lineX(fm, line, r, feld.getAlignment()), baseline);
                baseline += fm.getHeight();
            }
        } finally {
            g.dispose();
        }
    }

    private static int lineX(FontMetrics fm, String line, Rectangle r, int alignment) {
        int width = fm.stringWidth(line);
        return switch (alignment) {
        case SwingConstants.LEFT -> r.x + TEXT_INSET;
        case SwingConstants.RIGHT -> r.x + r.width - TEXT_INSET - width;
        default -> r.x + (r.width - width) / 2;
        };
    }

    public static Rectangle[] handlesOf(Rectangle r) {
        int h = HANDLE_SIZE;
        int left = r.x - h / 2;
        int centerX = r.x + r.width / 2 - h / 2;
        int right = r.x + r.width - h / 2;
        int top = r.y - h / 2;
        int centerY = r.y + r.height / 2 - h / 2;
        int bottom = r.y + r.height - h / 2;
        return new Rectangle[] { new Rectangle(left, top, h, h), new Rectangle(centerX, top, h, h),
                new Rectangle(right, top, h, h), new Rectangle(right, centerY, h, h),
                new Rectangle(right, bottom, h, h), new Rectangle(centerX, bottom, h, h),
                new Rectangle(left, bottom, h, h), new Rectangle(left, centerY, h, h) };
    }

    private static void paintHandles(Graphics2D g2d, UrkundenFeld feld) {
        Graphics2D g = (Graphics2D) g2d.create();
        Rectangle r = feld.getBounds();
        g.setColor(SELECTION_COLOR);
        g.drawRect(r.x, r.y, r.width, r.height);
        for (Rectangle handle : handlesOf(r)) {
            g.setColor(Color.WHITE);
            g.fillRect(handle.x, handle.y, handle.width, handle.height);
            g.setColor(SELECTION_COLOR);
            g.drawRect(handle.x, handle.y, handle.width, handle.height);
        }
        g.dispose();
    }
}
