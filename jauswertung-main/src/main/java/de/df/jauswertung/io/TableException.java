/*
 * Created on 24.01.2006
 */
package de.df.jauswertung.io;

public final class TableException extends Exception {

    private static final long serialVersionUID = -6431012784221130245L;

    private final String      sheet;
    private final String      file;

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