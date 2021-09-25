package de.df.jauswertung.test.files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;

public class FilesTest {

    private final static String Path = "../jauswertung-files/src/main/resources/";

    @ParameterizedTest
    @ValueSource(strings = { "DLRG 2021.rwe", "DLRG 2021.rwm", "International - Ocean.rwe", "International - Ocean.rwm",
            "International - Pool.rwm", "International - Pool Mixed.rwm" })
    void LadeRegelwerke(String name) {
        String filename = Path + "aks/" + name;
        Regelwerk regelwerk = AgeGroupIOUtils.ladeAKs(filename);
        assertNotNull(regelwerk);
        assertTrue(0 < regelwerk.getAks().length);
    }

    @ParameterizedTest
    @ValueSource(strings = { "default", "defaultm", "International - Ocean", "International - Ocean Mixed", 
            "International - Pool", "International - Pool Mixed" })
    void LadeStrafen(String name) {
        String filename = Path + "penalties/" + name;
        Strafen strafen = InputManager.ladeStrafen(filename, true);
        assertNotNull(strafen);
        assertTrue(0 < strafen.getKapitel().size());
    }

}
