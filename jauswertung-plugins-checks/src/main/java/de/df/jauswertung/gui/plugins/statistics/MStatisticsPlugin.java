/*
 * Created on 18.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MStatisticsPlugin extends ANullPlugin {

    private JMenuItem[] menu;
    private MenuInfo[] minfo;

    private CorePlugin core;

    public MStatisticsPlugin() {
        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(I18n.get("Statistics"));
        menu[0].setToolTipText(I18n.getToolTip("Statistics"));
        menu[0].setEnabled(false);
        menu[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showWindow();
            }
        });

        minfo = new MenuInfo[1];
        minfo[0] = new MenuInfo(I18n.get("Information"), 550, menu, 1020);
    }

    void showWindow() {
        ModalFrameUtil.showAsModal(new JStatisticsFrame(getController().getWindow(), core),
                getController().getWindow());
    }

    @Override
    public MenuInfo[] getMenues() {
        return minfo;
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        boolean b = core.getWettkampf().hasSchwimmer();
        menu[0].setEnabled(b);
    }
}