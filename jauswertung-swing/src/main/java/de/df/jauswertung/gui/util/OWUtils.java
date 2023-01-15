package de.df.jauswertung.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;

public final class OWUtils {

    private static final class OWSelectionCallbackProxy implements ISimpleCallback<OWSelection[]> {

        private final ISimpleCallback<OWSelection> cb;

        public OWSelectionCallbackProxy(ISimpleCallback<OWSelection> cb) {
            this.cb = cb;
        }

        @Override
        public void callback(OWSelection[] t) {
            if (t != null && t.length > 0) {
                cb.callback(t[0]);
            }
        }
    }

    public static <T extends ASchwimmer> boolean ShowRoundMultiSelector(JFrame owner, AWettkampf<T> wk, String title,
            String text, OWSelection[] data,
            ISimpleCallback<OWSelection[]> cb) {
        JDisciplineSelector ds = new JDisciplineSelector(title, text, wk, data, true, cb);
        ModalFrameUtil.showAsModal(ds, owner);
        return ds.isCancelled();
    }

    public static <T extends ASchwimmer> boolean ShowRoundSelector(JFrame owner, AWettkampf<T> wk, String title,
            String text, OWSelection[] data,
            ISimpleCallback<OWSelection> cb) {
        ISimpleCallback<OWSelection[]> cbx = new OWSelectionCallbackProxy(cb);
        JDisciplineSelector ds = new JDisciplineSelector(title, text, wk, data, false, cbx);
        ModalFrameUtil.showAsModal(ds, owner);
        return ds.isCancelled();
    }

    public static <T extends ASchwimmer> OWSelection[] getCreatedRounds(AWettkampf<T> wk, boolean onlyLatest) {
        Regelwerk rw = wk.getRegelwerk();
        LinkedList<OWDisziplin<T>> dx = new LinkedList<OWDisziplin<T>>(
                Arrays.asList(wk.getLauflisteOW().getDisziplinen()));

        List<OWSelection> daten = new ArrayList<OWSelection>();
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
                boolean isFinal = d.round == wk.getRegelwerk().getAk(d.akNummer).getDisziplin(d.disziplin, d.maennlich)
                        .getRunden().length;
                daten.add(
                        new OWSelection(rw.getAk(d.akNummer), d.akNummer, d.maennlich, d.disziplin, d.round, isFinal));
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[daten.size()]);
    }

    public static <T extends ASchwimmer> OWSelection[] getCurrentRounds(AWettkampf<T> wk) {
        Regelwerk rw = wk.getRegelwerk();
        LinkedList<OWDisziplin<T>> dx = new LinkedList<OWDisziplin<T>>(
                Arrays.asList(wk.getLauflisteOW().getDisziplinen()));

        HashSet<String> ids = new HashSet<String>(dx.stream().map(s -> s.Id).collect(Collectors.toList()));

        List<OWSelection> daten = new ArrayList<OWSelection>();
        for (OWDisziplin<T> d : dx) {
            String id = OWDisziplin.getId(d.akNummer, d.maennlich, d.disziplin, d.round + 1);
            if (!ids.contains(id)) {
                boolean isFinal = d.round == wk.getRegelwerk().getAk(d.akNummer).getDisziplin(d.disziplin, d.maennlich)
                        .getRunden().length;
                daten.add(
                        new OWSelection(rw.getAk(d.akNummer), d.akNummer, d.maennlich, d.disziplin, d.round, isFinal));
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[daten.size()]);
    }

    public static OWSelection[] getCreatableRounds(AWettkampf<ASchwimmer> wk) {
        List<OWSelection> daten = new ArrayList<OWSelection>();
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
                            for (OWDisziplin<ASchwimmer> d : wk.getLauflisteOW().getDisziplinen()) {
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
                                    boolean isFinal = i == wk.getRegelwerk().getAk(x).getDisziplin(z, y == 1)
                                            .getRunden().length;
                                    daten.add(new OWSelection(ak, x, y == 1, z, i, isFinal));
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(daten);
        return daten.toArray(new OWSelection[daten.size()]);
    }
}
