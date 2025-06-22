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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.print.util.JTimeSpacer;
import de.df.jauswertung.util.valueobjects.Startkarte;
import de.df.jutils.gui.JDottingLabel;
import de.df.jutils.gui.JMiddleline;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.AComponentMultiOnPagePrintable;

public final class StartkartenPrintable extends AComponentMultiOnPagePrintable {

    private static final String[][] text = new String[][]{
            {I18n.get("TimeNr", 1) + ":", I18n.get("TimeNr", 2) + ":", I18n.get("TimeNr", 3) + ":"},
            {I18n.get("AutomaticTimeSK"), I18n.get("TimeNr", 1) + ":", I18n.get("TimeNr", 2) + ":"}};

    private Startkarte[] startkarten = null;
    private boolean etime = true;

    public boolean hasEtime() {
        return etime;
    }

    public void setEtime(boolean etime) {
        this.etime = etime;
    }

    public StartkartenPrintable(AWettkampf<?> wk, PageMode mode, boolean empty, boolean allHeats, int minHeat,
                                int maxHeat) {
        this(new AWettkampf[]{wk}, mode, empty, allHeats, minHeat, maxHeat);
    }

    public StartkartenPrintable(AWettkampf<?>[] wks, PageMode mode, boolean empty, boolean allheats, int minheat,
                                int maxheat) {
        super(mode);
        if (wks != null && wks.length > 0) {
            @SuppressWarnings("unchecked")
            LinkedList<Startkarte> sk = SchwimmerUtils.toStartkarten((AWettkampf[]) wks, getPagesPerPage(), empty,
                                                                     allheats, minheat, maxheat);
            if (!sk.isEmpty()) {
                startkarten = sk.toArray(new Startkarte[sk.size()]);
            } else {
                startkarten = new Startkarte[0];
            }
        }
    }

    private static JLabel getUnderline() {
        JLabel label = new JLabel();
        label.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return label;
    }

    private JPanel getPanel(Startkarte s) {
        return EDTUtils.executeOnEDTwithReturn(() -> getPanelI(s, etime));
    }

