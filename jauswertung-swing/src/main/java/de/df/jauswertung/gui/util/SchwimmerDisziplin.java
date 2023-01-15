/*
 * Created on 27.06.2005
 */
package de.df.jauswertung.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.renderer.ListRenderDataProvider;
import de.df.jutils.gui.renderer.TableRenderDataProvider;
import de.df.jutils.util.StringTools;

public class SchwimmerDisziplin<T extends ASchwimmer>
        implements TableRenderDataProvider, ListRenderDataProvider, Comparable<SchwimmerDisziplin<T>> {

    public static boolean compressLists = true;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_EXTENDED_BY_DISCIPLINE = 1;
    public static final int MODE_EXTENDED_BY_AGEGROUP = 2;
    public static final int MODE_EXTENDED_BY_SEX = 4;

    public static final int MODE_FULL = 7;

    private final T swimmer;
    private final int discipline;
    private final int mode;

    private JLabel l;

    public SchwimmerDisziplin() {
        this((T) null, -1, MODE_NORMAL);
    }

    public SchwimmerDisziplin(T s, int disz) {
        this(s, disz, MODE_NORMAL);
    }

    private SchwimmerDisziplin(T s, int disz, int m) {
        swimmer = s;
        discipline = disz;
        mode = m;

        l = new JLabel();
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 1, 1, 1));
    }

    private static <T extends ASchwimmer> int getMode(Lauf<T> l, boolean extended) {
        int mode = MODE_NORMAL;
        if (extended) {
            if (!l.isOnlyOneAgeGroup() || l.isStartgroup()) {
                mode = mode | MODE_EXTENDED_BY_AGEGROUP;
            }
            if (!l.isOnlyOneDiscipline()) {
                mode = mode | MODE_EXTENDED_BY_DISCIPLINE;
            }
        }
        if (!l.isOnlyOneSex()) {
            mode = mode | MODE_EXTENDED_BY_SEX;
        }
        return mode;
    }

    public SchwimmerDisziplin(int x, Lauf<T> l, boolean extended) {
        this(l.getSchwimmer(x), l.getDisznummer(x), getMode(l, extended));
    }

    public T getSchwimmer() {
        return swimmer;
    }

    public String getMeldezeit() {
        if (swimmer == null) {
            return "";
        }
        if (discipline >= 0) {
            return StringTools.zeitString(swimmer.getMeldezeit(discipline));
        }
        return "";
    }

    public int getDiscipline() {
        return discipline;
    }

    @Override
    public String[] getTableRenderData() {
        String[] result = getTableRenderDataI();
        for (int x = 0; x < result.length; x++) {
            result[x] = result[x].replace("  ", " ");
        }
        if (compressLists) {
            while ((result.length >= 2) && (result[0].indexOf(result[1]) >= 0)) {
                String[] r = new String[result.length - 1];
                r[0] = result[0];
                System.arraycopy(result, 2, r, 1, result.length - 2);
                result = r;
            }
        }
        return result;
    }

    private String[] getTableRenderDataI() {
        if (swimmer == null) {
            return new String[] { " ", " " };
        }
        String disz = "";
        if (discipline >= 0) {
            disz = swimmer.getAK().getDisziplin(discipline, swimmer.isMaennlich()).getName();
        }
        String ext = "";
        boolean sex = ((mode & MODE_EXTENDED_BY_SEX) > 0);
        boolean agegroup = ((mode & MODE_EXTENDED_BY_AGEGROUP) > 0);
        boolean disciplineboolean = ((mode & MODE_EXTENDED_BY_DISCIPLINE) > 0);

        ArrayList<String> extensions = new ArrayList<>();

        if (swimmer instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) swimmer;
            if (t.getJahrgang() > 0) {
                extensions.add(I18n.yearToShortString(t.getJahrgang()));
            }
        }
        if (sex && !agegroup) {
            extensions.add(I18n.geschlechtToShortString(swimmer));
        }
        if (extensions.size() > 0) {
            StringBuilder sb = new StringBuilder(" (");
            sb.append(String.join(" ", extensions));
            sb.append(")");
            ext = sb.toString();
        }

        boolean isMultiline = swimmer.getWettkampf().isMultiline();

        String name1 = null;
        String name2 = null;

        Mannschaft m = isMultiline && swimmer instanceof Mannschaft ? (Mannschaft) swimmer : null;
        if (m != null && discipline >= 0) {
            if (swimmer.getName().equals(swimmer.getGliederung())) {
                name1 = m.getStarterShort(discipline, ", ");
            } else {
                name1 = m.getStarterShort(discipline, ", ");
                name2 = swimmer.getName();
            }
        } else {
            name1 = swimmer.getName();
        }

        LinkedList<String> text = new LinkedList<>();

        if (name2 != null) {
            text.addLast(
                    I18n.get("HeatTableLine1a", name1, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
            text.addLast(
                    I18n.get("HeatTableLine1b", name2, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
        } else {
            text.addLast(
                    I18n.get("HeatTableLine1", name1, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
        }
        if (!name1.contains(swimmer.getGliederung()) && (name2 == null || !name2.contains(swimmer.getGliederung()))) {
            text.addLast(
                    I18n.get("HeatTableLine2", name1, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
        }

        if (disciplineboolean) {
            text.addLast(
                    I18n.get("HeatTableLine3", name1, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
        }
        if (agegroup) {
            text.addLast(
                    I18n.get("HeatTableLine4", name1, swimmer.getGliederung(), swimmer.getAK().getName(),
                            I18n.geschlechtToShortString(swimmer), disz, ext));
        }

        return text.toArray(new String[text.size()]);
    }

    @Override
    public Component getListRenderData(Color f, Color b) {
        if (swimmer == null) {
            l.setIcon(null);
            l.setText(null);
            l.setForeground(f);
            l.setBackground(b);
            return l;
        }
        l.setForeground(f);
        l.setBackground(b);
        if (swimmer instanceof Teilnehmer) {
            l.setIcon(IconManager.getBigIcon("person"));
        } else {
            l.setIcon(IconManager.getBigIcon("team"));
        }
        l.setText(I18n.get("SwimmerInHeatqueue", swimmer.getName(),
                swimmer.getAK().getDisziplin(discipline, swimmer.isMaennlich()),
                StartnumberFormatManager.format(swimmer), swimmer.getAK().getName(), I18n.geschlechtToString(swimmer),
                swimmer.getGliederung(),
                getMeldezeit()));

        return l;
    }

    @Override
    public String toString() {
        if (swimmer == null) {
            return I18n.get("Nobody");
        }
        int zahl = discipline;
        return I18n.get("SwimmerInHeatqueue", swimmer.getName(),
                swimmer.getAK().getDisziplin(zahl, swimmer.isMaennlich()),
                StartnumberFormatManager.format(swimmer), swimmer.getAK().getName(), I18n.geschlechtToString(swimmer),
                swimmer.getGliederung());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof SchwimmerDisziplin) {
            return compareTo((SchwimmerDisziplin<T>) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (swimmer == null) {
            return 0;
        }
        return swimmer.hashCode();
    }

    @Override
    public int compareTo(SchwimmerDisziplin<T> sd) {
        if ((swimmer == null) && (sd.getSchwimmer() == null)) {
            return 0;
        }
        if (swimmer == null) {
            return -1;
        }
        if (sd.getSchwimmer() == null) {
            return 1;
        }
        int diff = swimmer.getAKNummer() - sd.swimmer.getAKNummer();
        if (diff != 0) {
            return diff;
        }
        diff = (swimmer.isMaennlich() ? 1 : 0) - (sd.swimmer.isMaennlich() ? 1 : 0);
        if (diff != 0) {
            return diff;
        }
        diff = swimmer.getName().compareToIgnoreCase(sd.swimmer.getName());
        if (diff != 0) {
            return diff;
        }

        diff = swimmer.getStartnummer() - sd.swimmer.getStartnummer();
        if (diff != 0) {
            return diff;
        }
        return discipline - sd.discipline;
    }
}