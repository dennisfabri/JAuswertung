/**
 * 
 */
package de.df.jauswertung.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.mxgraph.view.mxGraph;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.util.GraphUtils;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.print.ComponentPagePrintable;

public class UrkundenPrintable<T extends ASchwimmer> implements Printable {

    private static final String[]       IDS = new String[] { "<name>", "<gliederung>", "<altersklasse>", "<geschlecht>", "<platz>", "<punkte>", "<wertung>",
            "<mitglieder>", "<mitglieder2>", "<zeiten1>", "<zeiten2>", "<h>", "<hh>", "<m>", "<mm>", "<s>", "<ss>", "<s,0>", "<ss,0>", "<s,00>", "<ss,00>" };

    private Hashtable<String, Object>[] cells;

    private String[][]                  eintraege;

    @SuppressWarnings({ "unchecked" })
    public UrkundenPrintable(Hashtable<String, Object>[] cells) {
        if (cells == null) {
            cells = new Hashtable[0];
        }
        this.cells = cells;

        eintraege = new String[][] { { "Musterstadt 1", "Ortsgruppe Musterstadt", "AK 13/14", "männlich", "3", "1234,56", "",
                "Max Mustermann, Holger Mustermann, Daniel Mustermann, Hugo Mustermann", "", "1:23,45, 2:34,56", "", "0", "00", "1", "01", "23", "23", "23,5",
                "23,5", "23,45", "23,45" } };
        eintraege[0][8] = eintraege[0][7].replace(", ", "\n");
        eintraege[0][10] = eintraege[0][9].replace(", ", "\n");
    }

    @SuppressWarnings({ "unchecked" })
    public UrkundenPrintable(AWettkampf<T> wk, LinkedList<T> swimmers, int ak, boolean sex, boolean einzelwertung) {
        cells = (Hashtable<String, Object>[]) wk.getProperty((einzelwertung ? PropertyConstants.URKUNDE_EINZELWERTUNG : PropertyConstants.URKUNDE));

        NumberFormat df = NumberFormat.getNumberInstance();
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        JResultTable result = JResultTable.getResultTable(wk, wk.getRegelwerk().getAk(ak), sex, false, true, 0);

        LinkedList<String[]> temp = new LinkedList<String[]>();

        for (int x = 0; x < result.getRowCount(); x++) {
            SchwimmerResult<T> sr = result.getResult(x);

            if (sr.getPoints() < 0.005) {
                break;
            }
            if (sr.hasKeineWertung()) {
                break;
            }
            if (sr.getSchwimmer().isAusserKonkurrenz()) {
                break;
            }

            String[] eintrag = new String[21];

            eintrag[0] = sr.getSchwimmer().getName();
            if (sr.getSchwimmer() instanceof Teilnehmer) {
                Teilnehmer t = (Teilnehmer) sr.getSchwimmer();
                eintrag[0] = I18n.get("NameInformal", t.getVorname(), t.getNachname());
            }
            eintrag[1] = sr.getSchwimmer().getGliederung();
            eintrag[2] = sr.getSchwimmer().getAK().getName();
            eintrag[3] = I18n.geschlechtToString(sr.getSchwimmer());
            eintrag[4] = "" + sr.getPlace();
            eintrag[5] = df.format(sr.getPoints());
            if (einzelwertung) {
                eintrag[6] = sr.getSchwimmer().getAK().getDisziplin(0, sr.getSchwimmer().isMaennlich()).getName();
            } else {
                eintrag[6] = "Mehrkampf";
            }
            if (sr.getSchwimmer() instanceof Mannschaft) {
                Mannschaft m = (Mannschaft) sr.getSchwimmer();
                eintrag[7] = m.getMitgliedernamen(", ");
                eintrag[8] = m.getMitgliedernamen("\n");
            } else {
                eintrag[7] = "";
                eintrag[8] = "";
            }

            String zeiten = sr.getSchwimmer().getZeiten();
            eintrag[9] = zeiten.replace(";", ", ");
            eintrag[10] = zeiten.replace(";", "\n");

            if ((sr.getSchwimmer().getAK().getDiszAnzahl() == 1) && (sr.getSchwimmer().getAkkumulierteStrafe(0).getArt() != Strafarten.NICHT_ANGETRETEN)) {
                int zeit = sr.getSchwimmer().getZeit(0);
                int h = zeit / 100 / 60 / 60;
                int m = (zeit / 100 / 60) % 60;
                int s = (zeit / 100) % 60;
                int zt = (zeit / 10) % 10;
                int ht = zeit % 100;
                String ztx = "," + zt;
                String htx = "," + (ht < 10 ? "0" : "") + ht;
                eintrag[11] = "" + h;
                eintrag[12] = (h < 10 ? "0" : "") + eintrag[11];
                eintrag[13] = "" + m;
                eintrag[14] = (m < 10 ? "0" : "") + eintrag[13];
                eintrag[15] = "" + s;
                eintrag[16] = (s < 10 ? "0" : "") + eintrag[15];
                eintrag[17] = "" + s + ztx;
                eintrag[18] = (s < 10 ? "0" : "") + eintrag[17];
                eintrag[19] = "" + s + htx;
                eintrag[20] = (s < 10 ? "0" : "") + eintrag[19];
            } else {
                for (int y = 11; y < 21; y++) {
                    eintrag[y] = "";
                }
            }

            temp.addLast(eintrag);
        }

        eintraege = temp.toArray(new String[temp.size()][0]);
    }

    private mxGraph lastgraph = null;
    private int     lastpage  = -1;

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex >= eintraege.length) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setClip((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY(), (int) pageFormat.getImageableWidth(),
                (int) pageFormat.getImageableHeight());

        Paper p = new Paper();
        p.setSize(pageFormat.getWidth(), pageFormat.getHeight());
        p.setImageableArea(0, 0, p.getWidth(), p.getHeight());
        PageFormat pf = new PageFormat();
        // pf.setOrientation(pageFormat.getOrientation());
        pf.setPaper(p);

        if (lastgraph == null) {
            lastgraph = GraphUtils.createGraph(true);
            lastpage = pageIndex - 1;
        }
        if (lastpage != pageIndex) {
            // GraphUtils.clear(lastgraph);
            lastgraph = GraphUtils.createGraph(true);
            GraphUtils.populateGraph(lastgraph, cells, false, IDS, eintraege[pageIndex], true);
            // lastgraph.setSelectionEnabled(false);
            try {
                lastgraph.setSelectionCell(new Object[0]);
            } catch (NullPointerException npe) {
                // Nothing to do
            }

            lastpage = pageIndex;
        }
        mxGraph graph = lastgraph;
        JComponent display = GraphUtils.createDisplay(graph, true);

        JFrame f = new JFrame();
        f.add(display);
        f.pack();
        EDTUtils.sleep();
        f.removeAll();
        EDTUtils.sleep();

        ComponentPagePrintable.printComponent(display, g2d, pf);

        return PAGE_EXISTS;
    }
}