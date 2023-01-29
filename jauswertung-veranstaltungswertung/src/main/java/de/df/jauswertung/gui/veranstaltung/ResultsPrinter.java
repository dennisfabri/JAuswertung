package de.df.jauswertung.gui.veranstaltung;

import java.awt.Font;
import java.awt.print.Printable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.util.TableUtils;
import de.df.jauswertung.util.GesamtwertungWettkampf;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jtable.ColumnGroup;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.GroupableTableHeader;
import de.df.jutils.gui.jtable.JGroupableTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
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
    JCheckBox resultcompact;
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
        resultcompact = new JCheckBox(I18n.get("CompactView"));
        resultcompact.setSelected(true);
        resultcompact.setVisible(false);

        resultmodus = new JComboBox<>(new String[] { I18n.get("Organisation"), I18n.get("Qualifikationsebene") });

        panel = new JPanel(new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1, 4, 0)));

        panel.add(resultcompact, CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("Print.WertungNach")), CC.xy(4, 2));
        panel.add(resultmodus, CC.xy(6, 2));
        panel.add(resultpreview, CC.xy(8, 2));
        panel.add(resultprint, CC.xy(10, 2));
    }

    @SuppressWarnings({ "unchecked", "null", "rawtypes" })
    public static <T extends ASchwimmer> Printable getGesamtwertungPrintable(AWettkampf<T> wk, Veranstaltung vs,
            boolean gliederung) {

        AWettkampf[] wks = Veranstaltungsutils.getWettkaempfe(vs.getCompetitions());
        String[] nx = vs.getCompetitionNames();
        LinkedList<Integer> sizes = new LinkedList<>();
        LinkedList<String> names = new LinkedList<>();
        LinkedList<String> sexes = new LinkedList<>();
        for (int x = 0; x < wks.length; x++) {
            int amount = 0;
            if (wks[x] != null) {
                if (!gliederung) {
                    ListIterator<ASchwimmer> li = wks[x].getSchwimmer().listIterator();
                    while (li.hasNext()) {
                        ASchwimmer s = li.next();
                        if (s.getQualifikationsebene().trim().length() == 0) {
                            li.remove();
                        } else {
                            s.setGliederung(s.getQualifikationsebene());
                        }
                    }
                }
                for (int y = 0; y < wks[x].getRegelwerk().size(); y++) {
                    if (SearchUtils.hasSchwimmer(wks[x], wks[x].getRegelwerk().getAk(y))) {
                        amount++;
                        sexes.add(wks[x].getRegelwerk().getTranslation("femaleShort", I18n.get("sex1Short")));
                        sexes.add(wks[x].getRegelwerk().getTranslation("maleShort", I18n.get("sex2Short")));
                    }
                }
            }
            if (amount > 0) {
                sizes.addLast(amount);
                names.addLast(nx[x]);
            }
        }
        if (sizes.isEmpty()) {
            return EmptyPrintable.Instance;
        }

        TableModel tm = TableUtils.buildGesamtwertungsergebnis(wk, false, sexes.toArray(new String[sexes.size()]));
        if (tm == null || tm.getRowCount() == 0) {
            return EmptyPrintable.Instance;
        }

        ListIterator<Integer> lis = sizes.listIterator();
        ListIterator<String> lin = names.listIterator();
        int size = 0;
        String name = "";

        JGroupableTable t = new JGroupableTable(tm);
        if (tm instanceof ExtendedTableModel) {
            ExtendedTableModel etm = (ExtendedTableModel) tm;
            GroupableTableHeader gth = (GroupableTableHeader) t.getTableHeader();

            ColumnGroup cgx = null;
            for (int x = 0; x < (etm.getColumnCount() - 3) / 2; x++) {
                if (size <= 0) {
                    if (cgx != null) {
                        gth.addColumnGroup(cgx);
                    }

                    size = lis.next();
                    name = lin.next();
                    cgx = new ColumnGroup(name);
                }
                int ak = Integer.parseInt(etm.getExtendedTitleRows()[0].cells[3 + x].title);
                ColumnGroup cg = new ColumnGroup(wk.getRegelwerk().getAk(ak).getName());
                cg.add(t.getColumnModel().getColumn(3 + 2 * x));
                cg.add(t.getColumnModel().getColumn(3 + 2 * x + 1));

                cgx.add(cg);

                size--;
            }
            if (cgx != null) {
                gth.addColumnGroup(cgx);
            }
        }

        JTableUtils.setAlignmentRenderer(t,
                new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.RIGHT }, SwingConstants.RIGHT);

        return PrintManager.getPrintable(t, "", JTablePrintable.OPT_ALL, true, true);
    }

    @SuppressWarnings("rawtypes")
    Printable getPrintableCompact() {
        Veranstaltung vs = parent.getVeranstaltung();
        boolean gliederungen = resultmodus.getSelectedIndex() == 0;
        AWettkampf wk = Veranstaltungsutils.veranstaltung2Wettkampf(vs, gliederungen);
        Font f = parent.getSelectedFont();
        String title = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
        return getPrintableCompact(vs, wk, title, f);
    }

    @SuppressWarnings({ "unchecked" })
    static Printable getPrintableCompact(Veranstaltung vs, @SuppressWarnings("rawtypes") AWettkampf wk, String title,
            Font f) {
        if (f != null) {
            PrintManager.setFont(f);
        }

        Printable p = null;

        if (wk != null) {
            p = getGesamtwertungPrintable(wk, vs, true);
        }
        if (p == null) {
            return EmptyPrintable.Instance;
        }
        if (p instanceof EmptyPrintable) {
            return p;
        }
        HeaderFooterPrintable hfp = new HeaderFooterPrintable(p, new MessageFormat(title), null, f);
        hfp.setHeader(new MessageFormat(vs.getName()), HeaderFooterPrintable.LEFT);
        hfp.setHeader(new MessageFormat(vs.getLocationAndDate()), HeaderFooterPrintable.RIGHT);
        p = hfp;
        return PrintManager.getFinalPrintable(p, new Date(), (String) null, I18n.get("GroupEvaluation"));
    }

    Printable getPrintableNormal() {
        Font f = parent.getSelectedFont();
        if (f != null) {
            PrintManager.setFont(f);
        }

        Printable p = null;

        Veranstaltung vs = parent.getVeranstaltung();
        boolean gliederungen = resultmodus.getSelectedIndex() == 0;
        p = veranstaltung2Printable(vs, gliederungen);
        if (p == null) {
            return EmptyPrintable.Instance;
        }
        if (p instanceof EmptyPrintable) {
            return p;
        }
        String title = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
        HeaderFooterPrintable hfp = new HeaderFooterPrintable(p, new MessageFormat(title), null, f);
        hfp.setHeader(new MessageFormat(vs.getName()), HeaderFooterPrintable.LEFT);
        hfp.setHeader(new MessageFormat(vs.getLocationAndDate()), HeaderFooterPrintable.RIGHT);
        p = hfp;
        return PrintManager.getFinalPrintable(p, new Date(), (String) null, I18n.get("GroupEvaluation"));

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(AWettkampf wk) {
        // Nothing to do
    }

    @Override
    public String getName() {
        return I18n.get("Results");
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    Printable getPrintable(boolean compact) {
        if (compact) {
            return getPrintableCompact();
        }
        return getPrintableNormal();
    }

    void print() {
        Printable p = getPrintable(resultcompact.isSelected());
        if (p == null) {
            DialogUtils.inform(parent, I18n.get("NoDataToPrint"), I18n.get("NoDataToPrint.Note"));
            return;
        }
        PrintExecutor.print(p, I18n.get("GroupEvaluation"), parent);
    }

    void preview() {
        parent.setEnabled(false);
        // if (getPrintable(compact.isSelected()) == null) {
        // DialogUtils.normaleMeldung(parent, I18n.get("NoDataToPrint"));
        // return;
        // }
        PrintableCreator pc = null;
        if (resultcompact.isSelected()) {
            pc = new CompactPrintableCreator();
        } else {
            pc = new NormalPrintableCreator();
        }
        parent.setEnabled(true);
        PrintExecutor.preview(parent, pc, I18n.get("GroupEvaluation"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Printable veranstaltung2Printable(Veranstaltung vs, boolean gliederungen) {
        AWettkampf[] wks = Veranstaltungsutils.getWettkaempfe(vs.getCompetitions());
        if (!gliederungen) {
            for (AWettkampf wk : wks) {
                if (wk != null) {
                    LinkedList<ASchwimmer> swimmers = wk.getSchwimmer();
                    for (ListIterator<ASchwimmer> li = swimmers.listIterator(); li.hasNext();) {
                        ASchwimmer s = li.next();
                        if (s.getQualifikationsebene().length() == 0) {
                            li.remove();
                        } else {
                            s.setGliederung(s.getQualifikationsebene());
                        }
                    }
                }
            }
        }

        GesamtwertungWettkampf[] gws = new GesamtwertungWettkampf[wks.length];
        for (int x = 0; x < wks.length; x++) {
            if (wks[x] != null) {
                Regelwerk aks = wks[x].getRegelwerk();
                aks.setGesamtwertung(true);
                aks.setGesamtwertungHart(vs.isGesamtwertungHart());
                aks.setGesamtwertungSkalieren(vs.getGesamtwertungSkalieren());
                aks.setGesamtwertungsmodus(vs.getGesamtwertungsmodus());
                gws[x] = new GesamtwertungWettkampf(wks[x]);
            }
        }

        String[] titles = vs.getCompetitionNames();
        MultiplePrintable mp = new MultiplePrintable();
        for (int x = 0; x < wks.length; x++) {
            if (wks[x] != null)
                mp.add(PrintUtils.getGesamtwertungPrintable(wks[x], titles[x]));
        }

        return mp;
    }

    private final class CompactPrintableCreator implements PrintableCreator {

        @SuppressWarnings("rawtypes")
        private final AWettkampf wk;
        private final Veranstaltung vs;
        private final String t;
        private final Font f;

        public CompactPrintableCreator() {
            long start = System.currentTimeMillis();
            vs = parent.getVeranstaltung();
            System.out.println("Veranstaltung: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            boolean gliederungen = resultmodus.getSelectedIndex() == 0;
            wk = Veranstaltungsutils.veranstaltung2Wettkampf(vs, gliederungen);
            System.out.println("Wettkämpfe: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            f = parent.getSelectedFont();
            System.out.println("Font: " + (System.currentTimeMillis() - start));
            t = gliederungen ? vs.getTitleOrganization() : vs.getTitleQualifikationsebene();
            System.out.println("Title: " + (System.currentTimeMillis() - start));
        }

        @Override
        public Printable create() {
            return getPrintableCompact(vs, wk, t, f);
        }
    }

    final class NormalPrintableCreator implements PrintableCreator {
        @Override
        public Printable create() {
            return getPrintableNormal();
        }
    }
}