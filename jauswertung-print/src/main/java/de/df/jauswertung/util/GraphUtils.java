/*
 * Created on 22.03.2007
 */
package de.df.jauswertung.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.print.Printable;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.SwingConstants;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.UrkundenPrintable;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.MultiplePrintable;
import de.df.jutils.util.StringTools;

public class GraphUtils {

    public static <T extends ASchwimmer> Printable getDummyPrintable(Hashtable<String, Object>[] cells) {
        return new UrkundenPrintable<T>(cells);
    }

    public static <T extends ASchwimmer> boolean hasDocuments(AWettkampf<T> wk, boolean einzelwertung) {
        if (einzelwertung) {
            return (wk.getProperty(PropertyConstants.URKUNDE_EINZELWERTUNG) != null);
        }
        return (wk.getProperty(PropertyConstants.URKUNDE) != null);
    }

    public static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, boolean[][] selection, boolean einzelwertung) {
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
                    mp.add(new UrkundenPrintable<T>(wk, SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1), x, y == 1, einzelwertung));
                }
            }
        }
        return mp;
    }

    public static void addTextfield(mxGraph graph, Point offset, Dimension size, String text, Font f, int align, boolean borders) {
        if (f == null) {
            f = PrintManager.getDefaultFont();
        }
        mxCell cell = addTextfield(graph, offset.getX(), offset.getY(), size.width, size.getHeight(), text, borders);

        Hashtable<String, Object> style = styleToHashtable(cell.getStyle());
        setAlign(style, align);
        setFont(style, f);
        setStyle(graph, cell, hashtableToStyle(style));
    }

    public static void setFont(mxGraph graph, mxCell cell, Font font) {
        Hashtable<String, Object> style = styleToHashtable(cell.getStyle());
        setFont(style, font);
        setStyle(graph, cell, hashtableToStyle(style));
    }

    private static void setStyle(mxGraph graph, mxCell cell, String style) {
        graph.getModel().beginUpdate();
        graph.getModel().setStyle(cell, style);
        // cell.setStyle(style);
        graph.getModel().endUpdate();
        // graph.getView().invalidate(cell);
    }

    public static void setAlign(mxGraph graph, mxCell cell, int align) {
        Hashtable<String, Object> style = styleToHashtable(cell.getStyle());
        setAlign(style, align);
        setStyle(graph, cell, hashtableToStyle(style));
    }

    private static void setFont(Hashtable<String, Object> style, Font f) {
        // String family = f.getFamily();
        String name = f.getFontName();
        int size = f.getSize();
        int type = f.getStyle();

        style.put(mxConstants.STYLE_FONTFAMILY, name);
        style.put(mxConstants.STYLE_FONTSIZE, size);
        style.put(mxConstants.STYLE_FONTSTYLE, type);
    }

    private static void setAlign(Hashtable<String, Object> style, int align) {
        String a = mxConstants.ALIGN_LEFT;
        switch (align) {
        default:
        case SwingConstants.LEFT:
            a = mxConstants.ALIGN_LEFT;
            break;
        case SwingConstants.CENTER:
            a = mxConstants.ALIGN_CENTER;
            break;
        case SwingConstants.RIGHT:
            a = mxConstants.ALIGN_RIGHT;
            break;
        }
        style.put(mxConstants.STYLE_ALIGN, a);
    }

    public static mxCell addTextfield(mxGraph graph, double x, double y, double width, double height, String text, boolean borders) {

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

        graph.getModel().beginUpdate();
        mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, name, x, y, width, height);
        cell.setConnectable(false);
        graph.getModel().endUpdate();

        // DefaultGraphCell cell = new DefaultGraphCell(name);
        // String cellstring = cell.toString() + " / " + cell.getAttributes() +
        // " / ";
        // GraphConstants.setBounds(cell.getAttributes(), new
        // Rectangle2D.Double(x, y, width, height));
        // GraphConstants.setOpaque(cell.getAttributes(), false);
        // GraphConstants.setConnectable(cell.getAttributes(), false);
        // GraphConstants.setAutoSize(cell.getAttributes(), false);
        // try {
        // GraphConstants
        // .setFont(cell.getAttributes(), PrintManager.getFont());
        // } catch (RuntimeException re) {
        // throw new RuntimeException(cellstring + cell.toString() + " / "
        // + cell.getAttributes() + " / " + PrintManager.getFont(), re);
        // }
        // GraphConstants.setHorizontalAlignment(cell.getAttributes(),
        // SwingConstants.CENTER);
        // if (borders) {
        // GraphConstants.setBorder(cell.getAttributes(), new DashedBorder(
        // Color.GRAY, 10, 10));
        // }
        // DefaultPort port0 = new DefaultPort();
        // cell.add(port0);

        // graph.getGraphLayoutCache().insert(cell);
        // graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());

        return cell;
    }

    @SuppressWarnings({ "unchecked" })
    public static Hashtable<String, Object>[] collectGraph(mxGraph graph) {
        LinkedList<Hashtable<String, Object>> cells = new LinkedList<Hashtable<String, Object>>();

        graph.selectAll();
        Object[] gcells = graph.getSelectionCells();
        graph.selectCells(false, false);

        for (Object o : gcells) {
            if (o instanceof mxCell) {
                mxCell c = (mxCell) o;

                Hashtable<String, Object> ht = new Hashtable<String, Object>();

                mxRectangle r2d = graph.getBoundingBox(c);
                ht.put("offset", new Point((int) r2d.getX(), (int) r2d.getY()));
                ht.put("size", new Dimension((int) r2d.getWidth(), (int) r2d.getHeight()));

                Hashtable<String, Object> style = styleToHashtable(c.getStyle());
                ht.put("fontname", style.get(mxConstants.STYLE_FONTFAMILY));
                ht.put("fontstyle", toJavaFontStyle(style.get(mxConstants.STYLE_FONTSTYLE)));
                ht.put("fontsize", style.get(mxConstants.STYLE_FONTSIZE));
                ht.put("alignment", toJavaAlignment(style.get(mxConstants.STYLE_ALIGN)));

                ht.put("text", c.getValue().toString());

                cells.addLast(ht);
            }
        }
        if (cells.size() == 0) {
            return null;
        }
        return cells.toArray(new Hashtable[cells.size()]);
    }

    private static int toJavaFontStyle(Object o) {
        if (o == null) {
            return Font.PLAIN;
        }
        if (o instanceof String) {
            try {
                o = Integer.parseInt(o.toString());
            } catch (RuntimeException re) {
                // Nothing to do
            }
        }
        int style = Font.PLAIN;
        if (o instanceof Integer) {
            switch ((Integer) o) {
            default:
                style = Font.PLAIN;
                break;
            case mxConstants.FONT_BOLD:
                style = Font.BOLD;
                break;
            case mxConstants.FONT_ITALIC:
                style = Font.ITALIC;
                break;
            case mxConstants.FONT_BOLD + mxConstants.FONT_ITALIC:
                style = Font.BOLD + Font.ITALIC;
                break;
            }
        }
        return style;
    }

    private static int toJavaAlignment(Object o) {
        if (o == null) {
            return SwingConstants.CENTER;
        }
        int align = SwingConstants.CENTER;
        if (o instanceof String) {
            String a = (String) o;
            if (a.equalsIgnoreCase(mxConstants.ALIGN_LEFT)) {
                align = SwingConstants.LEFT;
            } else if (a.equalsIgnoreCase(mxConstants.ALIGN_RIGHT)) {
                align = SwingConstants.RIGHT;
            }
        } else if (o instanceof Integer) {
            align = (Integer) o;
        }
        return align;
    }

    public static Hashtable<String, Object> getDefaultStyle() {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        result.put(mxConstants.STYLE_FONTFAMILY, "DLRG Univers 55 Roman");
        result.put(mxConstants.STYLE_FONTSIZE, 12);
        result.put(mxConstants.STYLE_FONTSTYLE, Font.PLAIN);
        result.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        result.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);

        return result;
    }

    public static Hashtable<String, Object> styleToHashtable(String style) {
        Hashtable<String, Object> result = getDefaultStyle();
        if (style != null) {
            String[] part = style.split(";");
            for (String aPart : part) {
                String[] temp = aPart.split("=");
                if (temp.length == 2) {
                    temp[0] = temp[0].trim();
                    temp[1] = temp[1].trim();

                    if (temp[0].equalsIgnoreCase(mxConstants.STYLE_FONTFAMILY)) {
                        result.put(mxConstants.STYLE_FONTFAMILY, temp[1]);
                    } else if (temp[0].equalsIgnoreCase(mxConstants.STYLE_FONTSIZE)) {
                        result.put(mxConstants.STYLE_FONTSIZE, temp[1]);
                    } else if (temp[0].equalsIgnoreCase(mxConstants.STYLE_FONTSTYLE)) {
                        result.put(mxConstants.STYLE_FONTSTYLE, temp[1]);
                    } else if (temp[0].equalsIgnoreCase(mxConstants.STYLE_FONTCOLOR)) {
                        result.put(mxConstants.STYLE_FONTCOLOR, temp[1]);
                    } else if (temp[0].equalsIgnoreCase(mxConstants.STYLE_ALIGN)) {
                        result.put(mxConstants.STYLE_ALIGN, temp[1]);
                    }
                }
            }
        }
        return result;
    }

    public static String hashtableToStyle(Hashtable<String, Object> style) {
        StringBuilder sb = new StringBuilder();
        sb.append(";");
        sb.append(mxConstants.STYLE_FONTFAMILY).append("=").append(style.get(mxConstants.STYLE_FONTFAMILY)).append(";");
        sb.append(mxConstants.STYLE_FONTSIZE).append("=").append(style.get(mxConstants.STYLE_FONTSIZE)).append(";");

        String fs = "plain";
        Object s = style.get(mxConstants.STYLE_FONTSTYLE);
        if (s instanceof Integer) {
            switch ((Integer) s) {
            default:
            case Font.PLAIN:
                fs = "" + 0;
                break;
            case Font.BOLD:
                fs = "" + mxConstants.FONT_BOLD;
                break;
            case Font.ITALIC:
                fs = "" + mxConstants.FONT_ITALIC;
                break;
            case Font.BOLD + Font.ITALIC:
                fs = "" + (mxConstants.FONT_BOLD + mxConstants.FONT_ITALIC);
                break;
            }
        } else if (s instanceof String) {
            fs = s.toString();
        }

        sb.append(mxConstants.STYLE_FONTSTYLE).append("=").append(fs).append(";");
        sb.append(mxConstants.STYLE_FONTCOLOR).append("=").append(style.get(mxConstants.STYLE_FONTCOLOR)).append(";");
        String align = "center";
        Object a = style.get(mxConstants.STYLE_ALIGN);
        if (a instanceof Integer) {
            switch ((Integer) a) {
            case SwingConstants.LEFT:
                align = "left";
                break;
            case SwingConstants.RIGHT:
                align = "right";
                break;
            default:
            case SwingConstants.CENTER:
                align = "center";
                break;
            }
        } else if (a instanceof String) {
            align = a.toString();
        }
        sb.append(mxConstants.STYLE_ALIGN).append("=").append(align).append(";");
        return sb.toString();
    }

    public static <T extends ASchwimmer> void populateGraph(mxGraph graph, Hashtable<String, Object>[] cells, boolean borders, boolean print) {
        populateGraph(graph, cells, borders, null, null, print);
    }

    public static <T extends ASchwimmer> void populateGraph(mxGraph graph, Hashtable<String, Object>[] cells, boolean borders, String[] ids, String[] values,
            boolean print) {
        if (cells != null) {
            for (Hashtable<String, Object> ht : cells) {
                String text = (String) ht.get("text");
                if (ids != null) {
                    for (int x = 0; x < ids.length; x++) {
                        text = StringTools.replaceCaseInsensitive(text, ids[x], values[x]);
                    }
                    // text = StringEscapeUtils.escapeHtml(text);
                    // text = text.replace("&lt;br&gt;", "<br>");
                }
                String fname = (String) ht.get("fontname");
                int fstyle = Font.PLAIN;
                try {
                    fstyle = Integer.parseInt(ht.get("fontstyle").toString());
                } catch (RuntimeException re) {
                    // Nothing to do
                }
                int fsize = 12;
                try {
                    fsize = Integer.parseInt(ht.get("fontsize").toString());
                } catch (RuntimeException re) {
                    // Nothing to do
                }
                int align = SwingConstants.CENTER;
                try {
                    align = Integer.parseInt(ht.get("alignment").toString());
                } catch (RuntimeException re) {
                    // Nothing to do
                }
                addTextfield(graph, (Point) ht.get("offset"), (Dimension) ht.get("size"), text, getFont(fname, fstyle, fsize), align, borders);
            }
        }
    }

    private static Font getFont(String name, int style, int size) {
        try {
            return new Font(name, style, size);
        } catch (RuntimeException re) {
            return new Font("Dialog", style, size);
        }
    }

    public static mxGraph createGraph(boolean print) {

        mxGraph graph = new mxGraph();

        graph.setGridSize((int) (PageSetup.DPI / 2.54 / 16));
        graph.setGridEnabled(true);
        graph.setAllowNegativeCoordinates(true);

        mxStylesheet styles = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_OPACITY, 100);
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        styles.setDefaultVertexStyle(style);

        // graph.setGridColor(Color.GRAY);
        // graph.setGridSize();
        // graph.setAntiAliased(true);
        // graph.setHandleColor(Color.BLUE);
        // graph.setHighlightColor(Color.BLUE);
        // graph.setMarqueeColor(Color.BLUE);
        // graph.setScale(1);

        return graph;
    }

    // Method does not work properly
    // TODO: Investigate reason
    public static void clear(mxGraph graph) {
        // if (false) {
        // // Old Method
        // GraphModel model = new DefaultGraphModel();
        // GraphLayoutCache view = new GraphLayoutCache(model,
        // new DefaultCellViewFactory());
        // graph.setModel(model);
        // graph.setGraphLayoutCache(view);
        // }
        graph.removeCells();
    }

    public static mxGraphComponent createDisplay(mxGraph graph, boolean print) {
        mxGraphComponent jgraph = new mxGraphComponent(graph);

        jgraph.getViewport().setOpaque(false);
        jgraph.setBackground(Color.WHITE);

        // jgraph.setPageVisible(true);
        jgraph.setGridVisible(!print);
        jgraph.setToolTips(!print);
        jgraph.setGridVisible(false);
        jgraph.getConnectionHandler().setCreateTarget(true);

        return jgraph;
    }
}
