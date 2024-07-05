/*
 * Created on 12.02.2004
 */
package de.df.jauswertung.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jutils.io.FileUtils;
import de.df.jutils.util.Feedback;

/**
 * @author Dennis Fabri
 * @date 12.02.2004
 */
public final class ExcelImporter implements IImporter {

    public static final String[] SUFFIXES = { ".xls", ".xlsx", ".xlsm" };

    public ExcelImporter() {
        // Nothing to do
    }

    @Override
    public <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream is, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        if (w instanceof EinzelWettkampf) {
            return null;
        }
        fb.showFeedback(I18n.get("LoadingFile"));

        byte[] data = FileUtils.readFile(is);

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);

            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return ImportUtils.tablesToTeammembers(wk, fb, titles, tables, null);
        } catch (OfficeXmlFileException e) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);

            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return ImportUtils.tablesToTeammembers(wk, fb, titles, tables, null);
        }
    }

    @Override
    public <T extends ASchwimmer> Hashtable<ZWStartnummer, Double> zusatzwertungResults(InputStream is,
            AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        fb.showFeedback(I18n.get("LoadingFile"));

        byte[] data = FileUtils.readFile(is);

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }

            return ImportUtils.tablesToZWResult(wk, fb, titles, tables, null);
        } catch (OfficeXmlFileException io) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }

            return ImportUtils.tablesToZWResult(wk, fb, titles, tables, null);
        }
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream is, AWettkampf<T> wk, Feedback fb,
            LinkedList<T> data2, String filename)
            throws IOException, TableFormatException, TableEntryException, TableException {
        if (filename == null) {
            filename = "";
        }
        filename = filename.trim();
        fb.showFeedback(I18n.get("LoadingFileWithFilename", filename.length(), filename));

        byte[] data = FileUtils.readFile(is);

        LinkedList<T> result = null;

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }

            result = ImportUtils.tablesToRegistration(wk, fb, titles, tables, filename);
        } catch (OfficeXmlFileException io) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            result = ImportUtils.tablesToRegistration(wk, fb, titles, tables, filename);
        }
        if (result == null) {
            return data2;
        }
        if (data2 != null) {
            result.addAll(data2);
        }
        return result;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heats(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> zusatzwertung(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> results(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> KampfrichterVerwaltung referees(InputStream is, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, IOException {
        fb.showFeedback(I18n.get("LoadingFile"));

        byte[] data = FileUtils.readFile(is);

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return ImportUtils.tablesToReferees(wk, fb, titles, tables, null);
        } catch (OfficeXmlFileException io) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return ImportUtils.tablesToReferees(wk, fb, titles, tables, null);
        }
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case ZW_RESULTS:
        case REGISTRATION:
        case TEAM_MEMBERS:
        case REFEREES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String getName() {
        return "Microsoft Excel";
    }

    @Override
    public String[] getSuffixes() {
        return SUFFIXES;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heattimes(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registrationUpdate(InputStream name, AWettkampf<T> wk, Feedback fb,
            LinkedList<T> data, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> List<TeamWithStarters> starters(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return null;
    }
}