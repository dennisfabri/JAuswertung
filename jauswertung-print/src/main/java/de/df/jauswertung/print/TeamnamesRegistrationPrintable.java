package de.df.jauswertung.print;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.util.ComponentUtils;
import de.df.jauswertung.util.BarcodeUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.valueobjects.Team;
import de.df.jauswertung.util.valueobjects.Teammember;
import de.df.jutils.gui.JDottingLabel;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ComponentListPrintable;

public class TeamnamesRegistrationPrintable extends ComponentListPrintable {

    public TeamnamesRegistrationPrintable(MannschaftWettkampf wk, String gliederung) {
        this(wk.getStringProperty(PropertyConstants.NAME), wk.getRegelwerk(), getTeams(wk, gliederung), wk.getCurrentTeammembersRegistrationsId());
    }

    private TeamnamesRegistrationPrintable(String title, Regelwerk rw, LinkedList<Mannschaft> teams, String exportId) {
        super(20, -1, false, createPages(title, rw, teams, exportId));
    }

    private static LinkedList<Mannschaft> getTeams(MannschaftWettkampf wk, String gliederung) {
        if (gliederung == null) {
            return wk.getSchwimmer();
        }
        return SearchUtils.getSchwimmer(wk, new String[] { gliederung }, true);
    }

