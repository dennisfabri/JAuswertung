package de.df.jauswertung.gui.plugins.heatsview;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MHeatsViewPlugin extends ANullPlugin {

    CorePlugin core;
    WarningPlugin warner;
    private JMenuItem menu;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        warner = (WarningPlugin) plugincontroller.getFeature("de.df.jauswertung.warning", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (menu != null) {
            AWettkampf<?> wk = core.getWettkampf();
            menu.setEnabled(!wk.getLaufliste().isEmpty());
        }
    }

    void showHeatView() {
        warner.information(getController().getWindow(), I18n.get("Heatspresentation"), I18n.get("ShowHeats.Text"),
                I18n.get("ShowHeats.Note"), "ShowHeatsInfo");
        JHeatPresentationFrame.start(getController().getWindow(), core.getWettkampf());
    }

    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            menu = new JMenuItem(I18n.get("Heatspresentation"));
            if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
                menu.setEnabled(false);
            }
            menu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showHeatView();
                }
            });
        }
        return new MenuInfo[] { new MenuInfo(I18n.get("Information"), 550, menu, 2000) };
    }
}
