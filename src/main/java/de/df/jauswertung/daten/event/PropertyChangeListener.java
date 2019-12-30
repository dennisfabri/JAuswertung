/*
 * Created on 30.06.2005
 */
package de.df.jauswertung.daten.event;

import java.io.Serializable;

public interface PropertyChangeListener extends Serializable {
    void propertyChanged(Object source, String property);
}
