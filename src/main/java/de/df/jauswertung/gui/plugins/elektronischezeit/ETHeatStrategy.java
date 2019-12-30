package de.df.jauswertung.gui.plugins.elektronischezeit;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.util.Tupel;
import de.dm.ares.data.Heat;

class ETHeatStrategy<T extends ASchwimmer> implements IETStrategy {

    private final AWettkampf<T>                             wk;
    private final IPluginManager                            controller;
    private final IPlugin                                   plugin;

    private LinkedList<OWSelection>                         rounds     = new LinkedList<OWSelection>();
    private Hashtable<String, Tupel<Integer, OWSelection>>  roundsById = new Hashtable<String, Tupel<Integer, OWSelection>>();
    private Hashtable<Integer, Tupel<OWSelection, Integer>> heatids    = new Hashtable<Integer, Tupel<OWSelection, Integer>>();

    public ETHeatStrategy(IPluginManager controller, IPlugin plugin, JFrame parent, AWettkampf<T> w) {
        wk = w;
        this.controller = controller;
        this.plugin = plugin;

        initialize();
    }

    private OWLauf<T> getLauf(int index) {
        if (index < 0) {
            return null;
        }
        Tupel<OWSelection, Integer> owsi = heatids.get(index);
        if (owsi == null) {
            return null;
        }
        OWDisziplin<T> owd = wk.getLauflisteOW().getDisziplin(owsi.getFirst());
        return owd.getLaeufe().get(owsi.getSecond());
    }

    private void initialize() {
        LinkedList<Tupel<Integer, OWSelection>> heats = new LinkedList<Tupel<Integer, OWSelection>>();
        rounds.clear();
        roundsById.clear();
        heatids.clear();

        OWLaufliste<T> ll = wk.getLauflisteOW();

        // int bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
        Regelwerk rw = wk.getRegelwerk();
        int maxdisz = rw.getMaxDisciplineCount();
        for (int x = 0; x < rw.size(); x++) {
            Altersklasse ak = rw.getAk(x);
            for (int d = 0; d < ak.getDiszAnzahl(); d++) {
                for (int s = 0; s < 2; s++) {
                    Disziplin disziplin = ak.getDisziplin(d, s == 1);
                    int[] runden = disziplin.getRunden();
                    for (int r = 0; r <= runden.length; r++) {
                        OWSelection selection = new OWSelection(ak, x, s == 1, d, r, r == runden.length);
                        OWDisziplin<T> owd = ll.getDisziplin(selection);
                        if (owd != null && !owd.isEmpty()) {
                            int roundId1 = wk.getRegelwerk().getRundenId(owd);

                            int value = roundId1 > 0 ? roundId1 : 1000 + ((r * rw.size() + x) * maxdisz + d) * 2 + s + 1;
                            heats.add(new Tupel<Integer, OWSelection>(value, selection));
                        }
                    }
                }
            }
        }

        Collections.sort(heats, new Comparator<Tupel<Integer, OWSelection>>() {

            @Override
            public int compare(Tupel<Integer, OWSelection> o1, Tupel<Integer, OWSelection> o2) {
                return o1.getFirst() - o2.getFirst();
            }
        });

        int index = 0;
        for (Tupel<Integer, OWSelection> t : heats) {
            StringBuilder sb = new StringBuilder();
            sb.append(t.getFirst());
            sb.append(": ");
            sb.append(t.getSecond().getId());
            System.out.println(sb.toString());

            OWSelection ows = t.getSecond();

            rounds.addLast(ows);
            roundsById.put(ows.getId(), t);
            OWDisziplin<T> owd = wk.getLauflisteOW().getDisziplin(ows);
            for (int x = 0; x < owd.getLaeufe().size(); x++) {
                heatids.put(index, new Tupel<OWSelection, Integer>(ows, x));
                index++;
            }
        }
    }

    @Override
    public String getHeatname(int index) {
        if (index < 0) {
            return null;
        }
        Tupel<OWSelection, Integer> owsi = heatids.get(index);
        if (owsi == null) {
            return null;
        }
        OWDisziplin<T> owd = wk.getLauflisteOW().getDisziplin(owsi.getFirst());
        OWLauf<T> lauf = owd.getLaeufe().get(owsi.getSecond());

        int id = wk.getRegelwerk().getRundenId(owd);

        return "" + (lauf == null ? "" : id + "/" + lauf.getLaufnummer());
    }

    @Override
    public HeatMatchingMode isDirectMatching() {
        return HeatMatchingMode.Heat2Heat;
    }

    @Override
    public Heat[] generateHeats() {
        // Only for testing purposes
        return new Heat[0];
    }

