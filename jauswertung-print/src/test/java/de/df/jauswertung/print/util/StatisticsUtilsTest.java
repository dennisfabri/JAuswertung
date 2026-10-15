package de.df.jauswertung.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.util.StatisticsUtils.PenaltyCounter;

class StatisticsUtilsTest {

    @Test
    void didNotStartIsCountedInItsOwnColumnAndNotInTheSum() {
        PenaltyCounter counter = new PenaltyCounter();

        counter.addPenalty(new Strafe("Nicht angetreten", "NA", Strafarten.NICHT_ANGETRETEN, 0), false);

        assertEquals(1, counter.getAmount(3));
        assertEquals(0, counter.getAmount(4));
    }

    @Test
    void penaltiesPerAgegroupShowADidNotStartColumn() {
        Disziplin[] disziplinen = { new Disziplin("A", 1, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, true, "");
        regelwerk.setAk(0, new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false));
        EinzelWettkampf wk = new EinzelWettkampf(regelwerk, new Strafen());

        Container panel = (Container) StatisticsUtils.createPenaltiesPerAgegroupPanel(wk);

        assertEquals(3, countLabels(panel, I18n.get("DidNotStartShort")));
    }

    private static int countLabels(Container container, String text) {
        int count = 0;
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel l && text.equals(l.getText())) {
                count++;
            }
        }
        return count;
    }
}
