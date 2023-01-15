/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.daten.kampfrichter;

import java.io.Serializable;
import java.util.LinkedList;

public class KampfrichterVerwaltung implements Serializable {

    private static final long serialVersionUID = 126306416716148176L;

    private final LinkedList<KampfrichterEinheit> einheiten;

    public KampfrichterVerwaltung() {
        einheiten = new LinkedList<KampfrichterEinheit>();
    }

    public KampfrichterEinheit getEinheit(int x) {
        return einheiten.get(x);
    }

    public KampfrichterEinheit getEinheit(String name) {
        for (KampfrichterEinheit ke : einheiten) {
            if (ke.getName().equals(name)) {
                return ke;
            }
        }
        return null;
    }

    public void addEinheit(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        KampfrichterEinheit ke = new KampfrichterEinheit(name);
        einheiten.addLast(ke);
    }

    public int getEinheitenCount() {
        return einheiten.size();
    }

    public void clearInput() {
        for (KampfrichterEinheit ke : einheiten) {
            ke.clearInput();
        }
    }

}