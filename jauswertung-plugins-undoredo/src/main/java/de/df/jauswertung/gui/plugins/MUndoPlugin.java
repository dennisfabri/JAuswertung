/*
 * Created on 21.10.2005
 */
package de.df.jauswertung.gui.plugins;

import java.util.LinkedList;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JUndoButton;
import de.df.jutils.gui.JUndoButton.UndoListener;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MUndoPlugin extends ANullPlugin {

    private static final int UNDO_STEPS = 20;

    private ButtonInfo[] buttons;
    private MenuInfo[] menues;

    private JUndoButton<String> undo;
    private JUndoButton<String> redo;

    private JMenuItem undom;
    private JMenuItem redom;

    private LinkedList<String> undolist = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    private LinkedList<AWettkampf> undowk = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    private LinkedList<AWettkampf> redowk = new LinkedList<>();
    private LinkedList<String> redolist = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    private AWettkampf tempWK = null;

    private CorePlugin core = null;

    public MUndoPlugin() {
        EDTUtils.executeOnEDT(this::initGUI);
    }

    void initGUI() {
        undo = new JUndoButton<>(null, IconManager.getSmallIcon("undo"), UNDO_STEPS);
        redo = new JUndoButton<>(null, IconManager.getSmallIcon("redo"), UNDO_STEPS);

        undom = new JMenuItem(I18n.get("Undo"), IconManager.getSmallIcon("undo"));
        redom = new JMenuItem(I18n.get("Redo"), IconManager.getSmallIcon("redo"));

        buttons = new ButtonInfo[4];
        buttons[0] = new ButtonInfo(undo.getMainButton(), 200);
        buttons[1] = new ButtonInfo(undo.getArrowButton(), 201);
        buttons[2] = new ButtonInfo(redo.getMainButton(), 202);
        buttons[3] = new ButtonInfo(redo.getArrowButton(), 203);

        menues = new MenuInfo[2];
        menues[0] = new MenuInfo(I18n.get("Edit"), 200, undom, 1);
        menues[1] = new MenuInfo(I18n.get("Edit"), 200, redom, 2);

        undom.setToolTipText(I18n.getToolTip("Undo"));
        redom.setToolTipText(I18n.getToolTip("Redo"));
        undo.setToolTipText(I18n.getToolTip("Undo"));
        redo.setToolTipText(I18n.getToolTip("Redo"));

        undom.setEnabled(false);
        redom.setEnabled(false);

        undom.addActionListener(e -> {
            undo(0);
        });
        redom.addActionListener(e -> {
            redo(0);
        });

        undo.addUndoListener(new UndoListener<String>() {
            @Override
            public void undo(JUndoButton<String> source, int index) {
                MUndoPlugin.this.undo(index);
            }
        });
        redo.addUndoListener((source, index) -> {
            redo(index);
        });
    }

    private ASchwimmer schwimmer = null;
    private ASchwimmer schwimmerhlw = null;

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (core.getWettkampf() == tempWK) {
            // Undo/Redo in progress
            return;
        }

        if (redolist.size() > 0) {
            redo.removeAllSteps();
            redolist.clear();
            redowk.clear();
            redom.setEnabled(false);
        }

        if (undowk.isEmpty() || due.getTitle().equals("Init") || (due.getTitle().equals("NewCompetition"))
                || (due.getTitle().equals("SaveAs")) || (due.getTitle().equals("Load"))
                || (due.getTitle().equals("Save"))) {
            schwimmer = null;
            schwimmerhlw = null;

            undo.removeAllSteps();
            undolist.clear();
            undowk.clear();
            undom.setEnabled(false);

            save();
            return;
        }

        if (due.getTitle().equals("ChangeTime")) {
            schwimmerhlw = null;
            if (due.getData() != schwimmer) {
                schwimmer = (ASchwimmer) due.getData();
                undo.addStep(I18n.get("Event." + due.getTitle()));
                undolist.addFirst(due.getTitle());
                undom.setEnabled(true);
                if (undolist.size() > UNDO_STEPS) {
                    undolist.removeLast();
                    undowk.removeLast();
                }
            } else {
                undowk.removeFirst();
            }
            save();
            return;
        }
        schwimmer = null;

        if (due.getTitle().equals("ChangeZWPoints")) {
            if (due.getData() != schwimmerhlw) {
                schwimmerhlw = (ASchwimmer) due.getData();
                undo.addStep(I18n.get("Event." + due.getTitle()));
                undolist.addFirst(due.getTitle());
                undom.setEnabled(true);
                if (undolist.size() > UNDO_STEPS) {
                    undolist.removeLast();
                    undowk.removeLast();
                }
            } else {
                undowk.removeFirst();
            }
            save();
            return;
        }
        schwimmerhlw = null;

        undo.addStep(I18n.get("Event." + due.getTitle()));
        undolist.addFirst(due.getTitle());
        undom.setEnabled(true);
        if (undolist.size() > UNDO_STEPS) {
            undolist.removeLast();
            undowk.removeLast();
        }
        save();
    }

    /**
     * The list of snapshots always holds the current state in front of one snapshot per undo step. When a snapshot
     * fails, the history is cleared, and the next change starts it again with a new current state.
     */
    private void save() {
        try {
            undowk.addFirst(Utils.copy(core.getWettkampf()));
        } catch (RuntimeException e) {
            log.error("Snapshot for undo failed", e);
            schwimmer = null;
            schwimmerhlw = null;
            undo.removeAllSteps();
            undolist.clear();
            undowk.clear();
            undom.setEnabled(false);
            meldeFehler(I18n.get("UndoSnapshotFailed"), I18n.get("UndoSnapshotFailed.Note"));
        }
    }

    @SuppressWarnings("rawtypes")
    private AWettkampf restore(AWettkampf snapshot) {
        try {
            return Utils.copy(snapshot);
        } catch (RuntimeException e) {
            log.error("Restoring an undo snapshot failed", e);
            meldeFehler(I18n.get("UndoRestoreFailed"), I18n.get("UndoRestoreFailed.Note"));
            return null;
        }
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @SuppressWarnings("rawtypes")
    public void undo(int index) {
        AWettkampf wk = restore(undowk.get(index + 1));
        if (wk == null) {
            return;
        }
        for (int x = 0; x <= index; x++) {
            redo.addStep(undo.getStep(0));
            undo.removeSteps(1);
            redolist.addFirst(undolist.removeFirst());
            redowk.addFirst(undowk.removeFirst());
        }
        undom.setEnabled(undo.isUndoEnabled());
        redom.setEnabled(redo.isUndoEnabled());

        tempWK = wk;
        core.setWettkampf(tempWK, false);

        tempWK = null;
    }

    @SuppressWarnings("rawtypes")
    public void redo(int index) {
        AWettkampf wk = restore(redowk.get(index));
        if (wk == null) {
            return;
        }
        for (int x = 0; x <= index; x++) {
            undo.addStep(redo.getStep(0));
            redo.removeSteps(1);
            undolist.addFirst(redolist.removeFirst());
            undowk.addFirst(redowk.removeFirst());
        }
        undom.setEnabled(undo.isUndoEnabled());
        redom.setEnabled(redo.isUndoEnabled());

        tempWK = wk;
        core.setWettkampf(tempWK, false);

        tempWK = null;
    }

    void meldeFehler(String text, String hinweis) {
        EDTUtils.executeOnEDTAsync(() -> DialogUtils.warn(getController().getWindow(), text, hinweis));
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return buttons;
    }

    @Override
    public MenuInfo[] getMenues() {
        return menues;
    }
}