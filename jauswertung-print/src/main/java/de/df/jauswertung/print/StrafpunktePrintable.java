/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ComponentPagePrintable;

public final class StrafpunktePrintable<T extends ASchwimmer> implements Printable {

    private T schwimmer = null;
    private AWettkampf<T> wettkampf = null;
    private int lane = -1;
    private String heat = null;
    private Strafe strafe = null;
    private String disziplintext = "";

    public StrafpunktePrintable(AWettkampf<T> wk, T s, int disziplin, Strafe str) {
        wk = Utils.copy(wk);

        schwimmer = SearchUtils.getSchwimmer(wk, s);
        strafe = str;
        wettkampf = wk;

        if (disziplin == ASchwimmer.DISCIPLINE_NUMBER_SELF) {
            disziplintext = "";
        } else {
            disziplintext = schwimmer.getAK().getDisziplinenNamen()[disziplin];
        }

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        LinkedList<Lauf<T>> heats = wk.getLaufliste().getLaufliste();
        if ((disziplin != ASchwimmer.DISCIPLINE_NUMBER_SELF) && (heats != null) && (heats.size() > 0)) {
            ListIterator<Lauf<T>> iterator = heats.listIterator();
            while (iterator.hasNext()) {
                Lauf<T> current = iterator.next();
                for (int x = 0; x < current.getBahnen(); x++) {
                    T t = current.getSchwimmer(x);
                    if (s.equals(t)) {
                        if (current.getDisznummer(x) == disziplin) {
                            lane = (x + 1);
                            heat = current.getName(scheme);
                        }
                    }
                }
            }
        }
    }

    public StrafpunktePrintable(AWettkampf<T> wk, T s, String id, Strafe str) {
        wk = Utils.copy(wk);

        schwimmer = SearchUtils.getSchwimmer(wk, s);
        strafe = str;
        wettkampf = wk;
        // int disziplin = 0;

        disziplintext = I18n.getDisciplineFullName(wk, id);

        OWDisziplin<T> d = wk.getLauflisteOW().getDisziplin(id);

        LinkedList<OWLauf<T>> heats = d.getLaeufe();
        if ((heats != null) && (heats.size() > 0)) {
            ListIterator<OWLauf<T>> iterator = heats.listIterator();
            while (iterator.hasNext()) {
                OWLauf<T> current = iterator.next();
                for (int x = 0; x < current.getBahnen(); x++) {
                    T t = current.getSchwimmer(x);
                    if (s.equals(t)) {
                        lane = (x + 1);
                        heat = current.getName();
                    }
                }
            }
        }
    }

    static JLabel getUnderline() {
        JLabel label = new JLabel();
        label.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return label;
    }

    /**
     * getPanel() creates a JPanel that includes all data of the penalty and fields
     * needed for the formal workflow.
     * 
     * @return returns panel displaying the data
     */
    private JPanel getPanel() {
        final int rows = 40;

        // Create Layout
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createSpacingLayoutString(rows, 2));

        layout.setRowGroups(createGroups(rows));
        layout.setColumnGroups(new int[][] { { 4, 6, 8 } });

        JPanel panel = new JPanel(layout) {
            private static final long serialVersionUID = 1L;

            @Override
            public void add(Component comp, Object constraints) {
                Font font = PrintManager.getFont();
                if (font != null) {
                    comp.setFont(font);
                }
                super.add(comp, constraints);
            }
        };
        PanelBuilder pb = new PanelBuilder(layout, panel);

        int y = 2;

