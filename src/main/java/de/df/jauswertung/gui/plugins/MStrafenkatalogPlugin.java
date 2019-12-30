/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MStrafenkatalogPlugin extends ANullPlugin {

    private static final String ITEM = I18n.get("PenaltyCatalog");
    private static final String MENU = I18n.get("Extras");

    CorePlugin                  core;
    IPluginManager              controller;

    private JMenuItem[]         menu;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);

        controller = c;

        core = (CorePlugin) getController().getFeature("de.df.jauswertung.core", uid);

        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(ITEM);
        menu[0].setToolTipText(I18n.getToolTip("PenaltyCatalogue"));
        menu[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                PenaltyUtils.showPenalties(controller.getWindow(), core.getStrafen());

            }
        });
    } /*
       * (non-Javadoc)
       * @see
       * de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
       */

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 900, menu, 600) };
    }
}