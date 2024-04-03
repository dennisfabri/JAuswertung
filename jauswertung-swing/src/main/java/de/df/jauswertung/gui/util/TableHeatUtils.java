/*
 * Created on 20.01.2006
 */
package de.df.jauswertung.gui.util;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.data.ListUtils;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.gui.renderer.ConditionalCenterRenderer;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.util.StringTools;

public final class TableHeatUtils {

    private TableHeatUtils() {
        // Hide
    }

    private static final class Meldesorter implements Comparator<SchwimmerInfo> {

        private final int meldeindex;

        public Meldesorter(int index) {
            meldeindex = index;
        }

        @Override
        public int compare(SchwimmerInfo o1, SchwimmerInfo o2) {
            return (int) Math
                    .round((o1.getSchwimmer().getMeldepunkte(meldeindex) - o2.getSchwimmer().getMeldepunkte(meldeindex))
                            * 100);
        }
    }

    static class ExtendedModel {
        public TableModel model;
        public String[] heats;
    }

    private static <T extends ASchwimmer> Object[][] laufeinteilungToData(int diszAnzahl, SchwimmerInfo[] schwimmer,
            boolean numbering, boolean withAgegroup, boolean withOrganisation, int meldeindex, boolean zw,
            boolean showSN) {
        int offsetName = 1;
        int offsetAgegroup = (withAgegroup ? 1 : 0);
        int offsetOrganisation = (withOrganisation ? 2 : 0);
        int offset = (numbering ? 1 : 0) + offsetName + offsetAgegroup + offsetOrganisation + 1;

        Object[][] data = new Object[schwimmer.length][offset + diszAnzahl + (zw ? 1 : 0) + (showSN ? 1 : 0)];

        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[y].length; x++) {
                data[y][x] = "";
            }

            int pos = 0;

