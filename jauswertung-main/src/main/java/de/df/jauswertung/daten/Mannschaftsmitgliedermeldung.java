package de.df.jauswertung.daten;

import java.util.Stack;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;

public class Mannschaftsmitgliedermeldung {

    private MannschaftWettkampf mwk;

    public MannschaftWettkampf getWettkampf() {
        return mwk;
    }

    public Mannschaftsmitgliedermeldung() {
        this(null);
    }

    public Mannschaftsmitgliedermeldung(MannschaftWettkampf wettkampf) {
        mwk = wettkampf;
    }

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
        // migrator1(dom.getRootElement());
    }

    public static void migrator1(Element node) {
        // AWettkampf.migrator1(node.element("mwk"));
    }

    @SuppressWarnings("unused")
    private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
        migrator2(dom.getRootElement());
    }

    public static void migrator2(Element node) {
        AWettkampf.migrator2(node.element("mwk"));
    }

}
