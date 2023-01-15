/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.daten.schwimmer;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.*;
import junit.framework.TestCase;

public class SchwimmerTest extends TestCase {

    private Regelwerk aks = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwm");
    private MannschaftWettkampf wk = new MannschaftWettkampf(aks,
            InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", false));

    private ASchwimmer s2;
    private ASchwimmer s1;

    public SchwimmerTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        s1 = wk.createMannschaft("Name 1", false, "Gliederung 1", 0, "Bemerkung 1");
        s2 = wk.createMannschaft("Name 2", true, "Gliederung 2", 0, "Bemerkung 2");
    }

    @Override
    protected void tearDown() throws Exception {
        s1 = null;
        s2 = null;
    }

    public void testConstructor() {
        assertEquals(s1.getName(), "Name 1");
        assertEquals(s1.isMaennlich(), false);
        assertEquals(s1.getGliederung(), "Gliederung 1");
        assertEquals(s1.getAK(), aks.getAk(0));
        assertEquals(s1.getBemerkung(), "Bemerkung 1");
        assertEquals(s1.isAusserKonkurrenz(), false);
        assertEquals(s1.getMeldepunkte(0), 0, 0.001);
        for (int x = 0; x < s1.getAK().getDiszAnzahl(); x++) {
            assertEquals(s1.getZeit(x), 0);
        }
    }

    public void testGetAK() {
        assertSame(s1.getAK(), aks.getAk(0));
    }

    public void testToString() {
        assertEquals(s1.toString(), "S#0 - AK 12 weiblich - Gliederung 1 - Name 1");
    }

    public void testCompareTo() {
        assertTrue(s1.compareTo(s1) == 0);
        assertTrue(s1.compareTo(s2) == 0);
        assertTrue(s2.compareTo(s1) == 0);

        s2.setStartnummer(2);

        assertFalse(s1.compareTo(s2) == 0);
        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);

        try {
            assertFalse(s1.compareTo(null) == 0);
            fail();
        } catch (NullPointerException n) {
            // Everything ok here
        }
    }

    public void testEquals() {
        assertTrue(s1.equals(s1));
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));

        s2.setStartnummer(2);

        assertFalse(s1.equals(s2));
        assertFalse(s2.equals(s1));

        assertFalse(s1.equals(""));
        assertFalse(s1.equals(null));
    }

    public void testAK() {
        s1.setAKNummer(1, true);

        assertSame(s1.getAK(), aks.getAk(1));
    }

}
