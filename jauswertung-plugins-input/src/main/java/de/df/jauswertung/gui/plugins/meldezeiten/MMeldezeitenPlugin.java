package de.df.jauswertung.gui.plugins.meldezeiten;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MMeldezeitenPlugin extends ANullPlugin {

    private CorePlugin core = null;
    private MenuInfo[] menu = null;
    private JMenuItem item = null;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            item = new JMenuItem(I18n.get("Meldezeiten"));
            item.setToolTipText(I18n.getToolTip("Meldezeiten"));
            item.addActionListener(arg0 -> {
                showWindow();
            });
            menu = new MenuInfo[1];
            menu[0] = new MenuInfo(I18n.get("Prepare"), 510, item, 91);
            dataUpdated(null);
        }
        return menu;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (menu == null) {
            return;
        }
        item.setEnabled(core.getWettkampf().hasSchwimmer());
    }

    @SuppressWarnings({ "unchecked" })
    void showWindow() {
        @SuppressWarnings("rawtypes")
        ISimpleCallback<JMeldezeiten> sc = this::notifyChange;

        @SuppressWarnings("rawtypes")
        JMeldezeiten jm = new JMeldezeiten(getController().getWindow(), core.getWettkampf(), sc);
        ModalFrameUtil.showAsModal(jm, getController().getWindow());
    }

    /**
     * @param jm
     */
    @SuppressWarnings("rawtypes")
    void notifyChange(JMeldezeiten jm) {
        if (jm.hasChanged()) {
            getController().sendDataUpdateEvent("Meldezeiten", UpdateEventConstants.REASON_MELDEZEITEN_CHANGED, this);
        }
    }
}