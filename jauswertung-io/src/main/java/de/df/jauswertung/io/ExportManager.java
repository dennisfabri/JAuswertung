/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JTable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.exception.NotEnabledException;
import de.df.jauswertung.io.exception.NotSupportedException;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.SystemOutFeedback;

/**
 * @author Dennis Fabri
 * @date 08.01.2005
 */
public class ExportManager {

    public static final String[] NAMES = new String[] { I18n.get("Registrations"), I18n.get("Laufliste"),
            I18n.get("Startkarten"), I18n.get("Heatoverview"), I18n.get("ZWList"), I18n.get("ZWStartkarten"),
            I18n.get("Results"), I18n.get("ZWResults"), I18n.get("Protocol"), I18n.get("Referees"),
            I18n.get("Weitermeldung"), I18n.get("PenaltyCatalog"), I18n.get("Teammembers"),
            I18n.get("SchnellsteZeiten"), I18n.get("Heattimes"), I18n.get("Times") };

    private static ExportManager manager = new ExportManager();

    private HashMap<String, IExporter> exporters;

    public ExportManager() {
        exporters = new HashMap<>();
        put(new HtmlExporter());
        put(new HtmlZipExporter());
        put(new CsvExporter());
        put(new ExcelExporter());
        put(new XmlExporter());
        put(new PdfExporter());
        put(new TeammembersExporter());
        put(new EasyWKExporter());
        put(new JSONExporter());
    }

    public synchronized void put(IExporter e) {
        exporters.put(e.getName(), e);
    }

    private static IExporter getExporter(String name) {
        return manager.exporters.get(name);
    }

    public static ExportManager getInstance() {
        return manager;
    }

    public static <T extends ASchwimmer> boolean isEnabled(AWettkampf<T> wk, ImportExportTypes datatype) {
        if (wk == null) {
            return false;
        }
        if (!isSupported(datatype)) {
            return false;
        }
        switch (datatype) {
        case HEATLIST:
        case STARTKARTEN:
            return !wk.getLaufliste().isEmpty();
        case HEATTIMES:
            return wk.isHeatBased() && wk.hasLaufliste();
        case ZWLIST:
        case ZW_STARTKARTEN:
            return !wk.getHLWListe().isEmpty();
        case PROTOCOL:
        case REGISTRATION:
        case RESULTS:
        case BEST_TIMES:
        case TIMES:
        case WEITERMELDUNG:
            return wk.hasSchwimmer();
        case REFEREES:
            return wk.getKampfrichterverwaltung() != null;
        case PENALTIES:
            return true;
        case TEAMMEMBERS:
            return wk.hasSchwimmer() && (wk instanceof MannschaftWettkampf);
        case ZW_RESULTS:
            if (!wk.hasSchwimmer()) {
                return false;
            }
            ListIterator<T> li = wk.getSchwimmer().listIterator();
            while (li.hasNext()) {
                if (li.next().getAK().hasHLW()) {
                    return true;
                }
            }
            return false;
        case HEATS_OVERVIEW:
            JTable t = PrintUtils.getLaufuebersichtTable(wk);
            return (t != null);
        default:
            return false;
        }
    }

    public static <T extends ASchwimmer> boolean export(String format, OutputStream os, ImportExportTypes datatype, AWettkampf<T> wk,
            Feedback fb) throws NullPointerException, NotSupportedException, NotEnabledException {
        if (format == null) {
            throw new NullPointerException();
        }
        return export(getExporter(format), os, datatype, wk, fb);
    }

    public static <T extends ASchwimmer> boolean export(IExporter ie, OutputStream os, ImportExportTypes datatype, AWettkampf<T> wk,
            Feedback fb) throws NullPointerException, NotSupportedException, NotEnabledException {
        if (ie == null) {
            throw new NullPointerException();
        }
        if (!ie.isSupported(datatype)) {
            if ((datatype != ImportExportTypes.WEITERMELDUNG) || !ie.isSupported(ImportExportTypes.REGISTRATION)) {
                throw new NotSupportedException();
            }
        }
        if (!isEnabled(wk, datatype)) {
            throw new NotEnabledException("" + datatype);
        }
        if (fb == null) {
            fb = new SystemOutFeedback();
        }
        switch (datatype) {
        case STARTKARTEN:
            return ie.startkarten(os, wk, fb);
        case HEATLIST:
            return ie.heats(os, wk, fb);
        case ZW_STARTKARTEN:
            return ie.zusatzwertungStartkarten(os, wk, fb);
        case ZWLIST:
            return ie.zusatzwertung(os, wk, fb);
        case PROTOCOL:
            return ie.protocol(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case REGISTRATION:
            return ie.registration(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case BEST_TIMES:
            return ie.bestezeiten(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case RESULTS:
            return ie.results(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case REFEREES:
            return ie.referees(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case PENALTIES:
            return ie.penalties(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case WEITERMELDUNG:
            return ie.registration(os,
                    ResultUtils.convertResultsToMeldung(CompetitionUtils.getFilteredInstance(wk), false), fb);
        case TEAMMEMBERS:
            return ie.teammembers(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case ZW_RESULTS:
            return ie.zusatzwertungResults(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case HEATS_OVERVIEW:
            return ie.heatsoverview(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case HEATTIMES:
            return ie.heattimes(os, CompetitionUtils.getFilteredInstance(wk), fb);
        case TIMES:
            return ie.zeiten(os, CompetitionUtils.getFilteredInstance(wk), fb);
        default:
            return false;
        }
    }

    public static <T extends ASchwimmer> boolean export(ImportExportTypes datatype, String name, String format, AWettkampf<T> wk,
            Feedback fb) throws IOException, NullPointerException, NotSupportedException, NotEnabledException {
        boolean result = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(name);
            result = export(format, out, datatype, wk, fb);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            new File(name).delete();
            throw ioe;
        } finally {
            if (out != null) {
                out.close();
            }
            if (!result) {
                new File(name).delete();
            }
        }
        return result;

    }

    public static String[] getSupportedFormats() {
        Set<String> keys = manager.exporters.keySet();
        String[] types = new String[manager.exporters.size()];
        int x = 0;
        for (String key : keys) {
            types[x] = key;
            x++;
        }
        Arrays.sort(types);
        return types;
    }

    public static boolean isSupported(String format, ImportExportTypes type) {
        IExporter e = getExporter(format);
        if (e == null) {
            return false;
        }
        if (type == ImportExportTypes.WEITERMELDUNG) {
            type = ImportExportTypes.REGISTRATION;
        }
        return e.isSupported(type);
    }

    public static boolean isSupported(ImportExportTypes type) {
        if (type == ImportExportTypes.WEITERMELDUNG) {
            type = ImportExportTypes.REGISTRATION;
        }
        return isSupportedI(type);
    }

    private static boolean isSupportedI(ImportExportTypes type) {
        return getInstance().exporters.values().stream().anyMatch(e -> e.isSupported(type));
    }

    public static String[] getSuffixes(String format) {
        return getExporter(format).getSuffixes();
    }

    public static String getName(String format) {
        return getExporter(format).getName();
    }
}