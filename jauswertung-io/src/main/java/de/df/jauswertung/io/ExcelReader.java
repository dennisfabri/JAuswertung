package de.df.jauswertung.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class ExcelReader {

    private ExcelReader() {
        // Hide
    }

    public static Object[][] sheetToTable(String s, int sheetnumber) throws IOException {
        FileInputStream fis = new FileInputStream(s);
        Object[][] result = sheetToTable(fis, sheetnumber);
        fis.close();
        return result;
    }

    public static Object[][] sheetsToTable(String s) throws IOException {
        FileInputStream fis = new FileInputStream(s);
        Object[][][] result = sheetsToTable(fis);
        fis.close();
        return result;
    }

    public static Object[][] sheetToTable(InputStream name, int sheetnumber) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(name);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        return sheetToTable(wb, sheetnumber);
    }

    public static Object[][][] sheetsToTable(InputStream name) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(name);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        return sheetsToTable(wb);
    }

    public static Object[][][] sheetsToTable(HSSFWorkbook wb) {
        int amount = wb.getNumberOfSheets();
        Object[][][] result = new Object[amount][0][0];
        for (int x = 0; x < amount; x++) {
            result[x] = sheetToTable(wb, x);
        }
        return result;
    }

    public static Object[][] sheetToTable(HSSFWorkbook wb, int sheetnumber) {
        // HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        return sheetToTable(wb, sheetnumber, evaluator);
    }

    protected static Object[][] sheetToTable(Workbook wb, int sheetnumber, FormulaEvaluator evaluator) {
        if (wb == null) {
            throw new IllegalArgumentException("Workbook must not be null.");
        }
        if (evaluator == null) {
            throw new IllegalArgumentException("FormulaEvaluator must not be null.");
        }

        Sheet sheet = wb.getSheetAt(sheetnumber);
        if (wb.isSheetHidden(sheetnumber) || wb.isSheetVeryHidden(sheetnumber)) {
            return null;
        }
        short length = 0;
        {
            Row row = sheet.getRow(0);
            if (row == null) {
                return null;
            }
            try {
                @SuppressWarnings("rawtypes")
                Iterator cells = row.cellIterator();
                while (cells.hasNext()) {
                    Cell cell = (Cell) cells.next();

                    if (cell.getCellType() != CellType.BLANK) {
                        length = (short) Math.max(length, cell.getColumnIndex() + 1);
                    }
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                // Nothing to do
                // This exception is probably thrown because of numbers in the
                // title
            }
        }

        if (length == 0) {
            return null;
        }

        String sheetname = wb.getSheetName(sheetnumber);

        LinkedList<Object[]> lines = new LinkedList<>();
        Iterator<Row> it = sheet.rowIterator();
        while (it.hasNext()) {
            boolean success = false;
            // row = sheet.getRow(rowIndex);
            Row row = it.next();

            if (row != null) {
                Object[] dat = new Object[length + 1];
                dat[length] = sheetname;
                if (lines.isEmpty()) {
                    dat[length] = "Sheetname";
                }
                for (int x = 0; x < length; x++) {
                    dat[x] = "";
                    Cell cell = row.getCell(x);
                    dat[x] = evaluateCell(cell, evaluator);
                    if (dat[x].toString().length() > 0) {
                        success = true;
                    }
                }
                if (success) {
                    lines.addLast(dat);
                }
            }
        }

        return lines.toArray(new Object[lines.size()][0]);
    }

    private static Object evaluateCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell != null) {
            try {
                CellType celltype = cell.getCellType();
                if (celltype == CellType.FORMULA) {
                    celltype = cell.getCachedFormulaResultType();
                }
                switch (celltype) {
                default:
                    break;
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    return cell.getStringCellValue().trim();
                case BLANK:
                    return "";
                }
            } catch (RuntimeException re) {
                // Nothing to do
            }
            try {
                CellValue cellValue = evaluator.evaluate(cell);
                switch (cellValue.getCellType()) {
                default:
                    break;
                case BOOLEAN:
                    return cellValue.getBooleanValue();
                case NUMERIC:
                    return cellValue.getNumberValue();
                case STRING:
                    return cellValue.getStringValue().trim();
                case BLANK:
                    return "";
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
            // if everything failed:
            return "#Wert";
        }
        return "";
    }
}