    static JPanel getPanelI(Startkarte s, boolean etime) {
        FormLayout layout = new FormLayout("0dlu,fill:default:grow,0dlu",
                                           "0dlu,fill:default,1dlu,fill:default,1dlu,fill:default,"
                                                   + "1dlu,fill:default,1dlu,fill:default:grow," + "1dlu,fill:default,1dlu,fill:default,0dlu");
        JPanel panel = new JPanel(layout) {
            private static final long serialVersionUID = 1L;

            @Override
            public void add(Component comp, Object constraints) {
                Font font = PrintManager.getFont();
                if (font != null) {
                    comp.setFont(font);
                }
                comp.setForeground(Color.BLACK);
                super.add(comp, constraints);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        panel.add(createLabel(I18n.get("Startkarte")), CC.xy(2, 2, "center,center"));
        panel.add(new JMiddleline(2), CC.xy(2, 4));
        panel.add(createHeader(s), CC.xy(2, 6));
        panel.add(new JMiddleline(2), CC.xy(2, 8));
        panel.add(createTimepanel(etime), CC.xy(2, 10));
        panel.add(new JMiddleline(2), CC.xy(2, 12));
        panel.add(createLabel(I18n.get("ProgrammerShortInfo")), CC.xyw(1, 14, 3, "right,center"));

        return panel;
    }

    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        Font font = PrintManager.getFont();
        if (font != null) {
            label.setFont(font);
        }
        label.setForeground(Color.BLACK);
        return label;
    }

    private static String[] grow = {":grow", ":grow", ":grow", ":grow", "", "", "", ":grow"};

    private static JPanel createTimepanel(boolean etime) {
        StringBuilder sb = new StringBuilder();
        sb.append("0dlu");
        for (int x = 0; x < grow.length; x++) {
            sb.append(",fill:default");
            sb.append(grow[x]);
            if (x + 1 == grow.length) {
                sb.append(",0dlu");
            } else {
                sb.append(",4dlu");
            }
        }
        FormLayout layout = new FormLayout("0dlu,fill:default,1dlu,fill:default:grow,0dlu", sb.toString());
        // layout.setRowGroups(new int[][] { { 2, 4, 6, 8 } });

        JPanel panel = new JPanel(layout) {
            private static final long serialVersionUID = 1L;

            @Override
            public void add(Component comp, Object constraints) {
                Font font = PrintManager.getFont();
                if (font != null) {
                    comp.setFont(font);
                }
                comp.setForeground(Color.BLACK);
                super.add(comp, constraints);
            }
        };
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        String[] ltext = text[etime ? 1 : 0];

        panel.add(createLabel(ltext[0]), CC.xy(2, 2, "fill,center"));
        panel.add(new JTimeSpacer(true), CC.xy(4, 2));

        panel.add(createLabel(ltext[1]), CC.xy(2, 4, "fill,center"));
        panel.add(new JTimeSpacer(true), CC.xy(4, 4));

        panel.add(createLabel(ltext[2]), CC.xy(2, 6, "fill,center"));
        panel.add(new JTimeSpacer(true), CC.xy(4, 6));

        panel.add(createLabel(I18n.get("OfficialTime") + ":"), CC.xy(2, 8, "fill,center"));
        JTimeSpacer ts = new JTimeSpacer(true);
        ts.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 2), new EmptyBorder(4, 4, 4, 4)));
        panel.add(ts, CC.xy(4, 8));

        panel.add(createLabel(I18n.get("Beanstandungen") + ":"), CC.xy(2, 10));
        panel.add(createLabel(I18n.get("YesNo")), CC.xy(4, 10, "center,fill"));
        panel.add(new JLabel("(Details auf Fehlermeldekarte notieren)") {
            @Override
            public void setFont(Font font) {
                if (font != null) {
                    font = font.deriveFont(Math.round(font.getSize() * 0.8));
                }
                setForeground(Color.BLACK);
                super.setFont(font);
            }
        }, CC.xyw(2, 12, 3, "center,center"));

        panel.add(new JMiddleline(2), CC.xyw(2, 14, 3));
        panel.add(createLabel(I18n.get("Timekeeper") + ":"), CC.xy(2, 16));
        panel.add(getUnderline(), CC.xy(4, 16));

        return panel;
    }

    private static JPanel createHeader(Startkarte s) {
        FormLayout layout = new FormLayout(
                "0dlu,fill:default,1dlu,fill:default:grow,1dlu,fill:default:grow,1dlu,fill:default:grow,0dlu",
                FormLayoutUtils.createLayoutString(4, 1, 0));
        layout.setColumnGroups(new int[][]{{4, 8}});
        layout.setRowGroups(new int[][]{{2, 4, 6, 8}});

        JPanel panel = new JPanel(layout) {
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
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        if (s == null) {
            panel.add(createLabel(I18n.get("AgeGroup") + ":"), CC.xy(2, 4));
            panel.add(createLabel(I18n.get("Discipline") + ":"), CC.xy(2, 6));
            panel.add(createLabel(I18n.get("Heat") + ":"), CC.xy(2, 8));
            panel.add(createLabel(I18n.get("Lane") + ":"), CC.xy(6, 8));

            panel.add(getUnderline(), CC.xyw(2, 2, 7));
            panel.add(getUnderline(), CC.xyw(4, 4, 5));
            panel.add(getUnderline(), CC.xyw(4, 6, 5));
            panel.add(getUnderline(), CC.xy(4, 8));
            panel.add(getUnderline(), CC.xy(8, 8));
        } else {
            panel.add(createDottingLabel(createNameString(s)), CC.xyw(2, 2, 7));
            panel.add(createDottingLabel(s.getAK()), CC.xyw(2, 4, 5));
            panel.add(createLabel(createStartnumberString(s)), CC.xy(8, 4, "right,center"));
            panel.add(createDottingLabel(s.getDisziplin()), CC.xyw(2, 6, 7));
            if (s.getEvent() > 0) {
                panel.add(createLabel(I18n.get("EventNr", s.getEvent())), CC.xyw(2, 8, 3, "fill,fill"));
                panel.add(createLabel(I18n.get("HeatNr", s.getLauf(), 1)), CC.xy(6, 8, "center,fill"));
                panel.add(createLabel(I18n.get("LaneNumber", s.getBahn())), CC.xy(8, 8, "right,fill"));
            } else {
                panel.add(createLabel(I18n.get("HeatNr", s.getLauf(), 1)), CC.xyw(2, 8, 3, "fill,fill"));
                panel.add(createLabel(I18n.get("LaneNumber", s.getBahn())), CC.xyw(6, 8, 3, "fill,fill"));
            }
        }
        return panel;
    }

    private static JLabel createDottingLabel(String text) {
        JDottingLabel label = new JDottingLabel(text);
        Font font = PrintManager.getFont();
        if (font != null) {
            label.setFont(font);
        }
        label.setForeground(Color.BLACK);
        return label;
    }

    private static String createNameString(Startkarte s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.getName());
        boolean covered = s.getName().contains(s.getGliederung());
        boolean qgld = !s.getQualifikationsgliederung().isEmpty()
                && !s.getGliederung().contains(s.getQualifikationsgliederung());
        if (!covered) {
            sb.append(" - ");
        }
        if (!covered) {
            sb.append(s.getGliederung());
        }
        if (qgld) {
            sb.append(" (");
            sb.append(s.getQualifikationsgliederung());
            sb.append(")");
        }

        return sb.toString();
    }

    private static String createStartnumberString(Startkarte s) {
        if (s.getStartnummer().isBlank()) {
            return "";
        }

        return "S# " + s.getStartnummer();
    }

    @Override
    public JComponent getPanel(int page, int offset) {
        if (startkarten == null) {
            return getPanel(null);
        }
        int index = page * getPagesPerPage() + offset;
        if (index < startkarten.length) {
            return getPanel(startkarten[index]);
        }
        return null;
    }

    @Override
    public boolean pageExists(int page) {
        if (startkarten == null) {
            return page == 0;
        }
        int seiten = startkarten.length / getPagesPerPage();
        if ((startkarten.length % getPagesPerPage()) > 0) {
            seiten++;
        }
        return seiten > page;
    }
}
