package de.df.jauswertung.misc.anonymizer;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;

import java.util.Random;

public class WettkampfAnonymizer {

    public static void main(String[] args) {
        // anonymizeWettkampf("jauswertung-io/src/test/resources/competitions/dem24/DEM2024_Mannschaft_Vorlauf_Final.wk");
        // anonymizeWettkampf("jauswertung-io/src/test/resources/competitions/dem24/DEM2024_Mixed_Vorlauf_Final.wk");
    }

    private static void anonymizeWettkampf(String filename) {
        AWettkampf<?> wk = InputManager.ladeWettkampf(filename);
        wk.getSchwimmer().forEach(schwimmer -> anonymize(schwimmer));
        OutputManager.speichereWettkampf(filename.replace(".wk", "_anonym.wk"), wk);
    }

    private static void anonymize(ASchwimmer schwimmer) {
        if (schwimmer instanceof Teilnehmer t) {
            anonymizeTeilnehmer(t);
        } else {
            anonymizeMannschaft((Mannschaft) schwimmer);
        }
    }

    private static void anonymizeMannschaft(Mannschaft m) {
        m.setName(generateRandomName());
        m.setGliederung(generateRandomName());
        for (int x = 0; x < m.getMannschaftsmitgliederAnzahl(); x++) {
            anonymize(m.getMannschaftsmitglied(x));
        }
        m.setQualifikationsebene("");
    }

    private static void anonymize(Mannschaftsmitglied mannschaftsmitglied) {
        if (mannschaftsmitglied == null) {
            return;
        }
        if (!mannschaftsmitglied.getVorname().isBlank()) {
            mannschaftsmitglied.setVorname(generateRandomName());
            mannschaftsmitglied.setNachname(generateRandomName());
            mannschaftsmitglied.setJahrgang(0);
        }
    }

    private static void anonymizeTeilnehmer(Teilnehmer t) {
        t.setVorname(generateRandomName());
        t.setNachname(generateRandomName());
        t.setGliederung(generateRandomName());
        t.setQualifikationsebene("");
        t.setJahrgang(0);
    }

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";

    private static Random random = new Random();

    private static String generateRandomName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
