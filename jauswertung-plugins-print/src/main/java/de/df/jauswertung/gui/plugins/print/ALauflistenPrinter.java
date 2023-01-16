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

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import de.df.jutils.gui.util.ISimpleCallback;
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
abstract class ALauflistenPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;
    private JCheckBox comments;

    private final boolean showTimes;

    public ALauflistenPrinter(IPluginManager window, CorePlugin plugin, boolean showTimes, boolean allowNoteSelection) {
        core = plugin;
        controller = window;

        this.showTimes = showTimes;

        initGUI(allowNoteSelection);
    }

    private void initGUI(boolean allowNoteSelection) {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        comments = new JCheckBox(I18n.get("ColumnForNotes"));

        comments.setEnabled(false);
        print.setEnabled(false);
        preview.setEnabled(false);

        if (allowNoteSelection) {
            panel.add(comments, CC.xy(2, 2));
        }
        panel.add(preview, CC.xy(4, 2));
        panel.add(print, CC.xy(6, 2));
    }

    @SuppressWarnings("rawtypes")
    private void checkLaufliste() {
        AWettkampf wk = core.getWettkampf();

        boolean b = wk.hasSchwimmer() && wk.hasLaufliste();
        comments.setEnabled(b);
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

    Printable getPrintable() {
        return getPrintable(core.getWettkampf(), getName(), comments.isSelected());
    }

    Printable getPrintable(AWettkampf<?>[] wkx, String header, boolean withComments) {
        ArrayList<Printable> px = new ArrayList<>();
        for (AWettkampf<?> wk : wkx) {
            px.add(getPrintable(wk, header, withComments));
        }
        return new MultiplePrintable(px);
    }

    protected <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, String header, boolean withComments) {
        Printable p = new SprecherlistePrintable<T>(wk, false, showTimes, false, withComments,
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

        <T extends ASchwimmer> void printLaufliste(OWSelection[] t, boolean withComments) {
            PrintExecutor.print(getPrintable(getCompetitions(t), getName(), withComments), getName(), true,
                    controller.getWindow());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = t -> {
                    if (t != null && t.length > 0) {
                        printLaufliste(t, comments.isSelected());
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
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
                return getPrintable(wkx, getName(), comments.isSelected());
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk.isHeatBased()) {
                ISimpleCallback<OWSelection[]> cb = t -> {
                    if (t != null && t.length > 0) {
                        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(getCompetitions(t)),
                                getName(), IconManager.getIconBundle(), IconManager.getTitleImages());
                    }
                };
                OWUtils.ShowRoundMultiSelector(controller.getWindow(), wk, "Laufliste auswählen",
                        "Laufliste zum Drucken auswählen",
                        OWUtils.getCreatedRounds(wk, true), cb);
            } else {
                PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(new AWettkampf[] { wk }), getName(),
                        IconManager.getIconBundle(), IconManager.getTitleImages());
            }
        }
    }
}