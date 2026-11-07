/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;

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
import de.df.jauswertung.gui.util.*;
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

    static final int ALLE_BAHNEN = 0;

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JComboBox<String> pages;
    private JComboBox<String> bahnen;

    private String text;
    private final boolean bahnauswahl;

    private boolean allheats = true;
    private int minheat = 0;
    private int maxheat = 0;

    AFilledKartenPrinter(IPluginManager pm, CorePlugin plugin, String text, boolean bahnauswahl) {
        core = plugin;
        controller = pm;
        this.text = text;
        this.bahnauswahl = bahnauswahl;
        initGUI();
    }

    private void initGUI() {
        String bahnenSpalte = bahnauswahl ? "fill:default,4dlu," : "";
        FormLayout layout = new FormLayout(
                "4dlu:grow," + bahnenSpalte + "fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1));
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        pages = new JComboBox<>(new String[]{"1", "2", "4"});
        pages.setSelectedIndex(2);

        pages.setEnabled(false);
        preview.setEnabled(false);
        print.setEnabled(false);

        int spalte = 2;
        if (bahnauswahl) {
            bahnen = new JComboBox<>();
            bahnen.addItem(I18n.get("AllLanes"));
            bahnen.setEnabled(false);
            panel.add(bahnen, CC.xy(spalte, 2));
            spalte += 2;
        }
        panel.add(pages, CC.xy(spalte, 2));
        panel.add(new JLabel(I18n.get("PerPage")), CC.xy(spalte + 2, 2));
        panel.add(preview, CC.xy(spalte + 4, 2));
        panel.add(print, CC.xy(spalte + 6, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste();
        pages.setEnabled(b);
        print.setEnabled(b);
        preview.setEnabled(b);
        if (bahnauswahl) {
            updateBahnen(wk.getIntegerProperty(HEATS_LANES));
            bahnen.setEnabled(b);
        }
    }

    private void updateBahnen(int anzahl) {
        if (anzahl == bahnen.getItemCount() - 1) {
            return;
        }
        int bahn = getBahn();
        bahnen.removeAllItems();
        for (int x = 1; x <= anzahl; x++) {
            bahnen.addItem(I18n.get("LaneNumber", x));
        }
        bahnen.addItem(I18n.get("AllLanes"));
        bahnen.setSelectedIndex(bahn == ALLE_BAHNEN || bahn > anzahl ? anzahl : bahn - 1);
    }

    private int getBahn() {
        if (!bahnauswahl) {
            return ALLE_BAHNEN;
        }
        int index = bahnen.getSelectedIndex();
        return index < 0 || index == bahnen.getItemCount() - 1 ? ALLE_BAHNEN : index + 1;
    }

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

    @SuppressWarnings({"hiding", "rawtypes"})
    protected abstract Printable getPrintable(AWettkampf[] wk, PageMode mode, boolean printEmptyCards, boolean allheats,
                                              int minheat, int maxheat, int bahn);

    @SuppressWarnings("rawtypes")
    private PrintableCreator createPrintableCreator(AWettkampf[] wks, boolean all, int min, int max) {
        PageMode mode = getMode();
        int bahn = getBahn();
        return () -> getPrintable(wks, mode, PrintUtils.printEmptyCards, all, min, max, bahn);
    }

    <T extends ASchwimmer> boolean setPrintAllHeats(AWettkampf<T> wk) {
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

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null) {
                        PrintExecutor.print(createPrintableCreator(createCompetitionsFor(t), true, 0, 0), text, true,
                                            controller.getWindow());
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               EditMode.READ, cb);
            } else if (setPrintAllHeats(wk)) {
                PrintExecutor.print(createPrintableCreator(new AWettkampf[]{wk}, allheats, minheat, maxheat), text,
                                    true, controller.getWindow());
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null) {
                        PrintExecutor.preview(controller.getWindow(),
                                              createPrintableCreator(createCompetitionsFor(t), true, 0, 0), text,
                                              IconManager.getIconBundle(), IconManager.getTitleImages());
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               EditMode.READ, cb);
            } else if (setPrintAllHeats(wk)) {
                PrintExecutor.preview(controller.getWindow(),
                                      createPrintableCreator(new AWettkampf[]{wk}, allheats, minheat, maxheat), text,
                                      IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}
