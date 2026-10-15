package de.df.jauswertung.test.results.indoorfinals.penalties;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_AAE_FASTEST_HEAT_UNTOUCHED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_AVOID_ALMOST_EMPTY;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_FIRST_HEAT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_RESPECT_QUALIFICATIONS;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_SORTING_ORDER;
import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

/**
 * One discipline of the international pool rulebook (formula ILS with finals, 8 lanes). Swimmer n is the n-th fastest
 * in every round, so without penalties start number, place and points of the table 20, 18, 16, 14, 13, …, 1 match.
 */
final class IlsScenario {

    private static final int STRAFPUNKTE = 3;

    enum Art {
        /** Penalty points with "penalty counts as disqualification" switched off in the age group. */
        STRAFPUNKTE(new Strafe("Strafpunkte", "SP", Strafarten.STRAFPUNKTE, IlsScenario.STRAFPUNKTE), false),
        /** Penalty points with "penalty counts as disqualification" switched on, as in the shipped rulebook. */
        STRAFPUNKTE_ALS_DISQUALIFIKATION(
                new Strafe("Strafpunkte", "SP", Strafarten.STRAFPUNKTE, IlsScenario.STRAFPUNKTE), true),
        NICHT_ANGETRETEN(Strafe.NICHT_ANGETRETEN, true),
        DISQUALIFIKATION(new Strafe("Disqualifikation", "DQ", Strafarten.DISQUALIFIKATION, 0), true),
        AUSSCHLUSS(new Strafe("Ausschluss", "AU", Strafarten.AUSSCHLUSS, 0), true),
        WITHDRAW(Strafe.WITHDRAW, true);

        private final Strafe strafe;
        private final boolean strafeIstDisqualifikation;

        Art(Strafe strafe, boolean strafeIstDisqualifikation) {
            this.strafe = strafe;
            this.strafeIstDisqualifikation = strafeIstDisqualifikation;
        }
    }

    private static final int AK = 0;
    private static final boolean MALE = true;
    private static final int DISCIPLINE = 0;

    private final EinzelWettkampf wk;
    private final int schwimmer;
    private final int runden;
    private final Map<Integer, Map<Integer, Art>> strafen = new HashMap<>();

