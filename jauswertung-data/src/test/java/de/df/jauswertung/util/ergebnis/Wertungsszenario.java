package de.df.jauswertung.util.ergebnis;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;

/**
 * One age group, scored with a time based formula. Swimmers are given the points they should reach per discipline;
 * the scenario turns them into times of the formula, so the same scenario gives the same points for every formula.
 */
final class Wertungsszenario {

    static final String[] FORMELN = { FormelDLRG2007.ID, FormelDirectPoints.ID };

    private static final int REC = 6000;
    private static final Map<Double, Integer> DLRG_ZEITEN = Map.of(1000.0, 6000, 805.28, 7200, 583.25, 9000, 400.0,
            12000, 333.33, 15000, 266.67, 18000);

    private final String formel;
    private final EinzelWettkampf wk;
    private final Altersklasse ak;
    private final LinkedList<Zielrichterentscheid<Teilnehmer>> zielrichterentscheide = new LinkedList<>();

    Wertungsszenario(String formel, int disziplinen) {
        this(formel, disziplinen, false);
    }

    Wertungsszenario(String formel, int disziplinen, boolean hlw) {
        this.formel = formel;
        Disziplin[] d = new Disziplin[disziplinen];
        for (int x = 0; x < disziplinen; x++) {
            d[x] = new Disziplin("D" + (x + 1), REC, 50, 1);
        }
        Regelwerk regelwerk = new Regelwerk(1, true, formel);
        ak = new Altersklasse("AK", new Disziplin[][] { d, d }, hlw);
        ak.setStrafeIstDisqualifikation(false);
        regelwerk.setAk(0, ak);
        wk = new EinzelWettkampf(regelwerk, new Strafen());
    }

    Altersklasse ak() {
        return ak;
    }

    /**
     * @param punkte points per discipline; 0 means no time
     */
    Teilnehmer schwimmer(String name, double... punkte) {
        Teilnehmer t = wk.createTeilnehmer("", name, "", 2000, false, "Gliederung", 0, "");
        for (int x = 0; x < punkte.length; x++) {
            t.setDisciplineChoice(x, true);
            t.setZeit(x, zeit(punkte[x]));
        }
        wk.addSchwimmer(t);
        return t;
    }

    Teilnehmer schwimmerMitZeiten(String name, int... zeiten) {
        Teilnehmer t = wk.createTeilnehmer("", name, "", 2000, false, "Gliederung", 0, "");
        for (int x = 0; x < zeiten.length; x++) {
            t.setDisciplineChoice(x, true);
            t.setZeit(x, zeiten[x]);
        }
        wk.addSchwimmer(t);
        return t;
    }

    private int zeit(double punkte) {
        if (punkte == 0) {
            return 0;
        }
        if (formel.equals(FormelDirectPoints.ID)) {
            return (int) Math.round(punkte * 100);
        }
        Integer zeit = DLRG_ZEITEN.get(punkte);
        if (zeit == null) {
            throw new IllegalArgumentException("No time known for " + punkte + " points");
        }
        return zeit;
    }

    static void strafe(Teilnehmer t, int disziplin, Strafarten art, int punkte) {
        t.addStrafe(disziplin, new Strafe(art.name(), art.name().substring(0, 2), art, punkte));
    }

    static void allgemeineStrafe(Teilnehmer t, Strafarten art, int punkte) {
        strafe(t, ASchwimmer.DISCIPLINE_NUMBER_SELF, art, punkte);
    }

    static void hlw(Teilnehmer t, double punkte) {
        t.setHLWPunkte(0, punkte);
    }

    static void hlw(Teilnehmer t, HLWStates state) {
        t.setHLWState(0, state);
    }

    void zielrichterentscheid(int disziplin, Teilnehmer... reihenfolge) {
        Zielrichterentscheid<Teilnehmer> ze = new Zielrichterentscheid<>(List.of(reihenfolge));
        ze.setDisziplin(disziplin);
        zielrichterentscheide.add(ze);
        wk.setZielrichterentscheide(zielrichterentscheide);
    }

    String ergebnis() {
        return ergebnis(null, true);
    }

    /**
     * One line per swimmer in result order: name, place, points, "kW" for no rating, number of disciplines with a
     * severe penalty, and rank and points of every discipline.
     */
    String ergebnis(boolean[] auswahl, boolean zw) {
        SchwimmerResult<Teilnehmer>[] results = ResultCalculator.getResults(wk, ak, false, auswahl, zw);
        return java.util.Arrays.stream(results).map(Wertungsszenario::zeile).collect(Collectors.joining("\n"));
    }

    /**
     * Rank and points of every swimmer in one discipline, sorted by name.
     */
    String disziplin(int disziplin) {
        SchwimmerResult<Teilnehmer>[] results = ResultCalculator.getResults(wk, ak, false, null, true);
        return java.util.Arrays.stream(results).sorted(java.util.Comparator.comparing(r -> r.getSchwimmer().getNachname()))
                .map(r -> r.getSchwimmer().getNachname() + " " + r.getResults()[disziplin].getRank() + " "
                        + String.format(Locale.ROOT, "%.2f", r.getResults()[disziplin].getPoints()))
                .collect(Collectors.joining("\n"));
    }

    private static String zeile(SchwimmerResult<Teilnehmer> r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getSchwimmer().getNachname()).append(' ').append(r.getPlace()).append(' ')
                .append(String.format(Locale.ROOT, "%.4f", r.getPoints())).append(' ')
                .append(r.hasKeineWertung() ? "kW" : "-").append(' ').append(r.getDnf());
        for (SchwimmerData<Teilnehmer> d : r.getResults()) {
            sb.append(" | ").append(d.getRank()).append(' ')
                    .append(String.format(Locale.ROOT, "%.2f", d.getPoints()));
        }
        return sb.toString();
    }
}
