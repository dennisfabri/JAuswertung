/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JDetailsDialog;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
abstract class AFilledKartenPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JComboBox<String> pages;

    private String text;

    private boolean allheats = true;
    private int minheat = 0;
    private int maxheat = 0;

    public AFilledKartenPrinter(IPluginManager pm, CorePlugin plugin, String text) {
        core = plugin;
        controller = pm;
        this.text = text;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        pages = new JComboBox<>(new String[] { "1", "2", "4" });
        pages.setSelectedIndex(2);

        pages.setEnabled(false);
        preview.setEnabled(false);
        print.setEnabled(false);

        panel.add(pages, CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste();
        pages.setEnabled(b);
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

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }

    private PageMode getMode() {
        switch (pages.getSelectedIndex()) {
        case 0:
            return PageMode.ONE_PER_PAGE;
        case 1:
            return PageMode.TWO_PER_PAGE;
        default:
            return PageMode.FOUR_PER_PAGE;
        }
    }

    @SuppressWarnings("rawtypes")
    <T extends ASchwimmer> Printable getPrintable(AWettkampf[] wkx) {
        return getPrintable(wkx, getMode(), PrintUtils.printEmptyCards, allheats, minheat, maxheat);
    }

    @SuppressWarnings({ "hiding", "rawtypes" })
    protected abstract Printable getPrintable(AWettkampf[] wk, PageMode mode, boolean printEmptyCards, boolean allheats,
            int minheat, int maxheat);

    <T extends ASchwimmer> boolean askForDetails(AWettkampf<T> wk) {
        JDetailsDialog<T> details = new JDetailsDialog<>(controller.getWindow(), wk, text);
        EDTUtils.setVisible(details, true);
        if (details.isOk()) {
            allheats = details.printAllHeats();
            minheat = details.getMinHeat();
            maxheat = details.getMaxHeat();
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    <T extends ASchwimmer> boolean askForDetails(AWettkampf[] wk) {
        allheats = true;
        minheat = 0;
        maxheat = 0;
        return true;
    }

    <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        return ResultUtils.createCompetitionFor(cwk, t);
    }

    @SuppressWarnings("rawtypes")
    <T extends ASchwimmer> AWettkampf[] createCompetitionsFor(OWSelection[] t) {
        AWettkampf[] wk = new AWettkampf[t.length];
        for (int x = 0; x < t.length; x++) {
            wk[x] = createCompetitionFor(t[x]);
        }
        return wk;
    }

    final class PrintActionListener implements ActionListener {

        @SuppressWarnings("rawtypes")
        <T extends ASchwimmer> void printLaufliste(OWSelection[] t) {
            AWettkampf[] wks = createCompetitionsFor(t);
            if (askForDetails(wks)) {
                PrintExecutor.print(getPrintable(wks), text, true, controller.getWindow());
            }
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
                if (askForDetails(wk)) {
                    PrintExecutor.print(getPrintable(new AWettkampf[] { wk }), text, true, controller.getWindow());
                }
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        class PPrintableCreator implements PrintableCreator {

            @SuppressWarnings("rawtypes")
            private final AWettkampf[] wks;

            @SuppressWarnings("rawtypes")
            public PPrintableCreator(AWettkampf[] wks) {
                this.wks = wks;
            }

            @Override
            public Printable create() {
                return getPrintable(wks);
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null) {
                        AWettkampf[] wkx = createCompetitionsFor(t);
                        if (askForDetails(wkx)) {
                            PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(wkx), text,
                                    IconManager.getIconBundle(), IconManager.getTitleImages());
                        }
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                // PrintManager.preview(controller.getWindow(), new PPrintableCreator(wk),
                // I18n.get("Laufzeiten"),
                // IconManager.getIconBundle(), IconManager.getTitleImages());
                if (askForDetails(wk)) {
                    PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(new AWettkampf[] { wk }), text,
                            IconManager.getIconBundle(), IconManager.getTitleImages());
                }
            }
        }
    }
}