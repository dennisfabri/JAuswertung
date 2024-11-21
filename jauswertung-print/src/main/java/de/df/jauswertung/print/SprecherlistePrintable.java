/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.renderer.AlignmentCellRenderer;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ComponentPackingPrintable;
import de.df.jutils.util.StringTools;

import static de.df.jauswertung.daten.PropertyConstants.*;

public final class SprecherlistePrintable<T extends ASchwimmer> extends ComponentPackingPrintable {

    public SprecherlistePrintable(AWettkampf<T> wk, boolean showDisciplines, boolean showTimes, boolean showQuali,
            boolean showNotes, boolean showOrganisation,
            boolean showYearOfBirth) {
        super(5, 10, false,
                getPanels(wk, showDisciplines, showTimes, showQuali, showNotes, showOrganisation || wk.isEinzel(),
                        showYearOfBirth && wk.isEinzel(),
                        wk.isMultiline()));
    }

    private static boolean sindMannschaftsnamenEindeutig(MannschaftWettkampf mwk) {
        if (mwk.getAnzahlGliederungen() != mwk.getGliederungenMitQGliederung().size()) {
            return false;
        }
        for (Mannschaft m : mwk.getSchwimmer()) {
            if (!m.getName().contains(m.getGliederung())) {
                return false;
            }
            if (m.getMannschaftsmitgliederAnzahl() > 0) {
                return false;
            }
        }
        return true;
    }

    private static <T extends ASchwimmer> Component[] getPanels(AWettkampf<T> wk, boolean showDisciplines,
            boolean showTimes, boolean showQuali,
            boolean showNotes, boolean showOrganisation, boolean showYearOfBirth, boolean isMultiline) {
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

        if (showTimes) {
            showTimes = !FormelManager.isOpenwater(wk.getRegelwerk().getFormelID());
        }

        if (showOrganisation) {
            if (wk instanceof MannschaftWettkampf) {
                MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
                showOrganisation = !sindMannschaftsnamenEindeutig(mwk);
            }
        }

        LinkedList<Component> components = new LinkedList<>();
        int index = 0;
        for (Lauf<T> lauf : laufliste) {
            components.addLast(getPanel(wk, lauf, lanes, showDisciplines, showTimes, showQuali, showNotes,
                    showOrganisation, showYearOfBirth, isMultiline,
                    index, laufliste.size()));
            index++;
        }
        return components.toArray(new Component[0]);
    }