    private IlsScenario(int schwimmer, int... qualifizierte) {
        this.schwimmer = schwimmer;
        this.runden = qualifizierte.length + 1;
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/International - Pool.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/International - Pool", true));
        stream(wk.getRegelwerk().getAks()).forEach(ak -> ak.setLaufsortierung(Reihenfolge.ILSPool.getValue()));
        Altersklasse ak = wk.getRegelwerk().getAk(AK);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            int[] ids = new int[runden];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = 100 * (y + 1) + i;
            }
            ak.getDisziplin(y, true).setRunden(qualifizierte, ids);
            ak.getDisziplin(y, false).setRunden(qualifizierte, ids);
        }
        for (int x = 1; x <= schwimmer; x++) {
            Teilnehmer t = wk.createTeilnehmer("", "Teilnehmer", "" + x, 2000, MALE, "Test", AK, "");
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                t.setDisciplineChoice(y, y == DISCIPLINE);
            }
            t.setMeldezeit(0, x);
            wk.addSchwimmer(t);
        }
    }

    /**
     * @param qualifizierte number of swimmers qualifying for the next round, one entry per round before the final
     */
    static IlsScenario mit(int schwimmer, int... qualifizierte) {
        return new IlsScenario(schwimmer, qualifizierte);
    }

    IlsScenario strafe(int runde, Art art, int... startnummern) {
        wk.getRegelwerk().getAk(AK).setStrafeIstDisqualifikation(art.strafeIstDisqualifikation);
        for (int startnummer : startnummern) {
            strafen.computeIfAbsent(runde, r -> new HashMap<>()).put(startnummer, art);
        }
        return this;
    }

    /** Creates and swims the heats round by round, so that penalties affect who qualifies for the next round. */
    IlsScenario schwimme() {
        for (int runde = 0; runde < runden; runde++) {
            OWSelection selection = selection(runde);
            EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);
            wkx.setProperty(HEATS_LANES, 8);
            wkx.setProperty(HEATS_FIRST_HEAT, 1);
            wkx.setProperty(HEATS_SORTING_ORDER, Reihenfolge.Regelwerk.getValue());
            wkx.setProperty(HEATS_AVOID_ALMOST_EMPTY, true);
            wkx.setProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED, true);
            wkx.setProperty(HEATS_RESPECT_QUALIFICATIONS, true);
            wkx.getLaufliste().erzeugen();
            HeatsUtils.save(wk, selection, wkx);

            OWDisziplin<Teilnehmer> d = wk.getLauflisteOW().getDisziplin(selection);
            Map<Integer, Art> strafenDerRunde = strafen.getOrDefault(runde, Map.of());
            for (int x = 1; x <= schwimmer; x++) {
                Teilnehmer t = SearchUtils.getSchwimmer(wk, x);
                if (d.contains(t)) {
                    Art art = strafenDerRunde.get(x);
                    if (art != Art.NICHT_ANGETRETEN) {
                        t.getEingabe(selection.getId(), true).setZeit(6000 + x);
                    }
                    if (art != null) {
                        t.getEingabe(selection.getId(), true).addStrafe(art.strafe);
                    }
                }
            }
        }
        return this;
    }

    private OWSelection selection(int runde) {
        return new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, DISCIPLINE, runde);
    }

    static final String KOPF = "Ergebnis;Startnummer;Platz;Punkte;Zeit;Strafe;ohne Wertung;Qualifiziert";

    /** The results of all rounds and the overall result, one CSV line per swimmer in the order of the result. */
    List<String> ergebnisse() {
        List<String> zeilen = new ArrayList<>();
        zeilen.add(KOPF);
        for (int runde = 0; runde < runden; runde++) {
            OWSelection selection = selection(runde);
            EinzelWettkampf wkx = (EinzelWettkampf) ResultUtils.createCompetitionFor(wk, selection);
            if (!selection.isFinal) {
                wkx.getRegelwerk().setFormelID(FormelILS.ID);
            }
            boolean letzteRunde = runde == runden - 1;
            int folgerunde = runde + 1;
            for (SchwimmerResult<Teilnehmer> r : ResultCalculator.getResults(wkx, wkx.getRegelwerk().getAk(AK), MALE)) {
                String qualifiziert = letzteRunde ? "" : startet(folgerunde, startnummer(r)) ? "ja" : "nein";
                zeilen.add(zeile(rundenname(runde), r, qualifiziert));
            }
        }
        EinzelWettkampf w = (EinzelWettkampf) ResultUtils.generateEinzelwertungswettkampf(wk, AK, MALE, false);
        for (SchwimmerResult<Teilnehmer> r : ResultCalculator.getResults(w, w.getRegelwerk().getAk(DISCIPLINE), MALE)) {
            zeilen.add(zeile("Gesamt", r, ""));
        }
        return zeilen;
    }

    private boolean startet(int runde, int startnummer) {
        return wk.getLauflisteOW().getDisziplin(selection(runde)).contains(SearchUtils.getSchwimmer(wk, startnummer));
    }

    private String rundenname(int runde) {
        if (runde == runden - 1) {
            return "Finale";
        }
        return runde == 0 ? "Vorlauf" : "Zwischenlauf";
    }

    /** Competitions for single results renumber the swimmers, so they are identified by the first name. */
    private static int startnummer(SchwimmerResult<Teilnehmer> r) {
        return Integer.parseInt(r.getSchwimmer().getVorname());
    }

    private static String zeile(String ergebnis, SchwimmerResult<Teilnehmer> r, String qualifiziert) {
        SchwimmerData<Teilnehmer> sd = r.getResults()[0];
        return String.join(";", ergebnis, "" + startnummer(r), r.getPlace() > 0 ? "" + r.getPlace() : "",
                punkte(r.getPoints()), zeit(sd.getTime()), strafe(sd), r.hasKeineWertung() ? "ja" : "", qualifiziert);
    }

    private static String punkte(double punkte) {
        return punkte == Math.rint(punkte) ? "" + (long) punkte : ("" + punkte).replace('.', ',');
    }

    private static String zeit(int hundertstel) {
        if (hundertstel <= 0) {
            return "";
        }
        return String.format("%d:%02d,%02d", hundertstel / 6000, hundertstel / 100 % 60, hundertstel % 100);
    }

    private static String strafe(SchwimmerData<Teilnehmer> sd) {
        Strafe s = sd.getStrafe();
        return switch (s.getArt()) {
            case NICHTS -> s.isWithdraw() ? "Withdraw" : "";
            case STRAFPUNKTE -> "Strafpunkte " + s.getStrafpunkte();
            case NICHT_ANGETRETEN -> "Nicht angetreten";
            case DISQUALIFIKATION -> "Disqualifikation";
            case AUSSCHLUSS -> "Ausschluss";
        };
    }
}
