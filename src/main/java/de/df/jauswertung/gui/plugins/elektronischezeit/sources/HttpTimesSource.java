package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.IOException;

import de.df.jauswertung.gui.plugins.elektronischezeit.MElektronischeZeitPlugin;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.HttpUtils;
import de.df.jauswertung.util.Utils;
import de.dm.ares.data.Heat;

public class HttpTimesSource implements ITimesSource {

    // private final MElektronischeZeitPlugin plugin;

    public HttpTimesSource(MElektronischeZeitPlugin parent) {
        // plugin = parent;
    }

    @Override
    public Heat[] getHeats() {
        String address = SourcesConfig.getAddress();
        if (address.equalsIgnoreCase("dummy") && Utils.isInDevelopmentMode()) {
            // return plugin.generateHeats();
        }
        try {
            byte[] data = HttpUtils.download("http://" + address + ":1999/heats.xml");
            return (Heat[]) InputManager.unserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}