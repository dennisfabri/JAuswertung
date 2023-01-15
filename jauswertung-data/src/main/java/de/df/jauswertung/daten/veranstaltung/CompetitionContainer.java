package de.df.jauswertung.daten.veranstaltung;

import java.io.File;
import java.io.Serializable;

public class CompetitionContainer implements Serializable {
    private String name;
    private String filename;

    public CompetitionContainer() {
        this("", "");
    }

    public CompetitionContainer(String name, String filename) {
        setName(name);
        setFilename(filename);
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFilename(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException();
        }
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    // @SuppressWarnings("rawtypes")
    // public AWettkampf getCompetition() {
    // return InputManager.ladeWettkampf(filename);
    // }

    public boolean exists() {
        return new File(filename).exists();
    }
}