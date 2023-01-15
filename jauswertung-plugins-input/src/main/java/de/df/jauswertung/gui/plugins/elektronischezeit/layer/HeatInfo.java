package de.df.jauswertung.gui.plugins.elektronischezeit.layer;

public class HeatInfo {

    private final String ak;
    private final String disziplin;
    private final LaneInfo[] laneinfos;
    private final int event;
    private final int laufnummer;
    private final int laufbuchstabe;

    public HeatInfo(String ak, String disziplin, int event, int laufnummer, int laufbuchstabe, LaneInfo[] lanes) {
        this.ak = ak;
        this.disziplin = disziplin;
        laneinfos = lanes;
        this.event = event;
        this.laufbuchstabe = laufbuchstabe;
        this.laufnummer = laufnummer;
    }

    public LaneInfo[] getLanes() {
        return laneinfos;
    }

    public String getAltersklasse() {
        return ak;
    }

    public String getDisziplin() {
        return disziplin;
    }

    public int getLanecount() {
        return laneinfos == null ? 0 : laneinfos.length;
    }

    public int getLaufbuchstabe() {
        return laufbuchstabe;
    }

    public int getLaufnummer() {
        return laufnummer;
    }

    public int getEvent() {
        return event;
    }
}
