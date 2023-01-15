package de.df.jauswertung.daten;

import java.io.Serializable;

import de.df.jauswertung.daten.regelwerk.Strafe;

public class Timelimit implements Serializable {

    private String disziplin = "";
    private int time = 0;
    private int minage = 0;
    private int maxage = 0;
    private String agegroup = "";
    private boolean isMale = false;

    public Timelimit() {
    }

    public Timelimit(String disziplin, int time, int minage, int maxage, String agegroup, boolean male) {
        setDisziplin(disziplin);
        setTime(time);
        setMinage(minage);
        setMaxage(maxage);
        setAgegroup(agegroup);
        setMale(male);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean hasMinage() {
        return minage > 0;
    }

    public int getMinage() {
        return minage;
    }

    public void setMinage(int minage) {
        this.minage = minage;
    }

    public boolean hasMaxage() {
        return maxage > 0;
    }

    public int getMaxage() {
        return maxage;
    }

    public void setMaxage(int maxage) {
        this.maxage = maxage;
    }

    public String getDisziplin() {
        return disziplin;
    }

    public void setDisziplin(String disziplin) {
        this.disziplin = disziplin == null ? "" : disziplin.trim();
    }

    public boolean hasAgegroup() {
        return agegroup != null && (agegroup.trim().length() > 0);
    }

    public String getAgegroup() {
        return agegroup;
    }

    public void setAgegroup(String agegroup) {
        this.agegroup = agegroup == null ? "" : agegroup.trim();
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean isMale) {
        this.isMale = isMale;
    }

    public boolean matches(String disziplin, ASchwimmer s, int year) {
        boolean maennlich = s.isMaennlich();
        disziplin = disziplin.trim();
        if (isMale() != maennlich) {
            return false;
        }
        if (!getDisziplin().equalsIgnoreCase(disziplin)) {
            return false;
        }
        if (hasAgegroup()) {
            if (!getAgegroup().equalsIgnoreCase(s.getAK().getName().trim())) {
                return false;
            }
        }
        if (s instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) s;
            int age = t.getAlter(year);
            if (hasMinage()) {
                if (age == 0) {
                    return false;
                }
                if (age < getMinage()) {
                    return false;
                }
            }
            if (hasMaxage()) {
                if (age == 0) {
                    return false;
                }
                if (age > getMaxage()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBrokenBy(int zeit, Strafe strafe, String disziplin, ASchwimmer s, int year, int round,
            Timelimitchecktype type) {
        if (matches(disziplin, s, year)) {
            switch (type) {
            case UPPER_LIMIT:
                if (round > 0) {
                    return false;
                }
                if (strafe.isStrafe()) {
                    return true;
                }
                if (zeit <= 0) {
                    return true;
                }
                return zeit > getTime();
            case LOWER_LIMIT:
                if (strafe.isStrafe()) {
                    return false;
                }
                return zeit < getTime();
            }
        }
        return false;
    }
}