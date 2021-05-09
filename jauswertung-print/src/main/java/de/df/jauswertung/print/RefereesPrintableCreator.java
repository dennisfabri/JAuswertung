package de.df.jauswertung.print;

import java.awt.print.Printable;
import java.text.MessageFormat;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.HeaderFooterPrintable;

public class RefereesPrintableCreator {

    public static <T extends ASchwimmer> Printable createRefereesPrintable(AWettkampf<T> wk) {
        Printable p = null;
        KampfrichterVerwaltung kv = wk.getKampfrichterverwaltung();
        if (kv == null) {
            return EmptyPrintable.Instance;
        }
        int mode = wk.getIntegerProperty(PropertyConstants.PRINT_REFEREES_COMPACT, 1);
        switch (mode) {
        case 0:
            p = RefereesTableCreator.getPrintable(kv);
            break;
        default:
        case 1:
            p = RefereesTableCompactCreator.getPrintable(kv);
            break;
        case 2:
            p = RefereesTableVeryCompactCreator.getPrintable(kv);
            break;
        }
        p = new HeaderFooterPrintable(p, new MessageFormat(I18n.get("Referees")), null, PrintManager.getFont());
        return p;
    }

}
