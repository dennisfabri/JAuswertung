/*
 * Created on 28.10.2004
 */
package de.df.jauswertung.gui.util;

import java.awt.Component;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.zip.CRC32;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.Results;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.gui.jtable.ColumnFittingMouseAdapter;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri @date 28.10.2004
 */
public final class JResultTable extends JGroupableTable {

    public static boolean printRanksInResults = false;

    public static boolean resultsWithDecimals = false;

    Results<ASchwimmer> results = new Results<>();
    @SuppressWarnings("rawtypes")
    AWettkampf wk = null;
    Altersklasse ak = null;
    boolean maennlich;
    boolean[] select;
    boolean showZW;
    int qualification;

    boolean set = false;

    boolean einzel = false;

    private final ResultUpdater resultUpdater = new ResultUpdater();
    private AlignmentCellRenderer acr;

    ColumnGroup[] disziplinen = null;

    public static final int RANK_OFFSET = 0;
    public static final int NAME_OFFSET = 1;
    public static final int ORGANISATION_OFFSET = 2;
    public static final int QUALIFICATION_OFFSET = 3;
    public static final int BIRTH_OFFSET = 4;
    public static final int SCORE_OFFSET = 4;
    public static final int DIFF_OFFSET = 5;

    public static final int D_PENALTY_OFFSET = 3;
    public static final int D_POINTS_OFFSET = 2;
    public static final int D_TIME_OFFSET = 1;
    public static final int D_RANK_OFFSET = 0;

    public static final int D_COLUMNS = 4;
    public static final int PREFIX = 6;
    public static final int LENGTH = D_COLUMNS;

    public static double COLOR_DIMMER = 0.75;

    private final boolean print;

    private boolean showRanks = false;
    private boolean showTimes = true;

