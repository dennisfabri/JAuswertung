/*
 * Created on 03.02.2006
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;
import de.df.jauswertung.gui.util.ZWTableCellRenderer;
import de.df.jauswertung.print.util.BarcodeType;
import de.df.jauswertung.print.util.TableUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.ExtendedHeaderFooterPrintable;
import de.df.jutils.print.printables.HeaderFooterPrintable;
import de.df.jutils.print.printables.JTablePrintable;
import de.df.jutils.print.printables.MultiplePrintable;
import de.df.jutils.util.StringTools;

public final class PrintUtils {

    /**
     * Ergebnisse sind im Protokoll hochkant
     */
    public static boolean printProtocolResultsHorizontal = false;

    public static boolean printEmptyLanes = true;

    public static boolean printEmptyCards = false;

    public static boolean printPointsInDisciplineResults = true;

    public static boolean printChecksum = true;

    private static boolean compressLists = true;

    public static boolean printZWnames = false;

    public static boolean printOmitOrganisationForTeams = false;

    public static boolean printYearOfBirth = true;

    public static boolean printDidNotStart = true;

    public static BarcodeType barcodeType = BarcodeType.CODE128;

    public static boolean getCompressLists() {
        return compressLists;
    }

    public static void setCompressLists(boolean enabled) {
        compressLists = enabled;
        ZWTableCellRenderer.compressLists = enabled;
        SchwimmerDisziplin.compressLists = enabled;
    }

    static {
        setCompressLists(true);
    }

    private PrintUtils() {
        // Hide
    }

    public static <T extends ASchwimmer> Printable getNormalResultsPrintable(AWettkampf<T> wk, boolean reducedview,
            boolean points, int qualification) {
        wk = Utils.copy(wk);
        Regelwerk aks = wk.getRegelwerk();

        LinkedList<Printable> ps = new LinkedList<>();

        for (int y = 0; y < aks.size(); y++) {
            Altersklasse ak = aks.getAk(y);
            if (ak.hasMehrkampfwertung()) {
                for (int a = 0; a < 2; a++) {
                    GetFullPrintable<T> gfp = new GetFullPrintable<>(wk, ak, a == 1, reducedview, points,
                            qualification);
                    EDTUtils.executeOnEDT(gfp);
                    Printable p = gfp.printable;
                    if (p != null) {
                        ps.addLast(p);
                    }
                }
            }
        }
        if (ps.isEmpty()) {
            return EmptyPrintable.Instance;
        }
        return new MultiplePrintable(ps);
    }

    public static <T extends ASchwimmer> Printable getResultsPrintable(AWettkampf<T> wk, boolean reducedview,
            boolean points, boolean removeEmpty, int qualification) {
        if (wk == null) {
            return EmptyPrintable.Instance;
        }
        wk = Utils.copy(wk);
        Regelwerk aks = wk.getRegelwerk();

        LinkedList<Printable> ps = new LinkedList<>();

        for (int y = 0; y < aks.size(); y++) {
            Altersklasse ak = aks.getAk(y);
            if (ak.hasMehrkampfwertung()) {
                for (int a = 0; a < 2; a++) {
                    GetFullPrintable<T> gfp = new GetFullPrintable<>(wk, ak, a == 1, reducedview, points,
                            qualification);
                    EDTUtils.executeOnEDT(gfp);
                    Printable p = gfp.printable;
                    if (p != null) {
                        ps.addLast(p);
                    }
                }
            }
        }

        AWettkampf<T> wkx = CompetitionUtils.generateWertungsgruppenwettkampf(wk);
        if (wkx != null) {
            Regelwerk akx = wkx.getRegelwerk();
            for (int y = 0; y < akx.size(); y++) {
                Altersklasse ak = akx.getAk(y);
                if (ak.hasMehrkampfwertung()) {
                    for (int a = 0; a < 2; a++) {
                        GetFullPrintable<T> gfp = new GetFullPrintable<>(wkx, ak, a == 1, reducedview, points,
                                qualification);
                        EDTUtils.executeOnEDT(gfp);
                        Printable p = gfp.printable;
                        if (p != null) {
                            ps.addLast(p);
                        }
                    }
                }
            }
        }

        ps.addAll(getEinzelwertungPrintables(wk, removeEmpty, qualification));

        if (wk.getRegelwerk().hasGesamtwertung()) {
            ps.addLast(getGesamtwertungPrintable(wk));
        }
        if (ps.isEmpty()) {
            return null;
        }
        return new MultiplePrintable(ps);
    }

    public static <T extends ASchwimmer> Printable getGesamtwertungPrintable(AWettkampf<T> wk) {
        return getGesamtwertungPrintable(wk, I18n.get("GroupEvaluation"));
    }

    public static <T extends ASchwimmer> Printable getGesamtwertungPrintable(AWettkampf<T> wk, String titel) {
        JTable table = TableUtils.getGesamtwertungsergebnis(wk);
        if (table == null) {
            return EmptyPrintable.Instance;
        }
        return PrintManager.getPrintable(table,
                titel + (wk.getCurrentFilterIndex() > 0 ? " (" + wk.getCurrentFilter().getName() + ")" : ""),
                JTablePrintable.OPT_ALL, true, true);
    }

    private static class GetFullPrintable<T extends ASchwimmer> implements Runnable {

        private AWettkampf<T> wk;
        private Altersklasse ak;
        private boolean male;
        private boolean reducedview;
        private boolean points;
        private boolean checksum;
        private int qualification;

        Printable printable;

        public GetFullPrintable(AWettkampf<T> w, Altersklasse a, boolean m, boolean reducedview, boolean points,
                int qualification) {
            wk = w;
            ak = a;
            male = m;
            this.reducedview = reducedview;
            this.points = points;
            this.checksum = printChecksum;
        }

        @Override
        public void run() {
            printable = getFullPrintable(wk, ak, male, reducedview, points, checksum, qualification);
        }
    }

    /**
     * @param selected
     * @param wks
     * @param ps
     * @param cc
     */
    public static <T extends ASchwimmer> LinkedList<Printable> getIntermediateResults(boolean[][] selected,
            AWettkampf<T> wk, int max, boolean reducedview, boolean points, int qualification) {
        wk = Utils.copy(wk);
        LinkedList<Printable> ps = new LinkedList<>();
        Regelwerk aks = wk.getRegelwerk();
        for (int y = 0; y < selected[0].length; y++) {
            Altersklasse ak = aks.getAk(y);
            for (int a = 0; a < 2; a++) {
                if (selected[a][y]) {
                    Printable p = getIntermediatePrintable(wk, max, ak, a == 1, reducedview, points, printChecksum,
                            qualification);
                    if (p != null) {
                        ps.addLast(p);
                    }
                }
            }
        }
        return ps;
    }

    @SuppressWarnings({ "unchecked", "null" })
    public static <T extends ASchwimmer> LinkedList<Printable> getBestOfDisciplineResultsPrintable(boolean[][] selected,
            AWettkampf<T> wk, boolean reducedview, boolean points) {

        // Disziplinen erkennen
        boolean strafeIstDisqualifikation = false;
        Hashtable<String, Disziplin[]> disziplinen = new Hashtable<>();
        for (int x = 0; x < selected[0].length; x++) {
            if (selected[0][x] | selected[1][x]) {
                Altersklasse ak = wk.getRegelwerk().getAk(x);
                if (ak.isStrafeIstDisqualifikation()) {
                    strafeIstDisqualifikation = true;
                }
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    Disziplin f = new Disziplin(ak.getDisziplin(y, false));
                    Disziplin m = new Disziplin(ak.getDisziplin(y, true));

                    if (disziplinen.get(m.getName()) != null) {
                        Disziplin[] d = disziplinen.get(m.getName());
                        if (d[0].getRec() <= 0) {
                            d[0].setRec(f.getRec());
                        } else if ((d[0].getRec() > f.getRec()) && (f.getRec() > 0)) {
                            d[0].setRec(f.getRec());
                        }
                        if (d[1].getRec() <= 0) {
                            d[1].setRec(m.getRec());
                        } else if ((d[1].getRec() > m.getRec()) && (m.getRec() > 0)) {
                            d[1].setRec(m.getRec());
                        }
                    } else {
                        disziplinen.put(m.getName(), new Disziplin[] { f, m });
                    }
                }
            }
        }

        // Altersklassen aufbauen
        Hashtable<String, Integer> swimmer = new Hashtable<>();
        Regelwerk aks = new Regelwerk(wk.getRegelwerk().isEinzel(), wk.getRegelwerk().getFormelID());
        aks.setSize(disziplinen.size());
        {
            int x = 0;
            Enumeration<String> keys = disziplinen.keys();
            while (keys.hasMoreElements()) {
                String name = keys.nextElement();
                Disziplin[] d = disziplinen.get(name);
                Altersklasse ak = new Altersklasse("", new Disziplin[][] { { d[0] }, { d[1] } }, false);
                ak.setStrafeIstDisqualifikation(strafeIstDisqualifikation);
                ak.setEinzelwertung(true);
                ak.setChosenDisciplines(1, 1, 1);

                swimmer.put(name, x);
                aks.setAk(x, ak);

                x++;
            }
        }

        // Wettkampf aufbauen
        AWettkampf<T> nwk = null;
        EinzelWettkampf ewk = null;
        MannschaftWettkampf mwk = null;
        if (wk instanceof EinzelWettkampf) {
            ewk = new EinzelWettkampf(aks, wk.getStrafen());
            nwk = (AWettkampf<T>) ewk;
        } else {
            mwk = new MannschaftWettkampf(aks, wk.getStrafen());
            nwk = (AWettkampf<T>) mwk;
        }

        // Schwimmer hinzufügen
        for (int x = 0; x < selected[0].length; x++) {
            for (int y = 0; y < selected.length; y++) {
                if (selected[y][x]) {
                    Altersklasse ak = wk.getRegelwerk().getAk(x);
                    LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, ak, y == 1);
                    for (T s : swimmers) {
                        for (int a = 0; a < ak.getDiszAnzahl(); a++) {
                            T t = null;
                            if (wk instanceof EinzelWettkampf) {
                                Teilnehmer sx = (Teilnehmer) s;
                                t = (T) ewk.createTeilnehmer(sx.getNachname(), sx.getVorname(), sx.getJahrgang(),
                                        sx.isMaennlich(), sx.getGliederung(),
                                        swimmer.get(ak.getDisziplin(a, sx.isMaennlich()).getName()), sx.getBemerkung());
                            } else {
                                t = (T) mwk.createMannschaft(s.getName(), s.isMaennlich(), s.getGliederung(),
                                        swimmer.get(ak.getDisziplin(a, s.isMaennlich()).getName()), s.getBemerkung());
                            }
                            t.setDisciplineChoice(0, true);
                            t.setZeit(0, s.getZeit(a));
                            t.setStrafen(0, s.getStrafen(a));
                            nwk.addSchwimmer(t);
                        }
                    }
                }
            }
        }

        selected = new boolean[2][aks.size()];
        for (int x = 0; x < selected.length; x++) {
            for (int y = 0; y < selected[x].length; y++) {
                selected[x][y] = true;
            }
        }

        return getFullResultsPrintable(selected, nwk, true, true, 0);
    }

    /**
     * @param selected
     * @param wks
     * @param ps
     * @param cc
     */
    public static <T extends ASchwimmer> LinkedList<Printable> getFullResultsPrintable(boolean[][] selected,
            AWettkampf<T> wkx, boolean reducedview, boolean points, int qualification) {
        final AWettkampf<T> wk = Utils.copy(wkx);
        LinkedList<Printable> ps = new LinkedList<>();
        Regelwerk aks = wk.getRegelwerk();
        for (int y = 0; y < selected[0].length; y++) {
            Altersklasse ak = aks.getAk(y);
            for (int a = 0; a < 2; a++) {
                if (selected[a][y]) {
                    final boolean male = a == 1;
                    Printable p = EDTUtils.executeOnEDTwithReturn(
                            () -> getFullPrintable(wk, ak, male, reducedview, points, printChecksum, qualification));
                    if (p != null) {
                        ps.addLast(p);
                    }
                }
            }
        }
        return ps;
    }

    static <T extends ASchwimmer> Printable getIntermediatePrintable(AWettkampf<T> wk, int max, Altersklasse ak,
            boolean male, boolean reducedview, boolean points, boolean checksum, int qualification) {
        if (SearchUtils.getSchwimmer(wk, ak, male).isEmpty()) {
            return null;
        }

        boolean[] select = new boolean[ak.getDiszAnzahl()];
        for (int x = 0; x < select.length; x++) {
            select[x] = x < max;
        }
        if (max == 0) {
            if (wk.isCompetitionComplete() && wk.isHlwComplete()) {
                return getFullPrintable(wk, ak, male, reducedview, points, checksum, qualification);
            }
            int index = wk.getRegelwerk().getIndex(ak.getName());
            // max = wk.getToDisciplineComplete(index, male);
            for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                select[x] = wk.isDisciplineComplete(x, index, male);
            }
        }

        boolean hlw = wk.isHlwComplete(wk.getRegelwerk().getIndex(ak.getName()), male);
        JResultTable tm = JResultTable.getResultTable(wk, ak, male, select, true, hlw, qualification);

        return getResultsPrintable(wk, ak, male, reducedview, points, checksum, qualification, tm,
                I18n.get("IntermediateResults"));
    }

    public static JPanel createHeaderPanel(Regelwerk aks, Altersklasse ak, boolean male, boolean reducedview,
            boolean includeAkName) {
        return createHeaderPanel(aks, ak, male, reducedview, includeAkName, printChecksum);
    }

    public static JPanel createHeaderPanel(Regelwerk aks, Altersklasse ak, boolean male, boolean reducedview,
            boolean includeAkName, boolean checksum) {
        StringBuilder sb = new StringBuilder("fill:default");
        if (includeAkName) {
            sb.append(",1dlu,fill:default");
        }
        int min = (int) Math.round(0.01 + 0.5 * ak.getDiszAnzahl());
        if (ak.getDiszAnzahl() == 3) {
            min = 3;
        }
        for (int z = 1; z < min; z++) {
            sb.append(",fill:default");
        }
        sb.append(",1dlu");
        JPanel headerpanel = new JPanel(new FormLayout(
                "fill:default,1dlu,fill:default,1dlu,fill:default,1dlu,fill:default:grow", sb.toString()));

        int offset = 0;
        if (includeAkName) {
            headerpanel.add(PrintManager.getPrintLabel(ak.getName() + " " + I18n.geschlechtToString(aks, male)),
                    CC.xyw(1, 1 + offset, 7, "center,fill"));
            offset += 2;
        }

        if ((ak.getDiszAnzahl() == 1) && (reducedview)) {
            headerpanel.add(PrintManager.getPrintLabel(ak.getDisziplin(0, male).getName()),
                    CC.xyw(1, 1 + offset, 7, "center,fill"));
        } else {
            for (int z = 0; z < min; z++) {
                headerpanel.add(PrintManager.getPrintLabel(I18n.get("DisciplineNumber", (z + 1)) + "    "),
                        CC.xy(1, z + 1 + offset));
                Disziplin d = ak.getDisziplin(z, male);
                headerpanel.add(PrintManager.getPrintLabel(
                        I18n.get("DisciplineDescription", d.getName(), StringTools.zeitString(d.getRec()), d.getRec())
                                + "  "),
                        CC.xy(3, z + 1 + offset));
            }
            for (int z = min; z < ak.getDiszAnzahl(); z++) {
                headerpanel.add(PrintManager.getPrintLabel("    " + I18n.get("DisciplineNumber", (z + 1)) + "    "),
                        CC.xy(5, z - min + 1 + offset));
                Disziplin d = ak.getDisziplin(z, male);
                headerpanel.add(
                        PrintManager.getPrintLabel(I18n.get("DisciplineDescription", d.getName(),
                                StringTools.zeitString(d.getRec()), d.getRec()) + "  "),
                        CC.xy(7, z - min + 1 + offset));
            }
        }
        headerpanel.setOpaque(false);

        JFrame f = new JFrame();
        f.add(headerpanel);
        f.pack();

        return headerpanel;
    }

    private static JPanel createFooterPanel(Regelwerk aks, Altersklasse ak, boolean male, boolean reducedview,
            JResultTable rt, boolean checksum) {
        JPanel footerpanel = new JPanel(new FormLayout("0dlu,fill:default:grow,0dlu", "1dlu,fill:default,0dlu"));
        footerpanel.setOpaque(false);

        if (checksum) {
            footerpanel.add(
                    PrintManager.getPrintLabel(
                            I18n.get("ChecksumValue", rt.getChecksum(), ak.getChecksum(), aks.getChecksum())),
                    CC.xy(2, 2));
        }

        JFrame f = new JFrame();
        f.add(footerpanel);
        f.pack();

        return footerpanel;
    }

    static <T extends ASchwimmer> Printable getFullPrintable(AWettkampf<T> wk, Altersklasse ak, boolean male,
            boolean reducedview, boolean points, boolean checksum, int qualification) {
        if (SearchUtils.getSchwimmer(wk, ak, male).isEmpty()) {
            return null;
        }

        JResultTable tm = JResultTable.getResultTable(wk, ak, male, true, true, qualification);
        return getResultsPrintable(wk, ak, male, reducedview, points, checksum, qualification, tm, I18n.get("Results"));
    }

    private static <T extends ASchwimmer> Printable getResultsPrintable(AWettkampf<T> wk, Altersklasse ak, boolean male,
            boolean reducedview, boolean points, boolean checksum, int qualification, JResultTable tm,
            String resultstitle) {
        boolean times = true;
        if (FormelManager.getInstance().get(wk.getRegelwerk().getFormelID()).getDataType().equals(DataType.RANK)) {
            times = false;
        }

        JPanel headerpanel = createHeaderPanel(wk.getRegelwerk(), ak, male, reducedview, false, checksum);
        JPanel footerpanel = createFooterPanel(wk.getRegelwerk(), ak, male, reducedview, tm, checksum);

        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = w instanceof EinzelWettkampf;
        if (ak.getDiszAnzahl() == 1) {
            if (qualification > 0) {
                points = false;
            }

            if (!ak.hasHLW()) {
                JTableUtils.hideColumnAndRemoveData(tm,
                        JResultTable.PREFIX + JResultTable.D_POINTS_OFFSET + (einzel ? 1 : 0));
            }
            JTableUtils.hideColumnAndRemoveData(tm,
                    JResultTable.PREFIX + JResultTable.D_RANK_OFFSET + (einzel ? 1 : 0));
            if (!times) {
                JTableUtils.hideColumnAndRemoveData(tm,
                        JResultTable.PREFIX + (einzel ? 1 : 0) + JResultTable.D_TIME_OFFSET);
            }
            if (!points) {
                JTableUtils.hideColumnAndRemoveData(tm, JResultTable.DIFF_OFFSET + (einzel ? 1 : 0));
                JTableUtils.hideColumnAndRemoveData(tm, JResultTable.SCORE_OFFSET + (einzel ? 1 : 0));
            }
        } else {
            if (!times) {
                for (int x = ak.getDiszAnzahl() - 1; x >= 0; x--) {
                    JTableUtils.hideColumnAndRemoveData(tm, JResultTable.PREFIX + (einzel ? 1 : 0)
                            + x * JResultTable.D_COLUMNS + JResultTable.D_TIME_OFFSET);
                }
            }
            if (!tm.getShowRanks()) {
                for (int x = ak.getDiszAnzahl() - 1; x >= 0; x--) {
                    JTableUtils.hideColumnAndRemoveData(tm, JResultTable.PREFIX + (einzel ? 1 : 0)
                            + x * JResultTable.D_COLUMNS + JResultTable.D_RANK_OFFSET);
                }
            }
        }
        if (!einzel && PrintUtils.printOmitOrganisationForTeams) {
            JTableUtils.hideColumnAndRemoveData(tm, JResultTable.ORGANISATION_OFFSET);
        }
        String title = resultstitle + " - " + ak.toString() + " " + I18n.geschlechtToString(wk.getRegelwerk(), male)
                + (wk.getCurrentFilterIndex() > 0 ? " (" + wk.getCurrentFilter().getName() + ")" : "");
        Printable p = PrintManager.getPrintable(tm, (String) null, JTablePrintable.OPT_ALL, true, true);
        ExtendedHeaderFooterPrintable hfp = new ExtendedHeaderFooterPrintable(p, headerpanel, footerpanel,
                ExtendedHeaderFooterPrintable.LEFT, ExtendedHeaderFooterPrintable.LEFT, PrintManager.getFont());
        return new HeaderFooterPrintable(hfp, new MessageFormat(title), null, PrintManager.getFont());
    }

    public static <T extends ASchwimmer> LinkedList<Printable> getEinzelwertungPrintables(AWettkampf<T> wk,
            boolean removeEmpty, int qualification) {
        LinkedList<Printable> mp = new LinkedList<>();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, x, removeEmpty);
            if (w != null) {
                Printable p = getResultsPrintable(w, true, PrintUtils.printPointsInDisciplineResults, removeEmpty,
                        qualification);
                if (p != null) {
                    mp.add(p);
                }
            }
        }
        for (Wertungsgruppe wg : aks.getWertungsgruppen()) {
            AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, wg.getName(), removeEmpty);
            if (w != null) {
                Printable p = getResultsPrintable(w, true, PrintUtils.printPointsInDisciplineResults, removeEmpty,
                        qualification);
                if (p != null) {
                    mp.add(p);
                }
            }
        }
        return mp;
    }

    public static <T extends ASchwimmer> Printable getWertungsgruppenPrintables(AWettkampf<T> wk, boolean removeEmpty,
            int qualification) {
        Printable mp = null;
        AWettkampf<T> w = CompetitionUtils.generateWertungsgruppenwettkampf(wk, true);
        if (w != null) {
            mp = getResultsPrintable(w, true, PrintUtils.printPointsInDisciplineResults, removeEmpty, qualification);
        }
        if (mp == null) {
            return EmptyPrintable.Instance;
        }
        return mp;
    }

    public static <T extends ASchwimmer> JComponent[] getSchnellsteZeitenTables(AWettkampf<T> wk) {
        if (wk == null) {
            return null;
        }
        {
            wk = Utils.copy(wk);
            LinkedList<T> sw = wk.getSchwimmer();
            for (T s : sw) {
                if (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
                    wk.removeSchwimmer(s);
                }
            }
        }

        LinkedList<JComponent> mp = new LinkedList<>();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            ak.setStrafeIstDisqualifikation(true);
            ak.setEinzelwertung(true);
            for (int y = 0; y < 2; y++) {
                AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, x, y == 1);
                if (w != null) {
                    {
                        LinkedList<T> sw = w.getSchwimmer();
                        for (T s : sw) {
                            if (s.getAkkumulierteStrafe(0).isStrafe()) {
                                w.removeSchwimmer(s);
                            } else if (!s.hasInput(0)) {
                                w.removeSchwimmer(s);
                            } else if (s.getZeit(0) <= 0) {
                                w.removeSchwimmer(s);
                            }
                        }
                    }

                    Object[][] data = new Object[ak.getDiszAnzahl()][];
                    for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                        JResultTable result = JResultTable.getResultTable(w, w.getRegelwerk().getAk(z), y == 1, true,
                                true, 0);
                        if (result.getRowCount() > 0) {
                            ASchwimmer s = result.getResult(0).getSchwimmer();
                            int zeit = s.getZeit(0);
                            StringBuilder name = new StringBuilder(s.getName());
                            StringBuilder gld = new StringBuilder(s.getGliederung());
                            int pos = 1;
                            while ((pos < result.getRowCount())
                                    && (result.getResult(pos).getSchwimmer().getZeit(0) == zeit)) {
                                name.append(", ");
                                name.append(result.getResult(pos).getSchwimmer().getName());
                                gld.append(", ");
                                gld.append(result.getResult(pos).getSchwimmer().getGliederung());
                                pos++;
                            }
                            data[z] = new Object[] { name.toString(), gld.toString(),
                                    ak.getDisziplin(z, y == 1).getName(), StringTools.zeitString(s.getZeit(0)) };
                        } else {
                            data[z] = new Object[] { " ", " ", ak.getDisziplin(z, y == 1).getName(), " " };
                        }
                    }

                    JTable t = new JTable(new DefaultTableModel(data, new Object[] { I18n.get("Name"),
                            I18n.get("Organisation"), I18n.get("Discipline"), I18n.get("Time") }));
                    JTableUtils.setAlignmentRenderer(t,
                            new int[] { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT },
                            SwingConstants.RIGHT);
                    JPrintTable.initPrintableJTable(t);
                    t.getTableHeader().setBorder(new ExtendedLineBorder(Color.BLACK, 1, 1, 0, 1));
                    t.setBorder(new LineBorder(Color.BLACK, 1));
                    JTableUtils.setPreferredCellWidths(t);

                    if (PrintUtils.printOmitOrganisationForTeams
                            && (((AWettkampf<?>) wk) instanceof MannschaftWettkampf)) {
                        JTableUtils.hideColumnAndRemoveData(t, 1);
                    }

                    JPanel p = new JPanel(new FormLayout("1dlu,fill:default:grow,1dlu",
                            "1dlu,fill:default,1dlu,fill:default,0dlu,fill:default,4dlu")) {
                        private static final long serialVersionUID = -3026983883651885669L;

                        @Override
                        public void setFont(Font font) {
                            super.setFont(font);
                            for (Component c : getComponents()) {
                                c.setFont(font);
                            }
                        }
                    };
                    p.add(new JLabel(I18n.getAgeGroupAsString(aks, ak, y == 1) + "   "), CC.xy(2, 2, "center, fill"));
                    p.add(t.getTableHeader(), CC.xy(2, 4));
                    p.add(t, CC.xy(2, 6));

                    if (PrintManager.getFont() != null) {
                        p.setFont(PrintManager.getFont());
                    }

                    mp.addLast(p);
                }
            }
        }
        if (mp.isEmpty()) {
            return null;
        }
        return mp.toArray(new JComponent[mp.size()]);
    }

    public static <T extends ASchwimmer> LinkedList<Printable> getEinzelwertungPrintables(AWettkampf<T> wk, int ak,
            boolean removeEmpty, int qualification) {
        LinkedList<Printable> mp = new LinkedList<>();
        for (int y = 0; y < 2; y++) {
            AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, ak, y == 1);
            if (w != null) {
                Printable p = getResultsPrintable(w, true, PrintUtils.printPointsInDisciplineResults, removeEmpty,
                        qualification);
                if (p != null) {
                    mp.add(p);
                }
            }
        }
        return mp;
    }

    private static class LaufInfo<T extends ASchwimmer> {

        private AWettkampf<T> wk;
        private String first = "";
        private String last = "";
        private BitSet[] sgs;
        private LinkedList<String> disziplinen = new LinkedList<>();

        public LaufInfo(AWettkampf<T> wk, Lauf<T> lauf) {
            this.wk = wk;
            int sgsize = wk.getRegelwerk().getStartgruppen().length;
            sgs = new BitSet[] { new BitSet(sgsize), new BitSet(sgsize) };
            sgs[0].clear();
            sgs[1].clear();

            HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

            first = lauf.getName(scheme);
            last = first;

            for (int x = 0; x < lauf.getBahnen(); x++) {
                T t = lauf.getSchwimmer(x);
                if (t != null) {
                    sgs[t.isMaennlich() ? 1 : 0].set(wk.getRegelwerk().getStartgruppenindex(t.getAK()), true);
                    String d = t.getAK().getDisziplin(lauf.getDisznummer(x), t.isMaennlich()).getName();
                    if (!disziplinen.contains(d))
                        disziplinen.addLast(d);
                }
            }
        }

        public String getFirstHeat() {
            return first;
        }

        public String getLastHeat() {
            return last;
        }

        public String[] getStartgruppen() {
            Regelwerk aks = wk.getRegelwerk();
            Startgruppe[] sgx = aks.getEffektiveStartgruppen();
            LinkedList<String> result = new LinkedList<>();
            for (int x = 0; x < sgx.length; x++) {
                if (sgs[0].get(x) && sgs[1].get(x)) {
                    result.addLast(I18n.get("AgeGroupSex", sgx[x].getName(), I18n.get("mixed")));
                } else {
                    for (int y = 0; y < 2; y++) {
                        if (sgs[y].get(x)) {
                            result.addLast(
                                    I18n.get("AgeGroupSex", sgx[x].getName(), I18n.geschlechtToString(aks, y == 1)));
                        }
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        public String[] getDisziplinen() {
            return disziplinen.toArray(new String[disziplinen.size()]);
        }

        public boolean joinable(LaufInfo<T> lauf) {
            if (lauf.isPure() != this.isPure()) {
                return false;
            }
            return lauf.sgs[0].intersects(sgs[0]) || lauf.sgs[1].intersects(sgs[1]);
        }

        public void join(LaufInfo<T> lauf) {
            for (int x = 0; x < 2; x++) {
                sgs[x].or(lauf.sgs[x]);
            }
            last = lauf.last;
            for (String disziplin : lauf.disziplinen) {
                if (!disziplinen.contains(disziplin)) {
                    disziplinen.addLast(disziplin);
                }
            }
        }

        public boolean isPure() {
            return sgs[0].cardinality() + sgs[1].cardinality() <= 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            sb.append(" - ");
            sb.append(last);
            sb.append(":");
            for (String ak : getStartgruppen()) {
                sb.append(" ");
                sb.append(ak);
            }
            sb.append(" | ");
            for (String disziplin : getDisziplinen()) {
                sb.append(" ");
                sb.append(disziplin);
            }
            sb.append(" | ");
            sb.append(sgs[0]);
            sb.append("/");
            sb.append(sgs[1]);
            return sb.toString();
        }
    }

    public static <T extends ASchwimmer> Printable getLaufuebersicht(AWettkampf<T> wk) {
        if (!wk.hasLaufliste()) {
            return null;
        }
        JTable t = getLaufuebersichtTable(wk);
        if (t == null) {
            return null;
        }
        return new JTablePrintable(t, JTablePrintable.OPT_ALL, true, PrintMode.FIT_WIDTH, true, PrintManager.getFont());
    }

    public static <T extends ASchwimmer> JPrintTable getLaufuebersichtTable(AWettkampf<T> wk) {
        if (wk == null) {
            throw new NullPointerException("wk must not be null.");
        }
        if (wk.getLaufliste() == null) {
            return null;
        }
        if (!wk.hasLaufliste()) {
            return null;
        }
        Laufliste<T> liste = wk.getLaufliste();
        if (liste.getLaufliste() == null) {
            return null;
        }
        if (liste.getLaufliste().isEmpty()) {
            return null;
        }

        LinkedList<LaufInfo<T>> data = new LinkedList<>();

        {
            // Daten zu den Läufen sammeln
            ListIterator<Lauf<T>> li = liste.getLaufliste().listIterator();
            while (li.hasNext()) {
                data.addLast(new LaufInfo<>(wk, li.next()));
            }
        }

        {
            // Daten verdichten
            ListIterator<LaufInfo<T>> li = data.listIterator();
            LaufInfo<T> last = null;
            while (li.hasNext()) {
                LaufInfo<T> current = li.next();
                if (last == null) {
                    last = current;
                } else {
                    if (last.joinable(current)) {
                        li.remove();
                        last.join(current);
                    } else {
                        last = current;
                    }
                }
            }
        }

        Object[][] temp = new Object[data.size()][3];
        int x = 0;
        for (LaufInfo<T> lauf : data) {
            String[] aks = lauf.getStartgruppen();
            switch (aks.length) {
            case 0:
                temp[x][0] = "";
                break;
            case 1:
                temp[x][0] = aks[0];
                break;
            default:
                temp[x][0] = aks;
                break;
            }
            String[] disziplinen = lauf.getDisziplinen();
            switch (disziplinen.length) {
            case 0:
                temp[x][1] = "";
                break;
            case 1:
                temp[x][1] = disziplinen[0];
                break;
            default:
                temp[x][1] = disziplinen;
                break;
            }
            temp[x][2] = lauf.getFirstHeat() + " - " + lauf.getLastHeat();
            x++;
        }

        JPrintTable t = new JPrintTable(temp,
                new Object[] { I18n.get("AgeGroup"), I18n.get("Discipline"), I18n.get("Heats") });
        JTableUtils.setAlignmentRenderer(t,
                new int[] { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER }, SwingConstants.CENTER);
        t.setOpaque(true);
        t.setBackground(Color.WHITE);
        return t;
    }

    public static <T extends ASchwimmer> boolean hasLaufuebersicht(AWettkampf<T> wk) {
        if (wk == null) {
            return false;
        }
        if (wk.getLaufliste() == null) {
            return false;
        }
        Laufliste<T> liste = wk.getLaufliste();
        if (liste.getLaufliste().isEmpty()) {
            return false;
        }
        return true;
    }

    public static JTable getDisciplinesTable(Regelwerk aks) {
        int max = aks.getMaxDisciplineCount();

        Object[][] data = new Object[aks.size()][max + 1];
        Object[] titles = new Object[max + 1];

        titles[0] = I18n.get("AgeGroup");
        for (int x = 1; x < titles.length; x++) {
            titles[x] = I18n.get("DisciplineNumber", x);
        }

        for (int y = 0; y < data.length; y++) {
            Altersklasse ak = aks.getAk(y);
            data[y][0] = ak.getName();
            for (int x = 1; x < titles.length; x++) {
                if (x <= ak.getDiszAnzahl()) {
                    data[y][x] = I18n.getDisziplinShort(ak.getDisziplin(x - 1, true).getName());
                } else {
                    data[y][x] = " ";
                }
            }
        }

        TableModel model = new DefaultTableModel(data, titles);
        return new JTable(model);
    }
}