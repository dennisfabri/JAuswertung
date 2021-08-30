package de.df.jauswertung.print;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.MultiplePrintable;
import de.df.jutils.print.printables.RotatingPrintable;

public class ProtocolPrintable<T extends ASchwimmer> extends MultiplePrintable {
    public ProtocolPrintable(AWettkampf<T> wk) {
        add(new FirstPagePrintable(wk));
        add(PrintManager.getHeaderPrintable(PropertiesTableCreator.getPrintable(wk),
                I18n.get("CompetitionInformation")));
        add(RefereesPrintableCreator.createRefereesPrintable(wk));
        add(PrintableCreator.createInfoPagePrintable(wk));
        if (PrintUtils.printProtocolResultsHorizontal) {
            add(PrintUtils.getNormalResultsPrintable(wk, false, true, 0));
            add(PrintUtils.getWertungsgruppenPrintables(wk, false, 0));
        } else {
            add(new RotatingPrintable(PrintUtils.getNormalResultsPrintable(wk, false, true, 0)));
            add(new RotatingPrintable(PrintUtils.getWertungsgruppenPrintables(wk, false, 0)));
        }
        addAll(PrintUtils.getEinzelwertungPrintables(wk, false, 0));
        if (PrintUtils.printProtocolResultsHorizontal) {
            add(PrintUtils.getGesamtwertungPrintable(wk));
        } else {
            add(new RotatingPrintable(PrintUtils.getGesamtwertungPrintable(wk)));
        }
        add(PrintableCreator.createZielrichterentscheidPrintable(wk));
        if (!wk.isHeatBased()) {
            add(PrintableCreator.createPenaltiesPrintable(wk, true));
        }
    }
}