package de.df.jauswertung.gui.plugins.teammembersinput;

import de.df.jauswertung.gui.plugins.AFilePlugin;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.CorePlugin.Filemode;
import de.df.jauswertung.io.FileFilters;

public class MFilePlugin extends AFilePlugin<CorePlugin> {

    public MFilePlugin() {
        super("de.df.jauswertung.core", FileFilters.FF_TEAMMEMBERS, "Competition", "Teammembers", null);
    }

    @Override
    protected void prepare(CorePlugin core) {
        core.setMode(Filemode.Teammembers);
    }
}
