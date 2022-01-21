/*
 * Export.java Created on 3. Oktober 2002, 12:52
 */

package de.df.jauswertung.io;

import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.DataTableUtils.RegistrationDetails;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.util.Feedback;

/**
 * @author dennis
 */
public class CsvExporter extends EmptyExporter {

    public static boolean excelmode = true;

    public CsvExporter() {
        // Nothing to do
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "csv" };
    }

    @Override
    public String getName() {
        return "CSV";
    }

    /**
     * Exportiert die Schwimmer eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param WK
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.results(wk, false, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Meldeliste eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param wk
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public synchronized <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            LinkedList<T> schwimmer = wk.getSchwimmer();
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_STARTNUMMER);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_NAME);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            ExtendedTableModel tm = DataTableUtils.registration(wk, schwimmer, RegistrationDetails.EVERYTHING, null, true, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Laufliste eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param wk
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.heats(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            JTable t = PrintUtils.getLaufuebersichtTable(wk);
            if (t == null) {
                return false;
            }
            TableModel tm = t.getModel();
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
    public synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.referees(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.zusatzwertung(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.startkarten(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#isSupported(int)
     */
    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case HEATLIST:
        case ZWLIST:
        case STARTKARTEN:
        case REGISTRATION:
        case RESULTS:
        case ZW_STARTKARTEN:
        case REFEREES:
        case TEAMMEMBERS:
        case BEST_TIMES:
        case ZW_RESULTS:
        case HEATS_OVERVIEW:
            return true;
        case HEATTIMES:
        default:
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.zusatzwertungStartkarten(wk, PrintUtils.printZWnames, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.teammembers(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.zusatzwertungResults(wk, null, fb, true, PrintUtils.printZWnames);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.getSchnellsteZeiten(wk, false);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.easywkHeattimes(wk, false);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}