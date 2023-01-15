/*
 * StrafenKapitel.java Created on 7. August 2001, 20:50
 */

package de.df.jauswertung.daten.regelwerk;

/**
 * @author Dennis Fabri
 * @version 0.1
 */

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class StrafenKapitel implements Serializable {

    private static final long serialVersionUID = -7264804032680202370L;

    private LinkedList<StrafenParagraph> paragraphen;
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String kapitel;

    public StrafenKapitel(int kap) {
        paragraphen = new LinkedList<>();
        name = "";
        kapitel = "" + kap;
    }

    @Override
    public String toString() {
        return kapitel + " " + name;
    }

    public String getName() {
        return name;
    }

    public String getKapitel() {
        return kapitel;
    }

    public void setName(String sName) {
        if (sName == null) {
            return;
        }
        if (sName.length() == 0) {
            return;
        }
        name = sName;
    }

    public void setKapitel(String kName) {
        if (kName == null) {
            return;
        }
        if (kName.length() == 0) {
            return;
        }
        kapitel = kName;
    }

    public void addParagraph(StrafenParagraph sp) {
        removeParagraph(sp);
        paragraphen.addLast(sp);
    }

    public final Strafe getStrafe(String code) {
        ListIterator<StrafenParagraph> li = paragraphen.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next().getStrafe(code);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public LinkedList<StrafenParagraph> getParagraphen() {
        return new LinkedList<>(paragraphen);
    }

    public void removeParagraph(StrafenParagraph sp) {
        ListIterator<StrafenParagraph> li = paragraphen.listIterator();
        if (li.hasNext()) {
            do {
                StrafenParagraph temp = li.next();
                if (temp.equals(sp)) {
                    li.remove();
                }
            } while (li.hasNext());
        }
    }
}