    private static Component[] createPages(String title, Regelwerk rw, LinkedList<Mannschaft> teams, String exportId) {
        Collections.sort(teams, new Comparator<Mannschaft>() {
            @Override
            public int compare(Mannschaft o1, Mannschaft o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                int diff = o1.getQualifikationsebene().compareTo(o2.getQualifikationsebene());
                if (diff != 0) {
                    return diff;
                }
                diff = o1.getGliederung().compareTo(o2.getGliederung());
                if (diff != 0) {
                    return diff;
                }
                diff = o1.getAKNummer() - o2.getAKNummer();
                if (diff != 0) {
                    return diff;
                }
                diff = (o1.isMaennlich() ? 1 : 0) - (o2.isMaennlich() ? 1 : 0);
                if (diff != 0) {
                    return diff;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });

        ArrayList<Component> result = new ArrayList<Component>(teams.size());
        for (Mannschaft m : teams) {
            boolean empty = true;
            for (int i = 0; i < m.getMaxMembers(); i++) {
                if (!m.getMitgliedsname(i).isEmpty()) {
                    empty = false;
                    break;
                }
            }
            if (!empty) {
                result.add(createPage(title, rw, new Team(m, exportId)));
            }
        }
        return result.toArray(new Component[result.size()]);
    }

    private static final float[] levels = new float[] { 1.0f, 1.3f, 1.8f };

    private static JLabel createGrayText(String text, int level) {
        JLabel label = createText(text, level, false);
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    private static JLabel createText(String text, int level) {
        return createText(text, level, false);
    }

    private static JLabel createText(String text, int level, boolean dotted) {
        JLabel l;
        if (dotted) {
            l = new JDottingLabel(text);
        } else {
            l = new JLabel(text);
        }
        l.setBackground(Color.WHITE);
        l.setForeground(Color.BLACK);
        Font f = PrintManager.getDefaultFont();
        if (f != null) {
            if (level > 0) {
                f = f.deriveFont(f.getSize2D() * levels[level]);
            }
            l.setFont(f);
        }
        return l;
    }

    private static Component createSeparator() {
        JLabel l = createText(" ", 0);
        l.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return l;
    }

    private static Component createQRCode(String code) {
        Component c = BarcodeUtils.getQRCode(code);
        c.setPreferredSize(new Dimension(100, 100));
        return c;
    }

    private static Component createQRCode(String title, Team team) {
        JPanel p = new JPanel(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default:grow,0dlu,fill:default,0dlu"));
        p.setOpaque(false);
        p.add(createQRCode(team.getCode()), CC.xy(2, 2));

        if (!team.isExportIdEmpty()) {
            p.add(createText(team.getExportId(), 1), CC.xy(2, 4, "center,center"));
        }

        return p;
    }

    private static Component createPage(String title, Regelwerk rw, Team team) {
        JPanel p = new JPanel(new FormLayout("0dlu,fill:default:grow,0dlu,fill:default,0dlu",
                "0dlu,fill:default,1dlu,fill:default,1dlu,fill:default,1dlu,fill:default,0dlu"));
        p.setBackground(Color.WHITE);
        p.setForeground(Color.BLACK);
        p.add(createHeader(title, rw, team), CC.xy(2, 2));
        p.add(createQRCode(title, team), CC.xy(4, 2));
        p.add(createTable(title, team), CC.xyw(2, 6, 3));
        p.add(createFooter(title, team), CC.xyw(2, 8, 3));
        return p;
    }

    private static Component createHeader(String title, Regelwerk rw, Team team) {
        JPanel p = new JPanel(new FormLayout("0dlu,fill:default:grow,4dlu,fill:default:grow,0dlu", FormLayoutUtils.createLayoutString(4)));
        p.setBackground(Color.WHITE);
        p.setForeground(Color.BLACK);

        if (title == null || title.isEmpty()) {
            title = " ";
        }

        p.add(createText(title, 1, true), CC.xyw(2, 2, 3, "fill,center"));
        p.add(createText("Namentliche Meldung der Mannschaftsmitglieder", 2), CC.xyw(2, 4, 3, "left,center"));

        StringBuilder sb = new StringBuilder();

        sb.setLength(0);
        sb.append(team.getTeamname());

        p.add(createText(sb.toString(), 0), CC.xy(2, 6));
        p.add(createText(I18n.getAgeGroupAsString(rw, team.getAgegroup(), team.isMale()), 0), CC.xy(4, 6));

        sb.setLength(0);
        sb.append(team.getGliederung());
        if (!team.getQualifikationsgliederung().isEmpty()) {
            sb.append(" (");
            sb.append(team.getQualifikationsgliederung());
            sb.append(")");
        }
        p.add(createText(sb.toString(), 0), CC.xy(2, 8));

        sb.setLength(0);
        sb.append("S# ");
        sb.append(team.getId());
        p.add(createText(sb.toString(), 0), CC.xy(4, 8));

        return p;
    }

    private static Component createTable(String title, Team team) {
        String[] titles = new String[] { "#", I18n.get("Surname"), I18n.get("FirstName"), I18n.get("SexShort"), I18n.get("YearOfBirth") };

        Object[][] data = new Object[team.getMemberCount()][0];

        for (int x = 0; x < team.getMemberCount(); x++) {
            Object[] row = new Object[titles.length];
            Teammember tm = team.getMember(x);
            row[0] = "" + (x + 1);
            row[1] = tm.getLastname();
            row[2] = tm.getFirstname();
            row[3] = tm.getGeschlechtAsString();
            row[4] = tm.getJahrgangAsString();

            for (int i = 0; i < row.length; i++) {
                row[i] = "<html>&nbsp;<br>" + row[i] + "</html>";
            }

            data[x] = row;
        }

        DefaultTableModel tm = new DefaultTableModel(data, titles);
        JTable t = new JTable(tm);
        JTableUtils.setAlignmentRenderer(t,
                new int[] { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER },
                SwingConstants.LEFT);
        JTableUtils.setPreferredCellSizes(t);
        JPrintTable.initPrintableJTable(t);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setForeground(Color.BLACK);
        p.add(t.getTableHeader(), BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        p.setBorder(new LineBorder(Color.BLACK, 1));
        return p;
    }

    private static Component createFooter(String title, Team team) {
        JPanel p = new JPanel(new FormLayout("0dlu,fill:default,1dlu,fill:default:grow,4dlu,fill:default,1dlu,fill:default:grow,1dlu,fill:default,0dlu",
                "8dlu,fill:default,8dlu,fill:default,0dlu"));
        p.setBackground(Color.WHITE);
        p.setForeground(Color.BLACK);

        p.add(createText("Handschriftliche Änderungen:", 0), CC.xyw(2, 2, 3));
        p.add(createYesNo(), CC.xyw(6, 2, 3));
        p.add(createGrayText(I18n.getVersion(), 0), CC.xy(10, 2));

        p.add(createText("Datum, Ort:", 0), CC.xy(2, 4));
        p.add(createSeparator(), CC.xy(4, 4));
        p.add(createText("Unterschrift:", 0), CC.xy(6, 4));
        p.add(createSeparator(), CC.xyw(8, 4, 3));

        return p;
    }

    private static Component createYesNo() {
        JPanel p = new JPanel(new FormLayout("0dlu,center:default:grow,1dlu,center:default:grow,0dlu", "0dlu,fill:default,0dlu"));
        p.setBackground(Color.WHITE);
        p.setForeground(Color.BLACK);

        p.add(ComponentUtils.createCheckBox("Ja"), CC.xy(2, 2));
        p.add(ComponentUtils.createCheckBox("Nein"), CC.xy(4, 2));

        return p;
    }

}
