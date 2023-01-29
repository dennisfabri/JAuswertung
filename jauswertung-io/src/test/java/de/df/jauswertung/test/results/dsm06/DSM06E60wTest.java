/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.results.dsm06;

import java.io.FileNotFoundException;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.TableFormatException;
import de.df.jauswertung.test.util.TestImportUtils;
import de.df.jauswertung.test.util.TestUtils;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import junit.framework.TestCase;

public class DSM06E60wTest extends TestCase {

    private static EinzelWettkampf wk = null;

    private static final String FILE = "src/test/resources/competitions/dsm06/einzel-ak60w.csv";
    private static final boolean MALE = false;
    private static final int AK = 12;

    private static SchwimmerResult<Teilnehmer>[] results = null;
    private static Object[][] data = null;

    public DSM06E60wTest(String x) {
        super(x);
    }

    @Override
    protected void setUp() throws Exception {
        if (wk == null) {
            wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwe"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", true));
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

    private void checkRow(int x) {
        checkRow(x, 0);
    }

    private void checkRow(int x, int offset) {
        TestUtils.checkRow(data, results, x, offset);
    }
}
