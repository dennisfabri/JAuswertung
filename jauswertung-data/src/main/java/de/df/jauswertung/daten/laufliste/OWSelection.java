package de.df.jauswertung.daten.laufliste;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;

public class OWSelection implements Comparable<OWSelection> {

    public final Altersklasse ak;
    public final int akNummer;
    public final boolean male;
    public final int discipline;
    public final int round;

    public final boolean isFinal;

    private final String id;

    public String getId() {
        return id;
    }

    public OWSelection(Altersklasse ak, int akNummer, boolean male, int discipline, int round) {
        id = OWDisziplin.getId(akNummer, male, discipline, round);
        this.ak = ak;
        this.akNummer = akNummer;
        this.male = male;
        this.discipline = discipline;
        this.round = round;
        this.isFinal = ak.getDisziplin(discipline, male).isFinalRound(round);
    }

    @Override
    public int compareTo(OWSelection o) {
        if (o == null) {
            return -1;
        }
        int diff = akNummer - o.akNummer;
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = (male ? 1 : 0) - (o.male ? 1 : 0);
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = discipline - o.discipline;
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        diff = round - o.round;
        if (diff != 0) {
            return (int) Math.signum(diff);
        }
        return 0;
    }
}