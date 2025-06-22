/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jutils.gui.JMiddleline;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.AComponentMultiOnPagePrintable;

public final class ZieleinlaufkartenPrintable<T extends ASchwimmer> extends AComponentMultiOnPagePrintable {

    private Zieleinlaufkarte[] karten = null;

    public ZieleinlaufkartenPrintable(PageMode mode) {
        this(null, mode, true, 0, 0);
    }

    public ZieleinlaufkartenPrintable(AWettkampf<T>[] wks, PageMode mode, boolean allheats, int minheat, int maxheat) {
        super(mode);
        if (wks != null && wks.length > 0) {
            LinkedList<Zieleinlaufkarte> sk = SchwimmerUtils.toZieleinlauf(wks, getPagesPerPage(), allheats, minheat,
                                                                           maxheat);
            if (!sk.isEmpty()) {
                karten = sk.toArray(new Zieleinlaufkarte[0]);
            } else {
                karten = new Zieleinlaufkarte[0];
            }
        }
    }

    private static JComponent getUnderline() {
        JLabel label = new JLabel();
        label.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return label;
    }

    private static JComponent getMiddleline() {
        return new JMiddleline();
    }

    private static JPanel getPanel(Zieleinlaufkarte karte) {
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void add(Component comp, Object constraints) {
                Font font = PrintManager.getFont();
                if (font != null) {
                    comp.setFont(font);
                }
                setForeground(Color.BLACK);
                super.add(comp, constraints);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        SimpleTableBuilder tb = new SimpleTableBuilder(panel, 2, true);

        tb.add(I18n.get("Zieleinlaufkarte"), true, "center,center");
        tb.add(I18n.get("AgeGroup") + ": ");
        if (karte == null) {
            tb.add(getUnderline());
        } else {
            tb.add(karte.getStartgruppe());
        }
        tb.add(I18n.get("Heat") + ":");
        if (karte == null) {
            tb.add(getUnderline());
        } else {
            tb.add(karte.getLauf());
        }
        tb.add(I18n.get("Discipline") + ":");
        if (karte == null) {
            tb.add(getUnderline(), true);
        } else {
            tb.add(karte.getDisziplin(), true);
        }
        tb.addSeparator(getMiddleline());
        tb.add(I18n.get("Lane"), true, "center,center");
        tb.add(I18n.get("RankNr", 1) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 2) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 3) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 4) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 5) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 6) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 7) + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("RankNr", 8) + ":");
        tb.add(getUnderline(), true);
        tb.addSeparator(getMiddleline());
        tb.add(I18n.get("PlacingJudge") + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("Auswerter") + ":");
        tb.add(getUnderline(), true);
        tb.add(I18n.get("Wettkampfleiter") + ":");
        tb.add(getUnderline(), true);

        tb.addSeparator(getMiddleline());
        tb.add(I18n.get("ProgrammerShortInfo"), true, "right,center");

        JPanel p = tb.getPanel(karte == null);
        for (Component c : p.getComponents()) {
            c.setFont(PrintManager.getFont());
            c.setForeground(Color.BLACK);
        }
        return p;
    }

    @Override
    public JComponent getPanel(int page, int offset) {
        if (karten == null) {
            return getPanel(null);
        }
        int index = page * getPagesPerPage() + offset;
        if (index < karten.length) {
            return getPanel(karten[index]);
        }
        return null;
    }

    @Override
    public boolean pageExists(int page) {
        if (karten == null) {
            return page == 0;
        }
        int seiten = karten.length / getPagesPerPage();
        if ((karten.length % getPagesPerPage()) > 0) {
            seiten++;
        }
        return seiten > page;
    }
}
