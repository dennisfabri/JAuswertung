/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
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
    private static final String MENU = I18n.get("Information");

    CorePlugin core;
    IPluginManager controller;

    private JMenuItem[] menu;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);

        controller = c;

        core = (CorePlugin) getController().getFeature("de.df.jauswertung.core", uid);

        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(ITEM);
        menu[0].setToolTipText(I18n.getToolTip("PenaltyCatalogue"));
        menu[0].addActionListener(arg0 -> {
            PenaltyUIUtils.showPenalties(controller.getWindow(), core.getStrafen());

        });
    } /*
       * (non-Javadoc)
       * 
       * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
       */

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 550, menu, 1000) };
    }
}