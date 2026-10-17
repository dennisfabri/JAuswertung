package de.df.jauswertung.gui.veranstaltung;

import java.nio.file.Path;

import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.Utils;

final class Veranstaltungsdatei {

    private Veranstaltungsdatei() {
    }

    static Veranstaltung lade(String dateiname) {
        if (!(InputManager.ladeObject(dateiname) instanceof Veranstaltung vs)) {
            return null;
        }
        vs.verweiseAufloesen(absolut(dateiname));
        return vs;
    }

    static boolean speichere(String dateiname, Veranstaltung vs) {
        Veranstaltung kopie = Utils.copy(vs);
        kopie.verweiseRelativieren(absolut(dateiname));
        return OutputManager.speichereObject(dateiname, kopie);
    }

    private static String absolut(String dateiname) {
        return Path.of(dateiname).toAbsolutePath().toString();
    }
}
