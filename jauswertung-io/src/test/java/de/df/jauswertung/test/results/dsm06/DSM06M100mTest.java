/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.results.dsm06;

import java.io.FileNotFoundException;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.io.*;
import de.df.jauswertung.test.util.TestImportUtils;
import de.df.jauswertung.test.util.TestUtils;
import de.df.jauswertung.util.ergebnis.*;
import junit.framework.TestCase;

public class DSM06M100mTest extends TestCase {

    private static final String FILE = "src/test/resources/competitions/dsm06/mannschaft-ak100m.csv";
    private static final boolean MALE = true;
    private static final int AK = 5;

    public DSM06M100mTest(String x) {
        super(x);
    }

    private static MannschaftWettkampf wk = null;

    private static SchwimmerResult<Mannschaft>[] results = null;
    private static Object[][] data = null;

    @Override
    protected void setUp() throws Exception {
        if (wk == null) {
            wk = new MannschaftWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwm"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", false));
            data = TestImportUtils.importFile(wk, FILE);
            results = ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(AK), MALE);
        }
    }

    public void testResults() throws TableFormatException, FileNotFoundException {
        assertEquals(data[0].length, results.length);
        for (int x = 0; x < results.length; x++) {
            assertEquals(x + 1, results[x].getSchwimmer().getStartnummer());
        }
    }

    public void testResult1() throws TableFormatException, FileNotFoundException {
        int x = 0;
        checkRow(x);
    }

    public void testResult2() throws TableFormatException, FileNotFoundException {
        int x = 1;
        checkRow(x);
    }

    public void testResult3() throws TableFormatException, FileNotFoundException {
        int x = 2;
        checkRow(x);
    }

    public void testResult4() throws TableFormatException, FileNotFoundException {
        int x = 3;
        checkRow(x);
    }

    public void testResult5() throws TableFormatException, FileNotFoundException {
        int x = 4;
        checkRow(x);
    }

    public void testResult6() throws TableFormatException, FileNotFoundException {
        int x = 5;
        checkRow(x);
    }

    public void testResult7() throws TableFormatException, FileNotFoundException {
        int x = 6;
        checkRow(x);
    }

    public void testResult8() throws TableFormatException, FileNotFoundException {
        int x = 7;
        checkRow(x);
    }

    public void testResult9() throws TableFormatException, FileNotFoundException {
        int x = 8;
        checkRow(x);
    }

    private void checkRow(int x) {
        checkRow(x, 0);
    }

    private void checkRow(int x, int offset) {
        TestUtils.checkRow(data, results, x, offset);
    }
}
