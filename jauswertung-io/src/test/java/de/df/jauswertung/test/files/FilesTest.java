package de.df.jauswertung.test.files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.AgeGroupIOUtils;

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
    @ValueSource(strings = { "default.def", "defaultm.def", "International - Ocean.def", "International - Ocean Mixed.def", 
            "International - Pool.def", "International - Pool Mixed.def" })
    void LadeStrafen(String name) {
        String filename = Path + "penalties/" + name;
        Regelwerk regelwerk = AgeGroupIOUtils.ladeAKs(filename);
        assertNotNull(regelwerk);
        assertTrue(0 < regelwerk.getAks().length);
    }

}
