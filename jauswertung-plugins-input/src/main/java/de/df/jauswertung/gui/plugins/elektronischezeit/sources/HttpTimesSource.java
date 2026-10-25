package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.lisasp.alphatiming.legacy.api.Heat;
import org.lisasp.alphatiming.legacy.api.LegacySerialization;

import de.df.jauswertung.io.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpTimesSource implements ITimesSource {

    @Override
    public Heat[] getHeats() {
        String address = SourcesConfig.getAddress();
        Optional<TimesServerAddress> server = TimesServerAddress.parse(address);
        if (server.isEmpty()) {
            log.warn("The timing server address {} is invalid", address);
            return null;
        }
        try {
            byte[] data = HttpUtils.download(server.get().heatsUrl());
            return LegacySerialization.fromXML(new ByteArrayInputStream(data));
        } catch (IOException e) {
            log.warn("Could not load the heats from {}", address, e);
            return null;
        }
    }
}
