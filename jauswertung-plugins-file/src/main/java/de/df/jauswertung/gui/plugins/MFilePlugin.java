package de.df.jauswertung.gui.plugins;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.FileFilters;
import de.df.jutils.plugin.IPluginManager;

public class MFilePlugin extends AFilePlugin {

    public MFilePlugin() {
        super(FileFilters.FF_COMPETITION, null, MFilePlugin::neuerWettkampf);
    }

    @SuppressWarnings("rawtypes")
    private static void neuerWettkampf(IPluginManager controller, CorePlugin core) {
        if (controller.acceptWarning()) {
            JNeuerWettkampf jnw = new JNeuerWettkampf(controller.getWindow());
            jnw.setVisible(true);
            AWettkampf wk = jnw.getWettkampf();
            if (wk != null) {
                core.setWettkampf(wk, true);
            }
        }
    }
}
