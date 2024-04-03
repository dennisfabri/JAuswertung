package de.df.jauswertung.gui.plugins.elektronischezeit;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.HeatInfo;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.LaneInfo;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.gui.util.TimeStatus;
import de.df.jauswertung.util.RandomUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.dm.ares.data.Heat;
import de.dm.ares.data.LaneStatus;

class ETDLRGStrategy<T extends ASchwimmer> implements IETStrategy {

    private final AWettkampf<T> wk;
    private final JFrame parent;
    private final IPluginManager controller;
    private final IPlugin plugin;

    public ETDLRGStrategy(IPluginManager controller, IPlugin plugin, JFrame parent, AWettkampf<T> w) {
        wk = w;
        this.parent = parent;
        this.controller = controller;
        this.plugin = plugin;
    }

    @Override
    public String getHeatname(int index) {
        Lauf<T> l = wk.getLaufliste().getLaufliste().get(Math.max(0, index));
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();
        return l.getName(scheme);
    }

    @Override
    public HeatMatchingMode isDirectMatching() {
        LinkedList<Lauf<T>> laufliste = wk.getLaufliste().getLaufliste();
        if (laufliste.getFirst().getLaufnummer() <= 100) {
            return HeatMatchingMode.Heat2Competition;
        }
        for (Lauf<T> lauf : laufliste) {
            if (lauf.getLaufbuchstabe() > 0) {
                return HeatMatchingMode.Heat2Competition;
            }
        }
        return HeatMatchingMode.HeatModulo1002Competition;
    }

