/*
 * Created on 24.01.2006
 */
package de.df.jauswertung.io;

public final class TableEntryException extends Exception {

    private static final long serialVersionUID = -6431012784221130245L;

    private final String      data;

    private String            sheet            = null;
    private String            file             = null;
    private int               row              = -1;
    private int               column           = -1;

    public TableEntryException(String d, String file, String sheet, int row, int column) {
        super("Wrong Entry format (" + d + ")!");
        data = d;
        this.row = row;
        this.column = column;
        this.sheet = sheet;
        this.file = file;
    }

    public String getSheet() {
        return sheet;
    }

    public String getFile() {
        return file;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getData() {
        return data;
    }
}