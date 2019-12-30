package de.df.jauswertung.gui.plugins.heatsview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.print.PrintManager;

public class JHeatPresentationFrame<T extends ASchwimmer> extends JFrame {

    int                   counter    = 0;
    int                   index      = 0;
    int                   page       = 0;

    boolean               mirror     = false;
    boolean               blackWhite = true;
    private Color         background = Color.BLACK;
    private Color         foreground = Color.WHITE;

    private Painter       painter    = null;

    AWettkampf<T>         wk         = null;
    BufferStrategy        buffer     = null;

    private Component     panel      = null;
    private BufferedImage image      = null;

    public JHeatPresentationFrame(JFrame parent, AWettkampf<T> wkx) {
        super(I18n.get("Heatspresentation"));
        this.wk = wkx;

        setIconImages(IconManager.getTitleImages());

        setLayout(new FormLayout("0px,fill:default:grow,0px", "0px,fill:default:grow,0px"));

        panel = getContentPane();
        panel.setBackground(Color.BLACK);

        toggleBlackWhite();

        pack();

        setSize(400, 250);
        WindowUtils.center(this, parent);

        panel.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                image = null;
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        addActions();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    next();
                } else {
                    previous();
                }
            }
        });
    }

    private void addActions() {
        Runnable full = new Runnable() {
            @Override
            public void run() {
                full();
            }
        };
        Runnable full1 = new Runnable() {
            @Override
            public void run() {
                full(0);
            }
        };
        Runnable full2 = new Runnable() {
            @Override
            public void run() {
                full(1);
            }
        };
        Runnable next = new Runnable() {
            @Override
            public void run() {
                next();
            }
        };
        Runnable prev = new Runnable() {
            @Override
            public void run() {
                previous();
            }
        };
        Runnable switchMirror = new Runnable() {
            @Override
            public void run() {
                mirror = !mirror;
                paint();
            }
        };
        Runnable toggle = new Runnable() {
            @Override
            public void run() {
                toggleBlackWhite();
            }
        };
        Runnable close = new Runnable() {
            @Override
            public void run() {
                if (screen != null) {
                    full();
                }
                setVisible(false);
                try {
                    dispose();
                } catch (Exception ex) {
                    // Nothing to do
                }
            }
        };
        Runnable switchPage = new Runnable() {
            @Override
            public void run() {
                switchPage();
            }
        };

        WindowUtils.addEscapeAction(this, close);
        WindowUtils.addEnterAction(this, next);
        WindowUtils.addAction(this, prev, KeyEvent.VK_BACK_SPACE, 0, "Backspc");
        WindowUtils.addAction(this, prev, KeyEvent.VK_PAGE_UP, 0, "PageUp");
        WindowUtils.addAction(this, next, KeyEvent.VK_PAGE_DOWN, 0, "PageDown");
        WindowUtils.addAction(this, prev, KeyEvent.VK_UP, 0, "Up");
        WindowUtils.addAction(this, next, KeyEvent.VK_DOWN, 0, "Down");
        WindowUtils.addAction(this, prev, KeyEvent.VK_LEFT, 0, "Left");
        WindowUtils.addAction(this, next, KeyEvent.VK_RIGHT, 0, "Right");
        WindowUtils.addAction(this, next, KeyEvent.VK_SPACE, 0, "Space");
        WindowUtils.addAction(this, full, KeyEvent.VK_F11, 0, "F11");
        WindowUtils.addAction(this, full, KeyEvent.VK_F, 0, "F");
        WindowUtils.addAction(this, switchMirror, KeyEvent.VK_M, 0, "M");
        WindowUtils.addAction(this, full1, KeyEvent.VK_1, 0, "1");
        WindowUtils.addAction(this, full2, KeyEvent.VK_2, 0, "2");
        WindowUtils.addAction(this, toggle, KeyEvent.VK_I, 0, "i");
        WindowUtils.addAction(this, switchPage, KeyEvent.VK_S, 0, "s");
    }

    private void toggleBlackWhite() {
        blackWhite = !blackWhite;
        if (blackWhite) {
            background = Color.WHITE;
            foreground = Color.BLACK;
        } else {
            background = Color.BLACK;
            foreground = Color.WHITE;
        }
        // panel.setBackground(black);
        // setBackground(black);
        // getContentPane().setBackground(black);
        // setBackground(black);

        paint();

        // EDTUtils.setBackground(this, black);
    }

    private int getCurrentScreenIndex() {
        GraphicsDevice gd = this.getGraphicsConfiguration().getDevice();
        if (gd == null) {
            return 0;
        }
        if (!gd.isFullScreenSupported()) {
            return 0;
        }
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        for (int x = 0; x < devices.length; x++) {
            if (devices[x] == gd) {
                return x;
            }
        }
        return 0;
    }

    private void full() {
        full(getCurrentScreenIndex());
    }

    private GraphicsDevice screen = null;
    private final Cursor   cursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(1, 1), "");

    private void full(int screenIndex) {
        setVisible(false);
        try {
            dispose();
        } catch (Exception ex) {
            // Nothing to do
        }
        if (screen != null) {
            setUndecorated(false);
            GraphicsDevice device = screen;
            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(null);
            }
            setCursor(Cursor.getDefaultCursor());

            screen = null;
        } else {
            setUndecorated(true);

            GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screenIndex < 0 || screenIndex > devices.length) {
                screenIndex = 0;
            }
            GraphicsDevice device = devices[screenIndex];
            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(this);
            }

            // disable local pointer
            try {
                setCursor(cursor);
            } catch (IndexOutOfBoundsException ioobe) {
                // Catch a exception on linux systems (Maybe openjdk related)
            }

            screen = device;
        }
        setVisible(true);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            start();
        } else {
            stop();
        }
        super.setVisible(b);
    }

    private void stop() {
        if (painter != null) {
            painter.interrupt();
            painter = null;
        }
    }

    private void start() {
        if (painter == null) {
            painter = new Painter();
            painter.start();
        }
    }

    public static <E extends ASchwimmer> void start(JFrame parent, AWettkampf<E> wk) {
        JHeatPresentationFrame<E> hpf = new JHeatPresentationFrame<E>(parent, wk);
        hpf.setVisible(true);
    }

    private void drawCentered(Graphics2D g, String text, int width, int y) {
        drawCentered(g, text, width, y, 0);
    }

    private void drawCentered(Graphics2D g, String text, int width, int y, int xoffset) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, xoffset + (width - fm.stringWidth(text)) / 2, y);
    }

    synchronized void paint() {
        try {
            if (page > 1) {
                page = 0;
            }
            if (index == wk.getLaufliste().getLaufliste().size()) {
                // No more heats to preview
                page = 0;
            }
            if (index == 0) {
                // Display preview of first heat
                page = 1;
            }

            Lauf<?> lauf = wk.getLaufliste().getLaufliste().get(index - 1 + page);
            String name = lauf.getName();
            String ak = lauf.getAltersklasse();
            String dis = lauf.getDisziplin();
            String disx = lauf.getDisziplinShort();

            // if (buffer == null) {
            // createBufferStrategy(2);
            // buffer = getBufferStrategy();
            // }
            int pwidth = panel.getWidth();
            int pheight = panel.getHeight();
            if (image == null) {
                image = new BufferedImage(pwidth, pheight, ColorSpace.TYPE_RGB);
            }
            Graphics2D imagegraphics = (Graphics2D) image.getGraphics();
            if (mirror) {
                AffineTransform mirrorAT = AffineTransform.getTranslateInstance(pwidth, 0);
                mirrorAT.scale(-1.0, 1.0); // flip horizontally
                imagegraphics.transform(mirrorAT);
            }

            imagegraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (PrintManager.getFont() != null) {
                imagegraphics.setFont(PrintManager.getFont());
            }
            imagegraphics.setBackground(background);
            imagegraphics.setColor(foreground);

            imagegraphics.clearRect(0, 0, pwidth, pheight);

            Font font = imagegraphics.getFont();
            {
                imagegraphics.setFont(font.deriveFont((float) pheight / 10));
                FontMetrics fm = imagegraphics.getFontMetrics();
                if (fm.stringWidth(dis) > pwidth) {
                    drawCentered(imagegraphics, disx, pwidth, pheight - fm.getDescent());
                } else {
                    drawCentered(imagegraphics, dis, pwidth, pheight - fm.getDescent());
                }
            }

            switch (page) {
            case 0: {
                float size = (float) pheight / 10;
                imagegraphics.setFont(font.deriveFont(size));
                FontMetrics fm = imagegraphics.getFontMetrics();

                String titel = "Aktueller Lauf";
                drawCentered(imagegraphics, titel, pwidth, fm.getAscent());
                drawCentered(imagegraphics, ak, pwidth, fm.getAscent() + pheight / 10);

                size = (float) pheight * 3 / 4;
                imagegraphics.setFont(font.deriveFont(size));
                fm = imagegraphics.getFontMetrics();

                drawCentered(imagegraphics, name, pwidth, fm.getAscent() + pheight / 10 + (pheight * 9 / 10 - fm.getHeight()) / 2);
                break;
            }
            default: {
                float size = (float) pheight / 10;
                imagegraphics.setFont(font.deriveFont(size));
                FontMetrics fm = imagegraphics.getFontMetrics();

                String titel = "N\u00e4chster Lauf";
                drawCentered(imagegraphics, titel, pwidth, fm.getAscent());

                drawCentered(imagegraphics, ak, pwidth / 2, fm.getAscent() + pheight / 10);
                titel = "Lauf " + name;
                drawCentered(imagegraphics, titel, pwidth / 2, fm.getAscent() + pheight / 10, pwidth / 2);

                int height = (int) (pheight * 0.55);
                int bahnen = lauf.getBahnen();
                size = 1.0f * height / bahnen;

                imagegraphics.setFont(font.deriveFont(size));
                fm = imagegraphics.getFontMetrics();

                int length = -1;
                String[] text = new String[bahnen];
                for (int x = 0; x < bahnen; x++) {
                    ASchwimmer s = lauf.getSchwimmer(x);
                    StringBuffer sb = new StringBuffer();
                    sb.append(x + 1);
                    sb.append(": ");
                    if (s != null) {
                        sb.append(s.getName());
                        if ((!lauf.isOnlyOneAgeGroup()) && (!lauf.isOnlyOneSex())) {
                            sb.append(" (");
                            sb.append(I18n.getAgeGroupAsStringShort(s));
                            sb.append(")");
                        } else {
                            if (!lauf.isOnlyOneAgeGroup()) {
                                sb.append(" (");
                                sb.append(s.getAK().getName());
                                sb.append(")");
                            }
                            if (!lauf.isOnlyOneSex()) {
                                sb.append(" (");
                                sb.append(I18n.geschlechtToShortString(s));
                                sb.append(")");
                            }
                        }
                    }
                    text[x] = sb.toString();

                    length = Math.max(length, fm.stringWidth(text[x]));
                }

                int xpos = (pwidth - length) / 2;
                if (xpos < 0) {
                    xpos = 0;
                    imagegraphics.transform(AffineTransform.getScaleInstance(1.0 * pwidth / length, 1.0));
                }
                for (int x = 0; x < bahnen; x++) {
                    imagegraphics.drawString(text[x], xpos, (int) (pheight * 6 / 10 - height / 2 + size * (x + 0.5)));
                }

                break;
            }
            }
            imagegraphics.dispose();

            panel.getGraphics().drawImage(image, 0, 0, null);
        } catch (RuntimeException re) {
            // Nothing to do
        }
    }

    void next() {
        if (index < wk.getLaufliste().getLaufliste().size()) {
            index++;
            page = 0;
            counter = 0;
            paint();
        }
    }

    void previous() {
        if (index > 0) {
            index--;
            page = 0;
            counter = 0;
            paint();
        }
    }

    void switchPage() {
        page = 1 - page;
        counter = 0;
        paint();
    }

    class Painter extends Thread {

        @Override
        public void run() {
            while (!isInterrupted()) {
                paint();

                counter++;
                if (counter >= (page == 0 ? 10 : 20)) {
                    page++;
                    if (page >= 2) {
                        page = 0;
                    }
                    counter = 0;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}