        // Add general Information
        pb.addLabel(I18n.get("Competition") + ": ", CC.xyw(2, y, 3));
        pb.addLabel(wettkampf.getStringProperty(NAME), CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Location") + ": ", CC.xyw(2, y, 3));
        pb.addLabel(wettkampf.getStringProperty(LOCATION), CC.xyw(6, y, 5));

        // Add protocoll data
        y += 4;
        pb.addLabel(I18n.get("Penaltyprotocoll"), CC.xyw(2, y, 9, "center,center"));

        y += 4;
        pb.addLabel("1.", CC.xy(2, y));
        pb.addLabel(I18n.get("Penaltyregistration"), CC.xyw(4, y, 7));
        y += 2;
        pb.addLabel(I18n.get("AgeGroup") + ":", CC.xy(4, y));
        pb.addLabel(schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer), CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Name") + ":", CC.xy(4, y));
        pb.addLabel(schwimmer.getName(), CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Organisation") + ":", CC.xy(4, y));
        pb.addLabel(schwimmer.getGliederung(), CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Discipline") + ":", CC.xy(4, y));
        pb.addLabel(disziplintext, CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Heat") + ":", CC.xy(4, y));
        pb.addLabel(I18n.get("Lane") + ":", CC.xy(8, y));
        if (heat != null) {
            pb.addLabel(heat, CC.xy(6, y));
            pb.addLabel("" + lane, CC.xy(10, y));
        }

        y += 2;
        pb.addLabel(I18n.get("Paragraph") + ":", CC.xy(4, y));
        pb.addLabel(strafe.getShortname(), CC.xyw(6, y, 5));
        y += 2;
        pb.addLabel(I18n.get("Penalty") + ":", CC.xy(4, y));
        pb.addLabel(PenaltyUtils.getPenaltyText(strafe, schwimmer.getAK()), CC.xyw(6, y, 5));
        y += 2;

        JTextArea text = new JTextArea(strafe.getName());
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setBorder(null);
        pb.addLabel(I18n.get("Description") + ":", CC.xy(4, y));
        pb.add(text, CC.xywh(6, y, 5, 5));
        y += 8;
        pb.addLabel(I18n.get("Signature"), CC.xy(4, y));
        pb.add(getUnderline(), CC.xyw(6, y, 5));

        // Add Anouncement fields
        y += 4;
        pb.addLabel("2.", CC.xy(2, y));
        pb.addLabel(I18n.get("Announcement"), CC.xyw(4, y, 7));
        y += 4;
        pb.addLabel(I18n.get("TimeOfDay") + ":", CC.xy(4, y));
        pb.add(getUnderline(), CC.xy(6, y));
        pb.addLabel(I18n.get("Signature") + ":", CC.xy(8, y));
        pb.add(getUnderline(), CC.xy(10, y));

        // Add apeal fields
        y += 4;
        pb.addLabel("3.", CC.xy(2, y));
        pb.addLabel(I18n.get("Appeal"), CC.xyw(4, y, 7));
        y += 2;
        pb.addLabel("(  ) " + I18n.get("No"), CC.xy(4, y));
        pb.addLabel("(  ) " + I18n.get("Yes"), CC.xy(6, y));
        pb.addLabel("(  ) " + I18n.get("Attatchments") + ":", CC.xy(8, y));
        y += 4;
        pb.addLabel(I18n.get("TimeOfDay") + ":", CC.xy(4, y));
        pb.add(getUnderline(), CC.xy(6, y));
        pb.addLabel(I18n.get("Signature") + ":", CC.xy(8, y));
        pb.add(getUnderline(), CC.xy(10, y));

        // Add apeal decision fields
        y += 4;
        pb.addLabel("4.", CC.xy(2, y));
        pb.addLabel(I18n.get("AppealAccepted"), CC.xyw(4, y, 7));
        y += 2;
        pb.addLabel("(  ) " + I18n.get("No"), CC.xy(4, y));
        pb.addLabel("(  ) " + I18n.get("Yes"), CC.xy(6, y));
        y += 4;
        pb.addLabel(I18n.get("TimeOfDay") + ":", CC.xy(4, y));
        pb.add(getUnderline(), CC.xy(6, y));
        pb.addLabel(I18n.get("Signature") + ":", CC.xy(8, y));
        pb.add(getUnderline(), CC.xy(10, y));

        // Add apeal decision anouncement fields
        y += 4;
        pb.addLabel("5.", CC.xy(2, y));
        pb.addLabel(I18n.get("Announcement"), CC.xyw(4, y, 7));
        y += 4;
        pb.addLabel(I18n.get("TimeOfDay") + ":", CC.xy(4, y));
        pb.add(getUnderline(), CC.xy(6, y));
        pb.addLabel(I18n.get("Signature") + ":", CC.xy(8, y));
        pb.add(getUnderline(), CC.xy(10, y));

        // Add revision fields
        y += 4;
        pb.addLabel("6.", CC.xy(2, y));
        pb.addLabel(I18n.get("Revision"), CC.xyw(4, y, 7));
        y += 4;
        pb.addLabel(I18n.get("TimeOfDay") + ":", CC.xy(4, y));
        pb.add(getUnderline(), CC.xy(6, y));
        pb.addLabel(I18n.get("Signature") + ":", CC.xy(8, y));
        pb.add(getUnderline(), CC.xy(10, y));

        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        return panel;
    }

    private static int[][] createGroups(int rows) {
        int[][] groups = new int[2][0];
        groups[0] = new int[rows];
        groups[1] = new int[rows + 1];
        for (int x = 0; x < rows; x++) {
            groups[0][x] = (x + 1) * 2;
            groups[1][x] = x * 2 + 1;
        }
        groups[1][rows] = rows * 2 + 1;
        return groups;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int index) throws PrinterException {
        if (index > 0) {
            return NO_SUCH_PAGE;
        }

        ComponentPagePrintable.printComponent(getPanel(), g, pf);
        return PAGE_EXISTS;
    }
}