/*
 * Created on 20.01.2006
 */
package de.df.jauswertung.gui.util;

import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.util.EDTUtils;

public final class TableZWUtils {

    private TableZWUtils() {
        // Hide
    }

    public static <T extends ASchwimmer> JTable getZWUebersicht(AWettkampf<T> wk) {
        TableModel tm = getHLWUebersichtTableModel(wk);
        return EDTUtils.executeOnEDTwithReturn(() -> getZWUebersichtI(tm));
    }

    private static JTable getZWUebersichtI(TableModel tm) {
        JTable table = new JTable(tm);
        JTableUtils.setTableCellRenderer(table, new ZWTableCellRenderer());
        JTableUtils.setPreferredCellSizes(table);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private static <T extends ASchwimmer> TableModel getHLWUebersichtTableModel(AWettkampf<T> wk) {
        HLWListe<T> laufliste = wk.getHLWListe();
        if (laufliste == null) {
            return null;
        }
        if (laufliste.isEmpty()) {
            return null;
        }

        int phantoms = wk.getIntegerProperty(ZW_LANES);

        Object[] pause = new Object[phantoms + 2];
        for (int x = 0; x < pause.length; x++) {
            pause[x] = "";
        }
        pause[1] = I18n.get("Pause");

        LinkedList<Object[]> daten = new LinkedList<Object[]>();

        ListIterator<LinkedList<HLWLauf<T>>> lli = laufliste.getIterator();
        while (lli.hasNext()) {
            ListIterator<HLWLauf<T>> li = lli.next().listIterator();
            while (li.hasNext()) {
                HLWLauf<T> temp = li.next();
                Object[] o = new Object[phantoms + 2];
                if (temp != null) {
                    o[0] = temp.getName();
                    o[1] = "";
                    if (temp.getSchwimmer() != null) {
                        o[1] = temp.getAltersklasse(true);
                    }
                    for (int x = 0; x < phantoms; x++) {
                        if (temp.getSchwimmer(x) != null) {
                            o[x + 2] = temp.getSchwimmer(x);
                        } else {
                            o[x + 2] = "";
                        }
                    }
                } else {
                    for (int x = 0; x < phantoms; x++) {
                        o[x + 2] = "";
                    }
                }
                daten.addLast(o);
            }
            daten.addLast(pause);
        }

        daten.removeLast();

        Object[] titel = new Object[wk.getIntegerProperty(ZW_LANES) + 2];
        titel[0] = I18n.get("TimeOfDay");
        titel[1] = I18n.get("AgeGroup");
        for (int x = 0; x < wk.getIntegerProperty(ZW_LANES); x++) {
            titel[x + 2] = I18n.get("LaneNumber", x + 1);
        }

        return new SimpleTableModel(daten.toArray(new Object[daten.size()][0]), titel);
    }

    public static <T extends ASchwimmer> Object[] getHeatNames(AWettkampf<T> wk, boolean hlw) {
        if (hlw) {
            ListIterator<LinkedList<HLWLauf<T>>> lli = wk.getHLWListe().getIterator();

            LinkedList<Object> result = new LinkedList<Object>();
            while (lli.hasNext()) {
                LinkedList<HLWLauf<T>> ll = lli.next();
                ListIterator<HLWLauf<T>> li = ll.listIterator();
                while (li.hasNext()) {
                    result.addLast(new UniqueString(li.next().getName()));
                }
            }
            return result.toArray(new Object[result.size()]);
        }

        ListIterator<Lauf<T>> li = wk.getLaufliste().getLaufliste().listIterator();
        LinkedList<Object> result = new LinkedList<Object>();
        while (li.hasNext()) {
            result.addLast(new UniqueString(li.next().getName()));
        }
        return result.toArray(new Object[result.size()]);
    }

    /**
     * UniqueString is a class to support JComboBoxes with equals entries.
     */
    private static final class UniqueString {

        private final String text;

        public UniqueString(String s) {
            text = s;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}