    @Override
    public boolean checkTimes(int index, int[] timesx) {
        // ToDo: Implement checks
        return true;
    }

    public void getTime(int heatnr, int index, int time) {
        OWLauf<T> lauf = getLauf(heatnr);
        if (lauf == null) {
            return;
        }
        T t = lauf.getSchwimmer(index);
        if (t == null) {
            return;
        }
        Eingabe e = t.getEingabe(lauf.getDisciplineId(), true);
        e.setZeit(time);
    }

    @Override
    public void setTimes(int heatnr, int[] times) {
        OWLauf<T> lauf = getLauf(heatnr);

        for (int x = 0; x < lauf.getBahnen(); x++) {
            int time = 0;
            if (times.length > x) {
                time = times[x];
            }
            setTime(heatnr, x, time);
        }
    }

    @Override
    public void setNoPenalty(int heatnr, int row) {
    }

    @Override
    public void setDisqualification(int heatnr, int row) {
    }

    @Override
    public void setNA(int heatnr, int row) {
    }

    @Override
    public int getTime(int heatnr, int index) {
        OWLauf<T> lauf = getLauf(heatnr);
        if (lauf == null) {
            return 0;
        }
        T t = lauf.getSchwimmer(index);
        if (t == null) {
            return 0;
        }
        Eingabe e = t.getEingabe(lauf.getDisciplineId());
        if (e == null) {
            return 0;
        }
        return e.getZeit();
    }

    @Override
    public void setTime(int heatnr, int index, int timevalue) {
        OWLauf<T> lauf = getLauf(heatnr);
        if (lauf == null) {
            return;
        }
        T t = lauf.getSchwimmer(index);
        if (t == null) {
            return;
        }
        Eingabe e = t.getEingabe(lauf.getDisciplineId(), true);
        if (timevalue <= 0) {
            e.addStrafe(wk.getStrafen().getNichtAngetreten());
        } else {
            LinkedList<Strafe> ls = e.getStrafen();
            ListIterator<Strafe> li = ls.listIterator();
            while (li.hasNext()) {
                Strafe s = li.next();
                if (s.getArt() == Strafarten.NICHT_ANGETRETEN) {
                    li.remove();
                }
            }
            e.setStrafen(ls);
        }
        e.setZeit(timevalue);
        OWDisziplin<T> owd = wk.getLauflisteOW().getDisziplin(lauf.getDisciplineId());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.sendDataUpdateEvent("SetTime", REASON_POINTS_CHANGED | REASON_PENALTY, t, owd.disziplin, plugin);
            }
        });
    }

    @Override
    public String[] getHeatnames() {
        ArrayList<String> heats = new ArrayList<>(heatids.size());
        for (int i = 0; i < heatids.size(); i++) {
            heats.add(getHeatname(i));
        }
        return heats.toArray(new String[heats.size()]);
    }

    @Override
    public int getLanecount() {
        return wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
    }

    @Override
    public HeatInfo getHeat(int index) {
        OWLauf<T> lauf = getLauf(index);
        if (lauf == null) {
            return null;
        }
        LaneInfo[] lis = new LaneInfo[lauf.getBahnen()];
        for (int x = 0; x < lauf.getBahnen(); x++) {
            T t = lauf.getSchwimmer(x);
            int time = 0;
            String penalty = t == null ? "" : PenaltyUtils.getPenaltyShortText(t.getAkkumulierteStrafe(lauf.getDisciplineId()), t.getAK());
            int disz = wk.getLauflisteOW().getDisziplin(lauf.getDisciplineId()).disziplin;
            Eingabe e = t == null ? null : t.getEingabe(lauf.getDisciplineId());
            if (e != null) {
                time = e.getZeit();
            }
            lis[x] = new LaneInfo(t, disz, time, penalty);
        }

        OWDisziplin<T> owd = wk.getLauflisteOW().getDisziplin(lauf.getDisciplineId());
        Altersklasse ak = wk.getRegelwerk().getAk(owd.akNummer);
        Disziplin d = ak.getDisziplin(owd.disziplin, owd.maennlich);
        int event = d.getRundenId(owd.round);
        return new HeatInfo(I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, owd.maennlich),
                d.getName() + " - " + I18n.getRound(owd.round, owd.round == d.getRunden().length), event, lauf.getLaufnummer(), lauf.getLaufbuchstabe(), lis);
        // return new HeatInfo(I18n.getAgeGroupAsString(ak, owd.maennlich), d.getName()
        // + " - " + I18n.getRound(owd.round, owd.round == d.getRunden().length),
        // getIndex(owd.Id), lauf.getLaufnummer(), lis);
    }
}
