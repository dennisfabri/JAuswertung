package de.df.jauswertung.test.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleElement;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTableStyleType;

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

            // Every second row is filled with light blue
            XSSFCellStyle colored = (XSSFCellStyle) sheet.getRow(3).getCell(1).getCellStyle();
            assertEquals(FillPatternType.SOLID_FOREGROUND, colored.getFillPattern());
            assertEquals("FFDDEBF7", colored.getFillForegroundXSSFColor().getARGBHex());
            assertEquals(FillPatternType.NO_FILL, sheet.getRow(2).getCell(1).getCellStyle().getFillPattern());

            // Print setup
            assertTrue(sheet.getFitToPage());
            assertEquals(1, sheet.getPrintSetup().getFitWidth());
            assertTrue(sheet.getHeader().getLeft().contains("Testwettkampf"));
        }
    }

    private static ExtendedTableModel createPlainTable(String name, Object[][] data, Object... titles) {
        ExtendedTableModel tm = new ExtendedTableModel(data, titles);
        tm.setName(name);
        return tm;
    }

    private static XSSFWorkbook write(ExtendedTableModel[] tms, int groupsize) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriter.write(out, tms, groupsize, 0, 0, null);
        return new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
    }

    private static XSSFWorkbook write(ExtendedTableModel[][] tms) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String[] titles = new String[tms.length];
        Integer[] repeat = new Integer[tms.length];
        for (int i = 0; i < tms.length; i++) {
            titles[i] = tms[i][0].getName();
            repeat[i] = 0;
        }
        ExcelWriter.write(out, tms, 1, repeat, repeat, titles, null);
        return new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
    }

    @Test
    void writesPlainTablesAsExcelTables() throws IOException {
        ExtendedTableModel tm = createPlainTable("Meldung",
                new Object[][] { { "Erika", 12.5 }, { "Max", 13.25 }, { "Lisa", 14.0 } }, "Name", "Zeit");
        tm.setColumnFormats(new String[] { null, "0.00" });
        ExtendedTableModel second = createPlainTable("Kampfrichter", new Object[][] { { "Anna" } }, "Name");

        try (XSSFWorkbook wb = write(new ExtendedTableModel[] { tm, second }, 1)) {
            XSSFSheet sheet = wb.getSheet("Meldung");
            assertEquals(1, sheet.getTables().size());
            XSSFTable table = sheet.getTables().get(0);
            assertEquals("Tabelle1", table.getName());
            assertEquals("A1:B4", table.getArea().formatAsString());
            assertEquals("JAuswertung", table.getStyleName());
            assertTrue(table.getStyle().isShowRowStripes());
            assertEquals("A1:B4", table.getCTTable().getAutoFilter().getRef());
            assertEquals("Name", table.getColumns().get(0).getName());
            assertEquals("Zeit", table.getColumns().get(1).getName());

            // Cells keep their number format but leave borders and fill to the table style
            XSSFCellStyle style = sheet.getRow(2).getCell(1).getCellStyle();
            assertEquals("0.00", style.getDataFormatString());
            assertEquals(FillPatternType.NO_FILL, style.getFillPattern());
            assertEquals(BorderStyle.NONE, style.getBorderTop());

            assertEquals(FillPatternType.NO_FILL, sheet.getRow(0).getCell(0).getCellStyle().getFillPattern());
            assertEquals(0, sheet.getSheetConditionalFormatting().getNumConditionalFormattings());

            // Header and row stripes come from a custom table style: header and second row stripe in light blue
            CTStylesheet stylesheet = wb.getStylesSource().getCTStylesheet();
            CTTableStyle tableStyle = stylesheet.getTableStyles().getTableStyleArray(0);
            assertEquals("JAuswertung", tableStyle.getName());
            assertEquals(2, tableStyle.sizeOfTableStyleElementArray());
            CTTableStyleElement headerRow = tableStyle.getTableStyleElementArray(0);
            CTTableStyleElement stripe = tableStyle.getTableStyleElementArray(1);
            assertEquals(STTableStyleType.HEADER_ROW, headerRow.getType());
            assertEquals(STTableStyleType.SECOND_ROW_STRIPE, stripe.getType());
            CTDxf headerFormat = stylesheet.getDxfs().getDxfArray((int) headerRow.getDxfId());
            CTDxf stripeFormat = stylesheet.getDxfs().getDxfArray((int) stripe.getDxfId());
            assertTrue(headerFormat.getFont().getBArray(0).getVal());
            assertArrayEquals(new byte[] { (byte) 0xFF, (byte) 0xDD, (byte) 0xEB, (byte) 0xF7 },
                    headerFormat.getFill().getPatternFill().getBgColor().getRgb());
            assertArrayEquals(new byte[] { (byte) 0xFF, (byte) 0xDD, (byte) 0xEB, (byte) 0xF7 },
                    stripeFormat.getFill().getPatternFill().getBgColor().getRgb());

            // Table names are unique within the workbook
            assertEquals("Tabelle2", wb.getSheet("Kampfrichter").getTables().get(0).getName());
        }
    }

    @Test
    void keepsClassicLayoutForSheetsThatAreNoPlainTables() throws IOException {
        ExtendedTableModel duplicateTitles = createPlainTable("Doppelt", new Object[][] { { "a", "b" } }, "Name", "Name");
        ExtendedTableModel emptyTitle = createPlainTable("Leer", new Object[][] { { "a", "b" } }, "Name", " ");
        ExtendedTableModel noRows = createPlainTable("Ohne Daten", new Object[0][0], "Name");
        ExtendedTableModel stacked1 = createPlainTable("Gestapelt", new Object[][] { { "a" } }, "Name");
        ExtendedTableModel stacked2 = createPlainTable("Gestapelt", new Object[][] { { "b" } }, "Name");

        try (XSSFWorkbook wb = write(new ExtendedTableModel[][] { { duplicateTitles }, { emptyTitle }, { noRows },
                { stacked1, stacked2 }, { createTable() } })) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                assertTrue(wb.getSheetAt(i).getTables().isEmpty(), wb.getSheetName(i));
            }
            // Classic layout: titles have borders and fill
            XSSFCellStyle title = wb.getSheet("Doppelt").getRow(0).getCell(0).getCellStyle();
            assertEquals(BorderStyle.THIN, title.getBorderTop());
            assertEquals(FillPatternType.SOLID_FOREGROUND, title.getFillPattern());
        }

        // Grouped rows keep their group shading
        ExtendedTableModel grouped = createPlainTable("Gruppiert", new Object[][] { { "a" }, { "b" } }, "Name");
        try (XSSFWorkbook wb = write(new ExtendedTableModel[] { grouped }, 2)) {
            assertFalse(wb.getSheetAt(0).getTables().size() > 0);
        }
    }
}
