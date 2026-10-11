package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;

/**
 * Heats of a start group combine its age groups discipline by discipline, so they must have the same disciplines.
 */
class StartgroupValidationTest {

    private static final int AK_12 = 0;
    private static final int AK_13_14 = 1;
    private static final int AK_15_16 = 2;
    private static final int AK_50 = 10;
    private static final int AK_55 = 11;

    private static Regelwerk rulebookWithStartgroup(String name, int... ageGroups) {
        Regelwerk rw = AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe");
        Startgruppe[] sgs = rw.getStartgruppen();
        if (Arrays.stream(sgs).noneMatch(sg -> sg.getName().equals(name))) {
            sgs = Arrays.copyOf(sgs, sgs.length + 1);
            sgs[sgs.length - 1] = new Startgruppe(name);
            rw.setStartgruppen(sgs);
        }
        for (int ak : ageGroups) {
            rw.getAk(ak).setStartgruppe(name);
        }
        return rw;
    }

    @Test
    void shippedStartgroupsAreValid() {
        assertTrue(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe").isValid());
    }

    @Test
    void rejectsDifferentNumbersOfDisciplines() {
        // AK 13/14 has 3 disciplines, AK 15/16 - Offen has 6
        assertFalse(rulebookWithStartgroup("AK 15/16 - Offen", AK_13_14).isValid());
    }

    @Test
    void rejectsDifferentDisciplines() {
        // Both have 3 disciplines, but different ones
        assertFalse(rulebookWithStartgroup("AK 12-14", AK_12, AK_13_14).isValid());
    }

    @Test
    void acceptsEqualDisciplines() {
        assertTrue(rulebookWithStartgroup("AK 50-55", AK_50, AK_55).isValid());
    }
}
