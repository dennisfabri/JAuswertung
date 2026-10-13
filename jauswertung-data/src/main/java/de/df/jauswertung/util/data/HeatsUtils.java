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


    public record TeilnehmerAenderung<T extends ASchwimmer>(List<T> entfernt, List<T> hinzugefuegt) {

        public boolean isEmpty() {
            return entfernt.isEmpty() && hinzugefuegt.isEmpty();
        }
    }

    /**
     * Ermittelt, wie sich die Teilnehmer einer eingeteilten Runde ändern, wenn sie mit den Qualifizierten abgeglichen
     * werden, wie sie beim Erzeugen der Laufliste ermittelt würden. Die Runde selbst bleibt unverändert.
     */
    public static <T extends ASchwimmer> TeilnehmerAenderung<T> pruefeTeilnehmer(AWettkampf<T> wk, OWSelection t) {
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t);
        if (t.round == 0 || disziplin == null) {
            return new TeilnehmerAenderung<>(List.of(), List.of());
        }
        List<T> qualifizierte = ResultUtils.getQualifizierte(wk, t);
        Set<Integer> qualifiziert = qualifizierte.stream().map(ASchwimmer::getStartnummer).collect(Collectors.toSet());
        List<T> eingeteilte = disziplin.getSchwimmer();
        Set<Integer> eingeteilt = eingeteilte.stream().map(ASchwimmer::getStartnummer).collect(Collectors.toSet());
        return new TeilnehmerAenderung<>(
                eingeteilte.stream().filter(s -> !qualifiziert.contains(s.getStartnummer())).toList(),
                qualifizierte.stream().filter(s -> !eingeteilt.contains(s.getStartnummer())).toList());
    }

    /**
     * Übernimmt eine mit {@link #pruefeTeilnehmer(AWettkampf, OWSelection)} ermittelte Änderung. Verbleibende behalten
     * Lauf und Bahn, Hinzukommende sind noch nicht eingeteilt.
     */
    public static <T extends ASchwimmer> void aktualisiereTeilnehmer(AWettkampf<T> wk, OWSelection t,
            TeilnehmerAenderung<T> aenderung) {
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t);
        if (disziplin == null) {
            return;
        }
        aenderung.entfernt().forEach(disziplin::remove);
        aenderung.hinzugefuegt().forEach(disziplin::addSchwimmer);
    }
}
