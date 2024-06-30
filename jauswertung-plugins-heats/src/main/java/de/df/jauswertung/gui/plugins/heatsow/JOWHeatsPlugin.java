package de.df.jauswertung.gui.plugins.heatsow;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.heats.EditHeatlistUtils;
import de.df.jauswertung.gui.plugins.heats.JNewHeatsWizard;
import de.df.jauswertung.gui.plugins.heatsow.define.JOWHeatsEditWindow;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.print.RecorderPrintable;
import de.df.jauswertung.print.SprecherlistePrintable;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.MultiplePrintable;

public class JOWHeatsPlugin extends ANullPlugin {

    private static final String ITEM_DEFINE = I18n.get("DefineRounds");
    private static final String ITEM_NEW = I18n.get("New");
    private static final String ITEM_EDIT = I18n.get("Edit");
    private static final String ITEM_SHOW = I18n.get("Show");
    private static final String ITEM_DELETE = I18n.get("Delete");
    private static final String ITEM_MENU = I18n.get("Laufliste");
    private static final String MENU = I18n.get("Execute");

    CorePlugin core = null;
    private JMenuItem[] menu = null;
    JButton[] buttons = null;

    private JMenuItem define = null;
    private JMenuItem neu = null;
    private JMenuItem show = null;
    private JMenuItem edit = null;
    private JMenuItem delete = null;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);

        initMenues();
        initButtons();
        disableButtons();
    }

    private void initButtons() {
        buttons = new JButton[1];
        buttons[0] = new JButton(IconManager.getSmallIcon("laufliste"));
        buttons[0].setToolTipText(I18n.getToolTip("EditHeatlist"));
    }

    private void initMenues() {
        // Main
        define = new JMenuItem(ITEM_DEFINE);
        define.setToolTipText(I18n.getToolTip("DefineRounds"));
        define.addActionListener(new DefineActionListener());
        neu = new JMenuItem(ITEM_NEW, IconManager.getSmallIcon("new"));
        neu.setToolTipText(I18n.getToolTip("NewHeatlist"));
        neu.addActionListener(new NewActionListener());
        edit = new JMenuItem(ITEM_EDIT, IconManager.getSmallIcon("edit"));
        edit.setToolTipText(I18n.getToolTip("EditHeatlist"));
        edit.addActionListener(new EditActionListener());
        show = new JMenuItem(ITEM_SHOW);
        show.setToolTipText(I18n.getToolTip("ShowHeatlist"));
        show.addActionListener(new ShowActionListener());
        delete = new JMenuItem(ITEM_DELETE, IconManager.getSmallIcon("delete"));
        delete.setToolTipText(I18n.getToolTip("DeleteHeatlist"));
        delete.addActionListener(new DeleteActionListener());

        menu = new JMenuItem[1];
        menu[0] = new JMenu(ITEM_MENU);
        menu[0].setIcon(IconManager.getSmallIcon("laufliste"));
        menu[0].add(define);
        menu[0].add(new JSeparator());
        menu[0].add(neu);
        menu[0].add(edit);
        menu[0].add(delete);
        menu[0].add(new JSeparator());
        menu[0].add(show);
        // menu[0].add(new JSeparator());
    }

    private void ablaufDefinieren() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        JOWHeatsEditWindow<ASchwimmer> w = new JOWHeatsEditWindow<>(wk, t -> {
            if (t) {
                getController().sendDataUpdateEvent("ChangeRB",
                        UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_ZW_LIST_CHANGED
                                | UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                                | UpdateEventConstants.REASON_POINTS_CHANGED
                                | UpdateEventConstants.REASON_SWIMMER_CHANGED,
                        JOWHeatsPlugin.this);
            }
        });
        ModalFrameUtil.showAsModal(w, getController().getWindow());
    }

    private void loescheLaufliste() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        Consumer<OWSelection[]> cb = t -> {
            if (t != null) {
                for (OWSelection s : t) {
                    loescheLaufliste(s);
                }
            }
        };
        OWUtils.showRoundMultiSelector(getController().getWindow(), wk, "Laufliste löschen", "Mögliche Disziplinen",
                                       OWUtils.getCurrentRounds(wk), cb);
    }

    <T extends ASchwimmer> void loescheLaufliste(OWSelection t) {
        AWettkampf<T> wk = createCompetitionFor(t);
        delete(t, wk.getLaufliste());
    }

    private void bearbeiteLaufliste() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();

        Consumer<OWSelection> cb = t -> {
            if (t != null) {
                OWSelection s = t;
                AWettkampf<ASchwimmer> wkx = core.getWettkampf();
                OWDisziplin<ASchwimmer> d = wkx.getLauflisteOW().getDisziplin(s);
                if (d == null || d.isEmpty()) {
                    DialogUtils.inform(getController().getWindow(),
                            "Die Laufliste ist leer und kann nicht bearbeitet werden.",
                            "Ein möglicher Grund dafür ist, dass keine Schwimmer für die Lauferstellung zur Verfügung stehen.");
                    return;
                }
                bearbeiteLaufliste(s);
            }
        };
        OWUtils.showRoundSelector(getController().getWindow(), wk, "Laufliste bearbeiten", "Mögliche Disziplinen",
                                  OWUtils.getCurrentRounds(wk), cb);
    }

    private void zeigeLaufliste() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();

        Consumer<OWSelection> cb = t -> {
            if (t != null) {
                OWSelection s = t;
                AWettkampf<ASchwimmer> wkx = core.getWettkampf();
                OWDisziplin<ASchwimmer> d = wkx.getLauflisteOW().getDisziplin(s);
                if (d == null || d.isEmpty()) {
                    DialogUtils.inform(getController().getWindow(),
                            "Die Laufliste ist leer und kann nicht bearbeitet werden.",
                            "Ein möglicher Grund dafür ist, dass keine Schwimmer für die Lauferstellung zur Verfügung stehen.");
                    return;
                }
                zeigeLaufliste(s);
            }
        };
        OWUtils.showRoundSelector(getController().getWindow(), wk, "Laufliste anzeigen", "Mögliche Disziplinen",
                                  OWUtils.getCreatedRounds(wk, true), cb);
    }

    private <T extends ASchwimmer> void bearbeiteLaufliste(OWSelection t) {
        updateCompetitors(t);
        AWettkampf<T> wk = createCompetitionFor(t);
        EditHeatlistUtils.laufliste(getController(), this, wk, true, wkx -> save(t, wkx, false));
    }

    private <T extends ASchwimmer> void updateCompetitors(OWSelection ows) {
        if (ows.round == 0) {
            // First round: Nothing to do
            return;
        }
        OWSelection t1 = new OWSelection(ows.ak, ows.akNummer, ows.male, ows.discipline, ows.round - 1);

        AWettkampf<T> wk = createCompetitionFor(t1);

        AWettkampf<T> cwk = core.getWettkampf();
        int qualifiziert = cwk.getRegelwerk().getAk(ows.akNummer).getDisziplin(ows.discipline, ows.male)
                .getRunden()[t1.round];
        OWDisziplin<T> owd = cwk.getLauflisteOW().getDisziplin(ows);
        SchwimmerResult<T>[] result = ResultCalculator.getResults(wk, wk.getRegelwerk().getAk(t1.akNummer), t1.male);
        EDTUtils.waitOnEDT();

        LinkedList<T> gemeldet = owd.getSchwimmer();
        LinkedList<T> aktuell = new LinkedList<>();
        for (int x = 0; x < result.length; x++) {
            T t = result[x].getSchwimmer();
            Strafe s = t.getAkkumulierteStrafe(0);
            if (!s.isWithdraw() && !s.cancelsPoints()) {
                aktuell.add(t);
            }
            if (aktuell.size() >= qualifiziert) {
                break;
            }
        }

        for (T t : gemeldet) {
            boolean found = false;
            for (T tneu : aktuell) {
                if (t.getStartnummer() == tneu.getStartnummer()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                owd.remove(t);
            }
        }
        for (T t : aktuell) {
            boolean found = false;
            for (T tneu : gemeldet) {
                if (t.getStartnummer() == tneu.getStartnummer()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                owd.addSchwimmer(t);
            }
        }

    }

    <T extends ASchwimmer> void zeigeLaufliste(OWSelection t) {
        AWettkampf<T> wk = createCompetitionFor(t);
        EditHeatlistUtils.laufliste(getController(), this, wk, false, wkx -> save(t, wkx, false));
    }

    private void neueLaufliste() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        Consumer<OWSelection[]> cb = selection -> {
            if (selection != null) {
                boolean askForPrint = selection.length == 1;
                for (OWSelection t : selection)
                    neueLaufliste(t, askForPrint);
            }
        };
        OWUtils.showRoundMultiSelector(getController().getWindow(), wk, "Neue Laufliste", "Mögliche Disziplinen",
                                       OWUtils.getCreatableRounds(wk), cb);
    }

    <T extends ASchwimmer> void neueLaufliste(OWSelection t, boolean askForPrint) {
        AWettkampf<T> wk = createCompetitionFor(t);
        JNewHeatsWizard<?> jnhw = new JNewHeatsWizard<>(getController().getWindow(), this, wk, true);
        boolean ok = jnhw.start();
        if (ok) {
            save(t, wk, askForPrint);
        }
    }

    private <T extends ASchwimmer> void save(OWSelection t, AWettkampf<T> wkx, boolean askForPrint) {
        HeatsUtils.save(core.getWettkampf(), t, wkx);
        EditHeatlistUtils.notifyHeatlistChanged(getController(), this);

        print(askForPrint, t);
    }

    private <T extends ASchwimmer> void print(boolean askForPrint, OWSelection t) {
        if (askForPrint) {
            AWettkampf<T> wk = core.getWettkampf();
            if (wk.isOpenWater()) {
                if (DialogUtils.ask(getController().getWindow(), "Laufliste drucken?",
                        "Sollen die folgenden Listen direkt gedruckt werden?\n2x Kampfrichterliste\n1x Auswerterliste\n1x Meldeergebnis")) {
                    printOW(t);
                }
            } else {
                if (DialogUtils.ask(getController().getWindow(), "Laufliste drucken?",
                        "Sollen die folgenden Listen direkt gedruckt werden?\n1x Meldeergebnis\n2x Kampfrichterliste (je einmal mit und ohne Notizen)")) {
                    printPool(t);
                }
            }
        }
    }

    private static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk, boolean withComments,
            boolean withTimes, String title) {
        Printable p = new SprecherlistePrintable<T>(wk, false, withTimes, PrintUtils.printOmitOrganisationForTeams,
                withComments, !PrintUtils.printOmitOrganisationForTeams, PrintUtils.printYearOfBirth);
        return PrintManager.getFinalPrintable(PrintManager.getHeaderPrintable(p, title), wk.getLastChangedDate(), true,
                title);
    }

    private static <T extends ASchwimmer> Printable getPrintableRecorder(AWettkampf<T> wk) {
        Printable p = new RecorderPrintable<T>(wk, false, false, PrintUtils.printOmitOrganisationForTeams,
                !PrintUtils.printOmitOrganisationForTeams);
        return PrintManager.getFinalPrintable(PrintManager.getHeaderPrintable(p, I18n.get("Recorder-Laufliste")),
                wk.getLastChangedDate(), true, I18n.get("Recorder-Laufliste"));
    }

    private void printOW(OWSelection sel) {
        AWettkampf<?> wkx = createCompetitionFor(sel);
        MultiplePrintable mp = new MultiplePrintable();
        mp.add(getPrintableRecorder(wkx));
        mp.add(getPrintable(wkx, true, false, I18n.get("Kampfrichter-Laufliste")));
        mp.add(getPrintable(wkx, true, false, I18n.get("Kampfrichter-Laufliste")));
        mp.add(getPrintable(wkx, false, false, I18n.get("Registrationresult")));
        PrintExecutor.print(mp, I18n.get("AutoprintHeats"), true, getController().getWindow());
    }

    private void printPool(OWSelection sel) {
        AWettkampf<?> wkx = createCompetitionFor(sel);
        MultiplePrintable mp = new MultiplePrintable();
        mp.add(getPrintable(wkx, true, false, I18n.get("Kampfrichter-Laufliste")));
        mp.add(getPrintable(wkx, true, false, I18n.get("Kampfrichter-Laufliste")));
        mp.add(getPrintable(wkx, false, true, I18n.get("Registrationresult")));
        PrintExecutor.print(mp, I18n.get("AutoprintHeats"), true, getController().getWindow());
    }

    private <T extends ASchwimmer> void delete(OWSelection t, Laufliste<T> heats) {
        AWettkampf<T> wk = core.getWettkampf();
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t.akNummer, t.male, t.discipline, t.round);
        wk.removeDiscipline(disziplin.Id);
        EditHeatlistUtils.notifyHeatlistChanged(getController(), this);
    }

    private <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(OWSelection t) {
        AWettkampf<T> cwk = core.getWettkampf();
        return ResultUtils.createCompetitionFor(cwk, t);
    }

    class DefineActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            ablaufDefinieren();
        }
    }

    class NewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            neueLaufliste();
        }
    }

    class EditActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            bearbeiteLaufliste();
        }
    }

    class ShowActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            zeigeLaufliste();
        }
    }

    class DeleteActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            loescheLaufliste();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            return null;
        }
        return new MenuInfo[] { new MenuInfo(MENU, 520, menu, 101) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & REASON_NEW_WK) > 0) {
            updateButtons();
            return;
        }
        if ((due.getChangeReason() & REASON_LOAD_WK) > 0) {
            updateButtons();
            return;
        }
        long bitmap = REASON_LAUF_LIST_CHANGED | REASON_PROPERTIES_CHANGED | REASON_AKS_CHANGED | REASON_NEW_TN
                | REASON_SWIMMER_CHANGED | REASON_SWIMMER_DELETED;
        if ((due.getChangeReason() & bitmap) > 0) {
            updateButtons();
        }
    }

    private void disableButtons() {
        setButtons(false, false, true);
    }

    private void setButtons(boolean notEmpty, boolean enabled, boolean visible) {
        if (!visible) {
            menu[0].setVisible(false);
            buttons[0].setVisible(false);
            return;
        }

        menu[0].setVisible(true);
        buttons[0].setVisible(true);

        boolean on = notEmpty || enabled;
        menu[0].setEnabled(on);
        buttons[0].setEnabled(enabled);
        neu.setEnabled(notEmpty);
        delete.setEnabled(enabled);
        show.setEnabled(enabled);
        edit.setEnabled(enabled);
    }

    void updateButtons() {
        AWettkampf<?> wk = core.getWettkampf();
        if (!wk.isHeatBased()) {
            setButtons(true, false, false);
        } else {
            setButtons(wk.hasSchwimmer(), !wk.getLauflisteOW().isEmpty(), true);
        }
    }
}
