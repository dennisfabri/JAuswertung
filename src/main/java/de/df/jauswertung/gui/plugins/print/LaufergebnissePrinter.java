/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jutils.gui.util.ISimpleCallback;
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
class LaufergebnissePrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JButton preview;

    /**
     * 
     */
    public LaufergebnissePrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        print.setEnabled(false);
        preview.setEnabled(false);

        panel.add(preview, CC.xy(2, 2));
        panel.add(print, CC.xy(4, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste();
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
        return I18n.get("Laufzeiten");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T>[] wkx) {
        ArrayList<Printable> px = new ArrayList<Printable>();
        for (AWettkampf wk : wkx) {
            px.add(getPrintable(wk));
        }
        return new MultiplePrintable(px);
    }

    static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk) {
        JTable table = TableHeatUtils.getLaufzeiten(wk, PrintUtils.printEmptyLanes);
        Printable p = PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("Laufzeiten"), I18n.get("Laufzeiten"));
    }

    <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        return CompetitionUtils.createCompetitionFor(cwk, t);
    }

    final class PrintActionListener implements ActionListener {

        <T extends ASchwimmer> void printLaufliste(OWSelection[] t) {
            ArrayList<AWettkampf> wkx = new ArrayList<AWettkampf>();
            for (OWSelection s : t) {
                wkx.add(createCompetitionFor(s));
            }
            PrintManager.print(getPrintable((AWettkampf<ASchwimmer>[]) wkx.toArray(new AWettkampf[wkx.size()])), I18n.get("Laufzeiten"), true,
                    controller.getWindow());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = new ISimpleCallback<OWSelection[]>() {
                    @Override
                    public void callback(OWSelection[] t) {
                        if (t != null && t.length > 0) {
                            printLaufliste(t);
                        }
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen", "Laufliste zum Drucken auswählen",
                        OWUtils.getCreatedRounds(wk, false), cb);
            } else {
                PrintManager.print(getPrintable(wk), I18n.get("Laufzeiten"), true, controller.getWindow());
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        class PPrintableCreator implements PrintableCreator {

            private AWettkampf[] wkx;

            public PPrintableCreator(AWettkampf[] wk) {
                this.wkx = wk;
            }

            @Override
            public Printable create() {
                return getPrintable(wkx);
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = new ISimpleCallback<OWSelection[]>() {
                    @Override
                    public void callback(OWSelection[] t) {
                        if (t != null) {
                            ArrayList<AWettkampf> wkx = new ArrayList<AWettkampf>();
                            for (OWSelection s : t) {
                                wkx.add(createCompetitionFor(s));
                            }
                            PrintManager.preview(controller.getWindow(), new PPrintableCreator(wkx.toArray(new AWettkampf[wkx.size()])), I18n.get("Laufzeiten"),
                                    IconManager.getIconBundle(), IconManager.getTitleImages());
                        }
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen", "Laufliste zum Drucken auswählen",
                        OWUtils.getCreatedRounds(wk, false), cb);
            } else {
                PrintManager.preview(controller.getWindow(), new PPrintableCreator(new AWettkampf[] { wk }), I18n.get("Laufzeiten"),
                        IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}