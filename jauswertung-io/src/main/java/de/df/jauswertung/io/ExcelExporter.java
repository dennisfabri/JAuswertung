/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import java.io.IOException;
import java.io.OutputStream;

import de.df.jutils.gui.jtable.ExtendedTableModel;

/**
 * Diese Klasse sorgt fuer den Export der Ergebnisse in Excel-Dateien
 * 
 * @author dennis
 */
public class ExcelExporter extends ASpreadsheetExporter {

    @Override
    protected void write(OutputStream name, ExtendedTableModel[] tms, int groupsize, int repeatrows, int repeatcols,
            String competition)
            throws IOException {
        ExcelWriter.write(name, tms, groupsize, repeatrows, repeatcols, competition);
    }

    @Override
    protected void write(OutputStream name, ExtendedTableModel[][] tms, int groupsize, Integer[] repeatrows,
            Integer[] repeatcols,
            String[] titles, String competition) throws IOException {
        ExcelWriter.write(name, tms, groupsize, repeatrows, repeatcols, titles, competition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#getType()
     */
    @Override
    public String getName() {
        return "Microsoft Excel";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#getSuffixes()
     */
    @Override
    public String[] getSuffixes() {
        return new String[] { "xls" };
    }
}