package de.df.jauswertung.ares.gui;

import java.io.File;

class FileLocation {

    private final String filename;
    private final String name;

    FileLocation(String filename) {
        if (filename == null) {
            throw new NullPointerException();
        }
        this.filename = filename;
        int pos1 = filename.lastIndexOf(File.separator) + 1;
        int pos2 = filename.length();
        if (filename.toLowerCase().endsWith(".wk")) {
            pos2 -= 3;
        }
        name = filename.substring(pos1, pos2);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getFilename() {
        return filename;
    }
}