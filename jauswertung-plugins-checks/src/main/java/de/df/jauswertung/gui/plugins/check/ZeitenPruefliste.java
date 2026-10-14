package de.df.jauswertung.gui.plugins.check;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.TimeStatus;

/**
 * Swimmers with a missing or implausible time per discipline. A swimmer stays in the list after a correction, so that
 * the corrected entries remain visible until the list is built again.
 */
final class ZeitenPruefliste {

    private static final Comparator<StatusDetail> REIHENFOLGE = Comparator
            .comparingInt((StatusDetail e) -> e.schwimmer().getAKNummer())
            .thenComparing(e -> e.schwimmer().isMaennlich())
            .thenComparing(e -> e.schwimmer().getName());

    private final Zeitart art;
    private final List<List<StatusDetail>> eintraege = new ArrayList<>();

    ZeitenPruefliste(Zeitart art) {
        this.art = art;
    }

    Zeitart getArt() {
        return art;
    }

    <T extends ASchwimmer> void baue(AWettkampf<T> wk) {
        eintraege.clear();
        if (!art.isGeprueft(wk) || !wk.hasSchwimmer()) {
            return;
        }
        int anzahl = wk.getRegelwerk().getMaxDisciplineCount();
        for (int x = 0; x < anzahl; x++) {
            eintraege.add(new ArrayList<>());
        }
        for (T s : wk.getSchwimmer()) {
            for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                TimeStatus status = art.getStatus(wk, s, x);
                if (status != TimeStatus.NORMAL) {
                    eintraege.get(x).add(new StatusDetail(s, status));
                }
            }
        }
        if (eintraege.stream().allMatch(List::isEmpty)) {
            eintraege.clear();
            return;
        }
        eintraege.forEach(e -> e.sort(REIHENFOLGE));
    }

    /** Updates the lists after the swimmer has been changed elsewhere. */
    <T extends ASchwimmer> void pruefe(AWettkampf<T> wk, T s) {
        for (int x = 0; x < eintraege.size(); x++) {
            List<StatusDetail> liste = eintraege.get(x);
            int index = indexOf(liste, s);
            if (!s.isDisciplineChosen(x)) {
                if (index >= 0) {
                    liste.remove(index);
                }
                continue;
            }
            TimeStatus status = art.getStatus(wk, s, x);
            if (index >= 0) {
                liste.set(index, new StatusDetail(s, status));
            } else if (status != TimeStatus.NORMAL) {
                liste.add(new StatusDetail(s, status));
                liste.sort(REIHENFOLGE);
            }
        }
    }

    private static int indexOf(List<StatusDetail> liste, ASchwimmer s) {
        for (int i = 0; i < liste.size(); i++) {
            if (liste.get(i).schwimmer().equals(s)) {
                return i;
            }
        }
        return -1;
    }

    /** Number of disciplines with a list; 0 if no swimmer is listed at all. */
    int getDisziplinen() {
        return eintraege.size();
    }

    List<StatusDetail> getEintraege(int disziplin) {
        return eintraege.get(disziplin);
    }
}
