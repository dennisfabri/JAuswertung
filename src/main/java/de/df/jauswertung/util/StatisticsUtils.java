package de.df.jauswertung.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.JRotatingLabel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.print.PrintManager;

public class StatisticsUtils {

    public static <T extends ASchwimmer> LinkedList<JComponent> createLVOverviewPage(AWettkampf<T> wk, int max, boolean print) {
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
        return createOverviewPage(wk, max, print);
    }

    public static <T extends ASchwimmer> PenaltyCounter[][] countPenaltiesPerAgegroup(AWettkampf<T> wk) {
        Regelwerk aks = wk.getRegelwerk();
        PenaltyCounter[][] counter = new PenaltyCounter[aks.size()][2];
        for (int x = 0; x < counter.length; x++) {
            for (int y = 0; y < counter[x].length; y++) {
                counter[x][y] = new PenaltyCounter();
            }
        }

        for (T s : wk.getSchwimmer()) {
            LinkedList<Strafe> strafen = new LinkedList<Strafe>();
            strafen.addAll(s.getAllgemeineStrafen());
            for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                strafen.addAll(s.getStrafen(x));
            }

            PenaltyCounter c = counter[s.getAKNummer()][s.isMaennlich() ? 1 : 0];
            for (Strafe strafe : strafen) {
                c.addPenalty(strafe, s.getAK().isStrafeIstDisqualifikation());
            }
        }

        int max = 1;
        for (int x = 0; x < counter.length; x++) {
            if ((counter[x][0].getSumOfPenalties() + counter[x][1].getSumOfPenalties()) > 0) {
                max = x + 1;
            }
        }
        if (max < counter.length) {
            PenaltyCounter[][] newcounter = new PenaltyCounter[max][2];
            for (int x = 0; x < max; x++) {
                newcounter[x] = counter[x];
            }
            counter = newcounter;
        }

