package de.df.jauswertung.gui.veranstaltung;

import java.awt.Font;
import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.util.TableUtils;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.HeaderFooterPrintable;
import de.df.jutils.print.printables.JTablePrintable;
import de.df.jutils.print.printables.MultiplePrintable;

class ResultsPrinter implements Printer {

    private JButton resultpreview;
    private JButton resultprint;
    JComboBox<String> resultmodus;
    private JPanel panel;

    final JVeranstaltungswertung parent;

    public ResultsPrinter(JVeranstaltungswertung parent) {
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

        panel = new JPanel(new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1, 4, 0)));

        panel.add(new JLabel(I18n.get("Print.WertungNach")), CC.xy(4, 2));
        panel.add(resultmodus, CC.xy(6, 2));
        panel.add(resultpreview, CC.xy(8, 2));
        panel.add(resultprint, CC.xy(10, 2));
    }

    @SuppressWarnings("rawtypes")
    static Printable getGesamtwertungPrintable(Veranstaltungsergebnis ergebnis) {
        AWettkampf wk = ergebnis.wettkampf();
        TableModel tm = TableUtils.buildGesamtwertungsergebnis(wk, false, null);
        if (tm == null || tm.getRowCount() == 0) {
            return EmptyPrintable.Instance;
        }

        JGroupableTable t = new JGroupableTable(tm);
        if (tm instanceof ExtendedTableModel etm) {
            GroupableTableHeader gth = (GroupableTableHeader) t.getTableHeader();
            List<String> wettkaempfe = wettkampfJeSpalte(etm, ergebnis.wettkampfnamen());

            ColumnGroup wettkampf = null;
            for (int x = 0; x < wettkaempfe.size(); x++) {
                if ((wettkampf == null) || !wettkaempfe.get(x).equals(wettkaempfe.get(x - 1))) {
                    if (wettkampf != null) {
                        gth.addColumnGroup(wettkampf);
                    }
                    wettkampf = new ColumnGroup(wettkaempfe.get(x));
                }
                ColumnGroup cg = new ColumnGroup(wk.getRegelwerk().getAk(altersklasse(etm, x)).getName());
                cg.add(t.getColumnModel().getColumn(3 + 2 * x));
                cg.add(t.getColumnModel().getColumn(3 + 2 * x + 1));
                wettkampf.add(cg);
            }
            if (wettkampf != null) {
                gth.addColumnGroup(wettkampf);
            }
        }

        JTableUtils.setAlignmentRenderer(t,
                new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.RIGHT }, SwingConstants.RIGHT);

        return PrintManager.getPrintable(t, "", JTablePrintable.OPT_ALL, true, true);
    }

    /**
     * @return the competition of every age-group column of the overall result table, from left to right
     */
    static List<String> wettkampfJeSpalte(ExtendedTableModel etm, String[] wettkampfnamen) {
        return IntStream.range(0, (etm.getColumnCount() - 3) / 2)
                .mapToObj(x -> wettkampfnamen[altersklasse(etm, x)]).toList();
    }

    private static int altersklasse(ExtendedTableModel etm, int spalte) {
        return Integer.parseInt(etm.getExtendedTitleRows()[0].cells[3 + spalte].title);
    }

    private static Printable getPrintable(Veranstaltung vs, boolean gliederungen, Font f) {
        Veranstaltungsergebnis[] ergebnisse = VeranstaltungsUtils.veranstaltungsergebnisse(vs, gliederungen, true);
        String title = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
        return getPrintable(vs, ergebnisse, title, f);
    }

    private static MultiplePrintable getPrintable(Veranstaltung vs, Veranstaltungsergebnis[] ergebnisse, String title,
            Font f) {
        MultiplePrintable mp = new MultiplePrintable();
        if (ergebnisse == null) {
            return mp;
        }
        for (Veranstaltungsergebnis ergebnis : ergebnisse) {
            if (ergebnis.wettkampf().hasSchwimmer()) {
                mp.add(getPrintable(vs, ergebnis, title, f));
            }
        }
        return mp;
    }

    private static Printable getPrintable(Veranstaltung vs, Veranstaltungsergebnis ergebnis, String title, Font f) {
        if (f != null) {
            PrintManager.setFont(f);
        }

        Printable p = getGesamtwertungPrintable(ergebnis);
        if (p instanceof EmptyPrintable) {
            return p;
        }
        HeaderFooterPrintable hfp = new HeaderFooterPrintable(p, new MessageFormat(title), null, f);
        hfp.setHeader(new MessageFormat(vs.getName()), HeaderFooterPrintable.LEFT);
        hfp.setHeader(new MessageFormat(vs.getLocationAndDate()), HeaderFooterPrintable.RIGHT);
        return PrintManager.getFinalPrintable(hfp, new Date(), (String) null, I18n.get("GroupEvaluation"));
    }

    @Override
    public String getName() {
        return I18n.get("Results");
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    void print() {
        Veranstaltung vs = parent.getVeranstaltung();
        boolean gliederungen = resultmodus.getSelectedIndex() == 0;
        Font f = parent.getSelectedFont();
        PrintExecutor.print(() -> getPrintable(vs, gliederungen, f), I18n.get("GroupEvaluation"), parent);
    }

    void preview() {
        parent.setEnabled(false);
        PrintableCreator pc = new ResultsPrintableCreator();
        parent.setEnabled(true);
        PrintExecutor.preview(parent, pc, I18n.get("GroupEvaluation"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    private final class ResultsPrintableCreator implements PrintableCreator {

        private final Veranstaltungsergebnis[] ergebnisse;
        private final Veranstaltung vs;
        private final String t;
        private final Font f;

        ResultsPrintableCreator() {
            vs = parent.getVeranstaltung();
            boolean gliederungen = resultmodus.getSelectedIndex() == 0;
            ergebnisse = VeranstaltungsUtils.veranstaltungsergebnisse(vs, gliederungen, true);
            f = parent.getSelectedFont();
            t = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
        }

        @Override
        public Printable create() {
            return getPrintable(vs, ergebnisse, t, f);
        }
    }
}
