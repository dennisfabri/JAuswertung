package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import de.df.jauswertung.util.Utils;

public class SourcesConfig {

    public static Sources getSource() {
        String text = Utils.getPreferences().get("TimeSource", "aresfile");
        for (Sources source : Sources.class.getEnumConstants()) {
            if (source.toString().equals(text)) {
                return source;
            }
        }
        return Sources.http;
    }

    public static void setSource(Sources source) {
        Utils.getPreferences().put("TimeSource", source == null ? Sources.http.toString() : source.toString());
    }

    public static String getAddress() {
        return Utils.getPreferences().get("TimeServerAdress", "localhost");
    }

    public static void setAddress(String address) {
        Utils.getPreferences().put("TimeServerAdress", address == null ? "" : address);
    }

    public static String getAresfile() {
        return Utils.getPreferences().get("TimeAresfile", "");
    }

    public static void setAresfile(String file) {
        Utils.getPreferences().put("TimeAresfile", file == null ? "" : file);

    }
}