        return counter;
    }

    public static <T extends ASchwimmer> LinkedList<JComponent> createPenaltiesStatistics(AWettkampf<T> wk) {
        LinkedList<JComponent> list = new LinkedList<JComponent>();
        list.addLast(createPenaltiesPerAgegroupPanel(wk));
        if (wk.hasLaufliste() && wk.isDLRGBased()) {
            list.addLast(createPenaltiesPerLaneAndDisciplinePanel(wk));
        }
        return list;
    }

    private static <T extends ASchwimmer> PenaltyCounter[][][] countPenaltiesPerLaneAndDiscipline(AWettkampf<T> wk) {
        int lanes = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        PenaltyCounter[][][] counter = new PenaltyCounter[wk.getRegelwerk().size()][0][0];
        for (int x = 0; x < counter.length; x++) {
            counter[x] = new PenaltyCounter[wk.getRegelwerk().getAk(x).getDiszAnzahl()][lanes];
            for (int y = 0; y < counter[x].length; y++) {
                for (int z = 0; z < counter[x][y].length; z++) {
                    counter[x][y][z] = new PenaltyCounter();
                }
            }
        }

        Laufliste<T> laufliste = wk.getLaufliste();
        if (laufliste != null && laufliste.getLaufliste() != null) {
            for (Lauf<T> lauf : laufliste.getLaufliste()) {
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    T t = lauf.getSchwimmer(x);
                    if (t != null) {
                        int disz = lauf.getDisznummer(x);
                        LinkedList<Strafe> strafen = t.getStrafen(disz);
                        for (Strafe s : strafen) {
                            counter[t.getAKNummer()][disz][x].addPenalty(s, t.getAK().isStrafeIstDisqualifikation());
                        }
                    }
                }
            }
        }
        int maxak = -1;
        for (int x = 0; x < counter.length; x++) {
            int sum = 0;
            for (int y = 0; y < counter[x].length; y++) {
                for (int z = 0; z < counter[x][y].length; z++) {
                    sum += counter[x][y][z].getSumOfPenalties();
                }
            }
            if (sum > 0) {
                maxak = x;
            }
        }
        if (maxak < counter.length) {
            PenaltyCounter[][][] pcnew = new PenaltyCounter[maxak + 1][0][0];
            for (int x = 0; x < maxak + 1; x++) {
                pcnew[x] = counter[x];
            }
            counter = pcnew;
        }
        return counter;
    }

    public static <T extends ASchwimmer> JComponent createPenaltiesPerLaneAndDisciplinePanel(AWettkampf<T> wk) {
        PenaltyCounter[][][] counter = countPenaltiesPerLaneAndDiscipline(wk);

        int lanes = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        int rows = 3;
        for (int x = 0; x < counter.length; x++) {
            rows += counter[x].length + 3;
        }
        int[][] groups = new int[1][lanes + 1];
        for (int x = 0; x < lanes + 1; x++) {
            groups[0][x] = 2 * x + 6;
        }
        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(3 + lanes), FormLayoutUtils.createLayoutString(rows));
        layout.setColumnGroups(groups);
        JPanel panel = new JPanel(layout);
        panel.setName(I18n.get("PenaltiesPerLane"));

        panel.add(new JLabel(I18n.get("PenaltiesPerLane")), CC.xywh(2, 2, 2 * (lanes + 2) - 1, 1, "center,center"));

        panel.add(new JLabel(I18n.get("Lanes")), CC.xywh(6, 4, lanes * 2 - 1, 1, "center,center"));
        panel.add(new JLabel(I18n.get("Sum")), CC.xywh(6 + 2 * lanes, 4, 1, 3, "center,center"));

        for (int x = 0; x < lanes; x++) {
            panel.add(new JLabel("" + (x + 1)), CC.xy(6 + 2 * x, 6, "center,center"));
        }

        Regelwerk aks = wk.getRegelwerk();
        int row = 8;
        for (int x = 0; x < counter.length; x++) {
            Altersklasse ak = aks.getAk(x);
            panel.add(createLabel(ak.getName(), Color.LIGHT_GRAY), CC.xyw(2, row, 3 + 2 * lanes + 2));
            row += 2;
            for (int y = 0; y < counter[x].length; y++) {
                panel.add(new JLabel(ak.getDisziplin(y, false).getName()), CC.xy(4, row));
                int sum = 0;
                for (int z = 0; z < counter[x][y].length; z++) {
                    PenaltyCounter pc = counter[x][y][z];
                    // panel.add(new JLabel("" + pc.getPoints() + "/"
                    // + pc.getDisqualified() + "/" + pc.getExcluded()
                    // + "/" + pc.getSumOfPenalties()), CC.xy(6 + 2 * z,
                    // row));
                    if (pc.getSumOfPenalties() > 0) {
                        sum += pc.getSumOfPenalties();
                        panel.add(new JLabel("" + pc.getSumOfPenalties()), CC.xy(6 + 2 * z, row, "center,center"));
                    }
                }
                if (sum > 0) {
                    panel.add(new JLabel("" + sum), CC.xy(6 + 2 * lanes, row, "center,center"));
                }
                row += 2;
            }
            int sumall = 0;
            for (int y = 0; y < lanes; y++) {
                int sum = 0;
                for (int z = 0; z < counter[x].length; z++) {
                    sum += counter[x][z][y].getSumOfPenalties();
                }
                if (sum > 0) {
                    panel.add(new JLabel("" + sum), CC.xy(6 + 2 * y, row, "center,center"));
                }
                sumall += sum;
            }
            if (sumall > 0) {
                panel.add(new JLabel("" + sumall), CC.xy(6 + 2 * lanes, row, "center,center"));
            }
            panel.add(createLabel(I18n.get("Sum"), ColorUtils.calculateColor(Color.LIGHT_GRAY, Color.LIGHT_GRAY.brighter(), 0.5)),
                    CC.xyw(4, row, 3 + 2 * lanes));
            // panel.add(new JLabel(I18n.get("Sum")), CC.xy(4, row));
            row += 4;
        }

        return panel;
    }

    private static JLabel createLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        if (color != null) {
            l.setBackground(color);
            l.setOpaque(true);
        }
        return l;
    }

    public static <T extends ASchwimmer> JComponent createPenaltiesPerAgegroupPanel(AWettkampf<T> wk) {
        Regelwerk rw = wk.getRegelwerk();

        PenaltyCounter[][] counter = countPenaltiesPerAgegroup(wk);

        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(17), FormLayoutUtils.createLayoutString(3 + counter.length + 1));
        layout.setColumnGroups(new int[][] { { 6, 8, 10, 12, 16, 18, 20, 22, 26, 28, 30, 32 } });
        JPanel panel = new JPanel(layout);
        panel.setName(I18n.get("Penalties"));

        panel.add(new JLabel(I18n.get("Penalties")), CC.xywh(2, 2, 33, 1, "center,center"));

        panel.add(new JLabel(I18n.get("AgeGroup")), CC.xywh(2, 4, 1, 3, "center,center"));
        panel.add(new JLabel(I18n.geschlechtToString(rw, false)), CC.xywh(6, 4, 7, 1, "center,center"));
        panel.add(new JLabel(I18n.geschlechtToString(rw, true)), CC.xywh(16, 4, 7, 1, "center,center"));
        panel.add(new JLabel(I18n.get("Gesamt")), CC.xywh(26, 4, 7, 1, "center,center"));

        for (int x = 0; x < 3; x++) {
            panel.add(new JLabel("   "), CC.xy(4 + x * 10, 6));
            panel.add(new JLabel(I18n.get("Points")), CC.xy(6 + x * 10, 6, "center,center"));
            panel.add(new JLabel(I18n.get("DisqualifiedShort")), CC.xy(8 + x * 10, 6, "center,center"));
            panel.add(new JLabel(I18n.get("DebarmentShort")), CC.xy(10 + x * 10, 6, "center,center"));
            panel.add(new JLabel(I18n.get("Sum")), CC.xy(12 + x * 10, 6, "center,center"));
        }

        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < counter.length; x++) {
            panel.add(new JLabel(aks.getAk(x).getName()), CC.xy(2, 8 + 2 * x));
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 4; z++) {
                    int amount = counter[x][y].getAmount(z);
                    if (amount > 0) {
                        panel.add(new JLabel("" + amount), CC.xy(6 + 2 * z + 10 * y, 8 + 2 * x, "center,center"));
                    }
                }
            }
            for (int z = 0; z < 4; z++) {
                int amount = counter[x][0].getAmount(z) + counter[x][1].getAmount(z);
                if (amount > 0) {
                    panel.add(new JLabel("" + amount), CC.xy(26 + 2 * z, 8 + 2 * x, "center,center"));
                }
            }
        }

        {
            panel.add(new JLabel(I18n.get("Gesamt")), CC.xy(2, 8 + 2 * counter.length));
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 4; z++) {
                    int amount = 0;
                    for (int x = 0; x < counter.length; x++) {
                        amount += counter[x][y].getAmount(z);
                    }
                    if (amount > 0) {
                        panel.add(new JLabel("" + amount), CC.xy(6 + 2 * z + 10 * y, 8 + 2 * counter.length, "center,center"));
                    }
                }
            }
            for (int z = 0; z < 4; z++) {
                int amount = 0;
                for (int x = 0; x < counter.length; x++) {
                    amount += counter[x][0].getAmount(z) + counter[x][1].getAmount(z);
                }
                if (amount > 0) {
                    panel.add(new JLabel("" + amount), CC.xy(26 + 2 * z, 8 + 2 * counter.length, "center,center"));
                }
            }
        }

        return panel;
    }

    public static <T extends ASchwimmer> LinkedList<JComponent> createOverviewPage(AWettkampf<T> wk, int max, boolean print) {
        Regelwerk aks = wk.getRegelwerk();

        if (!wk.hasSchwimmer()) {
            return new LinkedList<JComponent>();
        }

        int size = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                size = x + 1;
            }
        }

        LinkedList<String> gliederungsliste = wk.getGliederungenMitQGliederung();
        Hashtable<String, Integer> gliederungen = new Hashtable<String, Integer>();
        boolean longgld = false;
        {
            int x = 0;
            for (String gld : gliederungsliste) {
                if (gld.length() > 3) {
                    longgld = true;
                }
                gliederungen.put(gld, x);
                x++;
            }
        }
        int glds = gliederungsliste.size();

        int[] statssum = new int[glds + 1];
        int[][][] stats = new int[glds][size][2];
        int[][] statsall = new int[size][2];

        for (T swimmer : wk.getSchwimmer()) {
            int ak = swimmer.getAKNummer();
            boolean male = swimmer.isMaennlich();
            int gld = gliederungen.get(swimmer.getGliederungMitQGliederung());

            statssum[glds]++;
            statssum[gld]++;
            stats[gld][ak][male ? 1 : 0]++;
            statsall[ak][male ? 1 : 0]++;
        }

        LinkedList<JComponent> ps = new LinkedList<JComponent>();

        int amount = gliederungsliste.size();
        int offset = 0;
        int tsize = checkBlocksize(max, amount);
        while (amount > 0) {
            JComponent px = overviewToPage(aks, size, gliederungsliste, longgld, glds, statssum, stats, statsall, offset, tsize, tsize == amount, print);
            ps.addLast(px);

            offset += tsize;
            amount -= tsize;
            tsize = checkBlocksize(max, amount);
        }
        return ps;
    }

    public static <T extends ASchwimmer> JComponent createStatistics(LinkedList<T> swimmer, Regelwerk aks, String title, boolean print) {
        int starts = 0;
        ListIterator<T> li = swimmer.listIterator();

        Hashtable<String, Boolean> gliederungenHt = new Hashtable<String, Boolean>();
        int[][] akTeilies = new int[aks.size()][2];
        int[][] akStarts = new int[aks.size()][2];
        while (li.hasNext()) {
            T s = li.next();
            gliederungenHt.put(s.getGliederung(), true);
            starts += s.getDisciplineChoiceCount();
            int ak = s.getAKNummer();
            int g = s.isMaennlich() ? 1 : 0;
            akTeilies[ak][g]++;
            akStarts[ak][g] += s.getDisciplineChoiceCount();
        }

        int anzahl = aks.size();
        for (int x = akTeilies.length - 1; x >= 0; x--) {
            if (akTeilies[x][0] + akTeilies[x][1] == 0) {
                anzahl--;
            } else {
                break;
            }
        }

        int anzahlGliederungen = gliederungenHt.size();

        // 1 Row for the title (if any)
        // 2 Rows for the general information
        // 1 blank line
        // 2 Headers
        // anzahl Table contents
        int rows = (title == null ? 1 : 1) + (anzahlGliederungen > 1 ? 1 : 0) + 2 + 1 + 2 + anzahl;
        int space = (print ? 1 : 4);
        FormLayout layout = new FormLayout("0dlu:grow,fill:default," + space + "dlu,center:default," + "" + space + "dlu,center:default," + space
                + "dlu,center:default," + "" + space + "dlu,center:default,0dlu:grow", FormLayoutUtils.createLayoutString(rows, space));
        layout.setColumnGroups(new int[][] { { 1, 11 }, { 4, 6, 8, 10 } });
        JPanel p = null;
        if (print) {
            p = new JPanel(layout) {
                private static final long serialVersionUID = 5430202481734186620L;

                @Override
                public void setFont(Font font) {
                    if (font == null) {
                        return;
                    }
                    super.setFont(font);
                    for (Component c : getComponents()) {
                        c.setFont(font);
                    }
                }
            };
            p.setBackground(Color.WHITE);
        } else {
            p = new JPanel(layout);
        }

        int offset = 0;
        if (title != null) {
            offset = 2;
            p.add(new JLabel(title), CC.xyw(2, 2, 9, "center,center"));
        } else {
            p.add(new JLabel(" "), CC.xyw(2, 2, 9, "center,center"));
        }
        offset = 2;

        String teilies = (swimmer.getFirst() instanceof Teilnehmer ? I18n.get("Swimmer") : I18n.get("Team"));

        p.add(new JLabel(teilies), CC.xy(2, 2 + offset));
        p.add(new JLabel("" + swimmer.size()), CC.xy(10, 2 + offset));

        p.add(new JLabel(I18n.get("Starts")), CC.xy(2, 4 + offset));
        p.add(new JLabel("" + starts), CC.xy(10, 4 + offset));

        offset += 2;

        if (anzahlGliederungen > 1) {
            p.add(new JLabel(I18n.get("Organisations")), CC.xy(2, 4 + offset));
            p.add(new JLabel("" + anzahlGliederungen), CC.xy(10, 4 + offset));
            offset += 2;
        }

        p.add(new JLabel(teilies), CC.xyw(4, 6 + offset, 3, "center,fill"));
        p.add(new JLabel(I18n.get("Starts")), CC.xyw(8, 6 + offset, 3, "center,fill"));

        p.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(4, 8 + offset));
        p.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(6, 8 + offset));
        p.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(8, 8 + offset));
        p.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(10, 8 + offset));

        for (int x = 0; x < anzahl; x++) {
            p.add(new JLabel(aks.getAk(x).getName()), CC.xy(2, 2 * x + 10 + offset));
            p.add(new JLabel("" + akTeilies[x][0]), CC.xy(4, 2 * x + 10 + offset));
            p.add(new JLabel("" + akTeilies[x][1]), CC.xy(6, 2 * x + 10 + offset));
            p.add(new JLabel("" + akStarts[x][0]), CC.xy(8, 2 * x + 10 + offset));
            p.add(new JLabel("" + akStarts[x][1]), CC.xy(10, 2 * x + 10 + offset));
        }

        if (print) {
            p.setFont(PrintManager.getFont());
        }

        return p;
    }

    public static <T extends ASchwimmer> JComponent getStarts(LinkedList<T> swimmer, Regelwerk aks, boolean print) {

        int max = 0;
        int[][][] starts = new int[aks.size()][2][0];
        for (int x = 0; x < aks.size(); x++) {
            int anzahl = aks.getAk(x).getDiszAnzahl();
            for (int y = 0; y < 2; y++) {
                starts[x][y] = new int[anzahl];
                for (int z = 0; z < starts[x][y].length; z++) {
                    starts[x][y][z] = 0;
                }
            }
            if (anzahl > max) {
                max = anzahl;
            }
        }

        for (T t : swimmer) {
            int ak = t.getAKNummer();
            int m = (t.isMaennlich() ? 1 : 0);
            for (int x = 0; x < t.getAK().getDiszAnzahl(); x++) {
                if (t.isDisciplineChosen(x)) {
                    starts[ak][m][x]++;
                }
            }
        }

        int rows = 0;
        for (int x = 0; x < starts.length; x++) {
            boolean active = false;
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < starts[x][y].length; z++) {
                    if (starts[x][y][z] > 0) {
                        active = true;
                        break;
                    }
                }
                if (active) {
                    break;
                }
            }
            if (active) {
                rows = x + 1;
            }
        }

        if (rows == 0) {
            return new JLabel();
        }

        int space = (print ? 1 : 4);

        StringBuffer sb = new StringBuffer();
        sb.append("0dlu:grow,");
        for (int x = 0; x < max; x++) {
            sb.append("fill:default," + space + "dlu,");
        }
        sb.append("fill:default,0dlu:grow");

        FormLayout layout = new FormLayout(sb.toString(), FormLayoutUtils.createLayoutString(rows + 3, space));
        JPanel p = null;
        if (print) {
            p = new JPanel(layout) {
                private static final long serialVersionUID = 5430202481734186620L;

                @Override
                public void setFont(Font font) {
                    if (font == null) {
                        return;
                    }
                    super.setFont(font);
                    for (Component c : getComponents()) {
                        c.setFont(font);
                    }
                }
            };
            p.setBackground(Color.WHITE);
        } else {
            p = new JPanel(layout);
        }
        p.add(new JLabel(" " + I18n.get("StartsPerDiscipline") + " "), CC.xyw(2, 2, max * 2 + 1, "center,fill"));

        for (int z = 0; z < max; z++) {
            p.add(new JLabel(" " + (z + 1) + " "), CC.xy(4 + 2 * z, 4, "center,fill"));
            p.add(new JLabel(" w / m "), CC.xy(4 + 2 * z, 6, "center,fill"));
        }

        for (int row = 0; row < rows; row++) {
            JLabel ak = new JLabel(aks.getAk(row).getName());
            p.add(ak, CC.xy(2, row * 2 + 8));
            for (int z = 0; z < starts[row][0].length; z++) {
                p.add(new JLabel(" " + starts[row][0][z] + " / " + starts[row][1][z] + " "), CC.xy(4 + 2 * z, row * 2 + 8, "center,fill"));
            }
        }

        if (print) {
            p.setFont(PrintManager.getFont());
        }

        return p;
    }

    private static int checkBlocksize(int max, int available) {
        if (max <= 0) {
            return available;
        }
        int max2 = Integer.MAX_VALUE;
        if (max < Integer.MAX_VALUE - 5) {
            max2 = max + 5;
        }
        if (available > max2) {
            return max;
        }
        return available;
    }

    private static JPanel overviewToPage(Regelwerk aks, int size, LinkedList<String> gliederungsliste, boolean longgld, int glds, int[] statssum,
            int[][][] stats, int[][] statsall, int toffset, int tsize, boolean last, boolean print) {
        glds = tsize;
        JPanel p = null;
        if (print) {
            p = new JPanel() {
                @Override
                public void setFont(Font font) {
                    if (font == null) {
                        return;
                    }
                    super.setFont(font);
                    for (Component c : getComponents()) {
                        Font f = font;
                        if ((c.getFont() != null) && (c.getFont().isItalic() || c.getFont().isBold())) {
                            if (c.getFont().isItalic()) {
                                f = font.deriveFont(Font.ITALIC);
                            } else {
                                f = font.deriveFont(Font.BOLD);
                            }
                        }
                        c.setFont(f);
                    }
                }
            };
        } else {
            p = new JPanel();
        }

        int space = (print ? 1 : 4);

        StringBuffer sb = new StringBuffer();
        sb.append("" + space + "dlu:grow,fill:default," + space + "dlu,center:default");
        for (int x = 0; x < size; x++) {
            sb.append("," + space + "dlu,center:default");
        }
        sb.append("," + space + "dlu:grow");
        FormLayout layout = new FormLayout(sb.toString(), FormLayoutUtils.createLayoutString(tsize + 5, space));
        // layout.setRowGroups(FormLayoutUtils.createGroups(glds + 5));
        p.setLayout(layout);

        p.add(new JLabel(I18n.get("RegistrationsPerOrganisation")), CC.xywh(2, 2, size * 2 + 3, 1, "center,center"));

        int offset = 4;

        p.add(new JLabel(I18n.get(longgld ? "Organisation" : "OrganisationShort")), CC.xywh(2, 2 + offset, 1, 3, "center,center"));
        p.add(new JLabel(I18n.get("SumSign")), CC.xywh(4, 2 + offset, 1, 3, "center,center"));
        String sexesheader = " " + I18n.geschlechtToShortString(aks, false) + " / " + I18n.geschlechtToShortString(aks, true) + " ";
        if (last) {
            p.add(new JLabel(I18n.get("SumSign")), CC.xy(2, 6 + offset + 2 * tsize));
            p.add(new JLabel("" + statssum[glds]), CC.xy(4, 6 + offset + 2 * tsize));
        }
        for (int x = 0; x < size; x++) {
            p.add(new JRotatingLabel(aks.getAk(x).getName()), CC.xy(6 + 2 * x, 2 + offset));
            p.add(new JLabel(sexesheader), CC.xy(6 + 2 * x, 4 + offset));

            if (last) {
                p.add(new JLabel(statsall[x][0] + " / " + statsall[x][1]), CC.xy(6 + 2 * x, 6 + offset + 2 * glds));
            }
        }

        for (int y = 0; y < tsize; y++) {
            p.add(new JLabel(gliederungsliste.get(y + toffset)), CC.xy(2, 6 + offset + 2 * y));
            p.add(new JLabel("" + statssum[y + toffset]), CC.xy(4, 6 + offset + 2 * y));
            for (int x = 0; x < size; x++) {
                p.add(new JLabel(stats[y + toffset][x][0] + " / " + stats[y + toffset][x][1]), CC.xy(6 + 2 * x, 6 + offset + 2 * y));
            }
        }

        if (print) {
            p.setBackground(Color.WHITE);
            p.setFont(PrintManager.getFont());
        }
        return p;
    }

    public static <T extends ASchwimmer> LinkedList<String> getGliederungenWithSwimmers(AWettkampf<T> wk) {
        LinkedList<String> g = wk.getGliederungenMitQGliederung();
        ListIterator<String> li = g.listIterator();
        while (li.hasNext()) {
            if (!SearchUtils.hasSchwimmer(wk, li.next())) {
                li.remove();
            }
        }
        return g;
    }

    public static <T extends ASchwimmer> LinkedList<String> getQualiGliederungenWithSwimmers(AWettkampf<T> wk) {
        LinkedList<String> g = wk.getQualigliederungen();
        ListIterator<String> li = g.listIterator();
        while (li.hasNext()) {
            if (!SearchUtils.hasSchwimmerForQGld(wk, li.next())) {
                li.remove();
            }
        }
        return g;
    }

    public static <T extends ASchwimmer> JComponent generateQualiGliederungStats(AWettkampf<T> wk, String gliederung) {
        wk = Utils.copy(wk);

        LinkedList<T> g = wk.getSchwimmer();
        ListIterator<T> li = g.listIterator();
        while (li.hasNext()) {
            T t = li.next();
            if (t.getQualifikationsebene().equals(gliederung)) {
                t.setGliederung(gliederung);
                t.setQualifikationsebene("");
            } else {
                li.remove();
            }
        }
        // TODO: Check QGld
        return createStatistics(SearchUtils.getSchwimmer(wk, new String[] { gliederung }, false), wk.getRegelwerk(), gliederung, false);
    }

    public static <T extends ASchwimmer> JComponent generateGliederungStats(AWettkampf<T> wk, String gliederung) {
        return createStatistics(SearchUtils.getSchwimmer(wk, new String[] { gliederung }, true), wk.getRegelwerk(), gliederung, false);
    }

    public static class PenaltyCounter {

        private int points       = 0;
        private int disqualified = 0;
        private int didNotStart  = 0;
        private int excluded     = 0;

        public void addPenalty(Strafe p, boolean disqualify) {
            if (p == null) {
                return;
            }
            if (disqualify) {
                p = new Strafe(p, true);
            }
            switch (p.getArt()) {
            case AUSSCHLUSS:
                excluded++;
                break;
            case DISQUALIFIKATION:
                disqualified++;
                break;
            case NICHT_ANGETRETEN:
                didNotStart++;
                break;
            case NICHTS:
                break;
            case STRAFPUNKTE:
                if (p.getStrafpunkte() > 0) {
                    points++;
                }
                break;
            }
        }

        public int getAmount(int pos) {
            switch (pos) {
            case 0:
                return getPoints();
            case 1:
                return getDisqualified();
            case 2:
                return getExcluded();
            case 3:
                return getSumOfPenalties();
            }
            return -1;
        }

        public int getPoints() {
            return points;
        }

        public int getDisqualified() {
            return disqualified;
        }

        public int getDidNotStart() {
            return didNotStart;
        }

        public int getExcluded() {
            return excluded;
        }

        public int getSumOfPenalties() {
            return points + disqualified + excluded;
        }
    }
}