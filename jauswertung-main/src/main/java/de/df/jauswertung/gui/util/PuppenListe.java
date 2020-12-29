/*
 * BahnenListe.java Created on 2. November 2001, 22:18
 */

package de.df.jauswertung.gui.util;

import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;

public class PuppenListe<T extends ASchwimmer> {

    private HLWListe<T>    daten = null;
    private AWettkampf<T>  wk    = null;
    private final String[] titel = { I18n.get("Time"), I18n.get("Name"), I18n.get("Organisation"), I18n.get("AgeGroup"),
            "        " + I18n.get("Points") + "        " };

    /** Creates new BahnenListe */
    public PuppenListe(AWettkampf<T> wettkampf) {
        wk = wettkampf;
        daten = wk.getHLWListe();
    }

    private String[][] toPuppenListe(int puppe) {
        if (daten == null) {
            return null;
        }
        if (puppe < 0) {
            return null;
        }
        if (puppe > wk.getIntegerProperty(ZW_LANES)) {
            return null;
        }

        LinkedList<String[]> ergebnis = new LinkedList<String[]>();

        String[] pause = new String[5];
        for (int x = 0; x < 5; x++) {
            pause[x] = "";
        }
        pause[1] = I18n.get("Pause");

        if (!daten.isEmpty()) {
            ListIterator<LinkedList<HLWLauf<T>>> lli = daten.getIterator();
            while (lli.hasNext()) {
                LinkedList<HLWLauf<T>> laufliste = lli.next();
                if (laufliste == null) {
                    return null;
                }
                if (laufliste.size() == 0) {
                    return null;
                }

                int heats = laufliste.size();

                for (int x = 0; x < heats; x++) {
                    String[] o = new String[5];
                    for (int y = 0; y < o.length; y++) {
                        o[y] = "";
                    }
                    HLWLauf<T> hlw = laufliste.get(x);
                    o[0] = hlw.getName();
                    T s = hlw.getSchwimmer(puppe - 1);
                    if (s != null) {
                        o[1] = s.getName();
                        o[2] = s.getGliederung();
                        o[3] = s.getAK().toString() + " " + I18n.geschlechtToShortString(s);
                    }
                    ergebnis.addLast(o);
                }

                ergebnis.addLast(pause);
            }
            ergebnis.removeLast();
        }
        return ergebnis.toArray(new String[ergebnis.size()][0]);
    }

    public JTable toJTable(int puppe) {
        JTable table = new JTable(getTableModel(puppe));
        JTableUtils.setTableCellRenderer(table, new AlignmentCellRenderer(new int[] { SwingConstants.RIGHT }, SwingConstants.LEFT));
        if (PrintUtils.printOmitOrganisationForTeams && (((AWettkampf) wk) instanceof MannschaftWettkampf)) {
            JTableUtils.hideColumnAndRemoveData(table, 2);
        }
        return table;
    }

    private DefaultTableModel getTableModel(int puppe) {
        String[][] ergebnis = toPuppenListe(puppe);
        if (ergebnis == null) {
            ergebnis = new String[0][0];
        }

        return new SimpleTableModel(ergebnis, titel);
    }
}