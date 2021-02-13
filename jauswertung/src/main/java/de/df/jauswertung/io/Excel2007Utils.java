package de.df.jauswertung.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class Excel2007Utils {

    private Excel2007Utils() {
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
        XSSFWorkbook wb = new XSSFWorkbook(name);
        return sheetToTable(wb, sheetnumber);
    }

    public static Object[][][] sheetsToTable(InputStream name) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(name);
        return sheetsToTable(wb);
    }

    public static Object[][][] sheetsToTable(XSSFWorkbook wb) {
        int amount = wb.getNumberOfSheets();
        Object[][][] result = new Object[amount][0][0];
        for (int x = 0; x < amount; x++) {
            result[x] = sheetToTable(wb, x);
        }
        return result;
    }

    public static Object[][] sheetToTable(XSSFWorkbook wb, int sheetnumber) {
        FormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        return ExcelReader.sheetToTable(wb, sheetnumber, evaluator);
    }
}
