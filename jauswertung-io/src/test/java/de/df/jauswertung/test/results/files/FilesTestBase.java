/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.results.files;

import java.io.File;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.io.InputManager;
import junit.framework.TestCase;

abstract class FilesTestBase extends TestCase {

    private static final String FILE = "../../required/archive/";

    private final String year;

    public FilesTestBase(String name, String y) {
        super(name);
        year = y;
    }

    @SuppressWarnings("rawtypes")
    private static AWettkampf check(String name) {
        String filename = FILE + name;
        if (!new File(filename).exists()) {
            return null;
        }
        AWettkampf wk = InputManager.ladeWettkampf(filename);
        assertNotNull(wk);
        return wk;
    }

    @SuppressWarnings("rawtypes")
    public void testResultDMSingle() {
        String name = "dm" + year + "-einzel.wk";
        AWettkampf wk = check(name);
        assertTrue(wk == null || wk instanceof EinzelWettkampf);
    }

    @SuppressWarnings("rawtypes")
    public void testResultDMTeam() {
        String name = "dm" + year + "-mannschaft.wk";
        AWettkampf wk = check(name);
        assertTrue(wk == null || wk instanceof MannschaftWettkampf);
    }

    @SuppressWarnings("rawtypes")
    public void testResultDSMSingle() {
        String name = "dsm" + year + "-einzel.wk";
        AWettkampf wk = check(name);
        assertTrue(wk == null || wk instanceof EinzelWettkampf);
    }

    @SuppressWarnings("rawtypes")
    public void testResultDSMTeam() {
        String name = "dsm" + year + "-mannschaft.wk";
        AWettkampf wk = check(name);
        assertTrue(wk == null || wk instanceof MannschaftWettkampf);
    }
}