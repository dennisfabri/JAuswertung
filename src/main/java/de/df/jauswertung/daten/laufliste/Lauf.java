/*
 * Lauf.java Created on 20. Juli 2001, 16:09
 */

package de.df.jauswertung.daten.laufliste;

/**
 * @author Dennis Mueller
 * @version 0.1
 */

import java.io.Serializable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.util.Converter;

public class Lauf<T extends ASchwimmer> implements Serializable {

    private static final long serialVersionUID = 6116586299663275071L;

    public static final int   HLW              = -1;

    private ASchwimmer[]      laufteilies      = null;
    private int[]             diszes           = null;
    private int               laufnummer       = 0;
    private int               laufbuchstabe    = 0;
    private boolean           leftToRight      = false;

    private boolean[]         usable           = null;

    private boolean           checked          = false;
    private boolean           retyped          = false;

    public Lauf(int bahnen, int lnummer, int lbuchstabe, boolean[] usable) {
        this(bahnen, lnummer, lbuchstabe);
        if (usable != null) {
            if (usable.length == bahnen) {
                this.usable = usable;
            } else {
                this.usable = new boolean[bahnen];
                for (int x = 0; x < bahnen; x++) {
                    if (usable.length > x) {
                        this.usable[x] = usable[x];
                    } else {
                        this.usable[x] = true;
                    }
                }
            }
        }
    }

    /** Creates new Lauf */
    Lauf(int bahnen, int lnummer, int lbuchstabe) {
        // setLeftToRight(true);
        if (lnummer > 0) {
            laufnummer = lnummer;
        }
        if (lbuchstabe > 0) {
            laufbuchstabe = lbuchstabe;
        }
        if (bahnen > 0) {
            laufteilies = new ASchwimmer[bahnen];
            diszes = new int[bahnen];
            for (int x = 0; x < bahnen; x++) {
                laufteilies[x] = null;
                diszes[x] = 0;
            }
        }
    }

    public boolean isUsable(int x) {
        if (usable == null) {
            return true;
        }
        return usable[x];
    }

    void updateDisziplin(int x, int disziplin) {
        diszes[x] = disziplin;
    }

    public int getBenutzbareBahnenAnzahl() {
        int anzahl = 0;
        for (int x = 0; x < getBahnen(); x++) {
            if (isUsable(x)) {
                anzahl++;
            }
        }
        return anzahl;
    }

    public void resetUsable() {
        usable = null;
    }

    public void setLeftToRight(boolean ltr) {
        leftToRight = ltr;
    }

    public boolean isLeftToRight() {
        return leftToRight;
    }

