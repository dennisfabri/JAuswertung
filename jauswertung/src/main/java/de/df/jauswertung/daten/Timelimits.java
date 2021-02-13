package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Arrays;

import de.df.jauswertung.daten.regelwerk.Strafe;

public class Timelimits implements Serializable {

    private String             name      = "";
    private String             shortname = "";
    private Timelimitchecktype check     = Timelimitchecktype.UPPER_LIMIT;

    private Timelimit[]        limits    = new Timelimit[0];

    public Timelimits() {
    }

    public String getName() {
        return name;
    }

    public String getShortname() {
        return shortname;
    }

    public Timelimitchecktype getCheck() {
        return check;
    }

    public Timelimit[] getLimits() {
        return limits;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public void setCheck(Timelimitchecktype check) {
        this.check = check;
    }

    public void setLimits(Timelimit[] limits) {
        this.limits = limits;
    }

    @Override
    public String toString() {
        return String.format("<html><body><p>%s&nbsp;<br/>%s&nbsp;</p></body></html>", name, shortname);
    }

    public boolean isBrokenBy(int zeit, Strafe strafe, String disziplin, ASchwimmer s, int year, int round) {
        return Arrays.stream(limits).anyMatch(m -> m.isBrokenBy(zeit, strafe, disziplin, s, year, round, check));
    }
}