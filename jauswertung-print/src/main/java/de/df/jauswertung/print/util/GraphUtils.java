/*
 * Created on 22.03.2007
 */
package de.df.jauswertung.print.util;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.print.Printable;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.SwingConstants;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.UrkundenPrintable;
import de.df.jauswertung.print.graphics.Dimension;
import de.df.jauswertung.print.graphics.Point;
import de.df.jauswertung.print.urkunden.UrkundenFeld;
import de.df.jauswertung.print.urkunden.UrkundenSeite;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.printables.MultiplePrintable;
import de.df.jutils.util.StringTools;

public class GraphUtils {

    private static final Font DEFAULT_FONT = new Font("DLRG Univers 55 Roman", Font.PLAIN, 12);

    public static <T extends ASchwimmer> Printable getDummyPrintable(Hashtable<String, Object>[] cells) {
        return new UrkundenPrintable<T>(cells);
    }

    public static <T extends ASchwimmer> boolean hasDocuments(AWettkampf<T> wk, boolean einzelwertung) {
        if (einzelwertung) {
            return (wk.getProperty(PropertyConstants.URKUNDE_EINZELWERTUNG) != null);
        }
        return (wk.getProperty(PropertyConstants.URKUNDE) != null);
    }

    public static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, boolean[][] selection,
            boolean einzelwertung) {
        if (einzelwertung) {
            if (wk.getProperty(PropertyConstants.URKUNDE_EINZELWERTUNG) == null) {
                return null;
            }
        } else {
            if (wk.getProperty(PropertyConstants.URKUNDE) == null) {
                return null;
            }
        }
        MultiplePrintable mp = new MultiplePrintable();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                if ((selection == null) || (selection[y][x])) {
                    mp.add(new UrkundenPrintable<T>(wk,
                            SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1), x, y == 1,
                            einzelwertung));
                }
            }
        }
        return mp;
    }

    public static UrkundenFeld addTextfield(UrkundenSeite seite, double x, double y, double width, double height,
            String text) {
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }
        if (width < 10) {
            width = 100;
        }
        if (height < 10) {
            height = 100;
        }

        String name = (text == null ? I18n.get("DocumentNewText") : text);
        return seite.addFeld(new Rectangle((int) x, (int) y, (int) width, (int) height), name, DEFAULT_FONT,
                SwingConstants.CENTER);
    }

    @SuppressWarnings({ "unchecked" })
    public static Hashtable<String, Object>[] collectGraph(UrkundenSeite seite) {
        LinkedList<Hashtable<String, Object>> cells = new LinkedList<>();

        for (UrkundenFeld feld : seite.getFelder()) {
            Hashtable<String, Object> ht = new Hashtable<>();
            Rectangle bounds = feld.getBounds();
            ht.put("offset", new Point(bounds.x, bounds.y));
            ht.put("size", new Dimension(bounds.width, bounds.height));
            ht.put("fontname", feld.getFont().getName());
            ht.put("fontstyle", feld.getFont().getStyle());
            ht.put("fontsize", feld.getFont().getSize());
            ht.put("alignment", feld.getAlignment());
            ht.put("text", feld.getText());
            cells.addLast(ht);
        }
        if (cells.isEmpty()) {
            return null;
        }
        return cells.toArray(new Hashtable[0]);
    }

    public static void populateGraph(UrkundenSeite seite, Hashtable<String, Object>[] cells) {
        populateGraph(seite, cells, null, null);
    }

    public static void populateGraph(UrkundenSeite seite, Hashtable<String, Object>[] cells, String[] ids,
            String[] values) {
        if (cells == null) {
            return;
        }
        for (Hashtable<String, Object> ht : cells) {
            String text = (String) ht.get("text");
            if (ids != null) {
                for (int x = 0; x < ids.length; x++) {
                    text = StringTools.replaceCaseInsensitive(text, ids[x], values[x]);
                }
            }
            Font font = getFont((String) ht.get("fontname"), toInt(ht.get("fontstyle"), Font.PLAIN),
                    toInt(ht.get("fontsize"), 12));
            Point offset = (Point) ht.get("offset");
            Dimension size = (Dimension) ht.get("size");
            seite.addFeld(new Rectangle(offset.x, offset.y, size.width, size.height), text, font,
                    toInt(ht.get("alignment"), SwingConstants.CENTER));
        }
    }

    private static int toInt(Object value, int defaultValue) {
        try {
            return Integer.parseInt(value.toString());
        } catch (RuntimeException re) {
            return defaultValue;
        }
    }

    private static Font getFont(String name, int style, int size) {
        try {
            return new Font(name, style, size);
        } catch (RuntimeException re) {
            return new Font("Dialog", style, size);
        }
    }

    public static UrkundenSeite createSeite(boolean editing) {
        UrkundenSeite seite = new UrkundenSeite();
        seite.setGridSize((int) (PageSetup.DPI / 2.54 / 16));
        seite.setEditing(editing);
        return seite;
    }
}
