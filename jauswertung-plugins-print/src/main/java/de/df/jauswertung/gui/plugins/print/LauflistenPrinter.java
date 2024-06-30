/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.ResultUtils;
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
class LauflistenPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;

    /**
     * 
     */
    public LauflistenPrinter(IPluginManager window, CorePlugin plugin) {
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
        return I18n.get("Laufliste");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    Printable getPrintable() {
        return getPrintable(core.getWettkampf());
    }

    static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T>[] wkx) {
        ArrayList<Printable> px = new ArrayList<>();
        for (AWettkampf wk : wkx) {
            px.add(getPrintable(wk));
        }
        return new MultiplePrintable(px);
    }

    private static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk) {
        JTable table = TableHeatUtils.getLaufliste(wk, PrintUtils.printEmptyLanes);
        Printable p = PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("Laufliste"), I18n.get("Laufliste"));
    }

    <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        return ResultUtils.createCompetitionFor(cwk, t);
    }

    final class PrintActionListener implements ActionListener {

        <T extends ASchwimmer> void printLaufliste(OWSelection[] t) {
            ArrayList<AWettkampf> wkx = new ArrayList<>();
            for (OWSelection s : t) {
                wkx.add(createCompetitionFor(s));
            }
            PrintExecutor.print(getPrintable((AWettkampf<ASchwimmer>[]) wkx.toArray(new AWettkampf[wkx.size()])),
                    I18n.get("Laufliste"), true, controller.getWindow());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null) {
                        printLaufliste(t);
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                PrintExecutor.print(getPrintable(), I18n.get("Laufliste"), true, controller.getWindow());
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        static class PPrintableCreator implements PrintableCreator {

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
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null) {
                        ArrayList<AWettkampf> wkx = new ArrayList<>();
                        for (OWSelection s : t) {
                            wkx.add(createCompetitionFor(s));
                        }
                        PrintExecutor.preview(controller.getWindow(),
                                new PPrintableCreator(wkx.toArray(new AWettkampf[wkx.size()])),
                                I18n.get("Laufliste"), IconManager.getIconBundle(), IconManager.getTitleImages());
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(new AWettkampf[] { wk }),
                        I18n.get("Laufliste"), IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}