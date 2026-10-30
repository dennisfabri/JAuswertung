/*
 * Created on 24.01.2006
 */
package de.df.jauswertung.io;

public final class TableException extends Exception {

    private final String sheet;
    private final String file;

    public TableException(String d, String file, String sheet) {
        super(d);
        this.file = file;
        this.sheet = sheet;
    }

    public String getSheet() {
        return sheet;
    }

    public String getFile() {
        return file;
    }

}