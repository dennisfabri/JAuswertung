package de.df.jauswertung.daten.regelwerk;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/*
 * Strafe.java Created on 7. August 2001, 20:15
 * @author Dennis Fabri
 */
public class Strafe implements Serializable {

    @Serial
    private static final long serialVersionUID = -6097335676567287451L;

    public static final Strafe NICHTS = new Strafe("", "", Strafarten.NICHTS, 0);
    public static final Strafe NICHT_ANGETRETEN = new Strafe("", "", Strafarten.NICHT_ANGETRETEN, 0);
    public static final Strafe NICHT_BEENDET = new Strafe("Nicht beendet", "n.b.", Strafarten.DISQUALIFIKATION, 0);
    public static final Strafe DISQUALIFIKATION = new Strafe("", "", Strafarten.DISQUALIFIKATION, 0);
    public static final Strafe AUSSCHLUSS = new Strafe("", "", Strafarten.AUSSCHLUSS, 0);
    public static final Strafe WITHDRAW = new Strafe("Withdraw", "WD", Strafarten.NICHTS, 0);
    public static final Strafe WITHDRAW_BEFORE_FIRST_START = new Strafe("Withdraw before first start", "WD1S",
            Strafarten.NICHT_ANGETRETEN, 0);

    @XStreamAsAttribute
    private String name = "";
    @XStreamAsAttribute
    private String shortname = "";
    @XStreamAsAttribute
    private Strafarten art;
    @XStreamAsAttribute
    private int hoehe = 0;

    // Keep for compatibility with XStream
    @SuppressWarnings("unused")
    @Deprecated
    private String durchsage = "";

    public Strafe(Strafe s) {
        if (s == null) {
            setName("");
            setShortname("");
            setArt(Strafarten.NICHTS);
            setStrafpunkte(0);
        } else {
            name = s.name;
            shortname = s.shortname;
            art = s.art;
            hoehe = s.getStrafpunkte();
        }
    }

    public Strafe(Strafe s, boolean beiStrafeDisqualifikation) {
        this(s);
        if (beiStrafeDisqualifikation && (getArt() == Strafarten.STRAFPUNKTE) && (getStrafpunkte() > 0)) {
            art = Strafarten.DISQUALIFIKATION;
            hoehe = 0;
        }
    }

    /**
     * Creates new Strafe
     */
    public Strafe(String strafenname, String kurzname, Strafarten strafenart, int strafenhoehe) {
        setName(strafenname);
        setShortname(kurzname);
        setArt(strafenart);
        if (strafenart == Strafarten.STRAFPUNKTE) {
            setStrafpunkte(strafenhoehe);
        }
    }

    public Strafe(int punkte) {
        this("", "", Strafarten.STRAFPUNKTE, punkte);
    }

    public Strafe() {
        this("", "", Strafarten.NICHTS, 0);
    }

    @Override
    public String toString() {
        String s = name;
        String typeText;

        switch (art) {
        case NICHTS:
            typeText = "Keine Strafe";
            break;
        case STRAFPUNKTE:
            typeText = hoehe + " Strafpunkte";
            break;
        case NICHT_ANGETRETEN:
            typeText = "Nicht Angetreten";
            break;
        case DISQUALIFIKATION:
            typeText = "Disqualifikation";
            break;
        case AUSSCHLUSS:
            typeText = "Ausschluss";
            break;
        default:
            typeText = "Unbekannt";
            break;
        }

        if (!s.isEmpty()) {
            s += " (" + typeText + ")";
        } else {
            s = typeText;
        }

        if (!shortname.isEmpty()) {
            s = shortname + ": " + s;
        }

        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Strafe) {
            return equals((Strafe) o);
        }
        return false;
    }

    public boolean equals(Strafe s) {
        if (s == null) {
            return false;
        }
        if (!name.equals(s.getName())) {
            return false;
        }
        if (!shortname.equals(s.getShortname())) {
            return false;
        }
        if (art != s.getArt()) {
            return false;
        }
        if (art != Strafarten.STRAFPUNKTE) {
            return true;
        }
        return hoehe == s.getStrafpunkte();
    }

    public boolean isStrafe() {
        return Objects.requireNonNull(art) != Strafarten.NICHTS;
    }

    public boolean cancelsPoints() {
        return switch (art) {
        case NICHTS, STRAFPUNKTE -> false;
        default -> true;
        };
    }

    public String getName() {
        return name;
    }

    public String getShortname() {
        return shortname;
    }

    public Strafarten getArt() {
        return art;
    }

    public int getStrafpunkte() {
        if (art != Strafarten.STRAFPUNKTE) {
            return 0;
        }
        return hoehe;
    }

    private void setName(String sName) {
        if (sName == null) {
            return;
        }
        name = sName;
    }

    private void setShortname(String sShortname) {
        if (sShortname == null) {
            return;
        }
        shortname = sShortname;
    }

    private void setArt(Strafarten sArt) {
        art = sArt;
    }

    private void setStrafpunkte(int sHoehe) {
        if (sHoehe <= 0) {
            sHoehe = 0;
        } else {
            setArt(Strafarten.STRAFPUNKTE);
        }
        hoehe = sHoehe;
    }

    public Strafe join(Strafe s1) {
        String n = name;
        String s = shortname;
        Strafarten a = Strafarten.NICHTS;
        int p = 0;

        if (n.isEmpty()) {
            n = s1.name;
        } else {
            if (!s1.name.isEmpty()) {
                n += " & " + s1.name;
            }
        }
        if (s.isEmpty()) {
            s = s1.shortname;
        } else {
            if (!s1.shortname.isEmpty()) {
                s += " & " + s1.shortname;
            }
        }
        if ((art == Strafarten.AUSSCHLUSS) || (s1.art == Strafarten.AUSSCHLUSS)) {
            a = Strafarten.AUSSCHLUSS;
        } else {
            if ((art == Strafarten.DISQUALIFIKATION) || (s1.art == Strafarten.DISQUALIFIKATION)) {
                a = Strafarten.DISQUALIFIKATION;
            } else {
                if ((art == Strafarten.STRAFPUNKTE) || (s1.art == Strafarten.STRAFPUNKTE)) {
                    a = Strafarten.STRAFPUNKTE;
                    p = getStrafpunkte() + s1.getStrafpunkte();
                } else {
                    if ((art == Strafarten.NICHT_ANGETRETEN) || (s1.art == Strafarten.NICHT_ANGETRETEN)) {
                        a = Strafarten.STRAFPUNKTE;
                        p = getStrafpunkte() + s1.getStrafpunkte();
                    } else {
                        a = Strafarten.NICHTS;
                        p = 0;
                    }
                }
            }
        }
        return new Strafe(n, s, a, p);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isWithdraw() {
        return getArt() == WITHDRAW.getArt() && WITHDRAW.getShortname().equalsIgnoreCase(shortname);
    }

    public boolean isDidNotStart() {
        return getArt() == Strafarten.NICHT_ANGETRETEN;
    }

}