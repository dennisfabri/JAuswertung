/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.SprecherlistePrintable;
import de.df.jauswertung.util.ResultUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.MultiplePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class SprecherlistePrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;

    private JComboBox<String> columns;

    public SprecherlistePrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        columns = new JComboBox<>(
                new String[] { I18n.get("None"), I18n.get("Disciplines"), I18n.get("Meldezeiten"),
                        I18n.get("DisciplinesAndMeldezeiten") });
        columns.setSelectedIndex(3);

        print.setEnabled(false);
        preview.setEnabled(false);
        columns.setEnabled(false);

        panel.add(new JLabel(I18n.get("AdditionalColumns")), CC.xy(2, 2));
        panel.add(columns, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste();
        print.setEnabled(b);
        preview.setEnabled(b);
        columns.setEnabled(b);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public String getName() {
        return I18n.get("SpeakerHeatList");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkLaufliste();
    }
        
    
    Printable getPrintable() {
        boolean discipline = (columns.getSelectedIndex() % 2 != 0);
        boolean times = (columns.getSelectedIndex() > 1);
        return getPrintable(core.getWettkampf(), getName(), false, discipline, times);
    }

    Printable getPrintable(AWettkampf<?>[] wkx, String header, boolean withComments, boolean withDiscipline, boolean withTimes) {
        List<Printable> px = new ArrayList<>();
        for (AWettkampf<?> wk : wkx) {
            px.add(getPrintable(wk, header, withComments, withDiscipline, withTimes));
        }
        return new MultiplePrintable(px);
    }

    protected <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, String header, boolean withComments, boolean withDisciplines, boolean withTimes) {
        boolean quali = false;
        for (T t : wk.getSchwimmer()) {
            if (t.getQualifikationsebene().length() > 0) {
                quali = true;
                break;
            }
        }
        Printable p = new SprecherlistePrintable<T>(wk, withDisciplines, withTimes, quali, withComments,
                !PrintUtils.printOmitOrganisationForTeams,
                PrintUtils.printYearOfBirth);
        return PrintManager.getFinalPrintable(PrintManager.getHeaderPrintable(p, header), wk.getLastChangedDate(), true,
                header);
    }

    <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        return ResultUtils.createCompetitionFor(cwk, t);
    }

    @SuppressWarnings("unchecked")
    <T extends ASchwimmer> AWettkampf<T>[] getCompetitions(OWSelection[] t) {
        Arrays.sort(t, new Comparator<OWSelection>() {
            @Override
            public int compare(OWSelection o1, OWSelection o2) {
                AWettkampf<T> cwk = core.getWettkampf();
                int pos1 = cwk.getRegelwerk().getRundenId(o1);
                int pos2 = cwk.getRegelwerk().getRundenId(o2);
                return pos1 - pos2;
            }
        });

        ArrayList<AWettkampf<?>> wkx = new ArrayList<>();
        for (OWSelection s : t) {
            wkx.add(createCompetitionFor(s));
        }
        return wkx.toArray(new AWettkampf[wkx.size()]);
    }

    final class PrintActionListener implements ActionListener {

        <T extends ASchwimmer> void printLaufliste(OWSelection[] t, boolean withComments, boolean withDisciplines, boolean withTimes) {
            PrintExecutor.print(getPrintable(getCompetitions(t), getName(), withComments, withDisciplines, withTimes), getName(), true,
                    controller.getWindow());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null && t.length > 0) {
                        boolean discipline = (columns.getSelectedIndex() % 2 != 0);
                        boolean times = (columns.getSelectedIndex() > 1);
                        printLaufliste(t, false, discipline, times);
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                PrintExecutor.print(getPrintable(), getName(), true, controller.getWindow());
            }
        }
    }

    final class PreviewActionListener implements ActionListener {

        class PPrintableCreator implements PrintableCreator {

            private AWettkampf<?>[] wkx;

            public PPrintableCreator(AWettkampf<?>[] wk) {
                this.wkx = wk;
            }

            @Override
            public Printable create() {
                boolean discipline = (columns.getSelectedIndex() % 2 != 0);
                boolean times = (columns.getSelectedIndex() > 1);
                return getPrintable(wkx, getName(), false, discipline, times);
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                Consumer<OWSelection[]> cb = t -> {
                    if (t != null && t.length > 0) {
                        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(getCompetitions(t)),
                                getName(), IconManager.getIconBundle(), IconManager.getTitleImages());
                    }
                };
                OWUtils.showRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                                               "Laufliste zum Drucken auswählen",
                                               OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(new AWettkampf[] { wk }), getName(),
                        IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}