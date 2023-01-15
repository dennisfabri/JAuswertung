package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAliasType;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAliasType("MMGEK")
public class Mannschaftsmitgliederexportkennung implements Serializable {
    @XStreamAsAttribute
    private String id;
    @XStreamAsAttribute
    private Date exportedOnDate;

    public Mannschaftsmitgliederexportkennung(String id) {
        this.id = id;
        this.exportedOnDate = new Date();
    }

    public String getId() {
        return id;
    }

    public Date getExportedOnDate() {
        return exportedOnDate;
    }
}
