package de.df.jauswertung.gui.veranstaltung;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.GesamtwertungSchwimmer;
import de.df.jauswertung.util.GesamtwertungWettkampf;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.ComponentListPrintable;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.HeaderFooterPrintable;
import de.df.jutils.util.StringTools;

class SiegerlistePrinter implements Printer {

    private JButton resultpreview;
    private JButton resultprint;
    JComboBox<String> resultmodus;
    private JPanel panel;

    final JVeranstaltungswertung parent;

    public SiegerlistePrinter(JVeranstaltungswertung parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        resultpreview = new JButton(I18n.get("Preview"));
        resultpreview.addActionListener(e -> {
            preview();
        });
        resultprint = new JButton(I18n.get("Print"));
        resultprint.addActionListener(e -> {
            print();
        });
        resultmodus = new JComboBox<>(new String[] { I18n.get("Organisation"), I18n.get("Qualifikationsebene") });

        panel = new JPanel(
                new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        FormLayoutUtils.createLayoutString(1, 4, 0)));

        panel.add(new JLabel(I18n.get("Print.WertungNach")), CC.xy(2, 2));
        panel.add(resultmodus, CC.xy(4, 2));
        panel.add(resultpreview, CC.xy(6, 2));
        panel.add(resultprint, CC.xy(8, 2));
    }

    private static Printable getPrintable(GesamtwertungWettkampf gwk, String left, String right, String title, Font f) {
        GesamtwertungSchwimmer[] schwimmer = gwk.getResult();

        int max = -1;

        int place = 0;
        double points = Double.MAX_VALUE;

        for (int z = 0; z < schwimmer.length; z++) {
            if (schwimmer[z].getPunkte() < points - 0.001) {
                place++;
                points = schwimmer[z].getPunkte();
            }
            if ((place > 0) && (place <= 3))
                max = z;
        }

        place = 0;
        points = Double.MAX_VALUE;
        if (max >= 0) {
            max++;
            Object[][] data = new Object[max][3];
            for (int z = 0; z < max; z++) {
                if (schwimmer[z].getPunkte() < points - 0.001) {
                    place++;
                    points = schwimmer[z].getPunkte();
                }
                data[z][0] = place + " ";
                data[z][1] = " " + schwimmer[z].getName() + " ";
                data[z][2] = StringTools.punkteString(schwimmer[z].getPunkte(), false) + "  ";
            }
            ExtendedTableModel etm = new ExtendedTableModel(data,
                    new String[] { I18n.get("Rank"), I18n.get("Name"), I18n.get("Points") });
            etm.setColumnAlignments(new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.RIGHT });
            JComponent parts = getResults(etm);

            Printable p = new ComponentListPrintable(false, parts);

            HeaderFooterPrintable hfp = new HeaderFooterPrintable(p, new MessageFormat(title), null, f);
            hfp.setHeader(new MessageFormat(left), HeaderFooterPrintable.LEFT);
            hfp.setHeader(new MessageFormat(right), HeaderFooterPrintable.RIGHT);
            return PrintManager.getFinalPrintable(hfp, new Date(), (String) null, title);
        }

        return EmptyPrintable.Instance;
    }

    private static JComponent getPseudoTable(ExtendedTableModel etm) {
        JPanel p = new JPanel(new FormLayout(FormLayoutUtils.createGrowingLayoutString(etm.getColumnCount(), 0),
                FormLayoutUtils.createLayoutString(etm.getRowCount() + 1, 1, 0))) {
            @Override
            public void setFont(Font font) {
                super.setFont(font);
                if (font != null) {
                    for (Component c : getComponents()) {
                        c.setFont(font);
                    }
                }
            }

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                if (bg != null) {
                    for (Component c : getComponents()) {
                        c.setBackground(bg);
                    }
                }
            }

            @Override
            public void setForeground(Color bg) {
                super.setForeground(bg);
                if (bg != null) {
                    for (Component c : getComponents()) {
                        c.setForeground(bg);
                    }
                }
            }
        };
        p.setBackground(Color.WHITE);

        Color marker = JPrintTable.getRowMarkerColor();

        for (int y = 0; y < etm.getColumnCount(); y++) {
            JLabel l1 = new JLabel(etm.getColumnName(y));
            l1.setHorizontalAlignment(SwingConstants.CENTER);
            l1.setOpaque(true);
            l1.setBackground(marker);
            l1.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
            p.add(l1, CC.xy(2 + 2 * y, 2));
        }

        for (int x = 0; x < etm.getRowCount(); x++) {
            for (int y = 0; y < etm.getColumnCount(); y++) {
                JLabel l1 = new JLabel(etm.getValueAt(x, y).toString());
                l1.setHorizontalAlignment(etm.getColumnAlignment(y));
                l1.setOpaque(true);
                if (x % 2 != 0) {
                    l1.setBackground(marker);
                } else {
                    l1.setBackground(Color.WHITE);
                }
                p.add(l1, CC.xy(2 + 2 * y, 4 + 2 * x));

                JLabel l2 = new JLabel(" ");
                l2.setOpaque(true);
                if (x % 2 != 0) {
                    l2.setBackground(marker);
                } else {
                    l2.setBackground(Color.WHITE);
                }
                p.add(l2, CC.xy(2 + 2 * y, 4 + 2 * x - 1));
            }
        }

        p.setFont(PrintManager.getFont());
        p.setForeground(Color.BLACK);

        p.setBorder(new LineBorder(Color.BLACK, 1));
        return p;
    }

    private static JComponent getResults(ExtendedTableModel etm) {

        JLabel title = new JLabel(etm.getName());
        title.setFont(PrintManager.getFont());

        FormLayout layout = new FormLayout(FormLayoutUtils.createGrowingLayoutString(1),
                "0dlu,fill:default,1dlu,fill:default,2dlu");
        JComponent c = new JPanel(layout);
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);

        c.add(title, CC.xy(2, 2, "center,center"));
        c.add(getPseudoTable(etm), CC.xy(2, 4));

        return c;
    }

    Printable getPrintable() {
        Veranstaltung vs = parent.getVeranstaltung();
        boolean gliederungen = resultmodus.getSelectedIndex() == 0;
        String title = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
        return getPrintable(vs, Veranstaltungsutils.veranstaltung2Wettkampf(vs, gliederungen), title,
                getSelectedFont());
    }

    @SuppressWarnings("rawtypes")
    Printable getPrintable(Veranstaltung vs, AWettkampf wk, String title, Font f) {
        if (f != null) {
            PrintManager.setFont(f);
        }

        if (!wk.getRegelwerk().hasGesamtwertung()) {
            return EmptyPrintable.Instance;
        }
        return getPrintable(new GesamtwertungWettkampf(wk), vs.getName(), vs.getLocationAndDate(), title, f);
        // return PrintManager.getFinalPrintable(p, new Date(), vs.getName(),
        // I18n
        // .get("GroupEvaluation"));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(AWettkampf wk) {
        Printable p = getPrintable();
        boolean enabled = (p != null);
        resultpreview.setEnabled(enabled);
        resultprint.setEnabled(enabled);
    }

    @Override
    public String getName() {
        return I18n.get("ListOfMedals");
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    void print() {
        Printable p = getPrintable();
        if (p == null) {
            DialogUtils.inform(parent, I18n.get("NoDataToPrint"), I18n.get("NoDataToPrint.Note"));
            return;
        }
        PrintExecutor.print(p, I18n.get("GroupEvaluation"), parent);
    }

    void preview() {
        parent.setEnabled(false);
        PrintableCreator pc = new SiegerlistePrintableCreator();
        parent.setEnabled(true);
        PrintExecutor.preview(parent, pc, I18n.get("GroupEvaluation"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    Font getSelectedFont() {
        Font font = parent.getSelectedFont();
        if (font != null) {
            font = font.deriveFont(font.getSize2D() * 2);
        }
        return font;
    }

    private final class SiegerlistePrintableCreator implements PrintableCreator {

        @SuppressWarnings("rawtypes")
        private final AWettkampf wk;
        private final Veranstaltung vs;
        private final String t;
        private final Font f;

        public SiegerlistePrintableCreator() {
            long start = System.currentTimeMillis();
            vs = parent.getVeranstaltung();
            System.out.println("Veranstaltung: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            boolean gliederungen = resultmodus.getSelectedIndex() == 0;
            wk = Veranstaltungsutils.veranstaltung2Wettkampf(vs, gliederungen);
            System.out.println("Wettkämpfe: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            f = getSelectedFont();
            System.out.println("Font: " + (System.currentTimeMillis() - start));
            t = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
            System.out.println("Title: " + (System.currentTimeMillis() - start));
        }

        @Override
        public Printable create() {
            return getPrintable(vs, wk, t, f);
        }
    }
}