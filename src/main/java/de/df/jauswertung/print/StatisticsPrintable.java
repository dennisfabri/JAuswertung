package de.df.jauswertung.print;

import java.awt.print.Printable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComponent;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.StatisticsUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.print.ComponentListPrintable2;
import de.df.jutils.print.MultiplePrintable;

public class StatisticsPrintable<T extends ASchwimmer> extends MultiplePrintable {

    public StatisticsPrintable(AWettkampf<T> wk) {
        super(preparePrintables(wk));
    }

    private static <T extends ASchwimmer> Printable createStatisticsPage(LinkedList<T> swimmer, Regelwerk aks, String title) {
        return new ComponentListPrintable2(false, StatisticsUtils.createStatistics(swimmer, aks, title, true), StatisticsUtils.getStarts(swimmer, aks, true));
    }

    private static <T extends ASchwimmer> Printable createOverviewLVPage(AWettkampf<T> wk, String title) {
        wk = Utils.copy(wk);
        for (T t : wk.getSchwimmer()) {
            if (t.getQualifikationsebene().length() > 0) {
                t.setGliederung(t.getQualifikationsebene());
                t.setQualifikationsebene("");
            } else {
                // wk.removeSchwimmer(t);
                t.setGliederung("(ohne)");
            }
        }
        return createOverviewPage(wk, title);
    }

    private static <T extends ASchwimmer> Printable createOverviewPage(AWettkampf<T> wk, String title) {
        LinkedList<JComponent> ps = StatisticsUtils.createOverviewPage(wk, 30, true);

        return new ComponentListPrintable2(false, ps.toArray(new JComponent[ps.size()]));
    }

    private static <T extends ASchwimmer> LinkedList<Printable> preparePrintables(AWettkampf<T> wk) {
        wk = Utils.copy(wk);

        LinkedList<Printable> printables = new LinkedList<Printable>();
        Printable p = createStatisticsPage(wk.getSchwimmer(), wk.getRegelwerk(), null);
        if (p != null) {
            printables.addLast(p);
        }
        p = createOverviewPage(wk, null);
        if (p != null) {
            printables.addLast(p);
        }
        if (checkQualigliederung(wk)) {
            p = createOverviewLVPage(wk, null);
            if (p != null) {
                printables.addLast(p);
            }
        }
        LinkedList<Printable> ps = generateGliederungStatsPrintables(wk);
        if (ps != null) {
            printables.addAll(ps);
        }

        return printables;
    }

    private static <T extends ASchwimmer> LinkedList<Printable> generateGliederungStatsPrintables(AWettkampf<T> wk) {
        LinkedList<Printable> printables = new LinkedList<Printable>();
        Printable p;
        LinkedList<String> g = wk.getGliederungenMitQGliederung();
        ListIterator<String> li = g.listIterator();
        while (li.hasNext()) {
            if (!SearchUtils.hasSchwimmer(wk, li.next())) {
                li.remove();
            }
        }
        String[] gliederungen = g.toArray(new String[g.size()]);
        for (String aGliederungen : gliederungen) {
            p = createStatisticsPage(SearchUtils.getSchwimmer(wk, new String[] { aGliederungen }, true), wk.getRegelwerk(), aGliederungen);
            if (p != null) {
                printables.addLast(p);
            }
        }
        return printables;
    }

    private static <T extends ASchwimmer> boolean checkQualigliederung(AWettkampf<T> wk) {
        for (T t : wk.getSchwimmer()) {
            if (t.getQualifikationsebene().length() > 0) {
                return true;
            }
        }
        return false;
    }
}