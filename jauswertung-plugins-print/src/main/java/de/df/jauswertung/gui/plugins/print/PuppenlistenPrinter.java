/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;

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
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.PuppenListe;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.JTablePrintable;
import de.df.jutils.print.printables.MultiplePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class PuppenlistenPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JComboBox number = null;

    public PuppenlistenPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        number = new JComboBox();
        number.addItem(I18n.get("AllLanes"));

        number.setEnabled(false);
        print.setEnabled(false);
        preview.setEnabled(false);

        panel.add(number, CC.xy(2, 2));
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = false;
        if (wk.hasSchwimmer()) {
            if ((wk.getHLWListe() != null) && (!wk.getHLWListe().isEmpty())) {
                b = true;
            }
            int length = wk.getIntegerProperty(ZW_LANES);
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
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */
    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("Lanelist") + " (" + I18n.get("LanelistInfo") + ")";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    private static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, int x) {
        PuppenListe<T> bl = new PuppenListe<>(wk);
        return PrintManager.getPrintable(bl.toJTable(x + 1), I18n.get("PuppeNummer", x + 1), JTablePrintable.OPT_ALL,
                true, true);
    }

    @SuppressWarnings({ "unchecked" })
    Printable getPrintable() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        Printable p = null;
        if (number.getSelectedIndex() == number.getItemCount() - 1) {
            MultiplePrintable mp = new MultiplePrintable();
            for (int x = 0; x < wk.getIntegerProperty(ZW_LANES); x++) {
                mp.add(getPrintable(wk, x));
            }
            p = mp;
        } else {
            p = getPrintable(wk, number.getSelectedIndex());
        }
        return PrintManager.getFinalPrintable(p, core.getLastChangedDate(), I18n.get("Puppenliste"),
                I18n.get("Puppenliste"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("Puppenliste"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = PuppenlistenPrinter.this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Puppenliste"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}