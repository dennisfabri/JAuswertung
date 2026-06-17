package de.df.jauswertung.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.util.ModalFrameUtil;

public final class OWUtils {

    private static final class OWSelectionCallbackProxy implements Consumer<OWSelection[]> {

        private final Consumer<OWSelection> cb;

        public OWSelectionCallbackProxy(Consumer<OWSelection> cb) {
            this.cb = cb;
        }

        @Override
        public void accept(OWSelection[] t) {
            if (t != null && t.length > 0) {
                cb.accept(t[0]);
            }
        }
    }

    private static <T extends ASchwimmer> void showRoundMultiSelector(JFrame owner, AWettkampf<T> wk, String title,
                                                                      String text, OWSelection[] selection, OWSelection[] fullSelection,
                                                                      Consumer<OWSelection[]> cb) {
        JDisciplineSelector ds = new JDisciplineSelector(title, text, wk, selection, fullSelection, true, cb);
        ModalFrameUtil.showAsModal(ds, owner);
    }

    public static <T extends ASchwimmer> void showRoundMultiSelector(JFrame owner, AWettkampf<T> wk, String title,
                                                                     String text, EditMode mode,
                                                                     Consumer<OWSelection[]> cb) {
        OWUtils.showRoundMultiSelector(owner, wk, title, text,
                                       getRounds(wk, mode), getAllRounds(wk, mode), cb);
    }

    private static <T extends ASchwimmer> OWSelection[] getRounds(AWettkampf<T> wk, EditMode mode) {
        return switch (mode) {
            case CREATE -> getCreatableRounds(wk);
            case READ, WRITE -> getCreatedRounds(wk, true);
            case DELETE -> getCurrentRounds(wk);
        };
    }

    private static <T extends ASchwimmer> OWSelection[] getAllRounds(AWettkampf<T> wk, EditMode mode) {
        return switch (mode) {
            case CREATE -> getCreatableRounds(wk);
            case READ -> getCreatedRounds(wk, false);
            case WRITE -> getCreatedRounds(wk, true);
            case DELETE -> getCurrentRounds(wk);
        };
    }

    public static <T extends ASchwimmer> void showRoundSelector(JFrame owner,
                                                                AWettkampf<T> wk,
                                                                String title,
                                                                String text,
                                                                EditMode mode,
                                                                Consumer<OWSelection> cb) {
        OWUtils.showRoundSelector(owner, wk, title, text,
                                  getRounds(wk, mode), getAllRounds(wk, mode), cb);
    }

    private static <T extends ASchwimmer> void showRoundSelector(JFrame owner, AWettkampf<T> wk, String title,
                                                                 String text, OWSelection[] selection, OWSelection[] fullSelection,
                                                                 Consumer<OWSelection> cb) {
        Consumer<OWSelection[]> cbx = new OWSelectionCallbackProxy(cb);
        JDisciplineSelector ds = new JDisciplineSelector(title, text, wk, selection, fullSelection, false, cbx);
        ModalFrameUtil.showAsModal(ds, owner);
    }

    public static <T extends ASchwimmer> OWSelection[] getCreatedRounds(AWettkampf<T> wk, boolean onlyLatest) {
        Regelwerk rw = wk.getRegelwerk();
        LinkedList<OWDisziplin<T>> dx = new LinkedList<>(
                Arrays.asList(wk.getLauflisteOW().getDisziplinen()));

        List<OWSelection> daten = new ArrayList<>();
        for (OWDisziplin<T> d : dx) {
            boolean selected = true;
            if (onlyLatest) {
                String id = OWDisziplin.getId(d.akNummer, d.maennlich, d.disziplin, d.round + 1);
                OWDisziplin<T> d1 = wk.getLauflisteOW().getDisziplin(id);
                if (d1 != null && !d1.getLaeufe().isEmpty()) {
                    selected = false;
                }
            }
            if (selected) {
                daten.add(
                        new OWSelection(rw.getAk(d.akNummer), d.akNummer, d.maennlich, d.disziplin, d.round));
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[0]);
    }

    public static <T extends ASchwimmer> OWSelection[] getCurrentRounds(AWettkampf<T> wk) {
        Regelwerk rw = wk.getRegelwerk();
        LinkedList<OWDisziplin<T>> dx = new LinkedList<>(
                Arrays.asList(wk.getLauflisteOW().getDisziplinen()));

        HashSet<String> ids = dx.stream().map(s -> s.Id).collect(Collectors.toCollection(HashSet::new));

        List<OWSelection> daten = new ArrayList<>();
        for (OWDisziplin<T> d : dx) {
            String id = OWDisziplin.getId(d.akNummer, d.maennlich, d.disziplin, d.round + 1);
            if (!ids.contains(id)) {
                daten.add(
                        new OWSelection(rw.getAk(d.akNummer), d.akNummer, d.maennlich, d.disziplin, d.round));
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[0]);
    }

    public static <T extends ASchwimmer> OWSelection[] getCreatableRounds(AWettkampf<T> wk) {
        List<OWSelection> daten = new ArrayList<>();
        Regelwerk rw = wk.getRegelwerk();
        for (int x = 0; x < rw.size(); x++) {
            Altersklasse ak = rw.getAk(x);
            for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                for (int y = 0; y < 2; y++) {
                    Disziplin dx = ak.getDisziplin(z, y == 1);
                    if (SearchUtils.hasSchwimmer(wk, ak, y == 1, z)) {
                        for (int i = 0; i < dx.getRunden().length + 1; i++) {
                            boolean generated = false;
                            String id = OWDisziplin.getId(x, y == 1, z, i);
                            for (OWDisziplin<T> d : wk.getLauflisteOW().getDisziplinen()) {
                                if (d.Id.equals(id)) {
                                    generated = true;
                                    break;
                                }
                            }
                            if (!generated) {
                                boolean ok = false;
                                if (i == 0) {
                                    ok = true;
                                } else {
                                    String id2 = OWDisziplin.getId(x, y == 1, z, i - 1);
                                    ok = CompetitionUtils.isDisciplineFinished(wk, id2);
                                }
                                if (ok) {
                                    daten.add(new OWSelection(ak, x, y == 1, z, i));
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[0]);
    }
}