    private JResultTable(DefaultTableModel dtm, boolean printable) {
        super(dtm);

        print = printable;

        if (print) {
            setShowRanks(printRanksInResults);
        } else {
            ColumnFittingMouseAdapter.enable(this);
            DataTipManager.get().register(this);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void setShowRanks(boolean showRanks) {
        if (this.showRanks == showRanks) {
            return;
        }
        this.showRanks = showRanks;
        if ((wk != null) && (ak != null)) {
            updateResult(wk, ak, maennlich, select, showZW, qualification);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void setTimes(boolean showTimes) {
        if (this.showTimes == showTimes) {
            return;
        }
        this.showTimes = showTimes;
        if ((wk != null) && (ak != null)) {
            updateResult(wk, ak, maennlich, select, showZW, qualification);
        }
    }

    void setACR(AlignmentCellRenderer acr) {
        this.acr = acr;
    }

    void updateShowRanksView() {
        if (acr != null) {
            boolean[] vis = new boolean[getColumnCount()];
            for (int x = 0; x < vis.length; x++) {
                vis[x] = true;
            }

            int offset = (isEinzel() ? 1 : 0);
            for (int x = ak.getDiszAnzahl() - 1; x >= 0; x--) {
                int column = PREFIX + offset + x * D_COLUMNS + D_RANK_OFFSET;
                if (showRanks) {
                    JTableUtils.unhideColumn(this, column, I18n.get("RankShort"));
                } else {
                    JTableUtils.hideColumnAndRemoveData(this, column);
                    vis[column] = false;
                }
            }
            acr.setVisible(vis);
        }
    }

    void updateShowTimesView() {
        if (acr != null) {
            boolean[] vis = new boolean[getColumnCount()];
            for (int x = 0; x < vis.length; x++) {
                vis[x] = true;
            }

            int offset = (isEinzel() ? 1 : 0);
            for (int x = ak.getDiszAnzahl() - 1; x >= 0; x--) {
                int column = PREFIX + offset + x * D_COLUMNS + D_TIME_OFFSET;
                if (showTimes) {
                    JTableUtils.unhideColumn(this, column, I18n.get("Time"));
                } else {
                    JTableUtils.hideColumnAndRemoveData(this, column);
                    vis[column] = false;
                }
            }
            acr.setVisible(vis);
        }
    }

    public boolean getShowRanks() {
        return showRanks;
    }

    public boolean isEinzel() {
        return einzel;
    }

    @SuppressWarnings("rawtypes")
    public SchwimmerResult getResult(int row) {
        return results.getResult(row);
    }

    public ASchwimmer getSelectedSchwimmer() {
        if (getSelectedRowCount() == 0) {
            return null;
        }
        return getResult(getSelectedRow()).getSchwimmer();
    }

    public int getSelectedDiscipline() {
        int col = getSelectedColumn();
        if (col < PREFIX + (einzel ? 1 : 0)) {
            return ASchwimmer.DISCIPLINE_NUMBER_SELF;
        }
        int dis = (col - PREFIX - (einzel ? 1 : 0)) / LENGTH;
        if (dis >= getSelectedSchwimmer().getAK().getDiszAnzahl()) {
            return ASchwimmer.DISCIPLINE_NUMBER_ZW;
        }
        return dis;
    }

    public boolean isHLW() {
        if (!getSelectedSchwimmer().getAK().hasHLW()) {
            return false;
        }
        return getSelectedColumn() == (getColumnCount() - 1);
    }

    public static int getColumnCount(int anzahl, boolean hlw, boolean einzel, int qualification) {
        return PREFIX + (einzel ? 1 : 0) + LENGTH * anzahl + (hlw ? 1 : 0) + (qualification > 0 ? 1 : 0);
    }

    public static JResultTable getResultTable(Altersklasse ak, int anzahl, boolean hlw, boolean einzel, boolean print,
            int qualification, String zwtext) {
        String[] headers = new String[getColumnCount(anzahl, hlw, einzel, qualification)];
        headers[RANK_OFFSET] = I18n.get("RankShort");
        headers[NAME_OFFSET] = I18n.get("Name");
        headers[ORGANISATION_OFFSET] = I18n.get("Organisation");
        int offset = 0;
        if (einzel) {
            headers[BIRTH_OFFSET] = I18n.get("YearOfBirthShort");
            offset = 1;
        }
        headers[SCORE_OFFSET + offset] = I18n.get("Score");
        headers[DIFF_OFFSET + offset] = I18n.get("DifferenceShort");
        for (int x = 0; x < anzahl; x++) {
            headers[PREFIX + D_RANK_OFFSET + offset + x * LENGTH] = I18n.get("RankShort");
            headers[PREFIX + D_TIME_OFFSET + offset + x * LENGTH] = I18n.get("Time");
            headers[PREFIX + D_POINTS_OFFSET + offset + x * LENGTH] = I18n.get("ScoreShort");
            headers[PREFIX + D_PENALTY_OFFSET + offset + x * LENGTH] = I18n.get("PenaltyShort");
        }
        if (hlw) {
            headers[PREFIX + offset + anzahl * LENGTH] = zwtext;
        }
        if (qualification > 0) {
            headers[PREFIX + offset + anzahl * LENGTH + (hlw ? 1 : 0)] = I18n.get("Qualification");
        }

        DefaultTableModel dtm = new SimpleTableModel(new Object[0][0], headers);

        JResultTable table = new JResultTable(dtm, print);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        TableColumnModel cm = table.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
        if (anzahl > 1) {
            table.disziplinen = new ColumnGroup[anzahl];
            for (int x = 0; x < anzahl; x++) {
                String disziplin = I18n.getDisziplinShort(ak.getDisziplin(x, true).getName());
                table.disziplinen[x] = new ColumnGroup(disziplin);
                for (int y = 0; y < LENGTH; y++) {
                    table.disziplinen[x].add(cm.getColumn(PREFIX + y + offset + LENGTH * x));
                }
                header.addColumnGroup(table.disziplinen[x]);
            }
        } else {
            table.disziplinen = null;
        }

        double[] colors = new double[table.getColumnCount()];
        AlignmentCellRenderer.BorderPositions[] borders = new AlignmentCellRenderer.BorderPositions[colors.length];
        for (int x = 0; x < colors.length; x++) {
            colors[x] = 1.0;
            borders[x] = AlignmentCellRenderer.BorderPositions.NONE;
        }

        double dimmer = COLOR_DIMMER;

        if (einzel) {
            colors[JResultTable.BIRTH_OFFSET] = dimmer;
        }
        colors[offset + DIFF_OFFSET] = dimmer;
        borders[offset + DIFF_OFFSET] = AlignmentCellRenderer.BorderPositions.RIGHT;
        for (int x = 0; x < anzahl; x++) {
            colors[PREFIX + D_RANK_OFFSET + offset + x * LENGTH] = dimmer;
            colors[PREFIX + D_POINTS_OFFSET + offset + x * LENGTH] = dimmer;
            if ((x + 1 < anzahl) || ak.hasHLW()) {
                borders[PREFIX + D_PENALTY_OFFSET + offset + x * LENGTH] = AlignmentCellRenderer.BorderPositions.RIGHT;
            }
        }
        if (hlw) {
            colors[PREFIX + offset + anzahl * LENGTH] = dimmer;
        }
        if (qualification > 0) {
            colors[PREFIX + offset + anzahl * LENGTH + (hlw ? 1 : 0)] = dimmer;
        }

        AlignmentCellRenderer acr = new AlignmentCellRenderer(
                new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT },
                SwingConstants.RIGHT);
        acr.setColor(colors);
        if (print && (ak.getDiszAnzahl() > 1)) {
            acr.setBorders(borders);
        }

        JTableUtils.setTableCellRenderer(table, acr);
        JTableUtils.setAlternatingTableCellRenderer(table);

        table.setACR(acr);

        return table;
    }

    public static <T extends ASchwimmer> JResultTable getResultTable(AWettkampf<T> wk, Altersklasse ak,
            boolean maennlich, boolean print, boolean hlw, int qualification) {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        JResultTable result = getResultTable(ak, ak.getDiszAnzahl(), ak.hasHLW(), w instanceof EinzelWettkampf, print,
                qualification, wk.getRegelwerk().getZusatzwertungShort());
        result.updateResult(wk, ak, maennlich, null, hlw, qualification);
        return result;
    }

    public static <T extends ASchwimmer> JResultTable getResultTable(AWettkampf<T> wk, Altersklasse ak,
            boolean maennlich, boolean[] select, boolean print, boolean hlw, int qualification) {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        JResultTable result = getResultTable(ak, ak.getDiszAnzahl(), ak.hasHLW(), w instanceof EinzelWettkampf, print,
                qualification, wk.getRegelwerk().getZusatzwertungShort());
        result.updateResult(wk, ak, maennlich, select, hlw, qualification);
        return result;
    }

    public <T extends ASchwimmer> void updateResult(AWettkampf<T> w, @SuppressWarnings("hiding") Altersklasse ak,
            @SuppressWarnings("hiding") boolean maennlich, @SuppressWarnings("hiding") boolean[] select, boolean hlw,
            @SuppressWarnings("hiding") int qualification) {
        synchronized (this) {
            this.wk = w;
            einzel = wk instanceof EinzelWettkampf;

            if (select == null) {
                select = new boolean[ak == null ? 0 : ak.getDiszAnzahl()];
                Arrays.fill(select, true);
            }

            resultUpdater.setData(w, ak, maennlich, select, hlw, qualification);
        }
        EDTUtils.executeOnEDT(resultUpdater);
    }

    void updateRowCount(int rows) {
        if (getRowCount() != rows) {
            while (getRowCount() > rows) {
                ((DefaultTableModel) getModel()).removeRow(getRowCount() - 1);
            }
            while (getRowCount() < rows) {
                Vector<String> v = new Vector<>();
                for (int x = 0; x < getColumnCount(); x++) {
                    v.add("");
                }
                ((DefaultTableModel) getModel()).addRow(v);
            }
            tableChanged(new TableModelEvent(getModel()));
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        String text = (value == null ? "" : value.toString().trim());
        if (print && (!text.isEmpty())) {
            text += " ";
        }
        super.setValueAt(text, row, column);
    }

    <T extends ASchwimmer> void updateDiscipline(int x, int y, SchwimmerData<T>[] daten, int offset, boolean choice,
            @SuppressWarnings("rawtypes") SchwimmerResult result) {
        if (!choice) {
            for (int a = 0; a < LENGTH; a++) {
                setValueAt("", y, PREFIX + a + LENGTH * x + offset);
            }
            return;
        }
        Strafe strafe = daten[x].getStrafe();
        setValueAt(PenaltyUtils.getPenaltyMediumText(strafe, daten[x].getSchwimmer().getAK()), y,
                PREFIX + D_PENALTY_OFFSET + LENGTH * x + offset);
        if (daten[x].getTime() > 0) {
            setValueAt(StringTools.zeitString(daten[x].getTime()), y, D_TIME_OFFSET + PREFIX + LENGTH * x + offset);
            int i = daten[x].getRank();
            Object platz = "";
            if (i > 0) {
                platz = i;
            }
            setValueAt(platz, y, D_RANK_OFFSET + PREFIX + LENGTH * x + offset);
        } else {
            setValueAt("", y, D_TIME_OFFSET + PREFIX + LENGTH * x + offset);
            setValueAt("", y, D_RANK_OFFSET + PREFIX + LENGTH * x + offset);
        }
        switch (strafe.getArt()) {
        case DISQUALIFIKATION:
        case AUSSCHLUSS:
            setValueAt("", y, PREFIX + D_RANK_OFFSET + LENGTH * x + offset);
            if (daten[x].getPoints() < 0.005) {
                setValueAt("", y, PREFIX + D_POINTS_OFFSET + LENGTH * x + offset);
            } else {
                setValueAt(StringTools.punkteString(daten[x].getPoints(), resultsWithDecimals), y,
                        PREFIX + D_POINTS_OFFSET + LENGTH * x + offset);
            }
            break;
        default:
            setValueAt(StringTools.punkteString(daten[x].getPoints(), resultsWithDecimals), y,
                    PREFIX + D_POINTS_OFFSET + LENGTH * x + offset);
            break;
        }

        if (result.hasKeineWertung() && result.getResults().length == 1) {
            setValueAt("", y, D_RANK_OFFSET + PREFIX + LENGTH * x + offset);
            setValueAt("", y, PREFIX + D_POINTS_OFFSET + LENGTH * x + offset);
        }
    }

    public boolean isInPrefix(int x) {
        x = x - PREFIX - (einzel ? 1 : 0);
        return x < 0;
    }

    public boolean isPenaltyColumn(int x) {
        x = x - PREFIX - (einzel ? 1 : 0);
        if (x < 0) {
            return false;
        }
        x = x % LENGTH;
        return x == D_PENALTY_OFFSET;
    }

    public boolean isRankColumn(int x) {
        x = x - PREFIX - (einzel ? 1 : 0);
        if (x < 0) {
            return false;
        }
        x = x % LENGTH;
        return x == D_RANK_OFFSET;
    }

    public boolean isTimeColumn(int x) {
        x = x - PREFIX - (einzel ? 1 : 0);
        if (x < 0) {
            return false;
        }
        x = x % LENGTH;
        return x == D_TIME_OFFSET;
    }

    public boolean isPointsColumn(int x) {
        x = x - PREFIX - (einzel ? 1 : 0);
        if (x < 0) {
            return false;
        }
        x = x % LENGTH;
        return x == D_POINTS_OFFSET;
    }

    @SuppressWarnings("rawtypes")
    public String getChecksum() {
        List<String> text = new ArrayList<>();
        for (SchwimmerResult sr : results) {
            StringBuilder sb = new StringBuilder();

            String sn = "" + sr.getSchwimmer().getStartnummer();
            sb.append(sn);
            StringTools.fill(sb, 10 - sn.length());

            sb.append(";");

            String penalty = PenaltyUtils.getPenaltyShortText(sr.getStrafe(), sr.getSchwimmer().getAK());
            sb.append(penalty);
            StringTools.fill(sb, 20 - penalty.length());

            for (int y = 0; y < sr.getResults().length; y++) {
                SchwimmerData sd = sr.getResults()[y];

                sb.append(";");

                String time = StringTools.zeitString(sd.getTime());
                StringTools.fill(sb, 10 - time.length());
                sb.append(time);

                sb.append(";");

                penalty = PenaltyUtils.getPenaltyShortText(sd.getStrafe(), sd.getSchwimmer().getAK());
                sb.append(penalty);
                StringTools.fill(sb, 20 - penalty.length());

                sb.append(";");

                String points = "" + sd.getStrafe().getStrafpunkte();
                StringTools.fill(sb, 5 - points.length());
                sb.append(points);
            }
            if (sr.getSchwimmer().getAK().hasHLW()) {
                sb.append(";");

                if (showZW) {
                    switch (sr.getSchwimmer().getHLWState()) {
                    case NICHT_ANGETRETEN:
                        sb.append("0      ");
                        break;
                    case ENTERED:
                        String hlwstring = "" + sr.getSchwimmer().getHLWPunkte();
                        sb.append("1");
                        StringTools.fill(sb, 6 - hlwstring.length());
                        sb.append(hlwstring);
                        break;
                    case NOT_ENTERED:
                        sb.append("2      ");
                        break;
                    case DISQALIFIKATION:
                        sb.append("3      ");
                        break;
                    }
                } else {
                    sb.append("2      ");
                }
            }
            text.add(sb.toString());
        }
        Collections.sort(text);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        try (PrintStream ps = new PrintStream(bos)) {
            for (String aText : text) {
                ps.print(aText);
                ps.print("|");
            }
        }
        CRC32 crc = new CRC32();
        crc.update(bos.toByteArray());
        return StringTools.asCode(crc.getValue());
    }

    class ResultUpdater implements Runnable {

        @SuppressWarnings("rawtypes")
        public void setData(AWettkampf wk, Altersklasse ak, boolean maennlich, boolean[] select, boolean hlw,
                int qualification) {
            JResultTable.this.wk = wk;
            JResultTable.this.ak = ak;
            JResultTable.this.maennlich = maennlich;
            JResultTable.this.select = select;
            JResultTable.this.showZW = hlw;
            JResultTable.this.qualification = qualification;
            set = true;
        }

        @Override
        @SuppressWarnings({ "unchecked" })
        public void run() {
            synchronized (this) {
                if (!set) {
                    return;
                }
                set = false;
            }
            if (ak == null) {
                updateRowCount(0);
                return;
            }

            boolean multiline = wk.getBooleanProperty(PropertyConstants.RESULT_MULTILINE, false);

            boolean onlyRanks = FormelManager.getInstance().get(wk.getRegelwerk().getFormelID()).getDataType()
                    .equals(DataType.RANK);
            setTimes(!onlyRanks);
            if (print) {
                setShowRanks(printRanksInResults || onlyRanks);
            } else {
                setShowRanks(onlyRanks);
            }

            results = new Results<>(ResultCalculator.getResults(wk, ak, maennlich, select, showZW));

            if (disziplinen != null) {
                for (int x = 0; x < disziplinen.length; x++) {
                    disziplinen[x].setText(I18n.getDisziplinShort(ak.getDisziplin(x, maennlich).getName()));
                }
            }

            // Update UI
            updateRowCount(results.size());

            int gliederunglength = -1;
            int qualilength = -1;

            int rows = results.size();
            for (int y = 0; y < rows; y++) {
                ASchwimmer s = results.getSchwimmer(y);
                Strafe str = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                if (str.getArt() == Strafarten.AUSSCHLUSS) {
                    setValueAt("-", y, RANK_OFFSET);
                } else {
                    if (s.isAusserKonkurrenz()) {
                        setValueAt(I18n.get("AusserKonkurrenzShort"), y, RANK_OFFSET);
                    } else {
                        if (results.hasKeineWertung(y)) {
                            setValueAt("-", y, RANK_OFFSET);
                        } else {
                            setValueAt("" + results.getPlace(y), y, RANK_OFFSET);
                        }
                    }
                }

                String name = s.getName();
                if (multiline && s instanceof Mannschaft) {
                    Mannschaft m = (Mannschaft) s;
                    String namen = "";
                    if (ak.getDiszAnzahl() == 1) {
                        namen = m.getStarterShort(0, ", ");
                        if (namen.length() == 0) {
                            namen = m.getMitgliedernamenShort(", ");
                        }
                    } else {
                        namen = m.getMitgliedernamenShort(", ");
                    }
                    name = I18n.get("TeamnameMultiline", s.getName(), namen);
                }
                String penalty = PenaltyUtils
                        .getPenaltyMediumText(s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF), s.getAK());
                if ((penalty.trim().length() > 0) && (!penalty.equals("-"))) {
                    name += " (" + penalty + ")";
                }
                setValueAt(name, y, NAME_OFFSET);
                setValueAt(s.getGliederung(), y, ORGANISATION_OFFSET);
                setValueAt(s.getQualifikationsebene(), y, QUALIFICATION_OFFSET);

                gliederunglength = Math.max(gliederunglength, s.getGliederung().length());
                qualilength = Math.max(qualilength, s.getQualifikationsebene().length());

                int offset = 0;
                if (einzel) {
                    setValueAt(I18n.yearToShortString(((Teilnehmer) s).getJahrgang()) + " ", y, BIRTH_OFFSET);
                    offset = 1;
                }

                if (str.getArt() == Strafarten.AUSSCHLUSS) {
                    setValueAt(I18n.get("DebarmentShort"), y, SCORE_OFFSET + offset);
                    setValueAt("-", y, DIFF_OFFSET + offset);
                } else {
                    if (results.hasKeineWertung(y)) {
                        setValueAt("", y, SCORE_OFFSET + offset);
                        setValueAt("", y, DIFF_OFFSET + offset);
                    } else {
                        setValueAt(StringTools.punkteString(results.getPoints(y)), y, SCORE_OFFSET + offset);
                        if (results.getPoints(y) <= 0.005) {
                            setValueAt("", y, DIFF_OFFSET + offset);
                        } else {
                            double d = results.getPointsDifferenceToFirst(y);
                            setValueAt(StringTools.punkteString(Math.abs(d), false), y, DIFF_OFFSET + offset);
                        }
                    }
                }

                @SuppressWarnings("rawtypes")
                SchwimmerData[] daten = results.getResults(y);
                for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                    updateDiscipline(x, y, daten, offset, results.getSchwimmer(y).isDisciplineChosen(x),
                            results.getResult(y));
                }
                if (ak.hasHLW()) {
                    if (showZW) {
                        switch (s.getHLWState()) {
                        case ENTERED:
                            setValueAt(StringTools.punkteString(s.getHLWPunkte(), resultsWithDecimals), y,
                                    PREFIX + ak.getDiszAnzahl() * LENGTH + offset);
                            break;
                        case NOT_ENTERED:
                            setValueAt("     ", y, PREFIX + ak.getDiszAnzahl() * LENGTH + offset);
                            break;
                        case NICHT_ANGETRETEN: {
                            String text = I18n.get("DidNotStartShort");
                            Strafe na = wk.getStrafen().getNichtAngetreten();
                            if (na.getShortname().length() > 0) {
                                text = na.getShortname();
                            }
                            setValueAt(text, y, PREFIX + ak.getDiszAnzahl() * LENGTH + offset);
                            break;
                        }
                        case DISQALIFIKATION: {
                            String text = I18n.get("DisqualificationShort");
                            Strafe na = wk.getStrafen().getDisqualifiziert();
                            if (na.getShortname().length() > 0) {
                                text = na.getShortname();
                            }
                            setValueAt(text, y, PREFIX + ak.getDiszAnzahl() * LENGTH + offset);
                            break;
                        }
                        }
                    } else {
                        setValueAt("     ", y, PREFIX + ak.getDiszAnzahl() * LENGTH + offset);
                    }
                }
                if (qualification > 0) {
                    int place = results.getPlace(y);
                    Strafe strafe = daten[0].getStrafe();
                    boolean hasNotWithdrawn = strafe == null || !strafe.isWithdraw();
                    boolean isQualified = place <= qualification && place > 0 && !results.hasKeineWertung(y)
                            && !results.isAusserKonkurrenz(y) && daten.length == 1 && hasNotWithdrawn;
                    if (isQualified) {
                        isQualified = daten[0].getRank() > 0 && daten[0].getTime() > 0;
                    }
                    if (!isQualified) {
                        if (place >= 0 && place < qualification) {
                            qualification++;
                        }
                    }
                    String text = isQualified ? "Q" : "   ";

                    int hlwOffset = ak.hasHLW() && showZW ? 1 : 0;

                    setValueAt(text, y, PREFIX + ak.getDiszAnzahl() * LENGTH + offset + hlwOffset);
                }
            }

            if (gliederunglength < I18n.get("Organisation").length()) {
                getColumnModel().getColumn(ORGANISATION_OFFSET).setHeaderValue(I18n.get("OrganisationShort"));
            } else {
                getColumnModel().getColumn(ORGANISATION_OFFSET).setHeaderValue(I18n.get("Organisation"));
            }
            if (qualilength < I18n.get("Qualifikationsebene").length()) {
                if (qualilength < I18n.get("QualifikationsebeneShort").length()) {
                    getColumnModel().getColumn(QUALIFICATION_OFFSET).setHeaderValue("");
                } else {
                    getColumnModel().getColumn(QUALIFICATION_OFFSET)
                            .setHeaderValue(I18n.get("QualifikationsebeneShort"));
                }
            } else {
                getColumnModel().getColumn(QUALIFICATION_OFFSET).setHeaderValue(I18n.get("Qualifikationsebene"));
            }

            JTableUtils.setPreferredCellWidths(JResultTable.this);
            if ((getRowCount() > 0) && (getSelectedRowCount() == 0)) {
                setRowSelectionInterval(0, 0);
            }

            updateShowRanksView();
            updateShowTimesView();

            updateRowHeights();
        }
    }

    private void updateRowHeights() {
        for (int row = 0; row < getRowCount(); row++) {
            int result = 0;

            for (int column = 0; column < getColumnCount(); column++) {
                Component comp = prepareRenderer(getCellRenderer(row, column), row, column);
                int height = comp.getPreferredSize().height;
                if (comp instanceof JComponent) {
                    JComponent jc = (JComponent) comp;
                    Insets i = jc.getInsets();
                    height += i.top + i.bottom;
                }
                result = Math.max(result, height);
            }

            setRowHeight(row, result + this.getRowMargin());
        }
    }
}