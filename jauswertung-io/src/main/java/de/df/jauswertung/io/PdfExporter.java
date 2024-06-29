/*
 * Created on 16.01.2005
 */
package de.df.jauswertung.io;

import java.awt.print.Printable;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.swing.JTable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.gui.util.TableZWUtils;
import de.df.jauswertung.print.PenaltyCatalogPrintable;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.PrintableCreator;
import de.df.jauswertung.print.ProtocolPrintable;
import de.df.jauswertung.print.RefereesPrintableCreator;
import de.df.jauswertung.print.StartkartenPrintable;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.io.PdfOutput;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.JTablePrintable;
import de.df.jutils.util.Feedback;

/**
 * @author Dennis Fabri
 * @date 16.01.2005
 */
public class PdfExporter extends EmptyExporter {

    @Override
    public String[] getSuffixes() {
        return new String[] { "pdf" };
    }

    @Override
    public String getName() {
        return "PDF";
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        return switch (type) {
        case HEATLIST, REGISTRATION, STARTKARTEN, RESULTS, PROTOCOL, PENALTIES, REFEREES, ZW_RESULTS, BEST_TIMES -> true;
        default -> false;
        };
    }

    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        LinkedList<T> schwimmer = wk.getSchwimmer();
        if (schwimmer == null) {
            return false;
        }
        Printable printable = PrintManager.getPrintable(
                DataTableUtils.registration(wk, schwimmer, DataTableUtils.RegistrationDetails.SHORT,
                        (wk instanceof EinzelWettkampf
                                ? new boolean[] { true, true, true, true, true, true, false, false, false, false,
                                        false }
                                : new boolean[] { true, true, false, true, true, true, false, false, false, false,
                                        false }),
                        false, true, null),
                (String) null, JTablePrintable.OPT_ALL, false, true);
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(printable, wk.getLastChangedDate(), I18n.get("Registrations"),
                        I18n.get("Registrations")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        JTable table = TableHeatUtils.getLaufliste(wk, PrintUtils.printEmptyLanes);
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(
                        PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true),
                        wk.getLastChangedDate(), I18n.get("Laufliste"), I18n.get("Laufliste")),
                false, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(PrintUtils.getResultsPrintable(wk, false, true, false, 0),
                        wk.getLastChangedDate(), true, I18n.get("Results")),
                false, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name,
                new StartkartenPrintable(wk, PageMode.FOUR_PER_PAGE, PrintUtils.printEmptyCards, true, 0, 0), true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        Printable p = PrintManager.getFinalPrintable(new ProtocolPrintable<>(wk), wk.getLastChangedDate(),
                new MessageFormat(I18n.get("Protocol")),
                I18n.get("Protocol"));
        return PdfOutput.write(name, p, true, fb);
    }

    /**
     * Exportiert die Kampfrichter eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name Name der Datei
     * @param wk   Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(RefereesPrintableCreator.createRefereesPrintable(wk),
                        wk.getLastChangedDate(), false, I18n.get("Referees")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(new PenaltyCatalogPrintable<T>(wk), wk.getLastChangedDate(),
                        I18n.get("PenaltyCatalog"),
                        I18n.get("PenaltyCatalog")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        ExtendedTableModel[] etm = DataTableUtils.zusatzwertungResults(wk, fb, PrintUtils.printZWnames);
        if (etm == null) {
            return false;
        }
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(PrintManager.getPrintable(etm, JTablePrintable.OPT_ALL, true, true),
                        wk.getLastChangedDate(), I18n.get("ZWResults"), I18n.get("ZWResults")),
                true, fb);
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        Printable p = PrintableCreator.createBestzeitenPrintable(wk);
        if (p == null) {
            return false;
        }
        return PdfOutput.write(name,
                PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("SchnellsteZeiten"),
                        I18n.get("SchnellsteZeiten")),
                true, fb);
    }
}