package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.IOException;

import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.util.HttpUtils;
import de.dm.ares.data.Heat;

public class HttpTimesSource implements ITimesSource {

    @Override
    public Heat[] getHeats() {
        try {
            String address = SourcesConfig.getAddress();
            byte[] data = HttpUtils.download("http://" + address + ":1999/legacy/heats.xml");
            return (Heat[]) InputManager.unserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}