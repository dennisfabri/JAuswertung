package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.df.jauswertung.daten.regelwerk.Strafe;

public class TimelimitsContainer implements Serializable {

    private List<Timelimits> limits = new LinkedList<Timelimits>();

    public TimelimitsContainer() {
    }

    public TimelimitsContainer(Collection<Timelimits> data) {
        limits = new LinkedList<Timelimits>(data);
    }

    public boolean hasTimelimits() {
        return false;
    }

    public Timelimits[] getTimelimits() {
        return limits.toArray(new Timelimits[limits.size()]);
    }

    public void add(Timelimits limit) {
        limits.add(limit);
    }

    public void remove(int index) {
        limits.remove(index);
    }

    public Timelimits getTimelimits(int index) {
        return limits.get(index);
    }

    public void update(TimelimitsContainer tlc) {
        setTimelimits(tlc.getTimelimits());
    }

    private void setTimelimits(Timelimits[] timelimits) {
        limits = new LinkedList<Timelimits>();
        limits.clear();
        for (Timelimits l : timelimits) {
            limits.add(l);
        }
    }

    public boolean isBrokenBy(int zeit, Strafe strafe, String disziplin, ASchwimmer s, int year, int round) {
        return limits.stream().anyMatch(m -> m.isBrokenBy(zeit, strafe, disziplin, s, year, round));
    }

    public boolean isEmpty() {
        return limits == null || limits.isEmpty();
    }
}