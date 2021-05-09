/*
 * Created on 15.03.2006
 */
package de.df.jauswertung.daten;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Filter implements Serializable {

    private static final long serialVersionUID = -3906984074449558946L;

    @XStreamAsAttribute
    private String            name             = "";
    private String[]          gliederungen     = null;

    public Filter() {
        this(null, null);
    }

    public Filter(String n, String[] fs) {
        if (n == null) {
            n = "";
        }
        name = n;
        gliederungen = fs;
    }

    public String getName() {
        return name;
    }

    public String[] getGliederungen() {
        return gliederungen;
    }

    @Override
    public String toString() {
        return name;
    }
}