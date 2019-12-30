/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.print.ComponentPackingPrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.util.Converter;
import de.df.jutils.util.StringTools;
import de.df.jutils.util.Tupel;

public final class RecorderPrintable<T extends ASchwimmer> extends ComponentPackingPrintable {

    public RecorderPrintable(AWettkampf<T> wk, boolean showDisciplines, boolean showTimes, boolean showQuali, boolean showOrganisation) {
        super(5, -1, false, getPanels(wk, showDisciplines, showTimes, showQuali, showOrganisation || wk.isEinzel()));
    }

    private static <T extends ASchwimmer> Component[] getPanels(AWettkampf<T> wk, boolean showDisciplines, boolean showTimes, boolean showQuali,
            boolean showOrganisation) {
        if (wk == null) {
            return null;
        }
        LinkedList<Lauf<T>> laufliste = wk.getLaufliste().getLaufliste();
        if (laufliste == null) {
            return new Component[0];
        }

        boolean[] lanes = new boolean[wk.getIntegerProperty(PropertyConstants.HEATS_LANES)];
        for (int x = 0; x < lanes.length; x++) {
            lanes[x] = wk.getLaufliste().isLaneUsed(x);
        }

        LinkedList<Component> components = new LinkedList<Component>();
        for (Lauf<T> lauf : laufliste) {
            components.addLast(getPanel(wk, lauf, lanes, showDisciplines, showTimes, showQuali, showOrganisation));
        }
        return components.toArray(new Component[components.size()]);
    }

    private static <T extends ASchwimmer> JPanel getPanel(AWettkampf<T> wk, Lauf<T> lauf, boolean[] lanes, boolean showDisciplines, boolean showTimes,
            boolean showQuali, boolean showOrganisation) {
        FormLayout layout = new FormLayout("1dlu,fill:default:grow,1dlu,fill:default:grow,1dlu,fill:default:grow,1dlu",
                "1dlu,fill:default,1dlu,fill:default,4dlu");
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        boolean isFinal = wk.getBooleanProperty("isFinal", false);
        int round = wk.getIntegerProperty("round", -1);
        int id = wk.getIntegerProperty("roundId", -1);
        int qualifiedPerHeat = wk.getIntegerProperty("qualifiedPerHeat", -1);

        String disziplin = lauf.getDisziplin();
        if (round >= 0) {
            String heattext = String.format("%03d-%02d%s", id, lauf.getLaufnummer(), Converter.characterString(lauf.getLaufbuchstabe()));
            disziplin = lauf.getDisziplin() + " - " + I18n.getRound(round, isFinal) + " - " + heattext;
        }

        boolean isOpenwater = FormelManager.isOpenwater(wk.getRegelwerk().getFormelID());
        String qualified = "";
        if (round >= 0 && !isFinal && isOpenwater) {
            qualified = " (" + I18n.get("QualifiedAmount", qualifiedPerHeat) + ")";
        }

        panel.add(createLabel(I18n.get("HeatNr", lauf.getName(), 1) + qualified), CC.xy(2, 2));
        panel.add(createLabel(lauf.getStartgruppe(), SwingConstants.CENTER), CC.xy(4, 2));
        panel.add(createLabel(disziplin, SwingConstants.RIGHT), CC.xy(6, 2));
        panel.add(getTablePanel(wk, lauf, lanes, showDisciplines, showTimes, showQuali, showOrganisation), CC.xyw(2, 4, 5, "fill,fill"));

        return panel;
    }

    private static JLabel createLabel(String name) {
        return createLabel(name, SwingConstants.LEFT);
    }

    private static JLabel createLabel(String name, int align) {
        JLabel l = new JLabel(name);
        if (PrintManager.getFont() != null) {
            l.setFont(PrintManager.getFont());
        }
        l.setHorizontalAlignment(align);
        return l;
    }

