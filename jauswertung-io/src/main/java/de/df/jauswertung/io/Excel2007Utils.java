package de.df.jauswertung.io;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class Excel2007Utils {

    private Excel2007Utils() {
        // Hide
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
