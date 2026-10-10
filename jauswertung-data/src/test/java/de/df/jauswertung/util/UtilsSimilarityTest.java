package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UtilsSimilarityTest {

    @ParameterizedTest
    @CsvSource({ "DLRG Bochum, DLRG Bochum", "DLRG Bochum, DLRG Bochum e.V.", "Wuppertal, Wupertal",
            "Gelsenkirchen, Gelsenkirchn", "Essen, Essen-Steele" })
    void similar(String s1, String s2) {
        assertTrue(Utils.areSimilar(s1, s2));
        assertTrue(Utils.areSimilar(s2, s1));
    }

    @ParameterizedTest
    @CsvSource({ "Bochum, Bocholt", "Hamm, Hamburg", "Köln, Bonn", "Weimar, Wismar", "Dettingen, Ertingen",
            "Gelsenkirchen-Buer, Gelsenkirchen-Horst" })
    void notSimilar(String s1, String s2) {
        assertFalse(Utils.areSimilar(s1, s2));
        assertFalse(Utils.areSimilar(s2, s1));
    }
}
