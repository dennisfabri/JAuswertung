package de.df.jauswertung.gui.plugins.print;

import static de.df.jutils.util.StringTools.toHtml;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.PropertiesTableCreator;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JIcon;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.ComponentPagePrintable;
import de.df.jutils.print.MultiplePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;
import de.df.jutils.print.RotatingPrintable;

/**
 * @author Dennis Fabri
 * @date 13.07.2005
 */
public class ProtocolPrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JLabel  filter;
    private JButton preview;
    private JLabel  warning;

    public ProtocolPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        print.setEnabled(false);
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("Protocol");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        boolean b = filteredwk.hasSchwimmer();
        print.setEnabled(b);
        preview.setEnabled(b);
        warning.setVisible(b && !filteredwk.isCompetitionComplete());
        filter.setVisible(b && wk.isFilterActive());
    }

    <T extends ASchwimmer> Printable getPrintable() {
        return getPrintable(core.getFilteredWettkampf());
    }

    public static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk) {
        MultiplePrintable mp = new MultiplePrintable();
        mp.add(new FirstPagePrintable(wk));
        mp.add(PrintManager.getHeaderPrintable(PropertiesTableCreator.getPrintable(wk), I18n.get("CompetitionInformation")));
        mp.add(RefereePrinter.getPrintable(wk));
        mp.add(InfopagePrinter.getPrintable(wk));
        if (PrintUtils.printProtocolResultsHorizontal) {
            mp.add(PrintUtils.getResultsPrintable(wk, false, true, false, 0));
            mp.add(PrintUtils.getWertungsgruppenPrintables(wk, false, 0));
        } else {
            mp.add(new RotatingPrintable(PrintUtils.getNormalResultsPrintable(wk, false, true, 0)));
            mp.add(new RotatingPrintable(PrintUtils.getWertungsgruppenPrintables(wk, false, 0)));
        }
        mp.addAll(PrintUtils.getEinzelwertungPrintables(wk, false, 0));
        if (PrintUtils.printProtocolResultsHorizontal) {
            mp.add(PrintUtils.getGesamtwertungPrintable(wk));
        } else {
            mp.add(new RotatingPrintable(PrintUtils.getGesamtwertungPrintable(wk)));
        }
        mp.add(ZielrichterentscheidPrinter.getPrintable(wk));
        if (!wk.isHeatBased()) {
            mp.add(PenaltylistePrinter.getPrintable(wk, true));
        }
        return PrintManager.getFinalPrintable(mp, wk.getLastChangedDate(), new MessageFormat(I18n.get("Protocol")), I18n.get("Protocol"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
                return;
            }

            PrintManager.print(getPrintable(), I18n.get("Protocol"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
                return;
            }

            PrintableCreator pc = new PrintableCreator() {
                @Override
                public Printable create() {
                    return getPrintable();
                }
            };
            PrintManager.preview(controller.getWindow(), pc, I18n.get("Protocol"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }

    final static class FirstPagePrintable extends ComponentPagePrintable {

        @SuppressWarnings("rawtypes")
        public FirstPagePrintable(AWettkampf wk) {
            super(createPage(wk));
        }

        private static JLabel createLabel(String text) {
            return createLabel(text, 1);
        }

        private static JLabel createLabel(String text, float factor) {
            JLabel l = new JLabel(text);
            if (PrintManager.getFont() != null) {
                Font f = PrintManager.getFont();
                f = f.deriveFont(f.getSize2D() * factor);
                l.setFont(f);
            }
            return l;
        }

        @SuppressWarnings({ "unchecked" })
        private static JComponent createChecksums(@SuppressWarnings("rawtypes") AWettkampf wk) {
            Regelwerk aks = wk.getRegelwerk();

            int count = 0;
            for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                    count++;
                }
            }

            JPanel p = new JPanel(new FormLayout("0dlu:grow,fill:default,1dlu,fill:default,1dlu,fill:default,1dlu,fill:default,0dlu:grow",
                    FormLayoutUtils.createLayoutString(count + 3)));

            p.add(createLabel(I18n.get("Checksums"), 2), CC.xyw(2, 2, 7, "center,center"));

            // p.add(createLabel(I18n.get("Rulebook")), CC.xy(2, 4));
            p.add(createLabel(I18n.get("Print.RulebookChecksums", aks.getChecksum(), aks.getJuniorsChecksum(), aks.getMastersChecksum())),
                    CC.xyw(1, 4, 9, "center,center"));

            p.add(createLabel(I18n.geschlechtToString(aks, false)), CC.xy(4, 6));
            p.add(createLabel(I18n.geschlechtToString(aks, true)), CC.xy(6, 6));

            int pos = 0;
            for (int x = 0; x < aks.size(); x++) {
                Altersklasse ak = aks.getAk(x);
                if (SearchUtils.hasSchwimmer(wk, ak)) {
                    p.add(createLabel(ak.getName()), CC.xy(2, 8 + 2 * pos));
                    for (int y = 0; y < 2; y++) {
                        if (SearchUtils.hasSchwimmer(wk, ak, y == 1)) {
                            JResultTable r = JResultTable.getResultTable(wk, ak, y == 1, true, true, 0);
                            p.add(createLabel(r.getChecksum()), CC.xy(4 + 2 * y, 8 + 2 * pos));
                        }
                    }
                    pos++;
                }
            }

            return p;
        }

        @SuppressWarnings("rawtypes")
        private static JComponent createPage(AWettkampf wk) {
            String css = I18n.get("CSS.Print");
            if (PrintManager.getFont() != null) {
                Font f = PrintManager.getFont();
                css = I18n.get("CSS.PrintPlusFont", f.getFontName(), f.getSize());
            }
            XHTMLPanel above = new XHTMLPanel();
            above.setDocumentFromString(
                    I18n.get("ProtocolTitleTop", css, toHtml(wk.getStringProperty(PropertyConstants.NAME)),
                            toHtml(wk.getStringProperty(PropertyConstants.LOCATION)), toHtml(wk.getStringProperty(PropertyConstants.DATE))),
                    "", new XhtmlNamespaceHandler());
            above.relayout();

            XHTMLPanel below = new XHTMLPanel();
            below.setDocumentFromString(
                    I18n.get("ProtocolTitleBottom", css, toHtml(wk.getStringProperty(PropertyConstants.NAME)),
                            toHtml(wk.getStringProperty(PropertyConstants.LOCATION)), toHtml(wk.getStringProperty(PropertyConstants.DATE))),
                    "", new XhtmlNamespaceHandler());
            below.relayout();

            BufferedImage bi = null;
            if (wk.getLogo() != null) {
                try {
                    bi = ImageIO.read(new ByteArrayInputStream(wk.getLogo()));
                } catch (IOException io) {
                    // Nothing to do
                    io.printStackTrace();
                }
            }
            JIcon image = new JIcon(bi, false);
            image.setOpaque(false);

            JComponent checksums = createChecksums(wk);

            above.setBackground(Color.WHITE);
            below.setBackground(Color.WHITE);
            image.setBackground(Color.WHITE);
            checksums.setBackground(Color.WHITE);

            above.setForeground(Color.BLACK);
            below.setForeground(Color.BLACK);
            image.setForeground(Color.BLACK);
            checksums.setForeground(Color.BLACK);

            if (PrintManager.getFont() != null) {
                above.setFont(PrintManager.getFont());
                below.setFont(PrintManager.getFont());
            }

            JPanel p = new JPanel(new FormLayout("fill:0dlu:grow", "fill:150dlu,fill:0dlu:grow,fill:default,fill:50dlu"));
            p.setOpaque(false);
            p.setBackground(Color.WHITE);

            p.add(above, CC.xy(1, 1));
            p.add(image, CC.xy(1, 2));
            p.add(checksums, CC.xy(1, 3));
            p.add(below, CC.xy(1, 4));

            return p;
        }
    }
}