package de.df.jauswertung.util.data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;

public class HeatsUtils {

    public static <T extends ASchwimmer> void save(AWettkampf<T> wk, OWSelection t, AWettkampf<T> wkx) {
        Laufliste<T> heats = wkx.getLaufliste();
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t.akNummer, t.male, t.discipline, t.round);
        if (disziplin == null) {
            disziplin = wk.getLauflisteOW().addDisziplin(t.akNummer, t.male, t.discipline, t.round);
        }
        disziplin.laeufe.clear();
        for (Lauf<T> lauf : heats.getLaufliste()) {
            OWLauf<T> l = new OWLauf<>(wk, disziplin.Id, lauf);
            for (int x = 0; x < l.getBahnen(); x++) {
                T tx = l.getSchwimmer(x);
                if (tx != null) {
                    T ti = SearchUtils.getSchwimmer(wk, tx);
                    if (ti instanceof Mannschaft && disziplin.round > 0) {
                        Mannschaft m = (Mannschaft) ti;
                        int[] starter = CompetitionUtils.getStarter(m, disziplin.akNummer, disziplin.maennlich,
                                disziplin.disziplin, disziplin.round - 1);
                        m.setStarter(disziplin.Id, starter);
                    }
                    disziplin.Schwimmer.add(ti);
                }
            }
            disziplin.laeufe.add(l);
        }
        wk.setProperty(PropertyConstants.HEATS_SORTING_ORDER,
                wkx.getIntegerProperty(PropertyConstants.HEATS_SORTING_ORDER, Reihenfolge.Regelwerk.getValue()));
    }


    /**
     * Gleicht die Teilnehmer einer eingeteilten Runde mit den Qualifizierten ab, wie sie beim Erzeugen der Laufliste
     * ermittelt würden. Verbleibende behalten Lauf und Bahn, Hinzukommende sind noch nicht eingeteilt.
     */
    public static <T extends ASchwimmer> void aktualisiereTeilnehmer(AWettkampf<T> wk, OWSelection t) {
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t);
        if (t.round == 0 || disziplin == null) {
            return;
        }
        List<T> qualifizierte = ResultUtils.getQualifizierte(wk, t);
        Set<Integer> qualifiziert = qualifizierte.stream().map(ASchwimmer::getStartnummer).collect(Collectors.toSet());
        for (T s : disziplin.getSchwimmer()) {
            if (!qualifiziert.contains(s.getStartnummer())) {
                disziplin.remove(s);
            }
        }
        Set<Integer> eingeteilt = disziplin.getSchwimmer().stream().map(ASchwimmer::getStartnummer)
                .collect(Collectors.toSet());
        for (T s : qualifizierte) {
            if (!eingeteilt.contains(s.getStartnummer())) {
                disziplin.addSchwimmer(s);
            }
        }
    }
}
