package de.df.jauswertung.misc.recupdater;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.SwingConstants;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.Excel2007Utils;
import de.df.jauswertung.io.ExcelReader;
import de.df.jauswertung.io.ExcelWriter;
import de.df.jauswertung.io.ImportUtils;
import de.df.jauswertung.io.TableEntryException;
import de.df.jauswertung.io.TableException;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.io.FileUtils;
import de.df.jutils.io.csv.Seconds;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.SystemOutFeedback;

public class RecordsUpdater {

    private static String OldRecords = "src/test/resources/competitions/Rekordwerte 2024.xlsx";
    private static String NewRecords = "src/test/resources/competitions/Rekordwerte 2024.xls";

    public static void main(String[] args) throws Exception {
        Records records = new Records(readRecords(OldRecords, new SystemOutFeedback()));
        
        // records.print();

        LinkedList<IImporter> importers = new LinkedList<>();
        // importers.add(new CompetitionImporter("dem2023-einzel.wk", "DEM2023"));
        // importers.add(new CompetitionImporter("dem2023-mannschaft.wk", "DEM2023"));
        importers.add(new CompetitionImporter("dmm2023_einzel.wk", "DMM2023"));
        importers.add(new CompetitionImporter("dmm2023_mannschaft.wk", "DMM2023"));
        // importers.add(new CompetitionImporter("dsm2023_einzel.wk", "DSM2023"));
        // importers.add(new CompetitionImporter("dsm2023_mannschaft.wk", "DSM2023"));
        // importers.add(new
        // WorldRecordsImporter("src/test/resources/competitions/WorldRecords-2018-12-13.xlsx",
        // 2018));

        for (IImporter importer : importers) {
            System.out.println(importer);
            importer.execute(records);
        }

        System.out.println("Nicht gefundene Rekorde:");
        for (Record record : records.getRecords()) {
            if (!record.isMatched()) {
                System.out.println(record);
            }
        }
        System.out.println("Aktualisierte Rekorde:");
        for (Record record : records.getRecords()) {
            if (record.isChanged()) {
                System.out.println(record);
            }
        }
        System.out.println("Writing records to " + NewRecords);
        writeRecords(records.getRecords());
        System.out.println("Fertig");
    }

    private static void writeRecords(Record[] records) throws Exception {
        LinkedList<Record> single = new LinkedList<>();
        LinkedList<Record> team = new LinkedList<>();

        for (Record record : records) {
            if (record.isTeam()) {
                team.addLast(record);
            } else {
                single.addLast(record);
            }
        }

        ExtendedTableModel[] etm = new ExtendedTableModel[2];
        etm[0] = recordsToTable(team, new SystemOutFeedback());
        etm[1] = recordsToTable(single, new SystemOutFeedback());

        etm[0].setName("Mannschaft");
        etm[1].setName("Einzel");

        FileOutputStream stream = new FileOutputStream(NewRecords);
        ExcelWriter.write(stream, etm, 1, 0, 0, null);
        stream.close();
    }

    public static ExtendedTableModel recordsToTable(LinkedList<Record> records, Feedback fb) {
        if (fb == null) {
            fb = new NullFeedback();
        }

        LinkedList<Integer> aligns = new LinkedList<>();
        LinkedList<String> formats = new LinkedList<>();
        LinkedList<Object> titles = new LinkedList<>();

        LinkedList<Object[]> result = new LinkedList<>();

        titles.addLast("Wettkampf");
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0");
        titles.addLast("Altersklasse");
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0");
        titles.addLast("m/w");
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0");
        titles.addLast("Disziplin");
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0");
        titles.addLast("Zeit");
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("m:ss.00");
        titles.addLast("Name");
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0");

        ListIterator<Record> li = records.listIterator();

        int length = records.size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent + 5) {
                percent = per;
                double y = ((double) percent) / 110;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            Record t = li.next();

            LinkedList<Object> row = new LinkedList<>();

            row.addLast(t.getCompetition());
            row.addLast(t.getAgegroup());
            row.addLast(t.isMale() ? "m" : "w");
            row.addLast(t.getDiscipline());
            row.addLast(new Seconds(t.getTime()));
            row.addLast(t.getName());

            result.addLast(row.toArray());
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));

        return tm;
    }

    private static LinkedList<Record> readRecords(String filename, Feedback fb)
            throws Exception, TableEntryException, TableException, IOException {
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
            return tablesToRecords(fb, titles, tables);
        } catch (OfficeXmlFileException e) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);

            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return tablesToRecords(fb, titles, tables);
        } finally {
            fb.showFeedback("Laden beendet");
        }
    }

    private static LinkedList<Record> tablesToRecords(Feedback fb, String[] titles, Object[][][] tables)
            throws Exception {
        LinkedList<Record> records = new LinkedList<>();

        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            if (!"Mannschaft".equals(title) && !"Einzel".equals(title)) {
                continue;
            }
            Object[][] sheet = tables[i];
            boolean team = "Mannschaft".equals(title);

            for (int row = 2; row < sheet.length; row++) {
                String competition = sheet[row][0].toString().trim();
                String agegroup = sheet[row][1].toString().trim();
                boolean male = ImportUtils.getMaennlich(null, sheet[row][2].toString().trim(), 2, row, title, "");
                String discipline = sheet[row][3].toString().trim();
                int time = getTime(sheet[row][4], "", title, row, 4);
                String name = sheet[row][5].toString().trim();

                records.add(new Record(competition, agegroup, male, discipline, time, name, team));
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
