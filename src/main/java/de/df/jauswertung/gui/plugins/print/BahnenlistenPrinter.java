/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.BahnenListe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.MultiplePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class BahnenlistenPrinter implements Printer {

    CorePlugin                core;
    IPluginManager            controller;

    private JPanel            panel;
    private JButton           print;
    private JButton           preview;
    private JComboBox<String> number = null;

    public BahnenlistenPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        number = new JComboBox<String>();
        number.addItem(I18n.get("AllLanes"));

        number.setEnabled(false);
        print.setEnabled(false);
        preview.setEnabled(false);

        panel.add(number, CC.xy(2, 2));
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste(AWettkampf wk) {

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste() && !wk.isHeatBased();
        if (b) {
            b = false;
            if ((wk.getLaufliste() != null) && (wk.getLaufliste().getLaufliste() != null)) {
                b = true;
            }
            int length = wk.getIntegerProperty(HEATS_LANES);
            if (length != number.getItemCount() - 1) {
                boolean max = number.getSelectedIndex() == number.getItemCount() - 1;
                if (number.getItemCount() > 0) {
                    number.removeItemAt(number.getItemCount() - 1);
                }

                while (length > number.getItemCount()) {
                    number.addItem(I18n.get("LaneNumber", number.getItemCount() + 1));
                }
                while (length < number.getItemCount()) {
                    number.removeItemAt(number.getItemCount() - 1);
                }

                number.addItem(I18n.get("AllLanes"));
                if (max) {
                    number.setSelectedIndex(length);
                }
            }
        }
        number.setEnabled(b);
        print.setEnabled(b);
        preview.setEnabled(b);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */
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
        return I18n.get("Lanelist") + " (" + I18n.get("LanelistInfo") + ")";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste(wk);
    }

    private static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, int x) {
        BahnenListe<T> bl = new BahnenListe<T>(wk);
        return PrintManager.getPrintable(bl.toJTable(x + 1), I18n.get("LaneNumber", x + 1), JTablePrintable.OPT_ALL, true, true);
    }

    Printable getPrintable() {
        AWettkampf<?> wk = core.getWettkampf();
        Printable p = null;
        if (number.getSelectedIndex() == number.getItemCount() - 1) {
            MultiplePrintable mp = new MultiplePrintable();
            for (int x = 0; x < wk.getIntegerProperty(HEATS_LANES); x++) {
                if (wk.getLaufliste().isLaneUsed(x)) {
                    mp.add(getPrintable(wk, x));
                }
            }
            p = mp;
        } else {
            p = getPrintable(wk, number.getSelectedIndex());
        }
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), I18n.get("Lanelist"), I18n.get("Lanelist"));
    }

    final class PrintActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintManager.print(getPrintable(), I18n.get("Lanelist"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = new PrintableCreator() {
                @Override
                public Printable create() {
                    return getPrintable();
                }
            };
            PrintManager.preview(controller.getWindow(), pc, I18n.get("Lanelist"), IconManager.getIconBundle(), IconManager.getTitleImages());
        }
    }
}