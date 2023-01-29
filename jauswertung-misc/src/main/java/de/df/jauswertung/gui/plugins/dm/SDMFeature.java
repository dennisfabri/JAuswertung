package de.df.jauswertung.gui.plugins.dm;

import de.df.jauswertung.gui.plugins.doping.MDopingPlugin;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class SDMFeature extends AFeature {

    @Override
    @SuppressWarnings("rawtypes")
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        // TODO Auto-generated method stub
        super.setController(plugincontroller, pluginuid);

        MDopingPlugin selection = (MDopingPlugin) plugincontroller.getFeature("de.df.jauswertung.doping", pluginuid);
        selection.addSelector(new DMSelector());
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do
    }
}