/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.print.util.ComponentUtils;
import de.df.jauswertung.util.BarcodeType;
import de.df.jauswertung.util.BarcodeUtils;
import de.df.jauswertung.util.BarcodeUtils.ZWResultType;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.valueobjects.ZWStartkarte;
import de.df.jutils.gui.JDottingLabel;
import de.df.jutils.gui.JMiddleline;
import de.df.jutils.gui.JRotatingLabel;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.print.AComponentMultiOnPagePrintable;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;

public final class ZWStartkartenPrintable<T extends ASchwimmer> extends AComponentMultiOnPagePrintable {

    @SuppressWarnings("rawtypes")
    private static final ZWStartkarte[] EMPTY = new ZWStartkarte[0];

    private final ZWStartkarte<T>[] startkarten;
    private final boolean membernames;
    private final BarcodeType type;

    public ZWStartkartenPrintable(PageMode mode) {
        this(null, mode, true, 0, 0, false, false, BarcodeType.NONE);
    }

    @SuppressWarnings("unchecked")
    public ZWStartkartenPrintable(AWettkampf<T> wk, PageMode mode, boolean allheats, int minheat, int maxheat,
            boolean membernames, boolean bylane, BarcodeType type) {
        super(mode);
        wk = Utils.copy(wk);
        this.membernames = membernames;
        if (wk != null) {
            LinkedList<ZWStartkarte<T>> sk = SchwimmerUtils.toZWStartkarten(wk.getHLWListe(), getPagesPerPage(),
                    allheats, minheat, maxheat, bylane);
            if (sk == null) {
                startkarten = EMPTY;
            } else {
                startkarten = sk.toArray(new ZWStartkarte[sk.size()]);
            }
            this.type = type;
        } else {
            startkarten = null;
            this.type = BarcodeType.NONE;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ZWStartkartenPrintable(AWettkampf wk, PageMode mode, boolean membernames, BarcodeType type) {
        super(mode);
        wk = Utils.copy(wk);
        this.membernames = membernames;
        if (wk != null) {
            LinkedList<ZWStartkarte<T>> sk = SchwimmerUtils.toZWStartkarten(wk, getPagesPerPage());
            if (sk == null) {
                startkarten = EMPTY;
            } else {
                startkarten = sk.toArray(new ZWStartkarte[sk.size()]);
            }
            this.type = type;
        } else {
            startkarten = EMPTY;
            this.type = BarcodeType.NONE;
        }
    }

    static JLabel getUnderline() {
        JLabel label = new JLabel(" ");
        label.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return label;
    }

    private JPanel getPanel(ZWStartkarte<T> s) {
        return EDTUtils.executeOnEDTwithReturn(() -> getPanelI(s, membernames, type));
    }

    private static final class JSpecialPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public JSpecialPanel(LayoutManager layout) {
            super(layout);
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
        }

        @Override
        public void add(Component comp, Object constraints) {
            Font font = PrintManager.getFont();
            if (font != null) {
                comp.setFont(font);
            }
            super.add(comp, constraints);
        }
    }

    private static <T extends ASchwimmer> JPanel getSchwimmerPanel(ZWStartkarte<T> s, boolean membernames,
            JComponent dns) {
        if (s != null) {
            return getSchwimmerFilledPanel(s, membernames, dns);
        }
        return getSchwimmerBlankPanel();
    }

    private static <T extends ASchwimmer> JPanel getSchwimmerBlankPanel() {
        FormLayout layout = new FormLayout(
                "0dlu,fill:default,1dlu,fill:default:grow," + "1dlu,fill:default,1dlu,fill:default:grow,0dlu",
                "0dlu,fill:max(default;20dlu)," + "1dlu,fill:max(default;20dlu)," + "1dlu,fill:max(default;20dlu),"
                        + "1dlu,fill:max(default;20dlu),0dlu");
        layout.setColumnGroups(new int[][] { { 4, 8 } });
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8 } });
        JPanel panel = new JSpecialPanel(layout);

        panel.add(new JLabel(I18n.get("AgeGroup") + ":"), CC.xy(2, 6, "left,bottom"));
        panel.add(new JLabel(I18n.get("Heat") + ":"), CC.xy(2, 8, "left,bottom"));
        panel.add(new JLabel(I18n.get("Lane") + ":"), CC.xy(6, 8, "left,bottom"));

        panel.add(getUnderline(), CC.xyw(2, 2, 7));
        panel.add(getUnderline(), CC.xyw(2, 4, 7));
        panel.add(getUnderline(), CC.xyw(4, 6, 5));
        panel.add(getUnderline(), CC.xy(4, 8));
        panel.add(getUnderline(), CC.xy(8, 8));

        return panel;
    }

    private static <T extends ASchwimmer> String createName1String(ZWStartkarte<T> s, boolean membernames) {
        StringBuffer sb = new StringBuffer();
        sb.append(s.getName());
        if (membernames && (s.getMember() != null)) {
            sb.append(" - ");
            sb.append(s.getMember());
        }
        return sb.toString();
    }

    private static <T extends ASchwimmer> String createName2String(ZWStartkarte<T> s, boolean membernames) {
        StringBuffer sb = new StringBuffer();
        boolean qgld = (s.getQualifikationsebene().length() > 0);
        sb.append(s.getGliederung());
        if (qgld) {
            sb.append(" (");
            sb.append(s.getQualifikationsebene());
            sb.append(")");
        }

        return sb.toString();
    }

    private static <T extends ASchwimmer> JPanel getSchwimmerFilledPanel(ZWStartkarte<T> s, boolean membernames,
            JComponent dns) {
        boolean bigger = s.getUhrzeit().length() == 0;
        int rows = 4 + (bigger ? 1 : 0);
        FormLayout layout = new FormLayout("0dlu,fill:default:grow,1dlu,fill:60dlu,1dlu,fill:default,0dlu",
                FormLayoutUtils.createLayoutString(rows, 1, 0));
        int[][] group = new int[1][rows];
        for (int x = 0; x < rows; x++) {
            group[0][x] = 2 + 2 * x;
        }
        layout.setRowGroups(group);
        JPanel panel = new JSpecialPanel(layout);

        String sn = s.getStartnummer();
        String uhrzeit = s.getUhrzeit();
        String bahn = s.getBahn();

        JDottingLabel name1 = new JDottingLabel(createName1String(s, membernames));
        panel.add(name1, CC.xyw(2, 2, 3));
        if (name1.getFont() != null) {
            Font f = name1.getFont();
            f = f.deriveFont((float) (f.getSize2D() * 1.2)).deriveFont(Font.BOLD);
            name1.setFont(f);
        }
        JDottingLabel name2 = new JDottingLabel(createName2String(s, membernames));
        panel.add(name2, CC.xy(2, 4));
        if (name2.getFont() != null) {
            Font f = name2.getFont();
            f = f.deriveFont((float) (f.getSize2D() * 1.2)).deriveFont(Font.BOLD);
            name2.setFont(f);
        }
        JLabel startn = new JLabel(sn);
        panel.add(startn, CC.xy(6, 2));
        if (startn.getFont() != null) {
            Font f = startn.getFont();
            f = f.deriveFont((float) (f.getSize2D() * 1.2)).deriveFont(Font.BOLD);
            startn.setFont(f);
        }
        panel.add(new JDottingLabel(I18n.getAgeGroupAsString(s.getSchwimmer())), CC.xy(2, 6));

        if (uhrzeit != null && uhrzeit.length() > 0) {
            panel.add(new JLabel(uhrzeit + ", " + I18n.get("Lane") + " " + bahn), CC.xy(2, 8 + (bigger ? 2 : 0)));
        }

        if (dns != null) {
            JPanel p = new JPanel(
                    new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default,1dlu,fill:default,0dlu"));
            p.setBackground(Color.WHITE);
            p.setOpaque(true);

            JComponent text = ComponentUtils.createCheckBox(I18n.get("DidNotStart"));
            if (PrintManager.getFont() != null) {
                Font f = PrintManager.getFont();
                f = f.deriveFont((float) (f.getSize2D() * 0.8));
                text.setFont(f);
            }

            p.add(text, CC.xy(2, 2, "center,bottom"));
            p.add(dns, CC.xy(2, 4));

            panel.add(p, CC.xywh(4, 4, 3, 5, "fill,fill"));
        }

        return panel;
    }

    private final static String[] INPUT_STRING = new String[] { "Referee", "Wettkampfleiter", "Schiedsgericht" };

    private static <T extends ASchwimmer> JPanel getInputPanel() {
        FormLayout layout = new FormLayout("0dlu,fill:default,1dlu,center:default,1dlu,fill:default:grow,0dlu",
                "fill:default,1dlu," + FormLayoutUtils.createGrowingLayoutString(3, 1, 0));
        JPanel panel = new JSpecialPanel(layout);

        panel.add(new JLabel(I18n.get("Question.Short.Passed")), CC.xy(4, 1, "center,center"));
        panel.add(new JLabel(I18n.get("Signature")), CC.xy(6, 1, "center,center"));

        for (int x = 0; x < INPUT_STRING.length; x++) {
            panel.add(new JLabel(I18n.get(INPUT_STRING[x])), CC.xy(2, 4 + 2 * x));
            panel.add(new JLabel(I18n.get("yesno")), CC.xy(4, 4 + 2 * x));
            panel.add(getUnderline(), CC.xy(6, 4 + 2 * x));
        }

        return panel;
    }

    private static <T extends ASchwimmer> JPanel getBarcodePanel(ZWStartkarte<T> s, JComponent yes, JComponent no) {
        FormLayout layout = new FormLayout("0dlu,fill:1dlu:grow,1dlu,fill:default,1dlu,fill:1dlu:grow,0dlu",
                "10dlu,fill:default,1dlu,fill:default:grow,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 6 } });
        JPanel panel = new JSpecialPanel(layout);

        panel.add(new JLabel(String.format(" %s ", I18n.get("yes"))), CC.xy(2, 2, "center,center"));
        panel.add(new JLabel(String.format(" %s ",I18n.get("no"))), CC.xy(6, 2, "center,center"));

        panel.add(yes, CC.xy(2, 4));
        panel.add(new JRotatingLabel(I18n.get("Question.Short.Passed")), CC.xy(4, 4, "center,center"));
        panel.add(no, CC.xy(6, 4));

        return panel;
    }

    static <T extends ASchwimmer> JPanel getPanelI(ZWStartkarte<T> s, boolean membernames, BarcodeType type) {
        JComponent yes = null;
        JComponent no = null;
        JComponent dns = null;

        if (s != null) {
            yes = BarcodeUtils.getBarcode(
                    BarcodeUtils.toCode(s.getStartnummerWert(), s.getStarterIndex(), ZWResultType.OK), type);
            no = BarcodeUtils.getBarcode(
                    BarcodeUtils.toCode(s.getStartnummerWert(), s.getStarterIndex(), ZWResultType.NOT_OK), type);
            dns = BarcodeUtils.getBarcode(
                    BarcodeUtils.toCode(s.getStartnummerWert(), s.getStarterIndex(), ZWResultType.DNS), type);
        }

        boolean growall = false;
        boolean barcode = (yes != null) && (no != null);

        StringBuffer rows = new StringBuffer();
        rows.append("0dlu,fill:default");
        rows.append(",1dlu,fill:default");
        rows.append(",1dlu,fill:default");
        if (growall) {
            rows.append(":grow");
        }
        rows.append(",1dlu,fill:default");
        rows.append(",1dlu,fill:default:grow");
        if (barcode) {
            rows.append(",1dlu,fill:default");
        }
        rows.append(",1dlu,fill:default");
        rows.append(",1dlu,fill:default");
        rows.append(",0dlu");

        FormLayout layout = new FormLayout("0dlu,fill:default:grow,0dlu", rows.toString());
        JPanel panel = new JSpecialPanel(layout);

        panel.add(new JLabel(I18n.get("ZWStartkarte")), CC.xy(2, 2, "center,center"));
        panel.add(new JMiddleline(), CC.xy(2, 4));
        panel.add(getSchwimmerPanel(s, membernames, dns), CC.xy(2, 6));
        panel.add(new JMiddleline(), CC.xy(2, 8));
        panel.add(getInputPanel(), CC.xy(2, 10));
        if (barcode) {
            panel.add(getBarcodePanel(s, yes, no), CC.xy(2, 12));
        }
        panel.add(new JMiddleline(), CC.xy(2, 12 + (barcode ? 2 : 0)));

        panel.add(new JLabel(I18n.get("ProgrammerShortInfo")), CC.xy(2, 14 + (barcode ? 2 : 0), "right,center"));

        return panel;
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