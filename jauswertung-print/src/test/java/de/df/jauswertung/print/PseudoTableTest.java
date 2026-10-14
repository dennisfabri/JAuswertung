package de.df.jauswertung.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jutils.gui.jtable.ExtendedTableModel;

class PseudoTableTest {

    private static final String[] SPALTEN = { "Platz", "Name", "Gliederung", " ", "Punkte" };

    @AfterEach
    void optionZuruecksetzen() {
        PrintUtils.printOmitOrganisationForTeams = false;
    }

    private static JComponent tabelle(int versteckteSpalte) {
        ExtendedTableModel etm = new ExtendedTableModel(
                new Object[][] { { "1 ", " Name ", " Gliederung ", " Ebene ", "20  " } }, SPALTEN);
        etm.setColumnAlignments(new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT,
                SwingConstants.LEFT, SwingConstants.RIGHT });
        JComponent tabelle = PseudoTable.create(etm, new Font(Font.DIALOG, Font.PLAIN, 10), versteckteSpalte);
        tabelle.setSize(tabelle.getPreferredSize().width + 100, tabelle.getPreferredSize().height);
        tabelle.doLayout();
        return tabelle;
    }

    private static int breite(JComponent tabelle, String text) {
        return Arrays.stream(tabelle.getComponents()).filter(c -> c instanceof JLabel l && l.getText().equals(text))
                .mapToInt(Component::getWidth).max().orElseThrow();
    }

    @Test
    void alleSpaltenSichtbar() {
        JComponent tabelle = tabelle(-1);

        for (String spalte : SPALTEN) {
            assertTrue(breite(tabelle, spalte) > 0, spalte);
        }
        assertTrue(breite(tabelle, " Gliederung ") > 0);
    }

    @Test
    void versteckteSpalteHatKeineBreite() {
        JComponent tabelle = tabelle(2);

        assertEquals(0, breite(tabelle, "Gliederung"));
        assertEquals(0, breite(tabelle, " Gliederung "));
        assertTrue(breite(tabelle, "Name") > 0);
        assertTrue(breite(tabelle, " Ebene ") > 0);
        assertTrue(breite(tabelle, "Punkte") > 0);
    }

    @Test
    void gliederungWirdOhneOptionGedruckt() {
        assertFalse(PrintUtils.isOrganisationOmitted(mock(EinzelWettkampf.class)));
        assertFalse(PrintUtils.isOrganisationOmitted(mock(MannschaftWettkampf.class)));
    }

    @Test
    void optionLaesstGliederungNurBeiMannschaftenWeg() {
        PrintUtils.printOmitOrganisationForTeams = true;

        assertFalse(PrintUtils.isOrganisationOmitted(mock(EinzelWettkampf.class)));
        assertTrue(PrintUtils.isOrganisationOmitted(mock(MannschaftWettkampf.class)));
    }
}
