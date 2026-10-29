package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.util.Optional;

record TimesServerAddress(String host, int port) {

    static final int DEFAULT_PORT = 1999;

    static Optional<TimesServerAddress> parse(String address) {
        String text = address.trim();
        int colon = text.indexOf(':');
        if (colon < 0) {
            return text.isEmpty() ? Optional.empty() : Optional.of(new TimesServerAddress(text, DEFAULT_PORT));
        }
        String host = text.substring(0, colon);
        try {
            int port = Integer.parseInt(text.substring(colon + 1));
            if (host.isEmpty() || port < 1 || port > 65535) {
                return Optional.empty();
            }
            return Optional.of(new TimesServerAddress(host, port));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    String heatsUrl() {
        return "http://" + host + ":" + port + "/legacy/heats.xml";
    }
}
