package de.df.jauswertung.util.ergebnis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormelManagerTest {

    @ParameterizedTest
    @CsvSource({ "DLRG1999, FormelDLRG", "DLRG2007, FormelDLRG2007", "DeutschlandPokal, FormelDP",
            "DSV2005, FormelDSV", "Linear, FormelLinear", "DirectPoints, FormelDirectPoints" })
    void formelWirdUeberIhreIdGefunden(String id, String klasse) {
        var formel = FormelManager.getInstance().get(id);

        assertEquals(klasse, formel.getClass().getSimpleName());
        assertEquals(id, formel.getID());
        assertEquals(DataType.TIME, formel.getDataType());
    }

    @ParameterizedTest
    @CsvSource({ "DLRG1999, true", "DLRG2007, true", "DeutschlandPokal, true", "DSV2005, true", "Linear, true",
            "DirectPoints, false" })
    void dlrgBasiert(String id, boolean dlrg) {
        assertEquals(dlrg, FormelManager.isDLRGBased(id));
        assertTrue(FormelManager.getInstance().get(id) != null);
    }
}
