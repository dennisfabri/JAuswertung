package de.df.jauswertung.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.table.TableModel;

import de.df.jutils.io.FileUtils;
import de.df.jutils.io.csv.*;
import de.df.jutils.util.StringTools;

public class CsvUtils {

    public static Object[][] read(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            return read(is);
        } catch (IOException ex) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Nothing to do
                }
            }
        }
    }

    public static Object[][] read(InputStream name) {

        String[] lines = FileUtils.readTextFile(name, StandardCharsets.UTF_8.name());
        if ((lines == null) || (lines.length <= 1)) {
            return null;
        }
        char separator = CsvImporter.identifySeparator(lines[0]);
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
        return data;
    }

    public static void write(OutputStream os, TableModel tm) {
        CsvWriter writer;
        if (CsvExporter.excelmode) {
            DecimalFormat df = new DecimalFormat();
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();

            CsvManager.setSeparator(dfs.getPatternSeparator());
            writer = CsvManager.getWriterInstance();
            writer.addConverter(new CsvSimpleIntegerConverter());
            writer.addConverter(new CsvSimpleDoubleConverter(dfs.getDecimalSeparator(), 2));
            writer.addConverter(new CsvSimpleSecondsConverter(dfs.getDecimalSeparator()));
        } else {
            CsvManager.setSeparator(',');
            writer = CsvManager.getWriterInstance();
            writer.addConverter(new CsvSimpleIntegerConverter());
            writer.addConverter(new CsvSimpleDoubleConverter('.', 2));
            writer.addConverter(new CsvSimpleSecondsConverter('.'));
        }
        writer.write(tm, os, StandardCharsets.UTF_8);
    }

}
