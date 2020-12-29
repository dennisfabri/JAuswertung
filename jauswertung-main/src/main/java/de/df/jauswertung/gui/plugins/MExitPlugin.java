/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MExitPlugin extends ANullPlugin {

    private static final String ITEM_STRING = I18n.get("Exit");
    private static final String MENU_STRING = I18n.get("File");

    private JMenuItem           exit        = new JMenuItem(ITEM_STRING, IconManager.getSmallIcon("exit"));

    IPluginManager              controller;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        controller = plugincontroller;
    }

    public MExitPlugin() {
        exit.setToolTipText(I18n.getToolTip("Exit"));
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                controller.quit();
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU_STRING, 100, exit, 1000) };
    }
}