package de.df.jauswertung.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.gui.util.I18n;

class DisziplinenTextTest {

    @Test
    void readsUmlautsInDisciplineNames() {
        assertEquals("100m Rücken", I18n.getDisziplinShort("100m Rückenschwimmen"));
    }
}
