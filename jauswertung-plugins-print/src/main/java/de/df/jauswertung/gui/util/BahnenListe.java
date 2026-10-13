/*
 * BahnenListe.java Created on 2. November 2001, 22:18
 */

package de.df.jauswertung.gui.util;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

import java.util.LinkedList;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.util.StringTools;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BahnenListe<T extends ASchwimmer> {

    private final Laufliste<T> daten;
    private final AWettkampf<T> wk;

    private final String[] titel = {I18n.get("Heat"), I18n.get("Name"), I18n.get("Organisation"),
            I18n.get("Discipline"), I18n.get("AgeGroup"),
            "        " + I18n.get("Time") + "        "};

    /**
     * Creates new BahnenListe
     */

    public BahnenListe(AWettkampf<T> wettkampf) {
        wk = wettkampf;
        daten = wk.getLaufliste();
    }

    private Object[][] toBahnenListe(int puppe) {
        if (daten == null) {
            return null;
        }
        if (puppe < 0) {
            return null;
        }
        if (puppe > wk.getIntegerProperty(HEATS_LANES)) {
            return null;
        }

        LinkedList<Lauf<T>> laufliste = daten.getLaufliste();
        if (laufliste == null) {
            return null;
        }
        if (laufliste.isEmpty()) {
            return null;
        }

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();
        boolean isMultiline = wk.isMultiline();

        Object[][] ergebnis = new Object[laufliste.size()][6];
        for (int x = 0; x < ergebnis.length; x++) {
            for (int y = 0; y < 6; y++) {
                ergebnis[x][y] = "";
            }
            try {
                Lauf<T> lauf = laufliste.get(x);
                ergebnis[x][0] = lauf.getName(scheme);
                T s = lauf.getSchwimmer(puppe - 1);
                if (s != null) {
                    int disz = lauf.getDisznummer(puppe - 1);
                    Mannschaft m = isMultiline && s instanceof Mannschaft ? (Mannschaft) s : null;
                    String name = s.getName();
                    if (m != null && name.equals(s.getGliederung())) {
                        String starter = m.getStarterShort(disz, ", ");
                        if (!starter.isEmpty()) {
                            name = StringTools.shorten(starter, 30, "...");
                        }
                    }

                    ergebnis[x][1] = name;
                    ergebnis[x][2] = s.getGliederung();
                    Altersklasse ak = s.getAK();
                    ergebnis[x][3] = I18n.getDisziplinShort(ak.getDisziplin(disz, s.isMaennlich()).toString());
                    ergebnis[x][4] = ak + " " + I18n.geschlechtToShortString(s);
                    if (s.startetNicht(disz)) {
                        for (int y = 1; y <= 4; y++) {
                            ergebnis[x][y] = new DurchgestrichenerText(ergebnis[x][y]);
                        }
                    }
                }
            } catch (IndexOutOfBoundsException ioobe) {
                log.warn("Index problem", ioobe);
            }
        }
        return ergebnis;
    }

    private DefaultTableModel getTableModel(int bahn) {
        Object[][] ergebnis = toBahnenListe(bahn);
        if (ergebnis == null) {
            ergebnis = new Object[0][0];
        }
        return new SimpleTableModel(ergebnis, titel);

    }

    public JTable toJTable(int bahn) {
        JTable table = new JTable(getTableModel(bahn));
        JTableUtils.setTableCellRenderer(table,
                                         new AlignmentCellRenderer<>(new int[]{SwingConstants.CENTER}, SwingConstants.LEFT));

        if (PrintUtils.printOmitOrganisationForTeams && (wk instanceof MannschaftWettkampf)) {
            JTableUtils.hideColumnAndRemoveData(table, 2);
        }
        Durchstreichen.tabelle(table);

        return table;
    }
}