    public boolean isStartgroup() {
        if (isEmpty()) {
            return false;
        }
        for (ASchwimmer t : laufteilies) {
            if (t != null) {
                String sg = t.getAK().getStartgruppe();
                if (sg != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getStartgruppe() {
        return getStartgruppe(true);
    }

    public String getStartgruppe(boolean kurz) {
        if (isEmpty()) {
            return "";
        }
        String startgruppe = getSchwimmer().getAK().getStartgruppe();
        if (startgruppe == null) {
            startgruppe = getSchwimmer().getAK().getName();
        }
        boolean male = getSchwimmer().isMaennlich();
        boolean mixedSex = false;
        for (ASchwimmer t : laufteilies) {
            if (t != null) {
                String sg = t.getAK().getStartgruppe();
                if (sg == null) {
                    sg = t.getAK().getName();
                }
                if (!sg.equals(startgruppe)) {
                    startgruppe = null;
                }
                if (t.isMaennlich() != male) {
                    mixedSex = true;
                }
            }
        }
        if (kurz) {
            if (startgruppe != null && startgruppe.toLowerCase().startsWith("ak ")) {
                startgruppe = startgruppe.substring(3);
            }
        }
        StringBuffer sb = new StringBuffer();
        if (startgruppe == null) {
            sb.append(I18n.get("Mixed"));
        } else {
            sb.append(startgruppe);
        }
        if (mixedSex) {
            if (startgruppe != null) {
                sb.append(" ");
                if (kurz) {
                    sb.append(I18n.get("mixedShort"));
                } else {
                    sb.append(I18n.get("mixed"));
                }
            }
        } else {
            sb.append(" ");
            if (kurz) {
                sb.append(I18n.geschlechtToShortString(this.getSchwimmer().getRegelwerk(), male));
            } else {
                sb.append(I18n.geschlechtToString(this.getSchwimmer().getRegelwerk(), male));
            }
        }
        return sb.toString();
    }

    public String getAltersklasse() {
        return getAltersklasse(true);
    }

    public String getAltersklasse(boolean kurz) {
        ASchwimmer sw = getSchwimmer();
        if (sw == null) {
            return "";
        }

        // true == liefere ak + maennlich bzw. weiblich zurueck
        // false == liefere ak + "gemischt" zurueck
        boolean selector = true;

        // true == liefere aknamen zurueck
        // false == liefere "Gemischt" zurueck
        boolean akselector = true;

        String ak = sw.getAK().toString();
        boolean maennlich = sw.isMaennlich();

        for (ASchwimmer s : laufteilies) {
            if (s != null) {
                Altersklasse a = s.getAK();
                if (a != null) {
                    if (!ak.equals(a.toString())) {
                        akselector = false;
                    }
                    if (selector && (maennlich != s.isMaennlich())) {
                        selector = false;
                    }
                }
            }
        }

        if (!akselector && !selector) {
            return I18n.get("Mixed");
        }

        if (kurz) {
            if (ak.toLowerCase().startsWith("ak ")) {
                ak = ak.substring(3);
            }
        }

        // Kurze Darstellung
        if (kurz) {
            if (!selector) {
                return ak + " " + I18n.get("mixedShort");
            }
            if (!akselector) {
                return I18n.get("Mixed") + " " + I18n.geschlechtToShortString(getSchwimmer().getRegelwerk(), maennlich);
            }
            return ak + " " + I18n.geschlechtToShortString(getSchwimmer().getRegelwerk(), maennlich);
        }

        // Normale Darstellung
        if (!selector) {
            return ak + " " + I18n.get("mixed");
        }
        if (!akselector) {
            return I18n.get("Mixed") + " " + I18n.geschlechtToString(getSchwimmer().getRegelwerk(), maennlich);
        }
        return ak + " " + I18n.geschlechtToString(getSchwimmer().getRegelwerk(), maennlich);
    }

    public boolean isOnlyOneAgeGroup() {
        ASchwimmer sw = getSchwimmer();
        if (sw == null) {
            return true;
        }

        boolean akselector = true;

        String ak = sw.getAK().toString();

        for (ASchwimmer s : laufteilies) {
            if (s != null) {
                Altersklasse a = s.getAK();
                if (a != null) {
                    if (!ak.equals(a.toString())) {
                        akselector = false;
                    }
                }
            }
        }

        if (!akselector) {
            return false;
        }
        return true;
    }

    public String getName() {
        return "" + laufnummer + Converter.characterString(laufbuchstabe);
    }

    @Override
    public String toString() {
        String s = getName() + ": " + getDisziplin() + " ";
        for (ASchwimmer laufteily : laufteilies) {
            if (laufteily != null) {
                s = s + " - " + laufteily.getName();
            } else {
                s = s + " - xxx";
            }
        }

        return s;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object arg0) {
        if (arg0 instanceof Lauf) {
            return equals((Lauf) arg0);
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public boolean equals(Lauf l) {
        return l.getName().equals(getName());
    }

    @SuppressWarnings("unchecked")
    public String getDisziplin() {
        String disziplin = null;

        for (int x = 0; x < laufteilies.length; x++) {
            T s = (T) laufteilies[x];
            String temp = checkDisziplin(x, s);
            if (disziplin == null) {
                disziplin = (temp != null ? temp.trim() : null);
            } else {
                if ((temp != null) && (!disziplin.equals(temp.trim()))) {
                    return I18n.get("Mixed");
                }
            }
        }

        if (disziplin == null) {
            return "";
        }
        return disziplin;
    }

    @SuppressWarnings("unchecked")
    public boolean isOnlyOneDiscipline() {
        String disziplin = null;

        for (int x = 0; x < laufteilies.length; x++) {
            T s = (T) laufteilies[x];
            String temp = checkDisziplin(x, s);
            if (disziplin == null) {
                disziplin = (temp != null ? temp.trim() : null);
            } else {
                if ((temp != null) && (!disziplin.equals(temp.trim()))) {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean isOnlyOneSex() {
        if (isEmpty()) {
            return true;
        }

        boolean male = getSchwimmer().isMaennlich();

        for (ASchwimmer laufteily : laufteilies) {
            T s = (T) laufteily;
            if (s != null) {
                if (s.isMaennlich() != male) {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getDisziplinShort() {
        String disziplin = null;

        for (int x = 0; x < laufteilies.length; x++) {
            T s = (T) laufteilies[x];
            String temp = checkDisziplin(x, s);
            if (disziplin == null) {
                disziplin = (temp != null ? temp.trim() : null);
            } else {
                if ((temp != null) && (!disziplin.equals(temp.trim()))) {
                    return I18n.get("Mixed");
                }
            }
        }

        if (disziplin == null) {
            return "";
        }
        return I18n.getDisziplinShort(disziplin);
    }

    /**
     * @param disz
     * @param disziplinNumber
     * @param swimmer
     * @return
     */
    private String checkDisziplin(int disziplinNumber, T swimmer) {
        if (swimmer != null) {
            Disziplin d = swimmer.getAK().getDisziplin(diszes[disziplinNumber], swimmer.isMaennlich());
            if (d != null) {
                return d.toString();
            }
        }
        return null;
    }

    public synchronized boolean addSchwimmer(T s, int disziplin) {
        return addSchwimmer(s, disziplin, usable);
    }

    public boolean join(Lauf<T> l) {
        if (l.diszes.length != diszes.length) {
            throw new IllegalArgumentException("Unequal lanes: Amount should be " + diszes.length + " but was " + l.diszes.length);
            // return false;
        }
        boolean[] lanes = null;
        if (l.usable != null) {
            if (usable != null) {
                lanes = new boolean[diszes.length];
                for (int x = 0; x < lanes.length; x++) {
                    lanes[x] = l.usable[x] && usable[x];
                }
            } else {
                lanes = l.usable;
            }
        } else {
            if (usable != null) {
                lanes = usable;
            } else {
                lanes = new boolean[diszes.length];
                for (int x = 0; x < lanes.length; x++) {
                    lanes[x] = true;
                }
            }
        }

        int amount = 0;
        for (boolean lane : lanes) {
            if (lane) {
                amount++;
            }
        }
        int teilnehmer = l.getAnzahl() + getAnzahl();
        if (amount < teilnehmer) {
            // Cannot join heats: Not enough free lanes!
            return false;
        }

        int[] disz = new int[diszes.length];
        ASchwimmer[] sw = new ASchwimmer[diszes.length];
        int pos = 0;
        while (!lanes[pos]) {
            pos++;
        }
        // In die Mitte schieben
        for (int x = 0; x < (amount - teilnehmer) / 2; x++) {
            do {
                pos++;
            } while (!lanes[pos]);
        }

        for (int x = 0; x < diszes.length; x++) {
            if (getSchwimmer(x) != null) {
                sw[pos] = getSchwimmer(x);
                disz[pos] = getDisznummer(x);
                laufteilies[x] = null;
                diszes[x] = 0;
                do {
                    pos++;
                } while ((lanes.length > pos) && (!lanes[pos]));
            }
        }
        for (int x = 0; x < diszes.length; x++) {
            if (l.getSchwimmer(x) != null) {
                sw[pos] = l.getSchwimmer(x);
                disz[pos] = l.getDisznummer(x);
                l.laufteilies[x] = null;
                l.diszes[x] = 0;
                do {
                    pos++;
                } while ((lanes.length > pos) && (!lanes[pos]));
            }
        }
        usable = lanes;
        diszes = disz;
        laufteilies = sw;

        return true;
    }

    public boolean isJoinable(Lauf<T> l) {
        if (l.diszes.length != diszes.length) {
            return false;
        }
        boolean[] lanes = null;
        if (l.usable != null) {
            if (usable != null) {
                lanes = new boolean[diszes.length];
                for (int x = 0; x < lanes.length; x++) {
                    lanes[x] = l.usable[x] && usable[x];
                }
            } else {
                lanes = l.usable;
            }
        } else {
            if (usable != null) {
                lanes = usable;
            } else {
                lanes = new boolean[diszes.length];
                for (int x = 0; x < lanes.length; x++) {
                    lanes[x] = true;
                }
            }
        }

        int amount = 0;
        for (boolean lane : lanes) {
            if (lane) {
                amount++;
            }
        }

        return amount >= l.getAnzahl() + getAnzahl();
    }

    private synchronized boolean addSchwimmer(T s, int disziplin, boolean[] lanes) {
        if (s == null) {
            return false;
        }
        if (laufteilies == null) {
            return false;
        }
        if (lanes == null) {
            lanes = new boolean[diszes.length];
            for (int x = 0; x < lanes.length; x++) {
                lanes[x] = true;
            }
        }
        if (isFull(lanes)) {
            return false;
        }

        if (leftToRight) {
            for (int x = 0; x < laufteilies.length; x++) {
                if ((laufteilies[x] == null) && (lanes[x])) {
                    setSchwimmer(s, disziplin, x);
                    return true;
                }
            }
            return false;
        }

        int pos = (laufteilies.length - 1) / 2;
        int mod = 0;
        for (int x = 0; x < laufteilies.length; x++) {
            if (mod > 0) {
                pos = pos + mod;
                mod = -mod - 1;
            } else {
                pos = pos + mod;
                mod = -mod + 1;
            }
            if ((laufteilies[pos] == null) && (lanes[pos])) {
                setSchwimmer(s, disziplin, pos);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeSchwimmer(int x) {
        if (laufteilies == null) {
            return false;
        }
        if (x < 0) {
            return false;
        }
        if (x >= laufteilies.length) {
            return false;
        }

        diszes[x] = 0;
        laufteilies[x] = null;
        return true;
    }

    public synchronized boolean removeSchwimmer(T s) {
        if (s == null) {
            return false;
        }
        if (laufteilies == null) {
            return false;
        }

        boolean zurueck = false;

        for (int x = 0; x < laufteilies.length; x++) {
            ASchwimmer w = laufteilies[x];
            if (w != null) {
                if (w.equals(s)) {
                    removeSchwimmer(x);
                    zurueck = true;
                }
            }
        }

        return zurueck;
    }

    public synchronized void leeren() {
        for (int x = 0; x < laufteilies.length; x++) {
            removeSchwimmer(x);
        }
    }

    public synchronized boolean setSchwimmer(T s, int ldisz, int pos) {
        if (s == null) {
            return false;
        }
        if ((ldisz < -1) || (ldisz >= s.getAK().getDiszAnzahl())) {
            return false;
        }
        if (laufteilies == null) {
            return false;
        }
        if (pos < 0) {
            return false;
        }
        if (pos >= laufteilies.length) {
            return false;
        }

        removeSchwimmer(pos);

        diszes[pos] = ldisz;
        laufteilies[pos] = s;
        return true;
    }

    @SuppressWarnings("unchecked")
    public T getSchwimmer(int x) {
        if (laufteilies == null) {
            return null;
        }
        if (x < 0) {
            return null;
        }
        if (x >= laufteilies.length) {
            return null;
        }

        return (T) laufteilies[x];
    }

    public int getDisznummer(int x) {
        if (diszes == null) {
            return 0;
        }
        if (x < 0) {
            return 0;
        }
        if (x >= diszes.length) {
            return 0;
        }

        return diszes[x];
    }

    @SuppressWarnings("unchecked")
    public T getSchwimmer() {
        if (laufteilies == null) {
            return null;
        }
        ASchwimmer ergebnis = null;
        for (ASchwimmer laufteily : laufteilies) {
            if (laufteily != null) {
                ergebnis = laufteily;
            }
        }
        return (T) ergebnis;
    }

    public int getSchwimmer(T s) {
        if (s == null) {
            return -1;
        }
        if (laufteilies == null) {
            return -1;
        }

        for (int x = 0; x < laufteilies.length; x++) {
            if ((laufteilies[x] != null) && (laufteilies[x].equals(s))) {
                return x;
            }
        }
        return -1;
    }

    void rotieren(int anzahl, int weite) {
        for (int x = 0; x < anzahl; x++) {
            rotieren(weite);
        }
    }

    synchronized void rotieren(int weite) {
        if (weite <= 0) {
            throw new IllegalArgumentException("Weite must be higher than 0 but was " + weite);
        }
        if (getAnzahl() <= 1) {
            return;
        }

        if (getAnzahl() == 2) {
            ASchwimmer s = null;
            int disziplin = 0;

            int x1 = -1;
            do {
                x1++;
                disziplin = diszes[x1];
                s = laufteilies[x1];
            } while (s == null);

            s = null;
            int x2 = x1;
            do {
                x2++;
                disziplin = diszes[x2];
                s = laufteilies[x2];
            } while (s == null);

            diszes[x2] = diszes[x1];
            diszes[x1] = disziplin;

            laufteilies[x2] = laufteilies[x1];
            laufteilies[x1] = s;
        } else {
            for (int x = 0; x < weite; x++) {
                int index = 0;
                while (laufteilies[index] == null) {
                    index++;
                }

                ASchwimmer first = laufteilies[index];
                int discipline = diszes[index];

                for (int y = index + 1; y < laufteilies.length; y++) {
                    if (laufteilies[y] != null) {
                        laufteilies[index] = laufteilies[y];
                        diszes[index] = diszes[y];
                        index = y;
                        laufteilies[index] = first;
                        diszes[index] = discipline;
                    }
                }
            }
        }
    }

    public boolean includes(T schwimmer, int disziplin) {
        for (int x = 0; x < laufteilies.length; x++) {
            if (schwimmer.equals(laufteilies[x])) {
                if (diszes[x] == disziplin) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getIndex(T schwimmer, int disziplin) {
        for (int x = 0; x < laufteilies.length; x++) {
            if (schwimmer.equals(laufteilies[x])) {
                if (diszes[x] == disziplin) {
                    return x;
                }
            }
        }
        return -1;
    }

    public int getLaufnummer() {
        return laufnummer;
    }

    public void setLaufnummer(int x) {
        if (x < 0) {
            return;
        }
        laufnummer = x;
    }

    public int getLaufbuchstabe() {
        return laufbuchstabe;
    }

    public void setLaufbuchstabe(int x) {
        if ((x < 0) || (x >= 26)) {
            return;
        }
        laufbuchstabe = x;
    }

    public int getBahnen() {
        return laufteilies.length;
    }

    public int getAnzahl() {
        int zahl = 0;
        for (ASchwimmer laufteily : laufteilies) {
            if (laufteily != null) {
                zahl++;
            }
        }
        return zahl;
    }

    public void setBahnen(int bahnen) {
        if (bahnen != laufteilies.length) {
            int[] d = new int[bahnen];
            ASchwimmer[] s = new ASchwimmer[bahnen];
            for (int x = 0; x < bahnen; x++) {
                if (x < laufteilies.length) {
                    d[x] = diszes[x];
                    s[x] = laufteilies[x];
                } else {
                    d[x] = 0;
                    s[x] = null;
                }
            }
            for (int x = bahnen; x < laufteilies.length; x++) {
                removeSchwimmer(x);
            }
            diszes = d;
            laufteilies = s;
        }
    }

    public boolean isFull() {
        if (usable != null) {
            return isFull(usable);
        }
        for (ASchwimmer laufteily : laufteilies) {
            if (laufteily == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isFull(boolean[] laneSelection) {
        for (int x = 0; x < laufteilies.length; x++) {
            if ((laufteilies[x] == null) && (laneSelection[x])) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (ASchwimmer laufteily : laufteilies) {
            if (laufteily != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isRetyped() {
        return retyped;
    }

    public void setRetyped(boolean retyped) {
        this.retyped = retyped;
    }

    public boolean alleZeitenEingegeben() {
        for (int x = 0; x < getBahnen(); x++) {
            if (getSchwimmer(x) != null) {
                ASchwimmer s = getSchwimmer(x);
                if (!s.hasInput(getDisznummer(x))) {
                    return false;
                }
            }
        }
        return true;
    }
}