/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import static de.df.jauswertung.io.ExportManager.HEATTIMES;

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
public class EasyWKExporter implements IExporter {

    @Override
    public final boolean isSupported(int type) {
        switch (type) {
        case HEATTIMES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public final synchronized <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    private <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel tm, int groupsize, int repeatrows, int repeatcols, String competition)
            throws IOException {
        write(name, new ExtendedTableModel[] { tm }, groupsize, repeatrows, repeatcols, competition);
    }

    @Override
    public final <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    /**
     * Exportiert die Kampfrichter eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param wk
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public final synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
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
            ExtendedTableModel etm = DataTableUtils.easywkHeattimes(wk, false);
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

    private <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel[] tms, int groupsize, int repeatrows, int repeatcols, String competition)
            throws IOException {
        CsvUtils.write(name, tms[0]);

        // ExcelWriter.write(name, tms, groupsize, repeatrows, repeatcols, competition);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#getType()
     */
    @Override
    public String getName() {
        return "EasyWK";
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#getSuffixes()
     */
    @Override
    public String[] getSuffixes() {
        return new String[] { "csv" };
    }
}