    /**
     * @param lauf
     * @return
     */
    private static <T extends ASchwimmer> JPanel getTablePanel(AWettkampf<T> wk, Lauf<T> lauf, boolean[] lanes, boolean showDisciplines, boolean showTimes,
            boolean showQuali, boolean showOrganisation) {
        ArrayList<Tupel<T, Object[]>> swimmers = new ArrayList<Tupel<T, Object[]>>();

        boolean isMultiline = wk.isMultiline();

        for (int x = 0; x < lauf.getBahnen(); x++) {
            Object[] data = new Object[8];
            T s = lauf.getSchwimmer(x);
            if (s != null) {
                data[0] = StartnumberFormatManager.format(s);
                Mannschaft m = isMultiline && s instanceof Mannschaft ? (Mannschaft) s : null;
                if (m != null) {
                    if (s.getName().equals(s.getGliederung())) {
                        data[1] = m.getStarterShort(lauf.getDisznummer(x), ", ");
                    } else {
                        data[1] = data[1] = I18n.get("TeamnameMultiline", s.getName(), m.getStarterShort(lauf.getDisznummer(x), ", "));
                    }
                } else {
                    data[1] = s.getName();
                }
                data[2] = s.getGliederung();
                data[3] = s.getQualifikationsebene();
                String ak = s.getAK().toString();
                if (ak.toLowerCase().startsWith("ak ")) {
                    ak = ak.substring(3);
                }
                data[4] = ak + " " + I18n.geschlechtToShortString(s);
                data[5] = I18n.getDisziplinShort(s.getAK().getDisziplin(lauf.getDisznummer(x), s.isMaennlich()).toString());
                data[6] = StringTools.zeitString(s.getMeldezeit(lauf.getDisznummer(x)));
                if (lauf.getBahnen() > 24) {
                    data[7] = "               ";
                } else {
                    data[7] = "<html><body>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;</body></html>";
                }

                swimmers.add(new Tupel<T, Object[]>(s, data));
            }
        }

        swimmers.sort(new Comparator<Tupel<T, Object[]>>() {
            @Override
            public int compare(Tupel<T, Object[]> o1, Tupel<T, Object[]> o2) {
                return o1.getFirst().getStartnummer() - o2.getFirst().getStartnummer();
            }
        });

        LinkedList<Object[]> datas = new LinkedList<Object[]>();

        for (Tupel<T, Object[]> tupel : swimmers) {
            datas.add(tupel.getSecond());
        }

        Object[] titles = new Object[] { I18n.get("StartnumberShort"), I18n.get("Name"), I18n.get("Organisation"), I18n.get("QualifikationsebeneShort"),
                I18n.get("AgeGroupShort"), I18n.get("Discipline", ""), I18n.get("Meldezeit"), I18n.get("Rank") };
        JTable table = new JTable(datas.toArray(new Object[datas.size()][0]), titles);
        if (!showTimes) {
            JTableUtils.hideColumnAndRemoveData(table, 6);
        }
        if (!showDisciplines) {
            JTableUtils.hideColumnAndRemoveData(table, 5);
        }
        if (!showQuali) {
            JTableUtils.hideColumnAndRemoveData(table, 3);
        }
        if (!showOrganisation) {
            JTableUtils.hideColumnAndRemoveData(table, 2);
        }
        if (PrintManager.getFont() != null) {
            table.getTableHeader().setFont(PrintManager.getFont());
            table.setFont(PrintManager.getFont());
        }
        JTableUtils.setPreferredCellWidths(table);
        JTableUtils.setTableCellRenderer(table, new AlignmentCellRenderer(new int[] { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
                SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.RIGHT }, SwingConstants.LEFT));
        JPrintTable.initPrintableJTable(table);
        JTableUtils.setPreferredCellSizes(table);

        FormLayout layout = new FormLayout("fill:default:grow", "fill:default,fill:default");

        JPanel tablePanel = new JPanel(layout);
        tablePanel.setBorder(new ExtendedLineBorder(Color.BLACK, 2));
        tablePanel.add(table.getTableHeader(), CC.xy(1, 1));
        tablePanel.add(table, CC.xy(1, 2));
        return tablePanel;
    }
}