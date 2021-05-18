package de.df.jauswertung.print.util;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.GesamtwertungSchwimmer;
import de.df.jauswertung.util.GesamtwertungWettkampf;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleCell;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleRow;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 02.01.2005
 */
public final class TableUtils {

    private TableUtils() {
        // Hide constructor
    }

    @SuppressWarnings("rawtypes")
    public static JTable getGesamtwertungsergebnis(AWettkampf wettkampf) {
        TableModel tm = buildGesamtwertungsergebnis(wettkampf);
        if (tm == null) {
            return null;
        }
        JGroupableTable t = new JGroupableTable(tm);
        if (tm instanceof ExtendedTableModel) {
            ExtendedTableModel etm = (ExtendedTableModel) tm;
            GroupableTableHeader gth = (GroupableTableHeader) t.getTableHeader();
            for (int x = 0; x < (etm.getColumnCount() - 3) / 2; x++) {
                int ak = Integer.parseInt(etm.getExtendedTitleRows()[0].cells[3 + x].title);
                ColumnGroup cg = new ColumnGroup(wettkampf.getRegelwerk().getAk(ak).getName());
                cg.add(t.getColumnModel().getColumn(3 + 2 * x));
                cg.add(t.getColumnModel().getColumn(3 + 2 * x + 1));
                gth.addColumnGroup(cg);
            }
        }
        JTableUtils.setTableCellRenderer(t, new AlignmentCellRenderer(new int[] { SwingConstants.RIGHT, SwingConstants.LEFT }, SwingConstants.RIGHT));
        t.getTableHeader().setReorderingAllowed(false);

        return t;
    }

    @SuppressWarnings("rawtypes")
    public static TableModel buildGesamtwertungsergebnis(AWettkampf wettkampf) {
        return buildGesamtwertungsergebnis(wettkampf, true, null);
    }

    @SuppressWarnings("rawtypes")
    public static TableModel buildGesamtwertungsergebnis(AWettkampf wettkampf, boolean nachkomma, String[] sexes) {
        if (!wettkampf.getRegelwerk().hasGesamtwertung()) {
            return null;
        }
        GesamtwertungWettkampf gesamt = new GesamtwertungWettkampf(wettkampf);
        Altersklasse ak = gesamt.getRegelwerk().getAk(0);
        GesamtwertungSchwimmer[] schwimmer = gesamt.getResult();
        if ((schwimmer != null) && (schwimmer.length > 0)) {
            return TableUtils.buildGesamtwertungsergebnis(schwimmer, gesamt.getRegelwerk(), ak, nachkomma, sexes);
        }
        return null;
    }

    private static Object[] createGesamtwertungTitles(Regelwerk rw, int min, int max, String[] sexes) {
        int aks = max - min;
        Object[] titles = new Object[3 + 2 * aks];
        titles[0] = "Platz";
        titles[1] = "Gliederung";
        titles[2] = "Punkte";
        if (sexes == null) {
            for (int x = 0; x < aks; x++) {

                titles[3 + 2 * x] = I18n.geschlechtToShortString(rw, false);
                titles[4 + 2 * x] = I18n.geschlechtToShortString(rw, true);
            }
        } else {
            if (sexes.length < 2 * max) {
                throw new RuntimeException("Sexes (" + sexes.length + ") must be at least double the size of max (" + max + ").");
            }
            for (int x = 0; x < aks * 2; x++) {
                titles[3 + x] = sexes[x + 2 * min];
            }
        }
        return titles;
    }

    /**
     * @param <T>
     * @param teilies
     * @param ak
     * @return
     */
    private static TableModel buildGesamtwertungsergebnis(final GesamtwertungSchwimmer[] teilies, final Regelwerk aks, final Altersklasse ak, boolean nachkomma,
            String sexes[]) {
        if (ak == null) {
            return null;
        }
        if (teilies == null) {
            return new SimpleTableModel(new Object[0][0], createGesamtwertungTitles(aks, 0, 0, sexes));
        }

        boolean[] used = new boolean[teilies[0].getAmount() / 2];
        for (int x = 0; x < used.length; x++) {
            for (GesamtwertungSchwimmer teily : teilies) {
                if (teily.getPunkte(x, false) + teily.getPunkte(x, true) > 0.005) {
                    used[x] = true;
                }
            }
        }
        int max = 0;
        for (int x = 0; x < used.length; x++) {
            if (used[x]) {
                max = x;
            }
        }
        max++;
        int min = used.length;
        for (int x = used.length - 1; x >= 0; x--) {
            if (used[x]) {
                min = x;
            }
        }

        if (min >= max) {
            min = 0;
            max = used.length;
        }

        double punkte = teilies[0].getPunkte() - 1;

        Object[][] o = new Object[teilies.length][3 + (max - min) * 2];

        for (int y = 0; y < teilies.length; y++) {
            GesamtwertungSchwimmer t = teilies[y];
            if ((Math.abs(punkte - t.getPunkte()) < 0.005) || (t.getPunkte() < 0.005)) {
                o[y][0] = "";
            } else {
                o[y][0] = "" + (y + 1) + "  ";
            }
            punkte = teilies[y].getPunkte();

            if (t.isAusserKonkurrenz()) {
                o[y][0] = "AK";
            }
            o[y][1] = t.getGliederung();
            o[y][2] = StringTools.punkteString(t.getPunkte(), nachkomma) + "  ";
            for (int z = 0; z < (max - min) * 2; z++) {
                double px = t.getPunkte(min + z / 2, (z % 2) != 0);
                if (px < 0.005) {
                    o[y][3 + z] = " ";
                } else {
                    o[y][3 + z] = StringTools.punkteString(px, nachkomma) + "  ";
                }
            }
        }

        TitleCell[] row1 = new TitleCell[3 + max - min];
        TitleCell[] row2 = new TitleCell[(max - min) * 2];

        row1[0] = new TitleCell("Platz", 1, 2);
        row1[1] = new TitleCell("Gliederung", 1, 2);
        row1[2] = new TitleCell("Punkte", 1, 2);
        for (int x = 0; x < max - min; x++) {
            row1[3 + x] = new TitleCell("" + (min + x), 2, 1);

            row2[x * 2] = new TitleCell(I18n.geschlechtToShortString(aks, false), 1, 1);
            row2[x * 2 + 1] = new TitleCell(I18n.geschlechtToShortString(aks, true), 1, 1);
        }

        TitleRow[] rows = new TitleRow[2];
        rows[0] = new TitleRow(row1);
        rows[1] = new TitleRow(row2);

        ExtendedTableModel etm = new ExtendedTableModel(o, createGesamtwertungTitles(aks, min, max, sexes));
        etm.setExtendedTitles(rows);

        return etm;
    }
}