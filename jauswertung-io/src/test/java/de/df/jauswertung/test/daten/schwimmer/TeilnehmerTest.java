/*
 * Created on 09.06.2005
 */
package de.df.jauswertung.test.daten.schwimmer;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.*;
import junit.framework.TestCase;

public class TeilnehmerTest extends TestCase {

    private Regelwerk aks = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 1999.rwe");
    private EinzelWettkampf wk = new EinzelWettkampf(aks,
            InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 1999", true));
    private Teilnehmer s;

    public TeilnehmerTest() {
        super();
    }

    @Override
    protected void setUp() {
        s = wk.createTeilnehmer("Name", "Vorname", 1999, true, "Gliederung", 0, "Bemerkung");
    }

    @Override
    protected void tearDown() throws Exception {
        s = null;
    }

    public void testTeilnehmer() {
        assertEquals(s.getName(), "Name, Vorname");
        assertEquals(s.getVorname(), "Vorname");
        assertEquals(s.getNachname(), "Name");
        assertEquals(s.isMaennlich(), true);
        assertEquals(s.getGliederung(), "Gliederung");
        assertEquals(s.getAK(), aks.getAk(0));
        assertEquals(s.getBemerkung(), "Bemerkung");
        assertEquals(s.isAusserKonkurrenz(), false);
        assertEquals(s.getMeldepunkte(0), 0, 0.001);
        assertEquals(s.getJahrgang(), 1999);
        assertEquals(s.getMaximaleHLW(), 1);
        for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
            assertEquals(s.getZeit(x), 0);
        }
    }

    public void testSetVorname() {
        s.setVorname("Test");

        assertEquals(s.getName(), "Name, Test");
        assertEquals(s.getVorname(), "Test");
        assertEquals(s.getNachname(), "Name");
    }

    public void testSetNachname() {
        s.setNachname("Test");

        assertEquals(s.getName(), "Test, Vorname");
        assertEquals(s.getVorname(), "Vorname");
        assertEquals(s.getNachname(), "Test");
    }

    public void testSetJahrgang() {
        s.setJahrgang(2004);
        assertEquals(s.getJahrgang(), 2004);
    }
}
