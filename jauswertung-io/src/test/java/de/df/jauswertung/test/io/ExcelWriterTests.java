package de.df.jauswertung.test.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.io.ExcelWriter;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleCell;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleRow;

class ExcelWriterTests {

    private static ExtendedTableModel createTable() {
        ExtendedTableModel tm = new ExtendedTableModel(
                new Object[][] { { "Erika Mustermann", 12.5, 1 }, { new String[] { "Max", "Mustermann" }, 13.25, 2 } },
                new Object[] { "Name", "Zeit", "Platz" });
        tm.setName("Ergebnisse");
        tm.setColumnFormats(new String[] { null, "0.00", "0" });
        tm.setExtendedTitles(new TitleRow[] {
                new TitleRow(new TitleCell[] { new TitleCell("Name", 1, 2), new TitleCell("Disziplin", 2, 1) }),
                new TitleRow(new TitleCell[] { new TitleCell("Zeit", 1, 1), new TitleCell("Platz", 1, 1) }) });
        return tm;
    }

    @Test
    void writesXlsxThatCanBeReadBack() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter.write(out, new ExtendedTableModel[] { createTable() }, 1, 0, 0, "Testwettkampf");

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            assertEquals(1, wb.getNumberOfSheets());
            Sheet sheet = wb.getSheet("Ergebnisse");

            // Extended titles
            assertTrue(sheet.getMergedRegions().contains(new CellRangeAddress(0, 1, 0, 0)));
            assertTrue(sheet.getMergedRegions().contains(new CellRangeAddress(0, 0, 1, 2)));
            assertEquals("Name", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Disziplin", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Zeit", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Platz", sheet.getRow(1).getCell(2).getStringCellValue());

            // Content
            assertEquals("Erika Mustermann", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals(CellType.NUMERIC, sheet.getRow(2).getCell(1).getCellType());
            assertEquals(12.5, sheet.getRow(2).getCell(1).getNumericCellValue());
            assertEquals("0.00", sheet.getRow(2).getCell(1).getCellStyle().getDataFormatString());
            assertEquals(2.0, sheet.getRow(3).getCell(2).getNumericCellValue());
            assertEquals("Max\nMustermann", sheet.getRow(3).getCell(0).getStringCellValue());
            assertTrue(sheet.getRow(3).getCell(0).getCellStyle().getWrapText());

            // Print setup
            assertTrue(sheet.getFitToPage());
            assertEquals(1, sheet.getPrintSetup().getFitWidth());
            assertTrue(sheet.getHeader().getLeft().contains("Testwettkampf"));
        }
    }
}
