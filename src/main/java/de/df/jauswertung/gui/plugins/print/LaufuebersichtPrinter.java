/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class LaufuebersichtPrinter implements Printer {

    private CorePlugin     core;
    private IPluginManager controller;

    private JPanel         panel;
    private JButton        print;
    private JButton        preview;

    public LaufuebersichtPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        panel.add(preview, CC.xy(2, 2));
        panel.add(print, CC.xy(4, 2));
    }

    void checkLaufliste() {
        AWettkampf<?> wk = core.getWettkampf();

        boolean b = false;
        if (!wk.isHeatBased() && wk.hasSchwimmer()) {
            if ((wk.getLaufliste() != null) && (wk.getLaufliste().getLaufliste() != null)) {
                b = PrintUtils.hasLaufuebersicht(wk);
            }
        }
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
        return I18n.get("Heatoverview");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    Printable getPrintable() {
        return PrintManager.getFinalPrintable(PrintUtils.getLaufuebersicht(core.getWettkampf()), core.getLastChangedDate(), I18n.get("Heatoverview"),
                I18n.get("Heatoverview"));
    }

    /**
     * 
     */
    void print() {
        PrintManager.print(getPrintable(), I18n.get("Heatoverview"), controller.getWindow());
    }

    void preview() {
        PrintableCreator pc = new PrintableCreator() {
            @Override
            public Printable create() {
                return getPrintable();
            }
        };
        PrintManager.preview(controller.getWindow(), pc, I18n.get("Heatoverview"), IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            print();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            preview();
        }
    }
}