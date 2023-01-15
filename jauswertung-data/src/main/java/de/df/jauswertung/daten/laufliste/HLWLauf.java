/*
 * HLW.java Created on 1. November 2001, 11:27
 */

package de.df.jauswertung.daten.laufliste;

import de.df.jauswertung.daten.ASchwimmer;

/**
 * @author Dennis Fabri
 * @version
 */
public class HLWLauf<T extends ASchwimmer> extends Lauf<T> {

    private static final long serialVersionUID = -5605619617566626988L;

    private Time name;

    /** Creates new HLW */
    HLWLauf(int bahnen) {
        super(bahnen, 0, 0);
        setLeftToRight(true);
        setName(null);
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    public Time getTime() {
        return name;
    }

    public void setName(Time uhrzeit) {
        if (uhrzeit == null) {
            return;
        }
        name = uhrzeit;
    }
}