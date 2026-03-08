package de.df.jauswertung.daten.misc;

import com.pmease.commons.xmt.VersionedDocument;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.print.PrintManager;
import org.dom4j.Element;

import javax.print.PrintService;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.Serial;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;

import static java.util.Arrays.stream;

/**
 * @author Dennis Fabri @since 15. Oktober 2001, 21:59
 */
public class BugReport implements Serializable {

    @Serial
    private static final long serialVersionUID = -6433894027809485390L;

    private String data = "";
    private String info = "";
    private Object daten = null;

    public BugReport() {
    }

    public BugReport(Throwable fehler, Thread thread, Object data) {
        daten = data;
        ReportBuilder reportBuilder = new ReportBuilder();
        // StringBuilder s = new StringBuilder();
        reportBuilder.addField("Thread", thread.getName(), thread.getClass());
        reportBuilder.addException("Exception", fehler);

        {
            try {
                reportBuilder.addTitle("Memory usage:");
                MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                reportBuilder.addField("Heap",
                                       "Init: %s, Max: %s, Used: %s, Commited: %s".formatted(mem.getInit(), mem.getMax(), mem.getUsed(), mem.getCommitted()));
                mem = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
                reportBuilder.addField("Non heap",
                                       "Init: %s, Max: %s, Used: %s, Commited: %s".formatted(mem.getInit(), mem.getMax(), mem.getUsed(), mem.getCommitted()));
                String memtotal = asReadableUnit(Runtime.getRuntime().totalMemory());
                String memfree = asReadableUnit(Runtime.getRuntime().freeMemory());
                String memmax = asReadableUnit(Runtime.getRuntime().maxMemory());
                reportBuilder.addField("Memory", "Total mem: %s, Free mem: %s, Max mem: %s".formatted(memtotal, memfree, memmax));
            } catch (Error e) {
                reportBuilder.addException("Problem while getting memory usage", e);
            }
        }

        String[][] systeminfo = getSystemInfos();

        reportBuilder.addTitle("System Information");
        for (int x = 0; x < systeminfo[0].length; x++) {
            if (!systeminfo[1][x].isEmpty()) {
                reportBuilder.addField(systeminfo[0][x], systeminfo[1][x]);
            }
        }

        reportBuilder.addField("Version", I18n.getVersion());
        reportBuilder.addTitle("Fonts");
        reportBuilder.addField("Font", PrintManager.getFont(), 2);
        reportBuilder.addField("Default Font", PrintManager.getDefaultFont(), 2);

        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        reportBuilder.addField("Installed fonts", stream(fonts).map(Font::getFontName).sorted().toList(), 2);
        reportBuilder.addField("DefaultFontLog", PrintManager.getDefaultFontLog(), 2);

        reportBuilder.addField("Printers", stream(PrinterJob.lookupPrintServices()).map(PrintService::getName).sorted().toList());

        this.data = reportBuilder.toString();
    }

    private String asReadableUnit(long l) {
        if (l < 0) {
            return "-";
        }
        if (l < 1024) {
            return "%d B".formatted(l);
        }
        if (l < 1024 * 1024) {
            return "%.2f KB".formatted(1.0 * l / 1024);
        }
        if (l < 1024 * 1024 * 1024) {
            return "%.2f MB".formatted(1.0 * l / (1024 * 1024));
        }
        return "%.2f GB".formatted(1.0 * l / (1024 * 1024 * 1024));
    }

    private static String[][] getSystemInfos() {
        String[][] result = new String[2][0];
        Enumeration<Object> props = System.getProperties().keys();
        LinkedList<String> keys = new LinkedList<>();
        LinkedList<String> values = new LinkedList<>();
        while (props.hasMoreElements()) {
            String key = (String) props.nextElement();
            values.addLast(System.getProperty(key));
            keys.addLast(key);
        }
        result[0] = keys.toArray(new String[0]);
        result[1] = values.toArray(new String[0]);
        return result;
    }

    public String getData() {
        return data;
    }

    public void setData(String i) {
        data = i;
    }

    public Object getDaten() {
        return daten;
    }

    public void anonymize() {
        daten = null;
    }

    @Override
    public String toString() {
        return "Data: " + data + "\nInfo: " + info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
        // migrator1(dom.getRootElement());
    }

    public static void migrator1(Element node) {
        Element wk = node.element("daten");
        AWettkampf.migrator1(wk);
    }

    @SuppressWarnings("unused")
    private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
        migrator2(dom.getRootElement());
    }

    public static void migrator2(Element node) {
        Element wk = node.element("daten");
        AWettkampf.migrator2(wk);
    }
}
