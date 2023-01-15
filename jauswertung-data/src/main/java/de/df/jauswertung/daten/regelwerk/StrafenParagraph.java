/*
 * StrafenParagraph.java Created on 7. August 2001, 20:29
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

public class StrafenParagraph implements Serializable {

    private static final long serialVersionUID = -4848924575259009244L;

    private LinkedList<Strafe> strafen = new LinkedList<>();
    @XStreamAsAttribute
    private String name = "";
    @XStreamAsAttribute
    private String par = "";

    public StrafenParagraph(int paragraph) {
        name = "Leer";
        par = "" + paragraph;
    }

    public boolean equals(StrafenParagraph sp) {
        if (!name.equals(sp.getName())) {
            return false;
        }
        return par.equals(sp.getPar());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StrafenParagraph) {
            return equals((StrafenParagraph) o);
        }
        return false;
    }

    public final Strafe getStrafe(String code) {
        ListIterator<Strafe> li = strafen.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next();
            if (s.getShortname().equals(code)) {
                return s;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return par + " " + name;
    }

    public String getName() {
        return name;
    }

    public String getPar() {
        return par;
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

    public void setPar(String pName) {
        if (pName == null) {
            return;
        }
        par = pName;
    }

    public void addStrafe(Strafe s) {
        removeStrafe(s);
        strafen.addLast(s);
    }

    public LinkedList<Strafe> getStrafen() {
        return new LinkedList<>(strafen);
    }

    public void removeStrafe(Strafe s) {
        ListIterator<Strafe> li = strafen.listIterator();
        if (li.hasNext()) {
            do {
                Strafe temp = li.next();
                if (temp.equals(s)) {
                    li.remove();
                }
            } while (li.hasNext());
        }
    }
}