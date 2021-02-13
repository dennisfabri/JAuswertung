package de.df.jauswertung.gui.plugins.core;

public class AgegroupResultSelection {

    public AgegroupResultSelection(int ak, boolean male, boolean[] times, boolean[] penalties, boolean zw) {
        if (times.length != penalties.length) {
            throw new IllegalArgumentException();
        }
        this.ak = ak;
        this.male = male;

        this.times = times;
        this.penalties = penalties;
        this.zusatzwertung = zw;
    }

    public boolean[] getTimes() {
        return times;
    }

    public boolean[] getPenalties() {
        return penalties;
    }

    public boolean hasZusatzwertung() {
        return zusatzwertung;
    }

    public int getAk() {
        return ak;
    }

    public boolean isMale() {
        return male;
    }

    private int       ak;
    private boolean   male;

    private boolean[] times;
    private boolean[] penalties;
    private boolean   zusatzwertung;

    public boolean isCompleteSelection() {
        if (!zusatzwertung) {
            return false;
        }
        for (int x = 0; x < times.length; x++) {
            if (!times[x]) {
                return false;
            }
            if (!penalties[x]) {
                return false;
            }
        }
        return true;
    }
}
