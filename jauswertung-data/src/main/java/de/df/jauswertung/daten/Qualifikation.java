package de.df.jauswertung.daten;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public enum Qualifikation {

    OFFEN("Offen", false, false), NICHT_QUALIFIZIERT("Nicht qualifiziert", false, false),
    QUALIFIZIERT("qualifiziert", true, false), NACHRUECKER("Nachrücker",
            false, false),
    DIREKT("Direkt", true, false), GESETZT("Gesetzt", true, true), GESPERRT("Gesperrt", false, true);

    @XStreamAsAttribute
    private final String name;
    @XStreamAsAttribute
    private final boolean manual;
    @XStreamAsAttribute
    private final boolean accepted;

    private Qualifikation(String n, boolean accepted, boolean manual) {
        name = n;
        this.accepted = accepted;
        this.manual = manual;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isManual() {
        return manual;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
