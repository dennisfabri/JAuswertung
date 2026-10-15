package de.df.jauswertung.gui.plugins.elektronischezeit.layer;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.I18n;

public class LaneInfo {

    private final String name;
    private final String gliederung;
    private final String agegroup;
    private final String penalty;
    private final int time;

    public LaneInfo(ASchwimmer s, int time, String penalty) {
        name = s == null ? "" : s.getName();
        agegroup = s == null ? "" : I18n.getAgeGroupAsString(s);
        gliederung = s == null ? "" : s.getGliederungMitQGliederung();
        this.time = time;
        this.penalty = penalty;
    }

    public String getName() {
        return name;
    }

    public String getGliederung() {
        return gliederung;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public String getPenalty() {
        return penalty;
    }

    public int getTime() {
        return time;
    }

}
