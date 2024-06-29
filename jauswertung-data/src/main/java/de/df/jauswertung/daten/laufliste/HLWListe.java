package de.df.jauswertung.daten.laufliste;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;

import de.df.jauswertung.daten.event.PropertyChangeListener;
import org.dom4j.Element;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;

public class HLWListe<T extends ASchwimmer> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7412435828304184620L;

    final AWettkampf<T> wk;

    LinkedList<LinkedList<HLWLauf<T>>> hlwliste;
    LinkedList<Time> startat;
    private Einteilung[] verteilung;

    /** Creates new Laufliste */
    public HLWListe(AWettkampf<T> wettkampf) {
        wk = wettkampf;
        hlwliste = null;
        startat = null;
    }

    public synchronized void erzeugen() {
        erzeugen(verteilung);
    }

    public synchronized void erzeugen(Einteilung[] aufteilung) {
        reset();
    }

    public synchronized void remove(T s) {
        resetAll();
    }

    public synchronized void check(T s) {
        resetAll();
    }

    public synchronized void check() {
        resetAll();
    }

    public void resetAll() {
        hlwliste = null;
        startat = null;
        verteilung = null;
    }

    public synchronized void reset() {
        resetAll();
    }

    public final class WettkampfChangeListener implements PropertyChangeListener {
        @Serial
        private static final long serialVersionUID = -2220683491274451995L;

        @Override
        public void propertyChanged(Object source, String property) {
        }
    }

    public static class Einteilung implements Serializable {

        private final int ak;
        private final boolean male;

        public Einteilung() {
            this(0, false);
        }

        public Einteilung(int ak, boolean male) {
            this.ak = ak;
            this.male = male;
        }

        public int getAK() {
            return ak;
        }

        public boolean isMaennlich() {
            return male;
        }
    }

    public static void migrator3(Element node) {
        for (Element einteilung : node.element("verteilung").elements()) {
            for (Element entry : einteilung.elements().toArray(Element[]::new)) {
                if (entry.getName().equals("first")) {
                    entry.setName("ak");
                } else if (entry.getName().equals("second")) {
                    entry.setName("male");
                }
            }
        }
    }
}