package de.df.jauswertung.gui.veranstaltung;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class JVeranstaltungsschriftartTest {

    @Test
    void noFontIsReturnedUntilTheUserChoseOne() {
        JVeranstaltungsschriftart panel = new JVeranstaltungsschriftart(null);

        assertNull(panel.getSelectedFont());
    }
}
