/*
 * Created on 16.01.2005
 */
package de.df.jauswertung.io;

import static de.df.jauswertung.io.ExportManager.BEST_TIMES;
import static de.df.jauswertung.io.ExportManager.HEATLIST;
import static de.df.jauswertung.io.ExportManager.PENALTIES;
import static de.df.jauswertung.io.ExportManager.PROTOCOL;
import static de.df.jauswertung.io.ExportManager.REFEREES;
import static de.df.jauswertung.io.ExportManager.REGISTRATION;
import static de.df.jauswertung.io.ExportManager.RESULTS;
import static de.df.jauswertung.io.ExportManager.STARTKARTEN;
import static de.df.jauswertung.io.ExportManager.ZWLIST;
import static de.df.jauswertung.io.ExportManager.ZW_RESULTS;
import static de.df.jauswertung.io.ExportManager.ZW_STARTKARTEN;

import java.awt.print.Printable;
import java.io.OutputStream;
import java.util.LinkedList;

import javax.swing.JTable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.gui.plugins.print.BestzeitenPrinter;
import de.df.jauswertung.gui.plugins.print.ProtocolPrinter;
import de.df.jauswertung.gui.plugins.print.RefereePrinter;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.gui.util.TableZWUtils;
import de.df.jauswertung.print.PenaltyCatalogPrintable;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.StartkartenPrintable;
import de.df.jauswertung.print.ZWStartkartenPrintable;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.io.PdfOutput;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.util.Feedback;

/**
 * @author Dennis Fabri
 * @date 16.01.2005
 */
public class PdfExporter implements IExporter {

    @Override
    public String[] getSuffixes() {
        return new String[] { "pdf" };
    }

    @Override
    public String getName() {
        return "PDF";
    }

    @Override
    public boolean isSupported(int type) {
        switch (type) {
        case HEATLIST:
        case ZWLIST:
        case REGISTRATION:
        case STARTKARTEN:
        case RESULTS:
        case PROTOCOL:
        case ZW_STARTKARTEN:
        case PENALTIES:
        case REFEREES:
        case ZW_RESULTS:
        case BEST_TIMES:
            return true;
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#registration(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        LinkedList<T> schwimmer = wk.getSchwimmer();
        if (schwimmer == null) {
            return false;
        }
        Printable printable = PrintManager.getPrintable(
                DataTableUtils.registration(wk, schwimmer, DataTableUtils.RegistrationDetails.SHORT,
                        (wk instanceof EinzelWettkampf ? new boolean[] { true, true, true, true, true, true, false, false, false, false, false }
                                : new boolean[] { true, true, false, true, true, true, false, false, false, false, false }),
                        false, true, null),
                (String) null, JTablePrintable.OPT_ALL, false, true);
        return PdfOutput.write(name, PrintManager.getFinalPrintable(printable, wk.getLastChangedDate(), I18n.get("Registrations"), I18n.get("Registrations")),
                true, fb);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#heats(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        JTable table = TableHeatUtils.getLaufliste(wk, PrintUtils.printEmptyLanes);
        return PdfOutput.write(name, PrintManager.getFinalPrintable(PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true),
                wk.getLastChangedDate(), I18n.get("Laufliste"), I18n.get("Laufliste")), false, fb);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#hlw(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        Printable p = PrintManager.getPrintable(TableZWUtils.getZWUebersicht(wk), (String) null, JTablePrintable.OPT_ALL, true, true);
        return PdfOutput.write(name, PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("ZWList"), I18n.get("ZWList")), false, fb);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#results(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(PrintUtils.getResultsPrintable(wk, false, true, false, 0), wk.getLastChangedDate(), true, I18n.get("Results")),
                false, fb);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#startkarten(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name, new StartkartenPrintable(wk, PageMode.FOUR_PER_PAGE, PrintUtils.printEmptyCards, true, 0, 0), true, fb);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name, new ZWStartkartenPrintable(wk, PageMode.FOUR_PER_PAGE, true, 0, 0, true, false, PrintUtils.barcodeType), true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name, ProtocolPrinter.getPrintable(wk), true, fb);
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
    public <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name, PrintManager.getFinalPrintable(RefereePrinter.getPrintable(wk), wk.getLastChangedDate(), false, I18n.get("Referees")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name, PrintManager.getFinalPrintable(new PenaltyCatalogPrintable<T>(wk), wk.getLastChangedDate(), I18n.get("PenaltyCatalog"),
                I18n.get("PenaltyCatalog")), true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        ExtendedTableModel[] etm = DataTableUtils.zusatzwertungResults(wk, fb, PrintUtils.printZWnames);
        if (etm == null) {
            return false;
        }
        return PdfOutput.write(name, PrintManager.getFinalPrintable(PrintManager.getPrintable(etm, JTablePrintable.OPT_ALL, true, true),
                wk.getLastChangedDate(), I18n.get("ZWResults"), I18n.get("ZWResults")), true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        Printable p = BestzeitenPrinter.getPrintable(wk);
        if (p == null) {
            return false;
        }
        return PdfOutput.write(name, PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("SchnellsteZeiten"), I18n.get("SchnellsteZeiten")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }
}