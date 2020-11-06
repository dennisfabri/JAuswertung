package de.df.jauswertung.dp.displaytool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.dp.displaytool.vm.CompetitionPresenter;
import de.df.jauswertung.dp.displaytool.vm.PresentationRow;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JHeatPresentationFrame extends JFrame {

    private static final long    serialVersionUID = -7816858433328257749L;

    private int                  counter          = 0;
    private int                  page             = 0;

    private boolean              mirror           = false;
    private boolean              blackWhite       = true;
    private Color                background       = Color.BLACK;
    private Color                foreground       = Color.WHITE;

    private Painter              painter          = null;

    private CompetitionPresenter presenter        = null;

    private BufferedImage        image            = null;
    private Component            panel            = null;

    private int                  rowCount         = 7;

    public JHeatPresentationFrame(JFrame parent, int rowCount, CompetitionPresenter presenter) {
        super("Results");
        this.rowCount = rowCount;
        this.presenter = presenter;

        setIconImages(IconManager.getTitleImages());

        setLayout(new FormLayout("0px,fill:default:grow,0px", "0px,fill:default:grow,0px"));

        panel = getContentPane();
        panel.setBackground(Color.BLACK);

        toggleBlackWhite();

        pack();

        setSize(400, 250);
        WindowUtils.center(this, parent);

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

        Runnable restore = new Runnable() {
            @Override
            public void run() {
                if (screen == null) {
                    restore();
                }
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
        WindowUtils.addAction(this, restore, KeyEvent.VK_R, 0, "r");
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

        paint();
    }

    private void restore() {
        setSize(960, 540);
        WindowUtils.center(JHeatPresentationFrame.this);
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
    private final Cursor   cursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(1, 1), "");

    private void full(int screenIndex) {
        setVisible(false);
        try {
            dispose();
        } catch (Exception ex) {
            // Nothing to do
        }
        if (screen != null) {
            setUndecorated(false);

            setExtendedState(NORMAL);

            GraphicsDevice device = screen;
            screen = null;
            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(null);
            }
            setCursor(Cursor.getDefaultCursor());

            EDTUtils.executeOnEDTAsync(new Runnable() {
                @Override
                public void run() {
                    restore();
                }
            });

        } else {
            System.out.println(getVirtualScreenBounds());
            setLocation(getVirtualScreenBounds().x, getVirtualScreenBounds().y);

            System.out.println(getLocation());
            setUndecorated(true);
            setAlwaysOnTop(true);

            setExtendedState(MAXIMIZED_BOTH);

            GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screenIndex < 0 || screenIndex > devices.length) {
                screenIndex = 0;
            }
            GraphicsDevice device = devices[screenIndex];
            if (device.isFullScreenSupported()) {
                // device.setFullScreenWindow(this);
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

    public Rectangle getVirtualScreenBounds() {
        return getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
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

    private void drawRight(Graphics2D g, String text, int width, int y, int xoffset) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, xoffset + (width - fm.stringWidth(text)), y);
    }

    private void drawLeft(Graphics2D g, String text, int width, int y, int xoffset) {
        g.drawString(text, xoffset, y);
    }

    synchronized void paint() {
        try {
            System.out.println("paint()");
            if (screen != null) {
                setLocation(getVirtualScreenBounds().x, getVirtualScreenBounds().y);
                setSize(getVirtualScreenBounds().width, getVirtualScreenBounds().height);
            }

            PresentationRow[] rows = presenter.getRows();
            if (page >= getPageCount(rows)) {
                page = 0;
            }

            int pwidth = panel.getWidth();
            int pheight = panel.getHeight();
            if (image == null || image.getHeight() != pheight || image.getWidth() != pwidth) {
                image = new BufferedImage(pwidth, pheight, ColorSpace.TYPE_RGB);
            }
            Graphics2D imagegraphics = (Graphics2D) image.getGraphics();
            if (mirror) {
                AffineTransform mirrorAT = AffineTransform.getTranslateInstance(pwidth, 0);
                mirrorAT.scale(-1.0, 1.0); // flip horizontally
                imagegraphics.transform(mirrorAT);
            }

            imagegraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            imagegraphics.setBackground(background);
            imagegraphics.setColor(foreground);

            imagegraphics.clearRect(0, 0, pwidth, pheight);

            int fontsize = 12;
            Font font = imagegraphics.getFont().deriveFont(1.0f * fontsize);
            imagegraphics.setFont(font);
            int ascent = imagegraphics.getFontMetrics().getAscent();
            while (ascent * rowCount <= image.getHeight()) {
                fontsize++;
                font = imagegraphics.getFont().deriveFont(1.0f * fontsize);
                imagegraphics.setFont(font);
                ascent = imagegraphics.getFontMetrics().getAscent();
            }
            fontsize -= 2;

            font = imagegraphics.getFont().deriveFont(1.0f * fontsize);
            imagegraphics.setFont(font);
            ascent = imagegraphics.getFontMetrics().getAscent();

            int offsetY = (image.getHeight() - rowCount * ascent) / 2;

            FontMetrics fm = imagegraphics.getFontMetrics();

            int column1End = 0;
            int column3End = 0;

            int offset1Right = 0;
            int offset2Left = 0;
            int offset3Right = 0;
            int offset4Right = 0;

            int spaceLength = fm.stringWidth(" ");

            for (PresentationRow row : rows) {
                column1End = Math.max(column1End, fm.stringWidth(row.rank));
                column3End = Math.max(column3End, fm.stringWidth(row.points));
            }
            offset1Right = -pwidth + column1End;
            offset2Left = column1End + spaceLength;
            offset3Right = -column3End - spaceLength;

            int minIndex = page * rowCount;
            int amount = Math.min(rows.length - minIndex, rowCount);

            String lastRank = "";
            int x = 0;
            for (PresentationRow row : Arrays.stream(rows).skip(minIndex).limit(amount).collect(Collectors.toList())) {
                if (!lastRank.equals(row.rank)) {
                    drawRight(imagegraphics, row.rank, pwidth, offsetY + fm.getAscent() * (x + 1), offset1Right);
                    lastRank = row.rank;
                }
                drawLeft(imagegraphics, row.name, pwidth, offsetY + fm.getAscent() * (x + 1), offset2Left);
                Color c = imagegraphics.getColor();
                imagegraphics.setColor(
                        ColorUtils.calculateColor(imagegraphics.getBackground(), imagegraphics.getColor(), 0.50));
                drawRight(imagegraphics, row.serc, pwidth, offsetY + fm.getAscent() * (x + 1), offset3Right);
                imagegraphics.setColor(c);
                drawRight(imagegraphics, row.points, pwidth, offsetY + fm.getAscent() * (x + 1), offset4Right);
                x++;
            }

            imagegraphics.dispose();

            Graphics g = panel.getGraphics();
            g.drawImage(image, 0, 0, null);
        } catch (RuntimeException re) {
            re.printStackTrace();
            // Nothing to do
        }
    }

    private int getPageCount(PresentationRow[] rows) {
        int d = rows.length / rowCount;
        int r = rows.length % rowCount;

        return d + (r > 0 ? 1 : 0);
    }

    void next() {
        if (page + 1 < getPageCount(presenter.getRows())) {
            page++;
        } else {
            page = 0;
        }
        counter = 0;
        paint();
    }

    void previous() {
        if (page > 0) {
            page--;
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

        public Painter() {
            this.setName(this.getClass().getName());
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    try {
                        counter++;
                        if (counter >= 10) {
                            next();
                            counter = 0;
                        } else {
                            paint();
                        }

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                System.out.println("Stopping painter");
            } catch (Exception ex) {
                // ex.printStackTrace();
            }
        }
    }
}