package de.df.jauswertung.misc.recupdater;

import java.io.*;
import java.util.LinkedList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.*;
import de.df.jutils.io.FileUtils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.SystemOutFeedback;

public class WorldRecordsImporter implements IImporter {

    private final String filename;
    private final int    year;

    public WorldRecordsImporter(String filename, int year) {
        this.filename = filename;
        this.year = year;
    }

    @Override
    public void execute(Records records) {
        try {
            LinkedList<Record> times = readRecords(filename, year, new SystemOutFeedback());
            records.update(times);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static LinkedList<Record> readRecords(String filename, int year, Feedback fb) throws Exception, TableEntryException, TableException, IOException {
        fb.showFeedback(I18n.get("LoadingFile") + " " + filename);

        FileInputStream is = new FileInputStream(filename);
        byte[] data = FileUtils.readFile(is);
        is.close();

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return tablesToRecords(fb, titles, tables, year);
        } catch (OfficeXmlFileException e) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);

            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return tablesToRecords(fb, titles, tables, year);
        } finally {
            fb.showFeedback("Laden beendet");
        }
    }

    private static String[][] nations = new String[][] { { "GER", "Deutschland" }, { "FRA", "Frankreich" }, { "ITA", "Italien" }, { "BEL", "Belgien" }, };

    private static LinkedList<Record> tablesToRecords(Feedback fb, String[] titles, Object[][][] tables, int year) throws Exception {
        LinkedList<Record> records = new LinkedList<Record>();

        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            Object[][] sheet = tables[i];

            for (int row = 1; row < sheet.length; row++) {
                String competition = sheet[row][10].toString().trim() + " " + year;
                boolean male = ImportUtils.getMaennlich(null, sheet[row][1].toString().trim(), 1, row, title, "");
                String discipline = sheet[row][2].toString().trim();
                int time = getTime(sheet[row][3], "", title, row, 4);

                boolean team = sheet[row][5].toString().trim().length() == 0;
                String name;
                if (team) {
                    name = sheet[row][6].toString().trim();
                    if (name.equalsIgnoreCase("National Team")) {
                        name = sheet[row][8].toString().trim();
                        for (String[] nation : nations) {
                            if (name.equalsIgnoreCase(nation[0])) {
                                name = nation[1];
                            }
                        }
                    }
                } else {
                    name = sheet[row][4].toString().trim() + ", " + sheet[row][5].toString().trim();
                }

                records.add(new Record(competition, "AK Offen", male, discipline, time, name, team));
                records.add(new Record(competition, "AK 17/18", male, discipline, time, name, team));
            }
        }

        return records;
    }

    private static int getTime(Object o, String file, String sheet, int row, int column) {
        int result = 0;
        try {
            if (o instanceof String) {
                try {
                    o = Double.parseDouble((String) o);
                } catch (RuntimeException re) {
                    // Nothing to do
                }
            }
            if (o instanceof Number) {
                double d = ((Number) o).doubleValue();
                if (d < 0.1) {
                    if (d < 0) {
                        d = 0;
                    } else {
                        d = d * 24 * 60 * 60 * 100;
                    }
                } else {
                    if (d < 100) {
                        d *= 100;
                    }
                }
                while (d >= 60 * 60 * 100) {
                    d -= 60 * 60 * 100;
                }
                result = (int) Math.round(d);
            } else {
                String s = o.toString().toLowerCase().trim();
                if (s.equals("+") || s.equals("x") || (s.length() == 0)) {
                    result = 0;
                } else {
                    if (s.equals("-")) {
                        result = 0;
                    } else {
                        result = getTime(s, file, sheet, row, column);
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Dummy");
        }
        return result;
    }
}
