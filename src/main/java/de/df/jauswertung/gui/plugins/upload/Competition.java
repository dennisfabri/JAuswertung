package de.df.jauswertung.gui.plugins.upload;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Competition {

    private long id;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private byte[] protocol;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getDate() {
        return date;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public byte[] getProtocol() {
        return protocol;
    }

    public void setProtocol(byte[] protocol) {
        this.protocol = protocol;
    }
}
