/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MKampfrichterPlugin extends ANullPlugin {

    private JMenuItem  neu;
    private JMenuItem  edit;
    private JMenu      menu;
    private CorePlugin core;

    public MKampfrichterPlugin() {
        // Nothing to do
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            menu = new JMenu(I18n.get("Referees"));

            neu = new JMenuItem(I18n.get("New"), IconManager.getSmallIcon("new"));
            neu.setToolTipText(I18n.getToolTip("NewReferees"));
            neu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    newReferees();
                }
            });

            edit = new JMenuItem(I18n.get("Edit"), IconManager.getSmallIcon("edit"));
            edit.setToolTipText(I18n.getToolTip("EditReferees"));
            edit.setEnabled(false);
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    showManager(false);
                }
            });

            menu.add(neu);
            menu.add(edit);
        }
        return new MenuInfo[] { new MenuInfo(I18n.get("Edit"), 200, menu, 980) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        edit.setEnabled(wk.getKampfrichterverwaltung() != null);
    }

    void newReferees() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        JNeueKV nkv = new JNeueKV(getController().getWindow());
        nkv.setVisible(true);
        if (nkv.getKampfrichter() == null) {
            return;
        }
        wk.setKampfrichterverwaltung(nkv.getKampfrichter());

        showManager(true);
        // getController().sendDataUpdateEvent("RefereesChanged", UpdateEventConstants.REASON_REFEREES_CHANGED, null);
    }

    void showManager(boolean createNew) {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        if (wk.getKampfrichterverwaltung() == null) {
            JNeueKV nkv = new JNeueKV(getController().getWindow());
            nkv.setVisible(true);
            if (nkv.getKampfrichter() == null) {
                return;
            }
            wk.setKampfrichterverwaltung(nkv.getKampfrichter());
        }
        JKampfrichterManager jkm = new JKampfrichterManager(getController().getWindow(), core, getController());
        if (createNew) {
            jkm.setChanged();
        }
        ModalFrameUtil.showAsModal(jkm, getController().getWindow());
    }
}