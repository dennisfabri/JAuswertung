package de.df.jauswertung.daten.misc;

import com.pmease.commons.xmt.VersionedDocument;
import de.df.jauswertung.daten.AWettkampf;
import org.dom4j.Element;

import java.util.Stack;

/**
 * @author Dennis Fabri @since 15. Oktober 2001, 21:59
 */
public class BugReport {

    private String data = "";
    private String info = "";
    private Object daten = null;

    public BugReport() {
    }

    public BugReport(String data, Object daten) {
        this.data = data;
        this.daten = daten;
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

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
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