    @Override
    public Heat[] generateHeats() {
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        Random rng = RandomUtils.getRandomNumberGenerator();
        Heat[] heats = new Heat[wk.getLaufliste().getLaufliste().size()];
        int x = 0;
        for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
            Heat h = new Heat(lauf.getName(scheme), lauf.getLaufnummer(), lauf.getLaufbuchstabe() + 1);

            for (int y = 0; y < lauf.getBahnen(); y++) {
                // Einer von 100 erhält keine Zeiten
                if (rng.nextInt(100) > 1) {
                    // Der Rest enthält 1-3 Zeiten
                    int max = rng.nextInt(2) + 1;
                    long time = 0;
                    for (int z = 0; z < max; z++) {
                        // Die Zeiten erhöhen sich zwischen 0:30 und 1:00
                        time += Math.round(rng.nextDouble() * 3000 + 3000);
                        h.store(y, time, LaneStatus.OfficialEnd);
                    }
                }
            }

            heats[x] = h;

            x++;
        }
        return heats;
    }

    @Override
    public boolean checkTimes(int index, int[] timesx) {
        Lauf<T> l = wk.getLaufliste().getLaufliste().get(Math.max(0, index));

        for (int x = 0; x < Math.min(l.getBahnen(), timesx.length); x++) {
            T t = l.getSchwimmer(x);
            if (t != null) {
                int disz = l.getDisznummer(x);
                TimeStatus ts = SchwimmerUtils.getTimeStatus(wk.getRegelwerk().getFormelID(), timesx[x],
                        t.getAK().getDisziplin(disz, t.isMaennlich()).getRec());
                switch (ts) {
                case FAST:
                    return false;
                case SLOW:
                    return false;
                case NONE:
                    break;
                case NORMAL:
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void setTimes(int index, int[] times) {
        Lauf<T> l = wk.getLaufliste().getLaufliste().get(Math.max(0, index));

        boolean empty = true;
        for (int x = 0; x < l.getBahnen(); x++) {
            T t = l.getSchwimmer(x);
            if (t != null) {
                int disz = l.getDisznummer(x);
                if (t.getZeit(disz) > 0) {
                    empty = false;
                    break;
                }
            }
        }
        if (!empty) {
            boolean b = DialogUtils.ask(parent, I18n.get("Question.OverwriteTimes"),
                    I18n.get("Question.OverwriteTimes.Note"));
            if (!b) {
                return;
            }
        }

        if (l.getBahnen() != times.length) {
            int[] tx = new int[l.getBahnen()];
            System.arraycopy(times, 0, tx, 0, Math.min(l.getBahnen(), times.length));
            times = tx;
        }
        for (int x = 0; x < l.getBahnen(); x++) {
            T t = l.getSchwimmer(x);
            int disz = l.getDisznummer(x);
            if (t != null) {
                if (times[x] <= 0) {
                    t.addStrafe(disz, wk.getStrafen().getNichtAngetreten());
                    t.setZeit(disz, 0);
                    SwingUtilities.invokeLater(() -> {
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, t,
                                disz, plugin);
                    });
                } else {
                    t.setZeit(disz, times[x]);
                    LinkedList<Strafe> ls = t.getStrafen(disz);
                    ListIterator<Strafe> li = ls.listIterator();
                    while (li.hasNext()) {
                        Strafe s = li.next();
                        if (s.getArt() == Strafarten.NICHT_ANGETRETEN) {
                            li.remove();
                        }
                    }
                    t.setStrafen(disz, ls);
                    SwingUtilities.invokeLater(() -> {
                        controller.sendDataUpdateEvent("SetTime", REASON_POINTS_CHANGED | REASON_PENALTY, t, disz,
                                plugin);
                    });
                }
            }
        }
    }

    @Override
    public void setNoPenalty(int heatnr, int row) {
        @SuppressWarnings("rawtypes")
        Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
        ASchwimmer s = lauf.getSchwimmer(row);
        s.setStrafen(lauf.getDisznummer(row), new LinkedList<>());

        int diszNummer = lauf.getDisznummer(row);

        SwingUtilities.invokeLater(() -> {
            controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, s, diszNummer,
                    plugin);
        });

    }

    @Override
    public void setDisqualification(int heatnr, int row) {
        @SuppressWarnings("rawtypes")
        Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
        ASchwimmer s = lauf.getSchwimmer(row);
        s.addStrafe(lauf.getDisznummer(row), Strafe.DISQUALIFIKATION);

        int diszNummer = lauf.getDisznummer(row);

        SwingUtilities.invokeLater(() -> {
            controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, s, diszNummer,
                    plugin);
        });
    }

    @Override
    public void setNA(int heatnr, int row) {
        @SuppressWarnings("rawtypes")
        Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
        ASchwimmer s = lauf.getSchwimmer(row);
        s.addStrafe(lauf.getDisznummer(row), Strafe.NICHT_ANGETRETEN);

        int diszNummer = lauf.getDisznummer(row);

        SwingUtilities.invokeLater(() -> {
            controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, s, diszNummer,
                    plugin);
        });
    }

    @Override
    public int getTime(int heatnr, int row) {
        @SuppressWarnings("rawtypes")
        Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
        ASchwimmer s = lauf.getSchwimmer(row);
        s.addStrafe(lauf.getDisznummer(row), Strafe.NICHT_ANGETRETEN);

        int diszNummer = lauf.getDisznummer(row);

        return s.getZeit(diszNummer);
    }

    @Override
    public void setTime(int heatnr, int row, int timevalue) {
        @SuppressWarnings("rawtypes")
        Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
        ASchwimmer s = lauf.getSchwimmer(row);
        s.addStrafe(lauf.getDisznummer(row), Strafe.NICHT_ANGETRETEN);

        int diszNummer = lauf.getDisznummer(row);

        s.setZeit(diszNummer, timevalue);
        controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED, s, diszNummer, plugin);
    }

    @Override
    public String[] getHeatnames() {
        return wk.getLaufliste().getHeatNames(wk.getHeatsNumberingScheme());
    }

    @Override
    public int getLanecount() {
        return wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
    }

    @Override
    public HeatInfo getHeat(int index) {
        Lauf<T> lauf = wk.getLaufliste().getLaufliste().get(index);

        LaneInfo[] laneinfos = new LaneInfo[lauf.getBahnen()];
        for (int x = 0; x < laneinfos.length; x++) {
            ASchwimmer t = lauf.getSchwimmer(x);
            if (t != null) {
                int disz = lauf.getDisznummer(x);
                String penalty = PenaltyUtils.getPenaltyShortText(t.getAkkumulierteStrafe(disz), t.getAK());
                LaneInfo lane = new LaneInfo(t, disz, t.getZeit(disz), penalty);
                laneinfos[x] = lane;
            }
        }

        HeatInfo heat = new HeatInfo(lauf.getAltersklasse(), lauf.getDisziplin(), 0, lauf.getLaufnummer(),
                lauf.getLaufbuchstabe(), laneinfos);

        return heat;
    }
}