/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import java.io.IOException;
import java.io.OutputStream;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.util.Feedback;

/**
 * Diese Klasse sorgt fuer den Export der Ergebnisse in Excel-Dateien
 * 
 * @author dennis
 */
public class EasyWKExporter extends EmptyExporter {

    @Override
    public final boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case HEAT_TIMES:
            return true;
        default:
            return false;
        }
    }

    private <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel tm, int groupsize, int repeatrows,
            int repeatcols, String competition)
            throws IOException {
        write(name, new ExtendedTableModel[] { tm }, groupsize, repeatrows, repeatcols, competition);
    }

    @Override
    public final <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel etm = DataTableUtils.easywkHeattimes(wk);
            if (etm == null) {
                return false;
            }
            write(name, etm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel[] tms, int groupsize,
            int repeatrows, int repeatcols, String competition)
            throws IOException {
        CsvUtils.write(name, tms[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#getType()
     */
    @Override
    public String getName() {
        return "EasyWK";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#getSuffixes()
     */
    @Override
    public String[] getSuffixes() {
        return new String[] { "csv" };
    }
}