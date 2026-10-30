package de.df.jauswertung.daten;

import com.thoughtworks.xstream.annotations.XStreamAliasType;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAliasType("MMGEK")
public class Mannschaftsmitgliederexportkennung {
    @XStreamAsAttribute
    private String id;

    public Mannschaftsmitgliederexportkennung(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
