package de.df.jauswertung.daten.misc;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.print.PrinterJob;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;

import javax.print.PrintService;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.print.PrintManager;

/**
 * @author Dennis Fabri @since 15. Oktober 2001, 21:59
 */
public class BugReport implements Serializable {

    private static final long  serialVersionUID = -6433894027809485390L;

    public static final String NEWLINE          = "\n";

    private String             data             = "";
    private String             info             = "";
    private Object             daten            = null;

    public BugReport() {
    }

    @SuppressWarnings("rawtypes")
    public BugReport(Throwable fehler, Thread thread, Class klasse, Object dat) {
        String stacktrace = null;
        String stacktrace2 = null;
        if (fehler != null) {
            CharArrayWriter caw = new CharArrayWriter();
            PrintWriter pw = new PrintWriter(caw);
            fehler.printStackTrace(pw);

            pw.close();
            stacktrace = caw.toString();

            StringBuilder s2 = new StringBuilder();
            s2.append(fehler.getClass().getName());
            for (StackTraceElement aStack : fehler.getStackTrace()) {
                s2.append("        at " + aStack.getClassName() + "." + aStack.getMethodName());
                s2.append("\n");
            }
            stacktrace2 = s2.toString();
        }
        String ort = "Unbekannt";
        if (klasse != null) {
            ort = klasse.toString();
        }
        daten = dat;

        StringBuffer s = new StringBuffer();
        s.append("Ort: ");
        s.append(ort);
        s.append(NEWLINE);
        if (thread != null) {
            s.append("Thread:");
            s.append(thread.toString());
        }

        if (fehler != null) {
            s.append(NEWLINE);
            s.append(NEWLINE);
            s.append("Fehlermeldung: ");
            s.append(fehler.toString());
        }
        if (stacktrace != null) {
            s.append(NEWLINE);
            s.append("StackTrace:");
            s.append(NEWLINE);
            s.append(stacktrace);
        }
        if (stacktrace2 != null) {
            s.append(NEWLINE);
            s.append("StackTrace 2:");
            s.append(NEWLINE);
            s.append(stacktrace2);
        }

        {
            try {
                MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                s.append(NEWLINE);
                s.append("Memory usage (heap and non heap):");
                s.append(NEWLINE);
                s.append("Init: ").append(mem.getInit()).append(", Max: ").append(mem.getMax()).append(", Used: ").append(mem.getUsed()).append(", Commited: ")
                        .append(mem.getCommitted());
                s.append(NEWLINE);
                mem = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
                s.append("Init: ").append(mem.getInit()).append(", Max: ").append(mem.getMax()).append(", Used: ").append(mem.getUsed()).append(", Commited: ")
                        .append(mem.getCommitted());
                s.append(NEWLINE);
                long memtotal = Runtime.getRuntime().totalMemory();
                long memfree = Runtime.getRuntime().freeMemory();
                long memmax = Runtime.getRuntime().maxMemory();
                s.append("Total mem: ").append(memtotal).append(", Free mem: ").append(memfree).append(", Max mem: ").append(memmax);

            } catch (Error e) {
                s.append(e.toString());
            }
        }

        String[][] systeminfo = getSystemInfos();
        if (systeminfo != null) {
            s.append(NEWLINE);
            s.append("Systeminformationen:");
            s.append(NEWLINE);
            for (int x = 0; x < systeminfo[0].length; x++) {
                if (systeminfo[1][x].length() > 0) {
                    s.append("  ");
                    s.append(systeminfo[0][x]);
                    s.append(": ");
                    s.append(systeminfo[1][x]);
                    s.append(NEWLINE);
                }
            }
        }

        s.append(NEWLINE);
        s.append("Version: ");
        s.append(I18n.getVersion());
        s.append(NEWLINE);
        s.append("Fonts:");
        s.append(NEWLINE);
        s.append("  Font: ");
        s.append(PrintManager.getFont());
        s.append(NEWLINE);
        s.append("  Default Font: ");
        s.append(PrintManager.getDefaultFont());
        s.append(NEWLINE);
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            s.append("  ");
            s.append(font);
            s.append(NEWLINE);
        }
        s.append("  DefaultFontLog:");
        s.append(PrintManager.getDefaultFontLog());

        s.append(NEWLINE);
        s.append("Printers:");
        s.append(NEWLINE);
        PrintService[] services = PrinterJob.lookupPrintServices();
        for (PrintService printService : services) {
            s.append("  ");
            s.append(printService.getName());
            s.append(NEWLINE);
        }

        data = s.toString();
    }

    private static String[][] getSystemInfos() {
        String[][] result = new String[2][0];
        @SuppressWarnings("rawtypes")
        Enumeration props = System.getProperties().keys();
        LinkedList<String> keys = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        while (props.hasMoreElements()) {
            String key = (String) props.nextElement();
            values.addLast(System.getProperty(key));
            keys.addLast(key);
        }
        result[0] = keys.toArray(new String[keys.size()]);
        result[1] = values.toArray(new String[values.size()]);
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