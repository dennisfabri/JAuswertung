/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.results.dm1999;

import java.io.FileNotFoundException;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.TableFormatException;
import de.df.jauswertung.test.util.TestImportUtils;
import de.df.jauswertung.test.util.TestUtils;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import junit.framework.TestCase;

public class DM1999MOffenwTest extends TestCase {

    private static final String FILE = "src/test/resources/competitions/dm1999/mannschaft-akoffenw.txt";
    private static final boolean MALE = false;
    private static final int AK = 4;

    public DM1999MOffenwTest(String x) {
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
            data = TestImportUtils.importFile(wk, AK, MALE, FILE, 1999);
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

    public void testResult10() throws TableFormatException, FileNotFoundException {
        int x = 9;
        checkRow(x);
    }

    public void testResult11() throws TableFormatException, FileNotFoundException {
        int x = 10;
        checkRow(x);
    }

    public void testResult12() throws TableFormatException, FileNotFoundException {
        int x = 11;
        checkRow(x);
    }

    public void testResult13() throws TableFormatException, FileNotFoundException {
        int x = 12;
        checkRow(x);
    }

    public void testResult14() throws TableFormatException, FileNotFoundException {
        int x = 13;
        checkRow(x);
    }

    public void testResult15() throws TableFormatException, FileNotFoundException {
        int x = 14;
        checkRow(x);
    }

    public void testResult16() throws TableFormatException, FileNotFoundException {
        int x = 15;
        checkRow(x);
    }

    public void testResult17() throws TableFormatException, FileNotFoundException {
        int x = 16;
        checkRow(x);
    }

    public void testResult18() throws TableFormatException, FileNotFoundException {
        int x = 17;
        checkRow(x);
    }

    public void testResult19() throws TableFormatException, FileNotFoundException {
        int x = 18;
        checkRow(x);
    }

    public void testResult20() throws TableFormatException, FileNotFoundException {
        int x = 19;
        checkRow(x);
    }

    public void testResult21() throws TableFormatException, FileNotFoundException {
        int x = 20;
        checkRow(x);
    }

    public void testResult22() throws TableFormatException, FileNotFoundException {
        int x = 21;
        checkRow(x);
    }

    public void testResult23() throws TableFormatException, FileNotFoundException {
        int x = 22;
        checkRow(x);
    }

    public void testResult24() throws TableFormatException, FileNotFoundException {
        int x = 23;
        checkRow(x);
    }

    public void testResult25() throws TableFormatException, FileNotFoundException {
        int x = 24;
        checkRow(x);
    }

    public void testResult26() throws TableFormatException, FileNotFoundException {
        int x = 25;
        checkRow(x);
    }

    public void testResult27() throws TableFormatException, FileNotFoundException {
        int x = 26;
        checkRow(x);
    }

    public void testResult28() throws TableFormatException, FileNotFoundException {
        int x = 27;
        checkRow(x);
    }

    private void checkRow(int x) {
        checkRow(x, 0);
    }

    private void checkRow(int x, int offset) {
        TestUtils.checkRow(data, results, x, offset);
    }
}
