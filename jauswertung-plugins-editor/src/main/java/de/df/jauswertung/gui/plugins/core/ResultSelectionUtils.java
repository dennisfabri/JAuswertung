package de.df.jauswertung.gui.plugins.core;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;

public class ResultSelectionUtils {

    public static <T extends ASchwimmer> boolean IsCompleteSelection(AWettkampf<T> wk,
            AgegroupResultSelection[] selection) {
        if (selection == null || selection.length == 0) {
            return false;
        }

        Regelwerk aks = wk.getRegelwerk();
        AgegroupResultSelection[][] temp = new AgegroupResultSelection[aks.size()][2];
        boolean[][] temp2 = new boolean[aks.size()][2];

        for (AgegroupResultSelection ag : selection) {
            temp[ag.getAk()][ag.isMale() ? 1 : 0] = ag;
        }

        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                temp2[x][y] = SearchUtils.hasSchwimmer(wk, aks.getAk(x), y == 1);
                if (temp2[x][y]) {
                    if (temp[x][y] == null) {
                        return false;
                    }
                }
            }
        }
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                if (temp2[x][y]) {
                    if (!temp[x][y].isCompleteSelection()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends ASchwimmer> AWettkampf<T> getResultWettkampf(AWettkampf wk) {
        wk = Utils.copy(wk);
        CompetitionUtils.minimizeCompetition(wk);
        for (Wertungsgruppe wg : wk.getRegelwerk().getWertungsgruppen()) {
            if (wg.isProtokollMitMehrkampfwertung()) {
                Hashtable<Integer, Altersklasse> aks = new Hashtable<Integer, Altersklasse>();
                int index = -1;
                for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                    Altersklasse ak = wk.getRegelwerk().getAk(x);
                    if ((ak.getWertungsgruppe() != null) && (ak.getWertungsgruppe().equals(wg.getName()))) {
                        aks.put(x, ak);
                        index = x;
                    }
                }
                LinkedList<ASchwimmer> swimmers = Utils.copy(wk).getSchwimmer();
                ListIterator<ASchwimmer> li = swimmers.listIterator();
                LinkedList<ASchwimmer> result = new LinkedList<ASchwimmer>();
                while (li.hasNext()) {
                    ASchwimmer s = li.next();
                    if (aks.containsKey(s.getAKNummer())) {
                        result.addLast(s);
                    }
                }
                if (!result.isEmpty()) {
                    Altersklasse ak = Utils.copy(wk.getRegelwerk().getAk(index));
                    ak.setStrafeIstDisqualifikation(wg.isStrafeIstDisqualifikation());
                    ak.setName(wg.getName());

                    index = wk.getRegelwerk().size();

                    wk.getRegelwerk().setSize(index + 1);
                    wk.getRegelwerk().setAk(index, ak);
                    for (ASchwimmer s : result) {
                        wk.addSchwimmer(s);
                        s.setAKNummer(index, false);
                    }
                }
            }
        }

        wk.changedNow();

        return wk;
    }

    public static <T extends ASchwimmer> AWettkampf<T> getResultWettkampf(AWettkampf<T> wk,
            AgegroupResultSelection[] selection) {
        wk = Utils.copy(wk);
        Regelwerk aks = wk.getRegelwerk();
        AgegroupResultSelection[][] temp = new AgegroupResultSelection[aks.size()][2];

        for (AgegroupResultSelection ag : selection) {
            temp[ag.getAk()][ag.isMale() ? 1 : 0] = ag;
        }

        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<T> schwimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                if (!schwimmer.isEmpty()) {
                    for (T t : schwimmer) {
                        if (temp[x][y] == null) {
                            for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                                t.setZeit(z, 0);
                                t.setStrafen(z, null);
                            }
                            t.clearHlw();
                        } else {
                            for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                                if (!temp[x][y].getTimes()[z]) {
                                    t.setZeit(z, 0);
                                }
                                if (!temp[x][y].getPenalties()[z]) {
                                    t.setStrafen(z, null);
                                }
                            }
                            if (!temp[x][y].hasZusatzwertung()) {
                                t.clearHlw();
                            }
                        }
                    }
                }
            }
        }

        wk.changedNow();

        return wk;
    }

}
