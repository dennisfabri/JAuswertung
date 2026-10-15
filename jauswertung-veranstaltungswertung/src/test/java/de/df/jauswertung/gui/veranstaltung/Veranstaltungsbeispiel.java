package de.df.jauswertung.gui.veranstaltung;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.veranstaltung.CompetitionContainer;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

/**
 * Event of several competitions, saved as files like the event scoring expects them.
 */
final class Veranstaltungsbeispiel {

    private final Path verzeichnis;
    private final List<CompetitionContainer> wettkaempfe = new ArrayList<>();

    Veranstaltungsbeispiel(Path verzeichnis) {
        this.verzeichnis = verzeichnis;
    }

    static EinzelWettkampf wettkampf(String... altersklassen) {
        Disziplin[] d = { new Disziplin("D1", 6000, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(altersklassen.length, true, FormelDLRG2007.ID);
        for (int x = 0; x < altersklassen.length; x++) {
            regelwerk.setAk(x, new Altersklasse(altersklassen[x], new Disziplin[][] { d, d }, false));
        }
        return new EinzelWettkampf(regelwerk, new Strafen());
    }

    /**
     * @param zeit 0 means no time yet, so the swimmer has no points
     */
    static Teilnehmer schwimmer(EinzelWettkampf wk, int ak, boolean maennlich, String gliederung, int zeit) {
        Teilnehmer t = wk.createTeilnehmer("", "Name", "Vorname", 2000, maennlich, gliederung, ak, "");
        t.setZeit(0, zeit);
        wk.addSchwimmer(t);
        return t;
    }

    Veranstaltungsbeispiel mit(String name, EinzelWettkampf wk) {
        String datei = verzeichnis.resolve(name + ".wk").toString();
        if (!OutputManager.speichereWettkampf(datei, wk)) {
            throw new IllegalStateException("Could not save " + datei);
        }
        wettkaempfe.add(new CompetitionContainer(name, datei));
        return this;
    }

    Veranstaltung veranstaltung() {
        Veranstaltung vs = new Veranstaltung();
        vs.setCompetitions(wettkaempfe);
        return vs;
    }

    /**
     * "Einzel" with AK 12 not yet swum and AK 13/14 swum, "Mannschaft" with AK 12 swum.
     */
    static Veranstaltung ersteAltersklasseOhnePunkte(Path verzeichnis) {
        EinzelWettkampf einzel = wettkampf("AK 12", "AK 13/14");
        schwimmer(einzel, 0, false, "Nord", 0);
        schwimmer(einzel, 1, false, "Nord", 6500);
        schwimmer(einzel, 1, false, "Süd", 7000);
        EinzelWettkampf mannschaft = wettkampf("AK 12");
        schwimmer(mannschaft, 0, false, "Nord", 7000);
        schwimmer(mannschaft, 0, false, "Süd", 6500);
        return new Veranstaltungsbeispiel(verzeichnis).mit("Einzel", einzel).mit("Mannschaft", mannschaft)
                .veranstaltung();
    }
}
