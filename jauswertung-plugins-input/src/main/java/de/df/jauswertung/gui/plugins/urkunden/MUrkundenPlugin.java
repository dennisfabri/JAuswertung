/*
 * Created on 17.03.2007
 */
package de.df.jauswertung.gui.plugins.urkunden;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

public class MUrkundenPlugin extends ANullPlugin {

    private MenuInfo[] menues;
    private CorePlugin core;

    public MUrkundenPlugin() {
        JMenuItem item = new JMenuItem(I18n.get("Mehrkampf"));
        item.addActionListener(arg0 -> {
            showEditor(false);
        });
        item.setToolTipText(I18n.getToolTip("MehrkampfurkundeBearbeiten"));
        JMenuItem item2 = new JMenuItem(I18n.get("Einzelwertung"));
        item2.addActionListener(arg0 -> {
            showEditor(true);
        });
        item2.setToolTipText(I18n.getToolTip("EinzelwertungsurkundeBearbeiten"));

        JMenu menu = new JMenu(I18n.get("Documents"));
        menu.setToolTipText(I18n.getToolTip("EditDocuments"));
        menu.add(item);
        menu.add(item2);

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Execute"), 520, menu, 3000);
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        return menues;
    }

    @SuppressWarnings({ "unchecked" })
    <T extends ASchwimmer> void showEditor(boolean einzelwertung) {
        JUrkundenEditor<T> ue = new JUrkundenEditor<>(getController().getWindow(), (AWettkampf<T>) core.getWettkampf(),
                getController(), einzelwertung);
        ModalFrameUtil.showAsModal(ue, getController().getWindow());
    }
}