    private static <T extends ASchwimmer> JPanel getPanel(AWettkampf<T> wk, Lauf<T> lauf, boolean[] lanes,
            boolean showDisciplines, boolean showTimes,
            boolean showQuali, boolean showNotes, boolean showOrganisation, boolean showYearOfBirth,
            boolean isMultiline, int heatIndex, int amountOfHeats) {
        FormLayout layout = new FormLayout("1dlu,fill:default:grow,1dlu,fill:default:grow,1dlu,fill:default:grow,1dlu",
                "1dlu,fill:default,1dlu,fill:default,4dlu");
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();
        boolean showSN = wk.isOpenWater();
        boolean isFinal = wk.getBooleanProperty(IS_FINAL, false);
        int round = wk.getIntegerProperty(ROUND, -1);
        int id = wk.getIntegerProperty(ROUND_ID, -1);
        int qualifiedPerHeat = wk.getIntegerProperty(QUALIFIED_PER_HEAT, -1);

        String disziplin = lauf.getDisziplin();
        if (round >= 0) {
            String heatText = String.format("%03d-%02d%s", id, lauf.getLaufnummer(),
                    StringTools.characterString(lauf.getLaufbuchstabe()));
            disziplin = lauf.getDisziplin() + " - " + I18n.getRound(round, isFinal) + " - " + heatText;
        }

        boolean isOpenwater = FormelManager.isOpenwater(wk.getRegelwerk().getFormelID());
        String qualified = "";
        if (round >= 0 && !isFinal && isOpenwater) {
            qualified = " (" + I18n.get("QualifiedAmount", qualifiedPerHeat) + ")";
        }

        String laufname = "";
        if (isFinal) {
            laufname = I18n.get("FinalNr", StringTools.asText(amountOfHeats - heatIndex - 1));
        } else {
            laufname = I18n.get("HeatNr", lauf.getName(scheme), 1) + qualified;
        }

        panel.add(createLabel(laufname), CC.xy(2, 2));
        panel.add(createLabel(lauf.getStartgruppe(), SwingConstants.CENTER), CC.xy(4, 2));
        panel.add(createLabel(disziplin, SwingConstants.RIGHT), CC.xy(6, 2));
        panel.add(
                getTablePanel(lauf, lanes, showDisciplines, showTimes, showQuali, showNotes, showOrganisation,
                        showYearOfBirth, isMultiline, showSN),
                CC.xyw(2, 4, 5, "fill,fill"));

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
    private static <T extends ASchwimmer> JPanel getTablePanel(Lauf<T> lauf, boolean[] lanes, boolean showDisciplines,
            boolean showTimes, boolean showQuali,
            boolean showNotes, boolean showOrganisation, boolean showYearOfBirth, boolean isMultiline, boolean showSN) {
        LinkedList<Object[]> datas = new LinkedList<>();
        for (int x = 0; x < lauf.getBahnen(); x++) {
            if (PrintUtils.printEmptyLanes || lanes[x]) {
                Object[] data = new Object[9];
                data[0] = (x + 1);
                ASchwimmer s = lauf.getSchwimmer(x);
                if (s == null) {
                    if (showSN) {
                        data[0] = " ";
                    }
                    for (int y = 1; y < data.length; y++) {
                        data[y] = " ";
                    }
                } else {
                    Teilnehmer t = s instanceof Teilnehmer ? (Teilnehmer) s : null;
                    Mannschaft m = isMultiline && s instanceof Mannschaft ? (Mannschaft) s : null;
                    if (showSN) {
                        data[0] = StartnumberFormatManager.format(s);
                    }
                    if (m != null) {
                        if (s.getName().equals(s.getGliederung()) && showOrganisation) {
                            data[1] = m.getStarterShort(lauf.getDisznummer(x), ", ");
                        } else {
                            data[1] = I18n.get("TeamnameMultiline", s.getName(),
                                    m.getStarterShort(lauf.getDisznummer(x), ", "));
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
                    data[5] = t != null ? I18n.yearToShortString(t.getJahrgang()) + " " : "";
                    data[6] = I18n.getDisziplinShort(
                            s.getAK().getDisziplin(lauf.getDisznummer(x), s.isMaennlich()).toString());
                    data[7] = StringTools.zeitString(s.getMeldezeit(lauf.getDisznummer(x)));
                    data[8] = "                ";
                }
                datas.addLast(data);
            }
        }
        Object[] titles = new Object[] { showSN ? I18n.get("StartnumberShort") : I18n.get("Lane"), I18n.get("Name"),
                I18n.get("Organisation"),
                I18n.get("QualifikationsebeneShort"),
                I18n.get("AgeGroupShort"), I18n.get("YearOfBirthShort"), I18n.get("Discipline", ""),
                I18n.get("Meldezeit"), I18n.get("Notes") };
        JTable table = new JTable(datas.toArray(new Object[datas.size()][0]), titles);
        if (!showNotes) {
            JTableUtils.hideColumnAndRemoveData(table, 8);
        }
        if (!showTimes) {
            JTableUtils.hideColumnAndRemoveData(table, 7);
        }
        if (!showDisciplines) {
            JTableUtils.hideColumnAndRemoveData(table, 6);
        }
        if (!showYearOfBirth) {
            JTableUtils.hideColumnAndRemoveData(table, 5);
        }
        if (!showQuali || !showOrganisation) {
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
        JTableUtils.setTableCellRenderer(table,
                new AlignmentCellRenderer(new int[] { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
                        SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT,
                        SwingConstants.RIGHT }, SwingConstants.LEFT));
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