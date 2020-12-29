/*
 * Created on 24.01.2006
 */
package de.df.jauswertung.io;

public final class TableFormatException extends Exception {

    private static final long serialVersionUID = -6431012784221130245L;

    private final int[]       data;
    private String            sheet;
    private String            file;

    public TableFormatException(int[] d, String file, String sheet) {
        super("Not all required fields found!");
        data = d;
        this.file = file;
        this.sheet = sheet;
    }

    public int[] getData() {
        return data;
    }

    public String getFile() {
        return file;
    }

    public String getSheet() {
        return sheet;
    }
}