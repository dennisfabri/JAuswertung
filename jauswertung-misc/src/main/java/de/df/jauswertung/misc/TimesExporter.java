package de.df.jauswertung.misc;

import java.io.File;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.io.InputManager;

public class TimesExporter {

    public static void main(String[] args) {

        for (int x = 2010; x < 2019; x++) {
            export(String.format("../../required/archive/dm%s-mannschaft.wk", x), String.format("DM%s", x));
            export(String.format("../../required/archive/dm%s-einzel.wk", x), String.format("DM%s", x));
        }

        for (int x = 2011; x < 2019; x++) {
            export(String.format("../../required/archive/dsm%s-mannschaft.wk", x), String.format("DSM%s", x));
            export(String.format("../../required/archive/dsm%s-einzel.wk", x), String.format("DSM%s", x));
        }
    }

    private static void export(String file, String title) {
        if (!new File(file).exists()) {
            System.err.println("-- Not found: " + file);
            return;
        }
        AWettkampf<?> wk1 = InputManager.ladeWettkampf(file);
        if (wk1 == null) {
            System.err.println("-- Could not load: " + file);
            return;
        }

        String timestamp = wk1.getStringProperty(PropertyConstants.DATE, "01.01.2000");
        if ("".equals(timestamp)) {
            timestamp = "01.01.2000";
        }
        if (timestamp.startsWith("Freitag, ")) {
            timestamp = timestamp.substring("Freitag, ".length());
        }
        if (timestamp.startsWith("Samstag, ")) {
            timestamp = timestamp.substring("Samstag, ".length());
        }
        if (timestamp.startsWith("Sonntag, ")) {
            timestamp = timestamp.substring("Sonntag, ".length());
        }

        if (timestamp.length() == "01.01.2000".length()) {
            timestamp = String.format("%s-%s-%s", timestamp.substring(6, 10), timestamp.substring(0, 2),
                    timestamp.substring(3, 5));
        }

        for (ASchwimmer s : wk1.getSchwimmer()) {
            Altersklasse ak = s.getAK();
            for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                if (s.isDisciplineChosen(x)) {
                    int zeit = s.getZeit(x);
                    Strafe str = s.getAkkumulierteStrafe(x);
                    String strafe = "";
                    if (str.isStrafe()) {
                        strafe = PenaltyUtils.getPenaltyShortText(str, ak);
                    }
                    System.out.println(String.format(
                            "EXECUTE [dbo].[StoreTime] '%s',%s,'%s','%s',%s,'%s',%s,'%s','%s','%s','%s'", ak.getName(),
                            (s instanceof Mannschaft) ? 1 : 0, ak.getDisziplin(x, s.isMaennlich()), s.getName(),
                            s.isMaennlich() ? 1 : 0, s.getBemerkung(),
                            zeit, strafe, timestamp, s.getGliederungMitQGliederung(), title, "xxxx"));
                }
            }
        }
    }

}
