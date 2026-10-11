package de.df.jauswertung.daten;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAliasType;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAliasType("MMGEK")
public class Mannschaftsmitgliederexportkennung implements Serializable {
    @XStreamAsAttribute
    private String id;

    public Mannschaftsmitgliederexportkennung(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
