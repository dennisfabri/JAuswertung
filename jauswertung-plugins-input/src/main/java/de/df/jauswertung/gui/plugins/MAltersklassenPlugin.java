/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.akeditor.JAKsEditor;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri @date 05.04.2004
 */
public class MAltersklassenPlugin extends ANullPlugin {

    private static final String AK_STRING     = I18n.get("Rulebook");
    private static final String EXTRAS_STRING = I18n.get("Edit");

    IPluginManager              controller;
    CorePlugin                  core;
    WarningPlugin               warner;

    private JMenuItem[]         menu;

    @Override
    public void setController(IPluginManager c, String newUid) {
        controller = c;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", newUid);
        warner = (WarningPlugin) controller.getFeature("de.df.jauswertung.warning", newUid);

        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(AK_STRING);
        menu[0].setToolTipText(I18n.getToolTip("RulebookEditor"));
        menu[0].addActionListener(new AltersklassenActionListener());
        menu[0].setAccelerator(I18n.getKeyStroke("rulebook"));
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(EXTRAS_STRING, 500, menu, 970) };
    }

    final class AltersklassenActionListener implements ActionListener {

        final class EditorCallback implements ISimpleCallback<JAKsEditor> {

            private final int       size;
            private final boolean[] empty;

            public EditorCallback(boolean[] empty) {
                size = empty.length;
                this.empty = empty;
            }

            @Override
            public void callback(JAKsEditor editor) {
                if (!editor.hasChanged()) {
                    return;
                }
                AWettkampf<?> wk = core.getWettkampf();

                boolean changedOrder = false;
                int[] indizes = editor.getIndizes();
                int[] indizes2 = new int[size];
                for (int x = 0; x < indizes2.length; x++) {
                    indizes2[x] = -1;
                    if (!empty[x]) {
                        for (int y = 0; y < indizes.length; y++) {
                            if (indizes[y] == x) {
                                indizes2[x] = y;
                                break;
                            }
                        }
                        if (indizes2[x] < 0) {
                            throw new IllegalStateException("Did not find index of non empty agegroup.");
                        }
                    }
                    if (x >= indizes.length || indizes2[x] != indizes[x]) {
                        changedOrder = true;
                    }
                }

                LinkedList<? extends ASchwimmer> schwimmer = wk.getSchwimmer();
                ListIterator<? extends ASchwimmer> li = schwimmer.listIterator();
                while (li.hasNext()) {
                    ASchwimmer s = li.next();
                    s.updateAK(indizes2[s.getAKNummer()], true);
                }
                wk.getLaufliste().check();
                if (wk.getLauflisteOW() != null) {
                    wk.getLauflisteOW().check(changedOrder);
                }
                wk.getHLWListe().check();
                wk.changedNow();
                controller.sendDataUpdateEvent("ChangeRB",
                        UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_ZW_LIST_CHANGED | UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                                | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED,
                        MAltersklassenPlugin.this);
            }
        }

        @Override
        @SuppressWarnings({ "unchecked" })
        public void actionPerformed(ActionEvent event) {
            warner.information(controller.getWindow(), I18n.get("Information"), I18n.get("RulebookeditorLimited"), I18n.get("RulebookeditorLimited.Note"),
                    "Rulebookeditor");
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            Regelwerk aks = wk.getRegelwerk();
            boolean[] empty = new boolean[aks.size()];
            for (int x = 0; x < empty.length; x++) {
                empty[x] = !SearchUtils.hasSchwimmer(wk, aks.getAk(x));
            }
            JAKsEditor editor = new JAKsEditor(controller.getWindow(), wk.getRegelwerk(), wk instanceof EinzelWettkampf, empty);
            editor.setCallback(new EditorCallback(empty));
            ModalFrameUtil.showAsModal(editor, controller.getWindow());
        }
    }
}