/*
 * Created on 03.01.2006
 */
package de.df.jauswertung.gui.plugins.http;

import static de.df.jauswertung.io.ExportManager.NAMES;

import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.core.*;
import de.df.jauswertung.io.*;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.*;
import de.df.jutils.data.EnumerationIterable;
import de.df.jutils.io.Transform;

class DataProvider {

    private CorePlugin                core      = null;
    private ExportMode                mode;
    private AgegroupResultSelection[] selection = new AgegroupResultSelection[0];

    public synchronized void setSelection(AgegroupResultSelection[] selection) {
        this.selection = selection;
    }

    synchronized ExportMode getExportMode() {
        return mode;
    }

    synchronized void setExportMode(ExportMode e) {
        mode = e;
        Utils.getPreferences().putInt("HttpServerExportMode", e.value);
    }

    public DataProvider(CorePlugin c) {
        if (c == null) {
            throw new NullPointerException();
        }
        core = c;
        int value = Utils.getPreferences().getInt("HttpServerExportMode", ExportMode.Everything.value);
        mode = ExportMode.Everything;
        for (ExportMode m : ExportMode.values()) {
            if (m.value == value) {
                mode = m;
                break;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized boolean knowsFile(String name) {
        if (name.equals("/favicon.ico") || name.equals("/global/layout/access/style/style.css") || name.equals("/style/custom.css")
                || name.equals("/robots.txt") || name.equals("empty.txt")) {
            return true;
        }

        AWettkampf wk = core.getWettkampf();

        if (name.startsWith("/images-") && name.substring(12, 13).equals("x") && name.endsWith(".zip") && (name.length() == "/images-0000x0000.zip".length())) {
            return true;
        }

        if (name.equals("/wettkampf.wk")) {
            return true;
        }

        if (name.startsWith("/export/")) {
            name = name.substring("/export/".length());

            String[] path = name.split("/");
            if (path.length < 2) {
                return false;
            }
            String format = path[0].replace("+", " ");
            name = path[1];

            int index = name.lastIndexOf(".");
            // String suffix = name.substring(index + 1);
            String prefix = name.substring(0, index).replace("+", " ");

            // String[] formats = ExportManager.getSupportedFormats();
            int type = -1;
            for (int x = 0; x < NAMES.length; x++) {
                if (prefix.equals(NAMES[x])) {
                    type = x;
                    break;
                }
            }
            if (type >= 0) {
                if (ExportManager.isSupported(format, type)) {
                    if (ExportManager.isEnabled(wk, type)) {
                        return true;
                    }
                }
            }
        }

        if (name.equals("/index.html") || name.equals("/")) {
            return true;
        }
        if (name.equals("/index.xml")) {
            return true;
        }

        if (name.equals("/results.html")) {
            return true;
        }
        if ((name.startsWith("/female") || (name.startsWith("/male"))) && name.endsWith(".html")) {
            return true;
        }
        if (name.equals("/groupevaluation.html")) {
            return true;
        }

        for (String key : new EnumerationIterable<String>(providers.keys())) {
            if (name.startsWith("/" + key + "/")) {
                IDataProvider dp = providers.get(key);
                boolean ok = dp.knowsFile(name.substring(key.length() + 2));
                if (ok) {
                    return true;
                }
            }
        }

        System.out.println("Unkown URL: " + name);
        return false;
    }

    @SuppressWarnings({})
    public synchronized void sendData(OutputStream out, String name) throws IOException {
        if (name.equals("/favicon.ico") || name.equals("/global/layout/access/style/style.css") || name.equals("/style/custom.css")
                || name.equals("/robots.txt") || name.equals("empty.txt")) {
            return;
        }

        AWettkampf<?> wk = core.getWettkampf();
        switch (mode) {
        default:
            wk = Utils.copy(wk);
            break;
        case Completed:
            wk = CompetitionUtils.createCompetitionWithCompleteDisciplines(wk);
            break;
        case Filtered:
            wk = ResultSelectionUtils.getResultWettkampf(wk, selection);
            break;
        }

        if (name.startsWith("/images-") && name.substring(12, 13).equals("x") && name.endsWith(".zip") && (name.length() == "/images-0000x0000.zip".length())) {
            createImageData(out, name, wk);
            return;
        }

        if (name.equals("/wettkampf.wk")) {
            createWettkampfData(out, wk);
            return;
        }

        if (name.startsWith("/export/")) {
            name = name.substring("/export/".length());

            String[] path = name.split("/");
            String format = path[0].replace("+", " ");
            name = path[1];

            int index = name.lastIndexOf(".");
            String prefix = name.substring(0, index).replace("+", " ");

            // String[] formats = ExportManager.getSupportedFormats();
            int type = -1;
            for (int x = 0; x < NAMES.length; x++) {
                if (prefix.equals(NAMES[x])) {
                    type = x;
                    break;
                }
            }
            if (type >= 0) {
                if (ExportManager.isSupported(format, type)) {
                    if (ExportManager.isEnabled(wk, type)) {
                        createExportData(out, wk, type, format);
                        return;
                    }
                }
            }
        }

        if (name.equals("/index.html") || name.equals("/")) {
            createIndexData(out, wk);
            return;
        }
        if (name.equals("/index.xml")) {
            createIndexXMLData(out, wk);
            return;
        }

        if (name.equals("/results.html")) {
            createResultsIndexData(out, wk);
            return;
        }
        if ((name.startsWith("/female") || (name.startsWith("/male"))) && name.endsWith(".html")) {
            createResultsData(out, wk, name);
            return;
        }
        if (name.equals("/groupevaluation.html")) {
            getGroupEvaluationData(out, wk);
            return;
        }

        for (String key : new EnumerationIterable<String>(providers.keys())) {
            if (name.startsWith("/" + key + "/")) {
                IDataProvider dp = providers.get(key);
                boolean ok = dp.provide(name.substring(key.length() + 2), out);
                if (ok) {
                    return;
                }
            }
        }

        throw new IOException("File not found: " + name);
    }

    @SuppressWarnings({})
    private static void createIndexXMLData(OutputStream out, AWettkampf<?> wk) throws IOException {
        try {
            Document doc = XmlExporter.generateIndex(wk);
            if (doc == null) {
                throw new NullPointerException();
            }
            Transform.transformDocument2XML(out, null, doc);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @SuppressWarnings({})
    private static void createIndexData(OutputStream out, AWettkampf<?> wk) throws IOException {
        try {
            Document doc = XmlExporter.generateIndex(wk);
            if (doc == null) {
                throw new NullPointerException();
            }
            Transform.writeHtmlDocument(out, "xsl/index.xsl", doc);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @SuppressWarnings({})
    private static void createResultsIndexData(OutputStream out, AWettkampf<?> wk) throws IOException {
        try {
            Document doc = XmlExporter.generateZipIndex(wk);
            if (doc == null) {
                throw new NullPointerException();
            }
            Transform.writeHtmlDocument(out, "xsl/multiindex.xsl", doc);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @SuppressWarnings({})
    private static void createResultsData(OutputStream out, AWettkampf<?> wk, String name) throws IOException {
        try {
            boolean male = name.startsWith("/male");

            String prefix = "/male";
            if (!male) {
                prefix = "/female";
            }

            int index = Integer.parseInt(name.substring(prefix.length(), name.length() - ".html".length()));

            Document doc = XmlExporter.generateResults(wk, index, male);
            if (doc == null) {
                throw new NullPointerException();
            }
            Transform.writeHtmlDocument(out, "xsl/results.xsl", doc);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    @SuppressWarnings({})
    private static void getGroupEvaluationData(OutputStream out, AWettkampf<?> wk) throws IOException {
        try {
            Document doc = XmlExporter.generateGesamtwertungResults(wk);
            if (doc == null) {
                throw new NullPointerException();
            }
            Transform.writeHtmlDocument(out, "xsl/results.xsl", doc);
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
    }

    @SuppressWarnings({})
    private static void createExportData(OutputStream out, AWettkampf<?> wk, int x, String format) throws IOException {
        if (!ExportManager.isEnabled(wk, x)) {
            throw new IOException("File not found!");
        }
        try {
            boolean result = ExportManager.export(format, out, x, wk, null);
            if (!result) {
                throw new IOException("Export failed!");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } catch (Exception t) {
            t.printStackTrace();
            throw new IOException("Export failed with exception! (" + t.getMessage() + ")");
        }
    }

    @SuppressWarnings({})
    private static void createImageData(OutputStream out, String name, AWettkampf<?> wk) throws IOException {
        try {
            int width = Integer.parseInt(name.substring(8, 12));
            int height = Integer.parseInt(name.substring(13, 17));
            printToZip(out, PrintUtils.getResultsPrintable(wk, false, true, false, 0), width, height);
        } catch (RuntimeException re) {
            throw new IOException(re.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static void createWettkampfData(OutputStream out, AWettkampf wk) {
        OutputManager.speichereObject(out, wk);
    }

    private static void printToZip(OutputStream out, Printable printable, int width, int height) throws IOException {
        Paper p = new Paper();
        p.setSize(width, height);
        p.setImageableArea(0, 0, width, height);
        PageFormat pf = new PageFormat();
        if (width > height) {
            pf.setOrientation(PageFormat.PORTRAIT);
        } else {
            pf.setOrientation(PageFormat.PORTRAIT);
        }
        pf.setPaper(p);

        ZipOutputStream zos = new ZipOutputStream(out);
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(0);

        int result = Printable.PAGE_EXISTS;
        int x = 0;
        do {
            BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            try {
                result = printable.print(i.getGraphics(), pf, x);
                if (result == Printable.PAGE_EXISTS) {
                    zos.putNextEntry(new ZipEntry("" + x + ".png"));
                    ImageIO.write(i, "png", zos);
                    zos.closeEntry();
                    x++;
                }
            } catch (PrinterException pe) {
                pe.printStackTrace();
                result = Printable.NO_SUCH_PAGE;
            }
        } while (result == Printable.PAGE_EXISTS);
        zos.close();
    }

    private Hashtable<String, IDataProvider> providers = new Hashtable<String, IDataProvider>();

    public String registerDataProvider(IDataProvider dp) {
        String key = null;
        while (key == null || providers.containsKey(key)) {
            key = UUID.randomUUID().toString();
        }
        providers.put(key, dp);
        dp.setKey(key);
        return key;
    }

    public void unregisterDataProvider(String key) {
        providers.remove(key);
    }
}