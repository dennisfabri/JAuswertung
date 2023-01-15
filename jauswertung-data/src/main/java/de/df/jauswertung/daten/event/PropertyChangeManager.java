/*
 * Created on 30.06.2005
 */
package de.df.jauswertung.daten.event;

import java.io.Serializable;
import java.util.LinkedList;

public class PropertyChangeManager implements Serializable {

    private static final long serialVersionUID = 1348391732920349046L;

    private LinkedList<PropertyChangeListener> listeners;
    private Object source;

    public PropertyChangeManager(Object src) {
        listeners = new LinkedList<PropertyChangeListener>();
        source = src;
    }

    public void clear() {
        listeners.clear();
    }

    public boolean remove(PropertyChangeListener pcl) {
        return listeners.remove(pcl);
    }

    public void add(PropertyChangeListener pcl) {
        listeners.addLast(pcl);
    }

    public void firePropertyChangeEvent(String property) {
        PropertyChangeListener[] pcls = null;
        synchronized (this) {
            pcls = listeners.toArray(new PropertyChangeListener[listeners.size()]);
        }
        for (PropertyChangeListener pcl : pcls) {
            try {
                pcl.propertyChanged(source, property);
            } catch (Exception t) {
                t.printStackTrace();
            }
        }
    }
}
