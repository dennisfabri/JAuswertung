package de.df.jauswertung.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.ergebnis.*;

import static de.df.jauswertung.daten.PropertyConstants.*;

public class ResultUtils {

    public static <T extends ASchwimmer> AWettkampf<T> convertResultsToMeldung(AWettkampf<T> wk, boolean alltimes) {
        wk = Utils.copy(wk);
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                Results<T> results = new Results<>(ResultCalculator.getResults(wk, aks.getAk(x), y == 1, null, true));
                // JResultTable result = JResultTable.getResultTable(wk, aks.getAk(x), y == 1,
                // false, true, 0);
                for (int z = 0; z < results.size(); z++) {
                    ASchwimmer s = results.getSchwimmer(z);
                    s.setMeldepunkte(0, results.getPoints(z));
                    s.setMeldungMitProtokoll(0, true);
                    for (int w = 0; w < s.getAK().getDiszAnzahl(); w++) {
                        Strafe str = s.getAkkumulierteStrafe(w);
                        switch (str.getArt()) {
                        case AUSSCHLUSS:
                        case DISQUALIFIKATION:
                        case NICHT_ANGETRETEN:
                            if (alltimes) {
                                s.setMeldezeit(w, s.getZeit(w));
                            } else {
                                s.setMeldezeit(w, 0);
                            }
                            break;
                        case NICHTS:
                        case STRAFPUNKTE:
                            if (alltimes || str.getStrafpunkte() <= 0) {
                                s.setMeldezeit(w, s.getZeit(w));
                            } else {
                                s.setMeldezeit(w, 0);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return wk;
    }

    public enum IncludeSex {
        Female, Male, Both
    }

    @SuppressWarnings({ "unchecked" })
    private static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampfI(AWettkampf<T> wk, int akn,
            IncludeSex filter, boolean removeEmpty, boolean forQualification) {
        if (wk == null) {
            throw new NullPointerException();
        }
        boolean einzel = wk instanceof EinzelWettkampf;
        Altersklasse ak = wk.getRegelwerk().getAk(akn);
        if (!ak.hasEinzelwertung()) {
            return null;
        }
        int anzahl = ak.getDiszAnzahl();
        Regelwerk aks = new Regelwerk(ak.getDiszAnzahl(), einzel, wk.getRegelwerk().getFormelID());
        wk.getRegelwerk().copyTranslationsTo(aks);
        for (int x = 0; x < anzahl; x++) {
            Altersklasse a = aks.getAk(x);
            a.setMemberCounts(ak.getMinMembers(), ak.getMaxMembers());
            a.setName(ak.getName());
            a.setDiszAnzahl(1);
            a.setDisziplin(ak.getDisziplin(x, true), 0, true);
            a.setDisziplin(ak.getDisziplin(x, false), 0, false);
            a.setStrafeIstDisqualifikation(ak.isStrafeIstDisqualifikation());
            a.setHLW(ak.hasHLW() && ak.isEinzelwertungHlw());
        }
        @SuppressWarnings("rawtypes")
        AWettkampf w = null;
        EinzelWettkampf ewk = null;
        MannschaftWettkampf mwk = null;
        if (einzel) {
            w = new EinzelWettkampf(aks, wk.getStrafen());
            ewk = (EinzelWettkampf) w;
        } else {
            w = new MannschaftWettkampf(aks, wk.getStrafen());
            mwk = (MannschaftWettkampf) w;
        }
        wk.copyPropertiesTo(w);
        int min = 0;
        int max = 2;
        switch (filter) {
        case Female:
            min = 0;
            max = 1;
            break;
        case Male:
            min = 1;
            max = 2;
            break;
        case Both:
            min = 0;
            max = 2;
            break;
        }

        for (int z = min; z < max; z++) {
            OWLaufliste<T> llow = wk.getLauflisteOW();
            OWLaufliste<T> llNeu = w.getLauflisteOW();
            if (llow != null && llNeu != null) {
                for (OWDisziplin<T> owd : llow.getDisziplinen()) {
                    if (owd.akNummer == akn && owd.maennlich == (z == 1)) {
                        OWDisziplin<T> neu = llNeu.addDisziplin(owd.disziplin, z == 1, 0, owd.round);
                        for (OWLauf<T> lauf : owd.laeufe) {
                            OWLauf<T> laufNeu = new OWLauf<>(w, neu.Id, lauf, false);
                            neu.laeufe.add(laufNeu);
                        }
                    }
                }
            }
        }

        w.disableUpdates();
        for (int z = min; z < max; z++) {
            Results<T> results = new Results<>(ResultCalculator.getResults(wk, ak, z == 1, null, true));
            for (int y = 0; y < results.size(); y++) {
                ASchwimmer s = results.getSchwimmer(y);
                if (removeEmpty && results.hasKeineWertung(y)) {
                    continue;
                }
                if (forQualification && s.hasWithdrawn(0)) {
                    continue;
                }
                boolean include = true;
                if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                    if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                        include = false;
                    }
                }
                for (int x = 0; x < anzahl; x++) {
                    if (include) {
                        if (s.isDisciplineChosen(x)) {
                            ASchwimmer neu;
                            if (einzel) {
                                Teilnehmer t = (Teilnehmer) s;
                                neu = ewk.createTeilnehmer(t.getCompetitorId(), t.getNachname(), t.getVorname(), t.getJahrgang(),
                                        t.isMaennlich(), t.getGliederung(), x, t.getBemerkung());
                            } else {
                                Mannschaft t = (Mannschaft) s;
                                neu = mwk.createMannschaft(t.getName(), t.isMaennlich(), t.getGliederung(), x,
                                        t.getBemerkung());
                                Mannschaft mneu = (Mannschaft) neu;
                                for (int i = 0; i < t.getMaxMembers(); i++) {
                                    t.getMannschaftsmitglied(i).copyTo(mneu.getMannschaftsmitglied(i));
                                }
                            }

                            neu.setAusserKonkurrenz(s.isAusserKonkurrenz());
                            neu.setZeit(0, s.getZeit(x));
                            neu.setStrafen(0, s.getStrafen(x));
                            neu.setStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                                    s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));
                            neu.setStarter(0, s.getStarter(x));
                            neu.setBemerkung("" + s.getStartnummer());
                            neu.setQualifikationsebene(s.getQualifikationsebene());
                            neu.setQualifikation(s.getQualifikation());

                            w.addSchwimmer(neu);

                            if (wk.isHeatBased()) {
                                OWLaufliste<T> llow = wk.getLauflisteOW();
                                OWLaufliste<T> llNeu = w.getLauflisteOW();

                                for (OWDisziplin<T> owd : llow.getDisziplinen()) {
                                    if (owd.akNummer == akn && owd.maennlich == s.isMaennlich() && owd.disziplin == x) {

                                        Eingabe e1 = s.getEingabe(owd.Id);
                                        if (e1 != null) {
                                            Eingabe e = neu.getEingabe(
                                                    OWDisziplin.getId(x, s.isMaennlich(), 0, owd.round), true);
                                            e.setStarter(e1.getStarter());
                                            e.setZeit(e1.getZeit());
                                            for (Strafe str : e1.getStrafen()) {
                                                e.addStrafe(str);
                                            }
                                            OWDisziplin<T> OWDneu = llNeu.getDisziplin(owd.disziplin, z == 1, 0,
                                                    owd.round);

                                            boolean found = false;

                                            for (OWLauf<T> lauf : owd.laeufe) {
                                                int lane = lauf.GetSchwimmerIndex(s.getStartnummer());
                                                if (lane >= 0) {
                                                    for (OWLauf<T> laufNeu : OWDneu.getLaeufe()) {
                                                        if (laufNeu.getLaufnummer() == lauf.getLaufnummer() && laufNeu
                                                                .getLaufbuchstabe() == lauf.getLaufbuchstabe()) {
                                                            found = true;
                                                            laufNeu.setSchwimmer(lane, (T) neu);
                                                        }
                                                    }
                                                }
                                            }
                                            if (found) {
                                                OWDneu.addSchwimmer((T) neu);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!w.getLaufliste().isEmpty()) {
            w.getLaufliste().removeEmptyHeats();
            w.getLaufliste().neueNummerierung();
        }

        if (!wk.getLaufliste().isEmpty() && wk.isHeatBased()) {
            int index = 0;
            w.getLaufliste().resetAll();
            for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
                Lauf<T> laufNeu = w.getLaufliste().add(index);
                laufNeu.setLaufnummer(lauf.getLaufnummer());
                laufNeu.setLaufbuchstabe(lauf.getLaufbuchstabe());
                for (int b = 0; b < lauf.getBahnen(); b++) {
                    boolean hasSwimmerAtLane = lauf.getSchwimmer(b) != null;
                    if (hasSwimmerAtLane) {
                        final int laneNumber = b;
                        Stream<T> stream = w.getSchwimmer().stream();
                        Stream<T> ts = stream
                                .filter(sw -> sw.getBemerkung()
                                        .equals("" + lauf.getSchwimmer(laneNumber).getStartnummer()));
                        T t = ts.filter(sw -> sw.getAKNummer() == lauf.getDisznummer(laneNumber)).toList().getFirst();
                        boolean disciplinesMatch = t != null;
                        if (disciplinesMatch) {
                            laufNeu.setSchwimmer(t, 0, b);
                        }
                    }
                }
                index++;
            }
        }

        w.enableUpdates();
        if (!w.hasSchwimmer()) {
            // return null;
        }

        LinkedList<Zielrichterentscheid<T>> neuezes = new LinkedList<>();
        LinkedList<Zielrichterentscheid<T>> zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk)[0];
        for (Zielrichterentscheid<T> ze : zes) {
            Zielrichterentscheid<T> neu = new Zielrichterentscheid<>();
            for (T t : ze.getSchwimmer()) {
                if (t.getAKNummer() == akn) {
                    LinkedList<T> ll = SearchUtils.getSchwimmer(w, w.getRegelwerk().getAk(ze.getDisziplin()),
                            t.isMaennlich(), "" + t.getStartnummer());
                    if (ll.size() == 1) {
                        neu.addSchwimmer(ll.getFirst());
                    }
                }
            }
            if (neu.isValid()) {
                neuezes.addLast(neu);
            }
        }
        w.setZielrichterentscheide(neuezes);

        wk.copyPropertiesTo(w);

        w.setFilter(wk.getFilter());
        w.setCurrentFilterIndex(wk.getCurrentFilterIndex());

        w.changedNow();

        return w;
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, int akn,
            boolean maennlich, boolean removeEmpty) {
        return generateEinzelwertungswettkampfI(wk, akn, maennlich ? IncludeSex.Male : IncludeSex.Female, removeEmpty,
                false);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, int akn,
            boolean removeEmpty) {
        return generateEinzelwertungswettkampfI(wk, akn, IncludeSex.Both, removeEmpty, false);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampfForQualification(AWettkampf<T> wk,
            int akn, boolean removeEmpty) {
        return generateEinzelwertungswettkampfI(wk, akn, IncludeSex.Both, removeEmpty, true);
    }

    public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, String wgname,
            boolean removeEmpty) {
        wk = Utils.copy(wk);
        Wertungsgruppe wg = wk.getRegelwerk().getWertungsgruppe(wgname);
        int index = -1;
        wk.disableUpdates();
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            if (wg.getName().equals(ak.getWertungsgruppe())) {
                LinkedList<T> temp = SearchUtils.getSchwimmer(wk, ak);
                if (index < 0) {
                    index = x;
                    ak.setEinzelwertung(wg.isProtokollMitEinzelwertung());
                    ak.setEinzelwertungHlw(wg.isEinzelwertungHlw());
                    ak.setStrafeIstDisqualifikation(wg.isStrafeIstDisqualifikation());
                    ak.setName(wg.getName());
                } else {
                    for (T t : temp) {
                        t.setAKNummer(index, false);
                    }
                }
            } else {
                for (T t : SearchUtils.getSchwimmer(wk, ak)) {
                    wk.removeSchwimmer(t);
                }
            }
        }
        wk.enableUpdates();
        if (index < 0) {
            return null;
        }
        return generateEinzelwertungswettkampfI(wk, index, IncludeSex.Both, removeEmpty, false);
    }

    public static <T extends ASchwimmer> boolean hasEinzelwertungswettkampf(AWettkampf<T> wk, int akn,
            boolean removeEmpty) {
        if (wk == null) {
            return false;
        }
        Altersklasse ak = wk.getRegelwerk().getAk(akn);
        if (!ak.hasEinzelwertung()) {
            return false;
        }
        int anzahl = ak.getDiszAnzahl();
        if (removeEmpty) {
            for (int z = 0; z < 2; z++) {
                Results<T> results = new Results<>(ResultCalculator.getResults(wk, ak, z == 1, null, true));
                // JResultTable jrt = JResultTable.getResultTable(wk, ak, z == 1, false, true,
                // 0);
                for (int x = 0; x < results.size(); x++) {
                    ASchwimmer s = results.getSchwimmer(x);
                    if (!results.hasKeineWertung(x)) {
                        for (int y = 0; y < anzahl; y++) {
                            if (s.isDisciplineChosen(y)) {
                                boolean include = true;
                                if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                                    if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                                        include = false;
                                    }
                                }
                                if (include) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int z = 0; z < 2; z++) {
                LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, ak, z == 1);
                for (T s : swimmers) {
                    for (int y = 0; y < anzahl; y++) {
                        if (s.isDisciplineChosen(y)) {
                            boolean include = true;
                            if (ak.hasHLW() && ak.isEinzelwertungHlw()) {
                                if ((s.getHLWPunkte() <= 0.005) && (s.hasHLWSet())) {
                                    include = false;
                                }
                            }
                            if (include) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public static <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(AWettkampf<T> cwk, OWSelection t) {
        if (cwk == null) {
            return null;
        }
        AWettkampf<T> wk = Utils.copy(cwk);
        Altersklasse akx = wk.getRegelwerk().getAk(t.akNummer);
        if (!akx.isEinzelwertungHlw()) {
            akx.setHLW(false);
        }
        Disziplin disziplin = akx.getDisziplin(t.discipline, t.male);

        // Remove disciplines before participants for performance reasons
        for (OWDisziplin<T> dx : wk.getLauflisteOW().getDisziplinen()) {
            if (!dx.Id.equals(t.getId())) {
                wk.getLauflisteOW().removeDiscipline(t.getId());
            }
        }

        OWDisziplin<T> d = cwk.getLauflisteOW().getDisziplin(t);

        int qualifiedPerHeat = wk.getIntegerProperty("qualifiedPerHeat", 0);
        if (!t.isFinal && disziplin.getRunden().length > t.round) {
            int amount = disziplin.getRunden()[t.round];
            if (d == null || d.getLaeufe() == null) {
                qualifiedPerHeat = 0;
            } else {
                int heats = d.getLaeufe().size();
                qualifiedPerHeat = amount / heats;
            }
        }

        wk.getLaufliste().clear();
        wk.getLaufliste().resetAll();
        wk.getLaufliste().setIsFinal(cwk.isFinal(t));
        wk.setProperty(IS_QUALIFIED, t.round > 0);
        wk.setProperty(QUALIFIED_PER_HEAT, qualifiedPerHeat);
        wk.setProperty(IS_FINAL, t.isFinal);
        wk.setProperty(ROUND, t.round);
        wk.setProperty(ROUND_ID, disziplin.getRundenId(t.round));
        wk.setProperty(PropertyConstants.HEATS_EMPTY_LIST, false);
        wk.setProperty(HEATS_LANES, d == null ? cwk.getIntegerProperty(HEATS_LANES, 99) : d.getBahnen());

        if (t.round < 0) {
            throw new IllegalArgumentException("round must be at least 0.");
        } else if (t.round == 0) {
            wk.disableUpdates();
            for (T s : new ArrayList<T>(wk.getSchwimmer())) {
                if ((s.getAKNummer() != t.akNummer) || (s.isMaennlich() != t.male)
                        || (!s.isDisciplineChosen(t.discipline))) {
                    wk.removeSchwimmer(s);
                }
            }
            wk.enableUpdates();
        } else if (d != null) {
            wk.disableUpdates();
            Set<Integer> sn = d.getSchwimmer().stream().map(ASchwimmer::getStartnummer).collect(Collectors.toSet());
            wk.removeSchwimmerWhen(s -> !sn.contains(s.getStartnummer()));
            wk.enableUpdates();
        } else {
            OWSelection t1 = new OWSelection(cwk.getRegelwerk().getAk(t.akNummer), t.akNummer, t.male, t.discipline,
                    t.round - 1);
            AWettkampf<T> wk1a = createCompetitionFor(cwk, t1);

            wk.disableUpdates();

            wk1a.getRegelwerk().getAk(t.akNummer).setEinzelwertung(true);
            wk1a.getRegelwerk().getAk(t.akNummer).setChosenDisciplines(1, 1, 1);
            wk1a.getRegelwerk().setFormelID(FormelILS.ID);
            AWettkampf<T> wk1 = generateEinzelwertungswettkampfForQualification(wk1a, t.akNummer, true);
            if (wk1 == null) {
                return null;
            }
            int qualified = cwk.getRegelwerk().getAk(t.akNummer).getDisziplin(t.discipline, t.male)
                    .getRunden()[t1.round];
            SchwimmerResult<T>[] results = ResultCalculator.getResults(wk1, wk1.getRegelwerk().getAk(0), t.male);

            HashSet<Integer> sn = new HashSet<>();
            for (SchwimmerResult<T> r : results) {
                SchwimmerData<T> rs = r.getResults()[0];
                if (rs.getRank() >= 0 && rs.getRank() <= qualified) {
                    int snx = Integer.parseInt(r.getSchwimmer().getBemerkung());
                    sn.add(snx);
                }
            }
            for (T s : wk.getSchwimmer()) {
                if (!sn.contains(s.getStartnummer())) {
                    wk.removeSchwimmer(s);
                }
            }

            wk.enableUpdates();
        }

        Regelwerk r = wk.getRegelwerk();
        Altersklasse ak = r.getAk(t.akNummer);
        ak.setDisciplineChoiceAllowed(false);

        if (t.round == 0) {
            for (T s : wk.getSchwimmer()) {
                s.setMeldezeit(0, s.getMeldezeit(t.discipline));
                s.setStarter(0, s.getStarter(t.discipline));
            }
        } else {
            String id = OWDisziplin.getId(t.akNummer, t.male, t.discipline, t.round - 1);
            for (T s : wk.getSchwimmer()) {
                s.setMeldezeit(0, s.getZeit(id));
                s.setStarter(0, CompetitionUtils.getStarter(s, t.akNummer, t.male, t.discipline, t.round));
            }
        }

        for (int x = 0; x < r.size(); x++) {
            if (x != t.akNummer) {
                r.getAk(x).setDiszAnzahl(0);
                r.getAk(x).setHLW(false);
            }
        }

        if (ak.getDiszAnzahl() > 0) {
            if (t.discipline > 0) {
                ak.setDisziplin(ak.getDisziplin(t.discipline, false), 0, false);
                ak.setDisziplin(ak.getDisziplin(t.discipline, true), 0, true);
            }
            ak.setDiszAnzahl(1);
        }
        for (T tx : wk.getSchwimmer()) {
            tx.updateAK(t.akNummer, true);
        }

        for (T s : new ArrayList<T>(wk.getSchwimmer())) {
            int zeit = 0;
            LinkedList<Strafe> strafen = new LinkedList<>();
            if (d != null) {
                T sx = SearchUtils.getSchwimmer(wk, s.getStartnummer());
                zeit = sx.getZeit(d.Id);
                strafen = sx.getStrafen(d.Id);
            }
            s.setBemerkung("" + s.getStartnummer());
            s.clear();
            s.setZeit(0, zeit);
            s.setStrafen(0, strafen);
        }

        if (d != null && !d.isEmpty()) {
            wk.setProperty(HEATS_LANES, d.getBahnen());
            Laufliste<T> ll = wk.getLaufliste();
            int x = 0;
            for (OWLauf<T> owl : d.laeufe) {
                Lauf<T> l = ll.add(x);
                l.setLaufnummer(owl.getLaufnummer());
                l.setLaufbuchstabe(owl.getLaufbuchstabe());
                for (int y = 0; y < owl.getBahnen(); y++) {
                    T tx = owl.getSchwimmer(y);
                    if (tx != null) {
                        l.setSchwimmer(SearchUtils.getSchwimmer(wk, tx.getStartnummer()), 0, y);
                    }
                }
                x++;
            }
        }

        wk.getLauflisteOW().clear();

        Regelwerk rw = wk.getRegelwerk();
        if (!t.isFinal || t.round == 0)
            rw.setFormelID(switch (rw.getFormelID()) {
            case FormelILSOutdoorFinals.ID -> FormelILSOutdoor.ID;
            case FormelDLRG2007Finals.ID -> FormelDLRG2007.ID;
            default -> FormelILS.ID;
            });

        return wk;
    }
}
