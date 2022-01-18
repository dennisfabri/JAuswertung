/*
 * Created on 18.04.2005
 */
package de.df.jauswertung.gui.plugins;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.emergency.Check;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class EmergencyPlugin extends ANullPlugin {

    CorePlugin core = null;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        return null;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isReason(UpdateEventConstants.REASON_LOAD_WK)) {
            try {
                @SuppressWarnings("rawtypes")
                AWettkampf wk = core.getWettkampf();
                new Check(this, this.getController(), core, "2022").check(wk);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
