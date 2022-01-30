/*
 * Created on 05.04.2005
 */
package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_ZW_LIST_CHANGED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MZielrichterentscheidPlugin extends ANullPlugin {

    static final long          BITMASK = REASON_LAUF_LIST_CHANGED | REASON_ZW_LIST_CHANGED | REASON_PROPERTIES_CHANGED;
    private JMenuItem          menu;
    @SuppressWarnings("rawtypes")
    JZielrichterentscheidFrame dialog;
    CorePlugin                 core;

    public MZielrichterentscheidPlugin() {
        menu = new JMenuItem(I18n.get("Zielrichterentscheid"));
        menu.setToolTipText(I18n.getToolTip("Zielrichterentscheid"));
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showZielrichterentscheid();
            }
        });
        menu.setAccelerator(I18n.getKeyStroke("zielrichterentscheid"));
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new IllegalStateException("Core not available");
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("Execute"), 520, menu, 1000) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        menu.setEnabled(core.getWettkampf().hasLaufliste());
    }

    public boolean isZielrichterentscheidEnabled() {
        return core.getWettkampf().hasLaufliste();
    }

    public void showZielrichterentscheid() {
        JZielrichterentscheidCheckDialog.start(getController(), core.getWettkampf(), new Runnable() {
            @Override
            @SuppressWarnings({ "synthetic-access", "serial", "unchecked", "rawtypes" })
            public void run() {
                if (dialog == null) {
                    dialog = new JZielrichterentscheidFrame(getController().getWindow()) {
                        @Override
                        public void apply() {
                            super.apply();
                            getController().sendDataUpdateEvent(new UpdateEvent("Zielrichterentscheid",
                                    UpdateEventConstants.REASON_ZIELRICHTERENTSCHEID_CHANGED | UpdateEventConstants.REASON_POINTS_CHANGED,
                                    MZielrichterentscheidPlugin.this));
                        }
                    };
                }
                dialog.start(core.getWettkampf());
            }
        });
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