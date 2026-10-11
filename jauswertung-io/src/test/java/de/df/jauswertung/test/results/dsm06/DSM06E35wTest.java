/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.results.dsm06;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DSM06E35wTest {

    private static EinzelWettkampf wk = null;

    private static final String FILE = "src/test/resources/competitions/dsm06/einzel-ak35w.csv";
    private static final boolean MALE = false;
    private static final int AK = 7;

    private static SchwimmerResult<Teilnehmer>[] results = null;
    private static Object[][] data = null;

    @BeforeEach
    public void setUp() throws Exception {
        if (wk == null) {
            wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwe"),
                    InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", true));
            data = TestImportUtils.importFile(wk, FILE);
            results = ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(AK), MALE);
        }
    }

    @Test
    public void testResults() throws TableFormatException, FileNotFoundException {
        assertEquals(data[0].length, results.length);
        for (int x = 0; x < results.length; x++) {
            assertEquals(x + 1, results[x].getSchwimmer().getStartnummer());
        }
    }

    @Test
    public void testResult1() throws TableFormatException, FileNotFoundException {
        int x = 0;
        checkRow(x);
    }

    @Test
    public void testResult2() throws TableFormatException, FileNotFoundException {
        int x = 1;
        checkRow(x);
    }

    @Test
    public void testResult3() throws TableFormatException, FileNotFoundException {
        int x = 2;
        checkRow(x);
    }

    @Test
    public void testResult4() throws TableFormatException, FileNotFoundException {
        int x = 3;
        checkRow(x);
    }

    @Test
    public void testResult5() throws TableFormatException, FileNotFoundException {
        int x = 4;
        checkRow(x);
    }

    @Test
    public void testResult6() throws TableFormatException, FileNotFoundException {
        int x = 5;
        checkRow(x);
    }

    @Test
    public void testResult7() throws TableFormatException, FileNotFoundException {
        int x = 6;
        checkRow(x);
    }

    @Test
    public void testResult8() throws TableFormatException, FileNotFoundException {
        int x = 7;
        checkRow(x);
    }

    @Test
    public void testResult9() throws TableFormatException, FileNotFoundException {
        int x = 8;
        checkRow(x);
    }

    @Test
    public void testResult10() throws TableFormatException, FileNotFoundException {
        int x = 9;
        checkRow(x);
    }

    @Test
    public void testResult11() throws TableFormatException, FileNotFoundException {
        int x = 10;
        checkRow(x);
    }

    @Test
    public void testResult12() throws TableFormatException, FileNotFoundException {
        int x = 11;
        checkRow(x);
    }

    @Test
    public void testResult13() throws TableFormatException, FileNotFoundException {
        int x = 12;
        checkRow(x);
    }

    @Test
    public void testResult14() throws TableFormatException, FileNotFoundException {
        int x = 13;
        checkRow(x);
    }

    private void checkRow(int x) {
        checkRow(x, 0);
    }

    private void checkRow(int x, int offset) {
        TestUtils.checkRow(data, results, x, offset);
    }
}
