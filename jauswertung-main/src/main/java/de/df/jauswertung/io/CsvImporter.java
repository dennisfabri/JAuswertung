/*
 * Created on 12.02.2004
 */
package de.df.jauswertung.io;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.io.FileUtils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;
import de.df.jutils.util.Tupel;

/**
 * @author Dennis Fabri
 * @date 12.02.2004
 */
public final class CsvImporter implements IImporter {

    private static final String[] SUFFIXES   = { ".csv" };

    private static final char[]   SEPARATORS = { ',', ',', ';' };

    static {
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) nf;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            SEPARATORS[0] = dfs.getPatternSeparator();
        }
    }

    public CsvImporter() {
        // Nothing to do
    }

    @Override
    public <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        fb.showFeedback(I18n.get("LoadingFile"));
        Object[][] data = CsvUtils.read(name);
        if (data == null) {
            fb.showFeedback(I18n.get("FileNotFoundOrEmpty"));
            return null;
        }
        return ImportUtils.tablesToTeammembers(wk, fb, new String[] { "" }, new Object[][][] { data }, null);
    }

    @Override
    public <T extends ASchwimmer> Hashtable<Tupel<Integer, Integer>, Double> zusatzwertungResults(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        fb.showFeedback(I18n.get("LoadingFile"));
        Object[][] data = CsvUtils.read(name);
        if (data == null) {
            fb.showFeedback(I18n.get("FileNotFoundOrEmpty"));
            return null;
        }

        return ImportUtils.tablesToZWResult(wk, fb, new String[] { "" }, new Object[][][] { data }, null);
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream name, AWettkampf<T> wk, Feedback fb, LinkedList<T> data2, String filename)
            throws TableFormatException, TableEntryException, TableException {
        if (filename == null) {
            filename = "";
        }
        filename = filename.trim();
        fb.showFeedback(I18n.get("LoadingFileWithFilename", filename.length(), filename));

        String[] lines = FileUtils.readTextFile(name);
        if ((lines == null) || (lines.length <= 1)) {
            fb.showFeedback(I18n.get("FileNotFoundOrEmpty"));
            return null;
        }
        char separator = identifySeparator(lines[0]);
        Object[][] data = new Object[lines.length][0];
        int maxline = 0;
        for (int x = 0; x < lines.length; x++) {
            String[] line = StringTools.separateCsvLine(lines[x], separator);
            if (line.length > maxline) {
                maxline = line.length;
            }
            data[x] = new Object[line.length];
            System.arraycopy(line, 0, data[x], 0, line.length);
        }

        // Ensure that the last column is always empty.
        // The last column is reserved for sheet-based agegroup detection
        // which does not work with csv
        maxline++;

        for (int x = 0; x < data.length; x++) {
            if (data[x].length < maxline) {
                Object[] line = new Object[maxline];
                System.arraycopy(data[x], 0, line, 0, data[x].length);
                for (int y = data[x].length; y < line.length; y++) {
                    line[y] = "";
                }
                data[x] = line;
            }
        }

        LinkedList<T> result = ImportUtils.tablesToRegistration(wk, fb, new String[] { "" }, new Object[][][] { data }, filename);
        if (result == null) {
            return data2;
        }
        if (data2 != null) {
            result.addAll(data2);
        }
        return result;
    }

    static char identifySeparator(String data) {
        for (char SEPARATOR : SEPARATORS) {
            int index = data.indexOf(SEPARATOR);
            if (index >= 0) {
                return SEPARATOR;
            }
        }
        return SEPARATORS[0];
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heats(InputStream name, AWettkampf<T> wk, Feedback fb) throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> zusatzwertung(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> results(InputStream name, AWettkampf<T> wk, Feedback fb) throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> KampfrichterVerwaltung referees(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        fb.showFeedback(I18n.get("LoadingFile"));
        Object[][] data = CsvUtils.read(name);
        if (data == null) {
            fb.showFeedback(I18n.get("FileNotFoundOrEmpty"));
            return null;
        }
        return ImportUtils.tablesToReferees(wk, fb, new String[] { "" }, new Object[][][] { data }, null);
    }

    @Override
    public boolean isSupported(int type) {
        switch (type) {
        case ImportManager.ZWRESULTS:
        case ImportManager.REGISTRATION:
        case ImportManager.TEAMMEMBERS:
        case ImportManager.REFEREES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String getName() {
        return "CSV";
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
}