/*
 * Created on 05.04.2005
 */
package de.df.jauswertung.gui.plugins.properties;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_ZW_LIST_CHANGED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.window.JOptionsDialog.OptionsListener;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MPropertiesPlugin extends ANullPlugin {

    static final long BITMASK = REASON_LAUF_LIST_CHANGED | REASON_ZW_LIST_CHANGED | REASON_PROPERTIES_CHANGED;
    private JMenuItem menu;
    JPropertiesDialog dialog;
    CorePlugin        core;
    WarningPlugin     warn;

    public MPropertiesPlugin() {
        menu = new JMenuItem(I18n.get("Properties"), IconManager.getSmallIcon("eigenschaften"));
        menu.setToolTipText(I18n.getToolTip("Properties"));
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showProperties();
            }
        });
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new NullPointerException("Core not available");
        }
        warn = (WarningPlugin) plugincontroller.getFeature("de.df.jauswertung.warning", pluginuid);
        if (warn == null) {
            throw new NullPointerException("Warn not available");
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("File"), 100, menu, 400) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do
    }

    void showProperties() {
        dialog = new JPropertiesDialog(MPropertiesPlugin.this.getController().getWindow());
        dialog.addOptionsListener(new OptionsListener() {
            @Override
            public void apply() {
                change();
            }

            @Override
            public void cancel() {
                // Nothing to do
            }
        });
        if (checkListen(core.getWettkampf())) {
            warn.information(getController().getWindow(), I18n.get("Properties"), I18n.get("PropertiesWarning"), I18n.get("PropertiesWarning.Note"),
                    "properties");
        }
        dialog.start(core.getWettkampf());
    }

    @SuppressWarnings("rawtypes")
    private boolean checkListen(AWettkampf wk) {
        Laufliste ll = wk.getLaufliste();
        return !ll.isEmpty();
    }

    void change() {
        try {
            dialog.apply();
            getController().sendDataUpdateEvent("ChangeProperties", BITMASK, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}