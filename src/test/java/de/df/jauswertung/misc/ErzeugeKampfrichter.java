/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.misc;

import static de.df.jauswertung.daten.kampfrichter.KampfrichterStufe.*;

import java.io.FileNotFoundException;

import de.df.jauswertung.daten.kampfrichter.*;
import de.df.jauswertung.io.OutputManager;

public final class ErzeugeKampfrichter {

    private ErzeugeKampfrichter() {
        // Hide constructor
    }

    private static void karisFWK() {
        KampfrichterVerwaltung kt = new KampfrichterVerwaltung();

        kt.addEinheit("Veranstaltungsleitung");
        KampfrichterEinheit ke = kt.getEinheit(0);
        ke.addPosition("Veranstaltungsleiter");
        ke.addPosition("Veranstaltungssprecher");
        ke.addPosition("Protokollführer");

        kt.addEinheit("Schiedsgericht");
        ke = kt.getEinheit(1);
        ke.addPosition("Leiter", KEINE);
        ke.addPosition("Schiedsrichter", KEINE);

        kt.addEinheit("Wettkampfleitung");
        ke = kt.getEinheit(2);
        ke.addPosition("Wettkampfleiter", KEINE);

        kt.addEinheit("Kampfgericht");
        ke = kt.getEinheit(3);
        ke.addPosition("Starter", KEINE);
        ke.addPosition("Auswerter", KEINE);
        ke.addPosition("Zeitnehmerobmann", KEINE);
        ke.addPosition("Zeitnehmer", KEINE, 6);
        ke.addPosition("Wenderichter", KEINE);
        ke.addPosition("Schwimmrichter", KEINE);
        ke.addPosition("Zielrichter", KEINE);

        OutputManager.speichereObject("referees/0. Freundschaftswettkampf.kv", kt);
    }

    private static void karisBM() {
        KampfrichterVerwaltung kt = new KampfrichterVerwaltung();

        kt.addEinheit("Veranstaltungsleitung");
        KampfrichterEinheit ke = kt.getEinheit(0);
        ke.addPosition("Veranstaltungsleiter");
        ke.addPosition("Veranstaltungssprecher");
        ke.addPosition("Protokollführer");

        kt.addEinheit("Schiedsgericht");
        ke = kt.getEinheit(1);
        ke.addPosition("Leiter", D12);
        ke.addPosition("Schiedsrichter", D12);

        kt.addEinheit("Wettkampfleitung");
        ke = kt.getEinheit(2);
        ke.addPosition("Wettkampfleiter", D12);

        kt.addEinheit("Kampfgericht");
        ke = kt.getEinheit(3);
        ke.addPosition("Starter", E1);
        ke.addPosition("Auswerter", E1);
        ke.addPosition("Zeitnehmerobmann", F1);
        ke.addPosition("Zeitnehmer", F1, 12);
        ke.addPosition("Wenderichter", F1, 2);
        ke.addPosition("Schwimmrichter", E1, 2);
        ke.addPosition("Zielrichter", F1, 3);

        OutputManager.speichereObject("referees/1. Bezirksmeisterschaften.kv", kt);
    }

    private static void karisLM() {
        KampfrichterVerwaltung kt = new KampfrichterVerwaltung();

        kt.addEinheit("Veranstaltungsleitung");
        KampfrichterEinheit ke = kt.getEinheit(0);
        ke.addPosition("Veranstaltungsleiter");
        ke.addPosition("Veranstaltungssprecher");
        ke.addPosition("Protokollführer");

        kt.addEinheit("Schiedsgericht");
        ke = kt.getEinheit(1);
        ke.addPosition("Leiter", D12);
        ke.addPosition("Schiedsrichter", D12, 2);

        kt.addEinheit("Wettkampfleitung");
        ke = kt.getEinheit(2);
        ke.addPosition("Wettkampfleiter", D12, 6);

        kt.addEinheit("Kampfgericht");
        ke = kt.getEinheit(3);
        ke.addPosition("Starter", E1);
        ke.addPosition("Auswerter", E1);
        ke.addPosition("Zeitnehmerobmann", F1);
        ke.addPosition("Zeitnehmer", F1, 12);
        ke.addPosition("Wenderichter", F1, 3);
        ke.addPosition("Schwimmrichter", E1, 3);
        ke.addPosition("Zielrichter", F1, 3);

        OutputManager.speichereObject("referees/2. Landesmeisterschaften.kv", kt);
    }

    private static void karisDM() {
        KampfrichterVerwaltung kt = new KampfrichterVerwaltung();

        kt.addEinheit("Veranstaltungsleitung");
        KampfrichterEinheit ke = kt.getEinheit(0);
        ke.addPosition("Veranstaltungsleiter");
        ke.addPosition("Veranstaltungssprecher");
        ke.addPosition("Protokollführer");

        kt.addEinheit("Schiedsgericht");
        ke = kt.getEinheit(1);
        ke.addPosition("Leiter", D12);
        ke.addPosition("Schiedsrichter", D12, 2);

        kt.addEinheit("Wettkampfleitung");
        ke = kt.getEinheit(2);
        ke.addPosition("Wettkampfleiter", D12, 6);

        kt.addEinheit("Kampfgericht");
        ke = kt.getEinheit(3);
        ke.addPosition("Starter", E1);
        ke.addPosition("Auswerter", E1);
        ke.addPosition("Zeitnehmerobmann", E1);
        ke.addPosition("Zeitnehmer", E1, 8);
        ke.addPosition("Wenderichter", E1, 4);
        ke.addPosition("Schwimmrichter", E1, 4);
        ke.addPosition("Zielrichter", E1, 3);

        OutputManager.speichereObject("referees/3. Deutsche Meisterschaften.kv", kt);
    }

    public static void main(String[] args) throws FileNotFoundException {
        karisFWK();
        karisBM();
        karisLM();
        karisDM();
    }
}