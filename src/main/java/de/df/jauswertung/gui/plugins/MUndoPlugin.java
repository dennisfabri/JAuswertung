/*
 * Created on 21.10.2005
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jutils.gui.JUndoButton;
import de.df.jutils.gui.JUndoButton.UndoListener;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MUndoPlugin extends ANullPlugin {

    private static final int    UNDO_STEPS = 20;

    private ButtonInfo[]        buttons;
    private MenuInfo[]          menues;

    private JUndoButton<String> undo;
    private JUndoButton<String> redo;

    private JMenuItem           undom;
    private JMenuItem           redom;

    private LinkedList<String>  undolist   = new LinkedList<String>();
    private LinkedList<byte[]>  undowk     = new LinkedList<byte[]>();
    private LinkedList<byte[]>  redowk     = new LinkedList<byte[]>();
    private LinkedList<String>  redolist   = new LinkedList<String>();

    @SuppressWarnings("rawtypes")
    private AWettkampf          tempWK     = null;

    private CorePlugin          core       = null;

    public MUndoPlugin() {
        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                initGUI();
            }
        });
    }

    void initGUI() {
        undo = new JUndoButton<String>(null, IconManager.getSmallIcon("undo"), UNDO_STEPS);
        redo = new JUndoButton<String>(null, IconManager.getSmallIcon("redo"), UNDO_STEPS);

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

        undom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo(0);
            }
        });
        redom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo(0);
            }
        });

        undo.addUndoListener(new UndoListener<String>() {
            @Override
            public void undo(JUndoButton<String> source, int index) {
                MUndoPlugin.this.undo(index);
            }
        });
        redo.addUndoListener(new UndoListener<String>() {
            @Override
            public void undo(JUndoButton<String> source, int index) {
                redo(index);
            }
        });
    }

    private ASchwimmer schwimmer    = null;
    private ASchwimmer schwimmerhlw = null;

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (core.getWettkampf() == tempWK) {
            // Undo/Redo in progress
            return;
        }

        if (redolist.size() > 0) {
            // Something has changed => no redo possible
            redo.removeAllSteps();
            redolist.clear();
            redowk.clear();
            redom.setEnabled(false);
        }

        if (due.getTitle().equals("Init") || (due.getTitle().equals("NewCompetition")) || (due.getTitle().equals("SaveAs")) || (due.getTitle().equals("Load"))
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

    private void save() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputManager.serializeObject(baos, core.getWettkampf());
        undowk.addFirst(baos.toByteArray());
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @SuppressWarnings("rawtypes")
    public void undo(int index) {
        for (int x = 0; x <= index; x++) {
            redo.addStep(undo.getStep(0));
            undo.removeSteps(1);
            redolist.addFirst(undolist.removeFirst());
            redowk.addFirst(undowk.removeFirst());
        }
        undom.setEnabled(undo.isUndoEnabled());
        redom.setEnabled(redo.isUndoEnabled());

        tempWK = (AWettkampf) InputManager.unserializeObject(new ByteArrayInputStream(undowk.get(0)));

        core.setWettkampf(tempWK, false);

        tempWK = null;
    }

    @SuppressWarnings("rawtypes")
    public void redo(int index) {
        tempWK = (AWettkampf) InputManager.unserializeObject(new ByteArrayInputStream(redowk.get(index)));
        for (int x = 0; x <= index; x++) {
            undo.addStep(redo.getStep(0));
            redo.removeSteps(1);
            undolist.addFirst(redolist.removeFirst());
            undowk.addFirst(redowk.removeFirst());
        }
        undom.setEnabled(undo.isUndoEnabled());
        redom.setEnabled(redo.isUndoEnabled());

        core.setWettkampf(tempWK, false);

        tempWK = null;
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