            SchwimmerInfo si = schwimmer[y];
            ASchwimmer t = si.getSchwimmer();
            if (t == null) {
                for (int x = 0; x < data[y].length; x++) {
                    data[y][x] = "";
                }
            } else {
                if (numbering) {
                    data[y][pos] = "" + (data.length - y);
                    pos++;
                }
                if (showSN) {
                    data[y][pos] = StartnumberFormatManager.format(t);
                    pos++;
                }

                boolean isMultiline = t.getWettkampf().isMultiline();

                String name = "";
                Mannschaft m = isMultiline && t instanceof Mannschaft ? (Mannschaft) t : null;
                if (m != null) {
                    if (t.getName().equals(t.getGliederung())) {
                        name = StringTools.shorten(m.getMitgliedernamenShort(", "), 30, "...");
                    } else {
                        name = I18n.get("TeamnameMultiline", t.getName(), m.getMitgliedernamenShort(", "));
                    }
                } else {
                    name = t.getName();
                }

                data[y][pos] = name + (t.isAusserKonkurrenz() ? " (AK)" : "");
                pos++;
                if (withOrganisation) {
                    data[y][pos] = t.getGliederung();
                    pos++;
                    data[y][pos] = t.getQualifikationsebene();
                    pos++;
                }
                if (withAgegroup) {
                    data[y][pos] = t.getAK().toString() + " " + I18n.geschlechtToShortString(t);
                    pos++;
                }

                data[y][pos] = StringTools.punkteString(t.getMeldepunkte(meldeindex));
                pos++;

                for (int x = 0; x < diszAnzahl; x++) {
                    if ((x < si.getLaufCount()) && (!si.getLauf(x).equals("-"))) {
                        data[y][pos + x] = si.getLauf(x) + " / " + si.getBahn(x);
                    } else {
                        data[y][pos + x] = " ";
                    }
                }
                if (zw) {
                    if (t.getAK().hasHLW()) {
                        ZWInfo[] infos = SchwimmerUtils.getZWInfo(t.getWettkampf(), t);

                        // Only display first time
                        StringBuilder sb = new StringBuilder();
                        if ((infos != null) && (infos.length > 0) && (infos[0] != null)
                                && (infos[0].getZeit() != null)) {
                            sb.append(infos[0].getZeit().trim());
                        }
                        if (sb.length() == 0) {
                            sb.append("-");
                        }

                        data[y][data[y].length - 1] = sb.toString();
                    } else {
                        data[y][data[y].length - 1] = " ";
                    }
                }
            }
        }
        return data;
    }

    private static <T extends ASchwimmer> SimpleTableModel[] buildLaufeinteilungTabellen(final AWettkampf<T> wk,
            boolean zw) {
        if (wk == null) {
            return null;
        }

        LinkedList<SchwimmerInfo> lsi = ListUtils.mergeLists(SchwimmerUtils.toInfo(wk));
        Collections.sort(lsi);
        ListIterator<SchwimmerInfo> li = lsi.listIterator();
        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        int anzahl = 0;
        SchwimmerInfo first = null;
        LinkedList<SchwimmerInfo[]> result = new LinkedList<>();
        SchwimmerInfo[] current = new SchwimmerInfo[bahnen];
        result.addLast(current);
        while (li.hasNext()) {
            SchwimmerInfo si = li.next();
            if (first == null) {
                first = si;
            }
            if (first.isSimilar(si)) {
                current[anzahl] = si;
                anzahl++;
                if (anzahl >= bahnen) {
                    current = new SchwimmerInfo[bahnen];
                    result.addLast(current);
                    anzahl = 0;
                    first = null;
                }
            } else {
                while (anzahl < bahnen) {
                    current[anzahl] = new SchwimmerInfo(null, new String[0]);
                    anzahl++;
                }
                current = new SchwimmerInfo[bahnen];
                result.addLast(current);
                current[0] = si;
                anzahl = 1;
                first = si;
            }
        }
        if (anzahl > 0) {
            while (anzahl < bahnen) {
                current[anzahl] = new SchwimmerInfo(null, new String[0]);
                anzahl++;
            }
        } else {
            result.removeLast();
        }

        Collections.sort(result, new Comparator<SchwimmerInfo[]>() {
            @Override
            public int compare(SchwimmerInfo[] o1, SchwimmerInfo[] o2) {
                SchwimmerInfo s1 = o1[0];
                SchwimmerInfo s2 = o2[0];

                if ((s1 == null) || (s2 == null)) {
                    return (s1 == null ? 1 : -1);
                }
                int i = s1.getSchwimmer().getAKNummer() - s2.getSchwimmer().getAKNummer();
                if (i != 0) {
                    return i * 2;
                }
                int g1 = s1.getSchwimmer().isMaennlich() ? 1 : 0;
                int g2 = s2.getSchwimmer().isMaennlich() ? 1 : 0;
                return g1 - g2;
            }
        });

        // Ggf. fast leere Tabellen zusammenfassen
        if ((bahnen > 4) && (result.size() >= 2)) {
            String[] nothing = new String[bahnen];
            for (int x = 0; x < nothing.length; x++) {
                nothing[x] = "";
            }
            SchwimmerInfo empty = new SchwimmerInfo(null, nothing);

            ListIterator<SchwimmerInfo[]> rli = result.listIterator();
            SchwimmerInfo[] previous = rli.next();
            while (rli.hasNext()) {
                SchwimmerInfo[] c = rli.next();
                if (getLength(previous) + getLength(c) > bahnen) {
                    previous = c;
                } else {
                    int posCurrent = 0;
                    int posPrevious = 0;
                    while ((posCurrent < c.length) && (!c[posCurrent].isEmpty())) {
                        while ((posPrevious < previous.length) && (!previous[posPrevious].isEmpty())) {
                            posPrevious++;
                        }
                        if (posPrevious < previous.length) {
                            previous[posPrevious] = c[posCurrent];
                            c[posCurrent] = empty;
                        }
                        posCurrent++;
                    }
                    rli.remove();
                }
            }
        }

        int diszAnzahl = 0;
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            diszAnzahl = Math.max(diszAnzahl, wk.getRegelwerk().getAk(x).getDiszAnzahl());
        }

        int meldeindex = wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0);
        SimpleTableModel[] models = new SimpleTableModel[result.size()];
        ListIterator<SchwimmerInfo[]> tables = result.listIterator();
        for (int x = 0; x < models.length; x++) {
            Object[][] data = laufeinteilungToData(diszAnzahl, tables.next(), false, true, true, meldeindex, zw, false);
            Object[] titel = new Object[5 + diszAnzahl + (zw ? 1 : 0)];

            titel[0] = I18n.get("Name");
            titel[1] = I18n.get("Organisation");
            titel[2] = I18n.get("QualifikationsebeneShort");
            titel[3] = I18n.get("AgeGroupShort");
            titel[4] = I18n.get("Points");
            for (int y = 0; y < diszAnzahl; y++) {
                titel[5 + y] = I18n.get("HeatShort") + " / " + I18n.get("LaneShort");
                // titel[4 + 2 * y] = I18n.get("LaneShort");
            }
            if (zw) {
                titel[titel.length - 1] = wk.getRegelwerk().getZusatzwertungShort();
            }
            models[x] = new SimpleTableModel(data, titel);
        }

        return models;
    }

    public static <T extends ASchwimmer> JTable getLaufeinteilungTabelle(final AWettkampf<T> wk, boolean zw) {
        if (wk == null) {
            return null;
        }

        SimpleTableModel model = buildLaufeinteilungTabelle(wk, zw);

        JTable table = new JGroupableTable(model);
        TableColumnModel cm = table.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
        ColumnGroup disziplinen = new ColumnGroup(I18n.get("Discipline"));
        for (int x = 0; x < (table.getColumnCount() - 5 - (zw ? 1 : 0)); x++) {
            ColumnGroup nummer = new ColumnGroup("" + (x + 1));
            nummer.add(cm.getColumn(5 + x));
            // nummer.add(cm.getColumn(4 + 2 * x));
            disziplinen.add(nummer);
        }
        header.addColumnGroup(disziplinen);

        JTableUtils.setPreferredCellSizes(table);
        JTableUtils.setTableCellRenderer(table, new ConditionalCenterRenderer(3));
        header.setReorderingAllowed(false);

        return table;
    }

    private static <T extends ASchwimmer> SimpleTableModel buildLaufeinteilungTabelle(final AWettkampf<T> wk,
            boolean zw) {
        if (wk == null) {
            return null;
        }

        LinkedList<SchwimmerInfo> lsi = ListUtils.mergeLists(SchwimmerUtils.toInfo(wk));
        Collections.sort(lsi);
        ListIterator<SchwimmerInfo> li = lsi.listIterator();
        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        int anzahl = 0;
        SchwimmerInfo first = null;
        LinkedList<SchwimmerInfo[]> result = new LinkedList<>();
        SchwimmerInfo[] current = new SchwimmerInfo[lsi.size()];
        result.addLast(current);
        while (li.hasNext()) {
            SchwimmerInfo si = li.next();
            if (first == null) {
                first = si;
            }
            current[anzahl] = si;
            anzahl++;
        }
        if (anzahl == 0) {
            return null;
        }

        Collections.sort(result, new Comparator<SchwimmerInfo[]>() {
            @Override
            public int compare(SchwimmerInfo[] o1, SchwimmerInfo[] o2) {
                SchwimmerInfo s1 = o1[0];
                SchwimmerInfo s2 = o2[0];

                if ((s1 == null) || (s2 == null)) {
                    return (s1 == null ? 1 : -1);
                }
                int i = s1.getSchwimmer().getAKNummer() - s2.getSchwimmer().getAKNummer();
                if (i != 0) {
                    return i * 2;
                }
                int g1 = s1.getSchwimmer().isMaennlich() ? 1 : 0;
                int g2 = s2.getSchwimmer().isMaennlich() ? 1 : 0;
                return g1 - g2;
            }
        });

        // Ggf. fast leere Tabellen zusammenfassen
        if ((bahnen > 4) && (result.size() >= 2)) {
            String[] nothing = new String[bahnen];
            for (int x = 0; x < nothing.length; x++) {
                nothing[x] = "";
            }
            SchwimmerInfo empty = new SchwimmerInfo(null, nothing);

            ListIterator<SchwimmerInfo[]> rli = result.listIterator();
            SchwimmerInfo[] previous = rli.next();
            while (rli.hasNext()) {
                SchwimmerInfo[] c = rli.next();
                if (getLength(previous) + getLength(c) > bahnen) {
                    previous = c;
                } else {
                    int posCurrent = 0;
                    int posPrevious = 0;
                    while ((posCurrent < c.length) && (!c[posCurrent].isEmpty())) {
                        while ((posPrevious < previous.length) && (!previous[posPrevious].isEmpty())) {
                            posPrevious++;
                        }
                        if (posPrevious < previous.length) {
                            previous[posPrevious] = c[posCurrent];
                            c[posCurrent] = empty;
                        }
                        posCurrent++;
                    }
                    rli.remove();
                }
            }
        }

        int diszAnzahl = 0;
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            diszAnzahl = Math.max(diszAnzahl, wk.getRegelwerk().getAk(x).getDiszAnzahl());
        }

        int meldeindex = wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0);
        SimpleTableModel[] models = new SimpleTableModel[result.size()];
        ListIterator<SchwimmerInfo[]> tables = result.listIterator();

        for (int x = 0; x < models.length; x++) {
            Object[][] data = laufeinteilungToData(diszAnzahl, tables.next(), false, true, true, meldeindex, zw, false);
            Object[] titel = new Object[5 + diszAnzahl + (zw ? 1 : 0)];

            titel[0] = I18n.get("Name");
            titel[1] = I18n.get("Organisation");
            titel[2] = I18n.get("QualifikationsebeneShort");
            titel[3] = I18n.get("AgeGroupShort");
            titel[4] = I18n.get("Points");
            for (int y = 0; y < diszAnzahl; y++) {
                titel[5 + y] = I18n.get("HeatShort") + " / " + I18n.get("LaneShort");
                // titel[4 + 2 * y] = I18n.get("LaneShort");
            }
            if (zw) {
                titel[titel.length - 1] = wk.getRegelwerk().getZusatzwertungShort();
            }
            models[x] = new SimpleTableModel(data, titel);
        }

        return models[0];
    }

    private static void insert(LinkedList<SchwimmerInfo> list, SchwimmerInfo si, int index) {
        ListIterator<SchwimmerInfo> li = list.listIterator(list.size());
        while (li.hasPrevious()) {
            SchwimmerInfo s = li.previous();
            if (!s.getLauf(index).equals("-")) {
                int difference = si.getHeatDifference(s, index);
                if (difference > 0) {
                    li.next();
                    li.add(si);
                    return;
                }
                if (difference == 0) {
                    int b1 = Integer.parseInt(si.getBahn(index));
                    int b2 = Integer.parseInt(s.getBahn(index));
                    if (b1 > b2) {
                        li.next();
                        li.add(si);
                        return;
                    }
                }
            }
        }
        list.addFirst(si);
    }

    private static <T extends ASchwimmer> ExtendedTableModel[] buildLaufeinteilungTabellenJeAK(final AWettkampf<T> wk,
            boolean zw) {
        if (wk == null) {
            return null;
        }

        LinkedList<SchwimmerInfo>[] infos = SchwimmerUtils.toInfo(wk);

        ExtendedTableModel[] models = new ExtendedTableModel[infos.length];

        int meldeindex = wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0);

        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < infos.length; x++) {
            LinkedList<SchwimmerInfo> lsi = infos[x];

            ASchwimmer t = lsi.getFirst().getSchwimmer();
            int ak = t.getAKNummer();
            int diszAnzahl = aks.getAk(ak).getDiszAnzahl();

            LinkedList<SchwimmerInfo> temp = new LinkedList<>();
            ListIterator<SchwimmerInfo> li = lsi.listIterator();
            while (li.hasNext()) {
                SchwimmerInfo si = li.next();
                if (si.getLauf(0).equals("-")) {
                    temp.addLast(si);
                    li.remove();
                }
            }

            if (!temp.isEmpty()) {
                for (int i = 1; i < diszAnzahl - 1; i++) {
                    li = temp.listIterator(temp.size());
                    while (li.hasPrevious()) {
                        SchwimmerInfo si = li.previous();
                        if (!si.getLauf(i).equals("-")) {
                            insert(lsi, si, i);
                            li.remove();
                        }
                    }
                    if (temp.isEmpty()) {
                        break;
                    }
                }
            }
            if (!temp.isEmpty()) {
                lsi.addAll(temp);
            }
            Collections.sort(lsi,
                    new Meldesorter(wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0)));

            Object[][] data = laufeinteilungToData(diszAnzahl, lsi.toArray(new SchwimmerInfo[lsi.size()]), true, false,
                    true, meldeindex, zw, false);
            Object[] titel = new Object[5 + diszAnzahl + (zw ? 1 : 0)];

            titel[0] = I18n.get("NumberShort");
            titel[1] = I18n.get("Name");
            titel[2] = I18n.get("Organisation");
            titel[3] = I18n.get("QualifikationsebeneShort");
            titel[4] = I18n.get("Points");
            for (int y = 0; y < diszAnzahl; y++) {
                titel[5 + y] = I18n.get("HeatShort") + " / " + I18n.get("LaneShort");
            }
            if (zw) {
                titel[titel.length - 1] = wk.getRegelwerk().getZusatzwertungShort();
            }
            models[x] = new ExtendedTableModel(data, titel);
            models[x].setName(aks.getAk(ak).getName() + " " + I18n.geschlechtToString(t));
        }

        return models;
    }

    @SuppressWarnings("unchecked")
    private static <T extends ASchwimmer> ExtendedTableModel[] buildLaufeinteilungTabellenJeGliederung(
            final AWettkampf<T> wk, boolean zw) {
        if (wk == null) {
            return null;
        }

        LinkedList<SchwimmerInfo>[] infos = SchwimmerUtils.toInfo(wk);

        LinkedList<String> tmpGlds = wk.getGliederungenMitQGliederung();

        String[] gliederungen = tmpGlds.toArray(new String[tmpGlds.size()]);
        {
            LinkedList<SchwimmerInfo>[] temp = new LinkedList[gliederungen.length];
            for (int x = 0; x < gliederungen.length; x++) {
                temp[x] = new LinkedList<>();
                for (LinkedList<SchwimmerInfo> info : infos) {
                    for (SchwimmerInfo si : info) {
                        if (si.getSchwimmer().getGliederungMitQGliederung().equals(gliederungen[x])) {
                            temp[x].addLast(si);
                        }
                    }
                }
            }
            infos = temp;
        }
        LinkedList<ExtendedTableModel> models = new LinkedList<>();

        int meldeindex = wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0);

        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < infos.length; x++) {
            if (!infos[x].isEmpty()) {
                LinkedList<SchwimmerInfo> lsi = infos[x];
                // Collections.sort(lsi);

                int diszAnzahl = aks.getMaxDisciplineCount();

                Object[][] data = laufeinteilungToData(diszAnzahl, lsi.toArray(new SchwimmerInfo[lsi.size()]), false,
                        true, false, meldeindex, zw, true);
                Object[] titel = new Object[4 + diszAnzahl + (zw ? 1 : 0)];

                titel[0] = I18n.get("StartnumberShort");
                titel[1] = I18n.get("Name");
                titel[2] = I18n.get("AgeGroup");
                titel[3] = I18n.get("Points");
                for (int y = 0; y < diszAnzahl; y++) {
                    titel[4 + y] = I18n.get("HeatShort") + " / " + I18n.get("LaneShort");
                    // titel[3 + 2 * y] = I18n.get("LaneShort");
                }
                if (zw) {
                    titel[titel.length - 1] = wk.getRegelwerk().getZusatzwertungShort();
                }
                ExtendedTableModel model = new ExtendedTableModel(data, titel);
                model.setColumnAlignment(0, SwingConstants.RIGHT);
                model.setName(gliederungen[x]);
                models.addLast(model);
            }
        }

        return models.toArray(new ExtendedTableModel[models.size()]);
    }

    private static int getLength(SchwimmerInfo[] previous) {
        int length = 0;
        for (SchwimmerInfo previou : previous) {
            if (!previou.isEmpty()) {
                length++;
            }
        }
        return length;
    }

    public static <T extends ASchwimmer> JTable[] getLaufeinteilungTabellen(final AWettkampf<T> wk, boolean zw) {
        if (wk == null) {
            return null;
        }

        SimpleTableModel[] models = buildLaufeinteilungTabellen(wk, zw);
        JTable[] tables = new JTable[models.length];
        for (int y = 0; y < models.length; y++) {
            JTable table = new JGroupableTable(models[y]);
            TableColumnModel cm = table.getColumnModel();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
            ColumnGroup disziplinen = new ColumnGroup(I18n.get("Discipline"));
            for (int x = 0; x < (table.getColumnCount() - 5 - (zw ? 1 : 0)); x++) {
                ColumnGroup nummer = new ColumnGroup("" + (x + 1));
                nummer.add(cm.getColumn(5 + x));
                // nummer.add(cm.getColumn(4 + 2 * x));
                disziplinen.add(nummer);
            }
            header.addColumnGroup(disziplinen);

            JTableUtils.setPreferredCellSizes(table);
            JTableUtils.setTableCellRenderer(table, new ConditionalCenterRenderer(3));
            header.setReorderingAllowed(false);

            tables[y] = table;
        }
        return tables;
    }

    public static <T extends ASchwimmer> JTable[] getLaufeinteilungTabellenJeAK(final AWettkampf<T> wk, boolean zw) {
        if (wk == null) {
            return null;
        }

        ExtendedTableModel[] models = buildLaufeinteilungTabellenJeAK(wk, zw);
        JTable[] tables = new JTable[models.length];
        for (int y = 0; y < models.length; y++) {
            JTable table = new JGroupableTable(models[y]);
            TableColumnModel cm = table.getColumnModel();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
            ColumnGroup disziplinen = new ColumnGroup(I18n.get("Discipline"));
            for (int x = 0; x < (table.getColumnCount() - 5 - (zw ? 1 : 0)); x++) {
                ColumnGroup nummer = new ColumnGroup("" + (x + 1));
                nummer.add(cm.getColumn(5 + x));
                // nummer.add(cm.getColumn(3 + 2 * x));
                disziplinen.add(nummer);
            }
            header.addColumnGroup(disziplinen);

            JTableUtils.setPreferredCellSizes(table);
            JTableUtils.setTableCellRenderer(table, new ConditionalCenterRenderer(2));
            header.setReorderingAllowed(false);

            tables[y] = table;
        }
        return tables;
    }

    public static <T extends ASchwimmer> JTable[] getLaufeinteilungTabellenJeGliederung(final AWettkampf<T> wk,
            boolean zw) {
        if (wk == null) {
            return null;
        }

        ExtendedTableModel[] models = buildLaufeinteilungTabellenJeGliederung(wk, zw);

        JTable[] tables = new JTable[models.length];
        for (int y = 0; y < models.length; y++) {
            JTable table = new JGroupableTable(models[y]);
            TableColumnModel cm = table.getColumnModel();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
            ColumnGroup disziplinen = new ColumnGroup(I18n.get("Discipline"));
            for (int x = 0; x < (table.getColumnCount() - 4 - (zw ? 1 : 0)); x++) {
                ColumnGroup nummer = new ColumnGroup("" + (x + 1));
                nummer.add(cm.getColumn(4 + x));
                // nummer.add(cm.getColumn(3 + 2 * x));
                disziplinen.add(nummer);
            }
            header.addColumnGroup(disziplinen);

            JTableUtils.setPreferredCellSizes(table);
            JTableUtils.setTableCellRenderer(table, new ConditionalCenterRenderer(2));
            header.setReorderingAllowed(false);

            tables[y] = table;
        }
        return tables;
    }

    static <T extends ASchwimmer> SimpleTableModel getLauflisteTableModel(AWettkampf<T> wk) {
        Laufliste<T> laufliste = wk.getLaufliste();
        if (laufliste == null) {
            return null;
        }

        Object[][] o = lauflisteToData(wk);

        Object[] titel = new Object[wk.getIntegerProperty(HEATS_LANES) + 3];
        titel[0] = I18n.get("NumberShort");
        titel[1] = I18n.get("AgeGroup");
        titel[2] = I18n.get("Discipline");
        for (int x = 0; x < wk.getIntegerProperty(HEATS_LANES); x++) {
            titel[x + 3] = I18n.get("LaneNumber", x + 1);
        }

        return new SimpleTableModel(o, titel) {
            private static final long serialVersionUID = -6113292199287086090L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static <T extends ASchwimmer> SimpleTableModel getLaufzeitenTableModel(AWettkampf<T> wk) {
        Laufliste<T> laufliste = wk.getLaufliste();
        if (laufliste == null) {
            return null;
        }

        Object[][] o = lauflisteToTimes(wk);

        Object[] titel = new Object[wk.getIntegerProperty(HEATS_LANES) + 3];
        titel[0] = I18n.get("NumberShort");
        titel[1] = I18n.get("AgeGroup");
        titel[2] = I18n.get("Discipline");
        for (int x = 0; x < wk.getIntegerProperty(HEATS_LANES); x++) {
            titel[x + 3] = I18n.get("LaneNumber", x + 1);
        }

        return new SimpleTableModel(o, titel) {
            private static final long serialVersionUID = -6113292199287086090L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static <T extends ASchwimmer> JTable getLaufliste(AWettkampf<T> wk, boolean emptylanes) {
        SimpleTableModel dtm = getLauflisteTableModel(wk);
        if (!emptylanes) {
            int x = 3;
            for (int z = 0; z < wk.getIntegerProperty(PropertyConstants.HEATS_LANES); z++) {
                if (!wk.getLaufliste().isLaneUsed(z)) {
                    dtm.removeColumn(x);
                } else {
                    x++;
                }
            }
        }
        return EDTUtils.executeOnEDTwithReturn(() -> getLaufliste(dtm));
    }

    public static <T extends ASchwimmer> JTable getLaufzeiten(AWettkampf<T> wk, boolean emptylanes) {
        SimpleTableModel dtm = getLaufzeitenTableModel(wk);
        if (!emptylanes) {
            int x = 3;
            for (int z = 0; z < wk.getIntegerProperty(PropertyConstants.HEATS_LANES); z++) {
                if (!wk.getLaufliste().isLaneUsed(z)) {
                    dtm.removeColumn(x);
                } else {
                    x++;
                }
            }
        }
        return EDTUtils.executeOnEDTwithReturn(() -> getLaufliste(dtm));
    }

    private static JTable getLaufliste(SimpleTableModel dtm) {
        JTable table = new JTable(dtm);

        JTableUtils.setTableCellRenderer(table, new AlignmentCellRenderer(
                new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT }, SwingConstants.CENTER));
        JTableUtils.setAlternatingTableCellRenderer(table);
        JTableUtils.setPreferredCellWidths(table);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    /**
     * @param <T>
     * @param laufliste
     * @return
     */
    private static <T extends ASchwimmer> Object[][] lauflisteToData(AWettkampf<T> wk) {
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        Laufliste<T> laufliste = wk.getLaufliste();
        int heats = laufliste.getLaufliste().size();
        int lanes = wk.getIntegerProperty(HEATS_LANES);
        Object[][] o = new Object[heats][lanes + 3];
        for (int y = 0; y < laufliste.getLaufliste().size(); y++) {
            Lauf<T> lauf = laufliste.getLaufliste().get(y);
            if (lauf != null) {
                o[y][0] = lauf.getName(scheme) + "  ";
                o[y][1] = " ";
                o[y][2] = " ";
                if (lauf.getSchwimmer() != null) {
                    o[y][1] = lauf.getStartgruppe();
                    o[y][2] = lauf.getDisziplinShort();
                }
                for (int x = 0; x < lanes; x++) {
                    if (lauf.getSchwimmer(x) != null) {
                        o[y][x + 3] = new SchwimmerDisziplin<T>(x, lauf, true);
                    } else {
                        o[y][x + 3] = " ";
                    }
                }
            } else {
                for (int x = 0; x < wk.getIntegerProperty(HEATS_LANES); x++) {
                    o[y][x + 3] = " ";
                }
            }
        }
        return o;
    }

    /**
     * @param <T>
     * @param laufliste
     * @return
     */
    private static <T extends ASchwimmer> Object[][] lauflisteToTimes(AWettkampf<T> wk) {
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        Laufliste<T> laufliste = wk.getLaufliste();
        int heats = laufliste.getLaufliste().size();
        int lanes = wk.getIntegerProperty(HEATS_LANES);
        Object[][] o = new Object[heats][lanes + 3];
        for (int y = 0; y < laufliste.getLaufliste().size(); y++) {
            Lauf<T> temp = laufliste.getLaufliste().get(y);
            if (temp != null) {
                o[y][0] = temp.getName(scheme);
                o[y][1] = " ";
                o[y][2] = " ";
                if (temp.getSchwimmer() != null) {
                    o[y][1] = temp.getAltersklasse(true);
                    o[y][2] = temp.getDisziplinShort();
                }
                for (int x = 0; x < lanes; x++) {
                    if (temp.getSchwimmer(x) != null) {
                        T t = temp.getSchwimmer(x);
                        int disz = temp.getDisznummer(x);
                        int zeit = t.getZeit(disz);
                        Object entry = StringTools.zeitString(zeit);
                        Strafe s = t.getAkkumulierteStrafe(disz);
                        if ((!s.equals(Strafe.NICHTS)) && !((s.getArt() == Strafarten.STRAFPUNKTE)
                                && (s.getShortname().length() == 0) && (s.getStrafpunkte() == 0))) {
                            String[] tmp = new String[2];
                            tmp[0] = entry.toString();
                            tmp[1] = I18n.getPenaltyShort(s);
                            if (tmp[1].length() > 0) {
                                entry = tmp;
                            }
                        }
                        o[y][x + 3] = entry;
                    } else {
                        o[y][x + 3] = " ";
                    }
                }
            } else {
                for (int x = 0; x < wk.getIntegerProperty(HEATS_LANES); x++) {
                    o[y][x + 3] = " ";
                }
            }
        }
        return o;
    }

    private static <T extends ASchwimmer> Object[][] laufeinteilungToDataKompakt(int diszAnzahl,
            SchwimmerInfo[] schwimmer) {
        Object[][] data = new Object[schwimmer.length][3 + diszAnzahl];
        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[y].length; x++) {
                data[y][x] = " ";
            }

            SchwimmerInfo si = schwimmer[y];
            ASchwimmer t = si.getSchwimmer();
            if (t != null) {
                boolean isMultiline = t.getWettkampf().isMultiline();

                String name = "";
                Mannschaft m = isMultiline && t instanceof Mannschaft ? (Mannschaft) t : null;
                if (m != null) {
                    if (t.getName().equals(t.getGliederung())) {
                        name = StringTools.shorten(m.getMitgliedernamenShort(", "), 30, "...");
                    } else {
                        name = I18n.get("TeamnameMultiline", t.getName(), m.getMitgliedernamenShort(", "));
                    }
                } else {
                    name = t.getName();
                }

                data[y][0] = name + (t.isAusserKonkurrenz() ? " (AK)" : "");
                data[y][1] = t.getGliederung();
                data[y][2] = t.getAK().toString() + " " + I18n.geschlechtToShortString(t);

                for (int x = 0; x < diszAnzahl; x++) {
                    if (x < si.getLaufCount()) {
                        data[y][3 + x] = si.getBahn(x);
                    } else {
                        data[y][3 + x] = "-";
                    }
                }
            }
        }
        return data;
    }

    private static <T extends ASchwimmer> ExtendedModel[] buildLaufeinteilungKompakt(final AWettkampf<T> wk) {
        if (wk == null) {
            return null;
        }

        LinkedList<SchwimmerInfo> lsi = ListUtils.mergeLists(SchwimmerUtils.toInfo(wk));
        Collections.sort(lsi);
        ListIterator<SchwimmerInfo> li = lsi.listIterator();
        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        int anzahl = 0;
        SchwimmerInfo first = null;
        LinkedList<SchwimmerInfo[]> result = new LinkedList<>();
        SchwimmerInfo[] current = new SchwimmerInfo[bahnen];
        result.addLast(current);
        while (li.hasNext()) {
            SchwimmerInfo si = li.next();
            if (first == null) {
                first = si;
            }
            if (first.isSimilar(si)) {
                current[anzahl] = si;
                anzahl++;
                if (anzahl >= bahnen) {
                    current = new SchwimmerInfo[bahnen];
                    result.addLast(current);
                    anzahl = 0;
                    first = null;
                }
            } else {
                while (anzahl < bahnen) {
                    current[anzahl] = new SchwimmerInfo(null, new String[0]);
                    anzahl++;
                }
                current = new SchwimmerInfo[bahnen];
                result.addLast(current);
                current[0] = si;
                anzahl = 1;
                first = si;
            }
        }
        if (anzahl > 0) {
            while (anzahl < bahnen) {
                current[anzahl] = new SchwimmerInfo(null, new String[0]);
                anzahl++;
            }
        } else {
            result.removeLast();
        }

        int diszAnzahl = 0;
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            diszAnzahl = Math.max(diszAnzahl, wk.getRegelwerk().getAk(x).getDiszAnzahl());
        }

        ExtendedModel[] models = new ExtendedModel[result.size()];
        ListIterator<SchwimmerInfo[]> tables = result.listIterator();
        for (int x = 0; x < models.length; x++) {
            models[x] = new ExtendedModel();
            models[x].heats = new String[diszAnzahl];

            SchwimmerInfo[] table = tables.next();

            Object[][] data = laufeinteilungToDataKompakt(diszAnzahl, table);
            Object[] titel = new Object[3 + diszAnzahl];

            titel[0] = I18n.get("Name");
            titel[1] = I18n.get("Organisation");
            titel[2] = I18n.get("AgeGroup");
            for (int y = 0; y < diszAnzahl; y++) {
                titel[3 + y] = I18n.get("Lane");
                models[x].heats[y] = "-";
                for (SchwimmerInfo aTable : table) {
                    try {
                        String s = aTable.getLauf(y);
                        if ((s != null) && (!s.equals("-"))) {
                            models[x].heats[y] = s;
                        }
                        // break;
                    } catch (IndexOutOfBoundsException ioobe) {
                        ioobe.printStackTrace();
                        // Nothing to do
                    }
                }
            }
            models[x].model = new SimpleTableModel(data, titel);
        }

        return models;
    }

    public static <T extends ASchwimmer> JTable[] getLaufeinteilungKompaktTabellen(final AWettkampf<T> wk) {
        if (wk == null) {
            return null;
        }

        ExtendedModel[] models = buildLaufeinteilungKompakt(wk);
        JTable[] tables = new JTable[models.length];
        for (int y = 0; y < models.length; y++) {
            JTable table = new JGroupableTable(models[y].model);
            TableColumnModel cm = table.getColumnModel();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
            for (int x = 0; x < models[y].heats.length; x++) {
                int value = 1;
                if (models[y].heats[x].equals("-")) {
                    value = 0;
                }
                ColumnGroup heat = new ColumnGroup(I18n.get("HeatNr", models[y].heats[x], value));
                heat.add(cm.getColumn(3 + x));

                ColumnGroup nummer = new ColumnGroup("" + (x + 1));
                nummer.add(heat);

                header.addColumnGroup(nummer);
            }

            JTableUtils.setPreferredCellSizes(table);
            JTableUtils.setTableCellRenderer(table, new ConditionalCenterRenderer(3));
            header.setReorderingAllowed(false);

            tables[y] = table;
        }
        return tables;
    }

    public static <T extends ASchwimmer> boolean checkLaufeinteilungKompaktTabellen(final AWettkampf<T> wk) {
        if (wk == null) {
            return false;
        }
        if (!wk.hasSchwimmer()) {
            return false;
        }
        if ((wk.getLaufliste() == null) || (wk.getLaufliste().getLaufliste() == null)) {
            return false;
        }

        LinkedList<SchwimmerInfo> lsi = ListUtils.mergeLists(SchwimmerUtils.toInfo(wk));
        Collections.sort(lsi);
        ListIterator<SchwimmerInfo> li = lsi.listIterator();
        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        int anzahl = 0;
        SchwimmerInfo first = null;
        LinkedList<SchwimmerInfo[]> result = new LinkedList<>();
        SchwimmerInfo[] current = new SchwimmerInfo[bahnen];
        result.addLast(current);
        while (li.hasNext()) {
            SchwimmerInfo si = li.next();
            if (first == null) {
                first = si;
            }
            if (first.isSimilar(si)) {
                current[anzahl] = si;
                anzahl++;
                if (anzahl >= bahnen) {
                    current = new SchwimmerInfo[bahnen];
                    result.addLast(current);
                    anzahl = 0;
                    first = null;
                }
            } else {
                while (anzahl < bahnen) {
                    current[anzahl] = new SchwimmerInfo(null, new String[0]);
                    anzahl++;
                }
                current = new SchwimmerInfo[bahnen];
                result.addLast(current);
                current[0] = si;
                anzahl = 1;
                first = si;
            }
        }

        int diszAnzahl = 0;
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            diszAnzahl = Math.max(diszAnzahl, wk.getRegelwerk().getAk(x).getDiszAnzahl());
        }

        ListIterator<SchwimmerInfo[]> tables = result.listIterator();
        while (tables.hasNext()) {
            SchwimmerInfo[] table = tables.next();
            int count = 0;
            for (SchwimmerInfo aTable : table) {
                if (aTable != null) {
                    if (count < aTable.getLaufCount()) {
                        count = aTable.getLaufCount();
                    }
                }
            }
            for (int z = 0; z < count; z++) {
                String[] names = new String[table.length];
                for (int y = 0; y < table.length; y++) {
                    try {
                        names[y] = table[y].getLauf(z);
                        if ((names[y] == null) || (names[y].equals("-"))) {
                            names[y] = null;
                        }
                    } catch (IndexOutOfBoundsException | NullPointerException ioobe) {
                        names[y] = null;
                    }
                }
                for (int y = 0; y < names.length - 1; y++) {
                    if (names[y] != null) {
                        if (names[y + 1] == null) {
                            names[y + 1] = names[y];
                        }
                        if (!names[y].equals(names[y + 1])) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
