/*
 * Altersklasse.java Created on 9. Februar 2001, 12:51
 */

package de.df.jauswertung.daten.regelwerk;

/**
 * Hier werden die Information festgehalten, die fuer die jeweilige Altersklasse
 * gelten.
 * 
 * @author Dennis Fabri
 */
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Stack;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jutils.util.StringTools;

public class Altersklasse implements Serializable {

    private static final long serialVersionUID = -3984390739372155907L;

    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private boolean hlw;
    private Disziplin[][] disziplinen;
    private boolean[] gesamtwertung = { true, true };
    private double[] minpunkte = { 0, 0 };
    private int[] alter = { 0, 0 };
    private int[] alterMannschaft = { 0, 0 };
    @XStreamAsAttribute
    private boolean kompakt = false;
    @XStreamAsAttribute
    private boolean diszchoice = false;
    @XStreamAsAttribute
    private int mindisz = 1;
    @XStreamAsAttribute
    private int useddisz = 1;
    @XStreamAsAttribute
    private int maxdisz = 1;
    @XStreamAsAttribute
    private boolean keineMehrkampfwertung = false;
    @XStreamAsAttribute
    private boolean einzelwertung = false;
    @XStreamAsAttribute
    private boolean einzelwertunghlw = false;
    @XStreamAsAttribute
    private boolean strafeIstDisqualifikation = false;
    @XStreamAsAttribute
    private int laufsortierung = Laufliste.REIHENFOLGE_MELDEZEITEN;
    @XStreamAsAttribute
    private boolean laufrotation = true;
    @XStreamAsAttribute
    private int minMembers = 4;
    @XStreamAsAttribute
    private int maxMembers = 5;
    @XStreamAsAttribute
    private String startgruppe = null;
    @XStreamAsAttribute
    private String wertungsgruppe = null;

    private InterneStartgruppe interneStartgruppe = new InterneStartgruppe();

    /**
     * Erzeugt eine neue Altersklasse.
     * 
     * @param _akNummer   Nummer der Altersklasse
     * @param _geschlecht das Geschlecht
     * @param einzel      true wenn es eine EinzelAK ist, sonst false
     */
    Altersklasse(int akNummer) {
        this("", null, false);
        erzeuge(akNummer);
    }

    /**
     * Erzeugt eine Altersklasse mit selbstdefinierten Werten.
     * 
     * @param _name       Gibt den Namen an.
     * @param _diszAnzahl Gibt die Anzahl der Disziplinen an.
     * @param _diszes     Gibt die einzelnen Disziplinen als Array an.
     * @param _hlw        Gibt an, ob eine Zusatzwertung durchgefuehrt werden muss
     *                    oder nicht.
     */
    public Altersklasse(String aName, Disziplin[][] aDiszes, boolean aHlw) {
        setName(aName);
        disziplinen = aDiszes;
        hlw = aHlw;
    }

    /**
     * Erzeugt eine Altersklasse mit selbstdefinierten Werten.
     * 
     * @param _name       Gibt den Namen an.
     * @param _geschlecht Gibt das Geschlecht an.
     * @param _diszAnzahl Gibt die Anzahl der Disziplinen an.
     * @param _diszes     Gibt die einzelnen Disziplinen als Array an.
     * @param _hlw        Gibt an, ob eine HLW durchgefuehrt werden muss oder nicht.
     */
    public Altersklasse(String aName, Disziplin[][] aDiszes, boolean aHlw, boolean aKompakt) {
        this(aName, aDiszes, aHlw);
        kompakt = aKompakt;
    }

    /**
     * Erzeugt eine leere Altersklasse.
     */
    public Altersklasse() {
        this("", null, false);
    }

    /*
     * public static Altersklasse generateGesamtwertungsAK() { return
     * gesamtwertungAK; }
     */
    private void erzeuge(int akNummer) {
        setName("AK " + (akNummer + 1));
        hlw = false;
        disziplinen = new Disziplin[][] { { new Disziplin(), new Disziplin(), new Disziplin(), new Disziplin() },
                { new Disziplin(), new Disziplin(), new Disziplin(), new Disziplin() } };
    }

    public synchronized void setKompaktToString(boolean b) {
        kompakt = b;
    }

    public boolean getKompaktToString() {
        return kompakt;
    }

    /**
     * Liefert den Namen mit Geschlecht zurueck.
     * 
     * @return Liefert den Namen mit Geschlecht.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Liefert den Namen zurueck.
     * 
     * @return Gibt den Namen ohne Geschlecht zurueck.
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Namen.
     * 
     * @param _name Dies ist der neue Name.
     */
    public synchronized void setName(String aName) {
        if (aName == null) {
            aName = "";
        }
        name = aName.trim();
    }

    public void setMinimalPoints(boolean male, double d) {
        minpunkte[male ? 1 : 0] = d;
    }

    public double getMinimalPoints(boolean male) {
        return minpunkte[male ? 1 : 0];
    }

    /**
     * Liefert die entsprechende Disziplin zurueck.
     * 
     * @param disz Gibt die Nummer der Disziplin an. Dabei entspricht 0 der ersten
     *             Disziplin.
     * @return Liefert den Rec-Wert
     */
    public Disziplin getDisziplin(int disz, boolean maennlich) {
        if ((getDiszAnzahl() > disz) && (0 <= disz)) {
            return disziplinen[(maennlich ? 1 : 0)][disz];
        }
        return null;
    }

    public Disziplin getDisziplin(String disz, boolean maennlich) {
        for (int x = 0; x < disziplinen[0].length; x++) {
            Disziplin d = disziplinen[(maennlich ? 1 : 0)][x];
            if (d.getName().equalsIgnoreCase(disz)) {
                return d;
            }
        }
        return null;
    }

    public Disziplin[] getDisziplinen(boolean maennlich) {
        Disziplin[] result = new Disziplin[disziplinen[(maennlich ? 1 : 0)].length];
        System.arraycopy(disziplinen[(maennlich ? 1 : 0)], 0, result, 0, result.length);
        return result;
    }

    public Disziplin[][] getDisziplinen() {
        Disziplin[][] result = new Disziplin[disziplinen.length][disziplinen[0].length];
        for (int x = 0; x < result.length; x++) {
            for (int y = 0; y < result[x].length; y++) {
                result[x][y] = disziplinen[x][y];
            }
        }
        return result;
    }

    /**
     * Setzt eine neue Disziplin
     * 
     * @param dz   Dies ist der Parameter, der die neue Disziplin enthaelt.
     * @param disz Dies ist die Nummer der Disziplin, die gesetzt werden soll. 0
     *             entspricht der ersten Disziplin
     */
    public void setDisziplin(Disziplin dz, int disz, boolean maennlich) {
        if ((getDiszAnzahl() > disz) && (0 <= disz) && (dz != null)) {
            disziplinen[(maennlich ? 1 : 0)][disz] = dz;
        }
    }

    /**
     * Gibt an, ob eine HLW durchgefuehrt werden muss oder nicht.
     * 
     * @return TRUE wenn HLW durchgefuehrt wird, sonst FALSE.
     */
    public boolean hasHLW() {
        return hlw;
    }

    /**
     * Legt fest, ob eine HLW durchgefuehrt werden muss.
     * 
     * @param _hlw TRUE wenn eine HLW durchgefuehrt werden soll, sonst FALSE.
     */
    public void setHLW(boolean aHlw) {
        hlw = aHlw;
    }

    /**
     * Liefert die Anzahl der Disziplinen zurueck.
     * 
     * @return Anzahl der Disziplinen
     */
    public int getDiszAnzahl() {
        if (disziplinen == null) {
            return 0;
        }
        return Math.min(disziplinen[0].length, disziplinen[1].length);
    }

    /**
     * Setzt die Anzahl der Disziplinen neu fest. Die alten Disziplinen werden dabei
     * gel\u00F6scht.
     * 
     * @param _diszAnzahl Neue Anzahl der Disziplinen.
     */
    public void setDiszAnzahl(int diszAnzahl) {
        if (disziplinen == null) {
            disziplinen = new Disziplin[2][0];
        }
        if (getDiszAnzahl() == diszAnzahl) {
            return;
        }
        Disziplin[][] disziplinenNeu = new Disziplin[2][diszAnzahl];
        for (int x = 0; x < diszAnzahl; x++) {
            if (disziplinen[0].length > x) {
                disziplinenNeu[0][x] = disziplinen[0][x];
            } else {
                disziplinenNeu[0][x] = new Disziplin();
            }
            if (disziplinen[1].length > x) {
                disziplinenNeu[1][x] = disziplinen[1][x];
            } else {
                disziplinenNeu[1][x] = new Disziplin();
            }
        }
        disziplinen = disziplinenNeu;
    }

    public String[] getDisziplinenNamen() {
        String[] namen = new String[disziplinen[0].length];
        for (int x = 0; x < disziplinen[0].length; x++) {
            namen[x] = disziplinen[0][x].getName();
        }
        return namen;
    }

    public boolean getGesamtwertung(boolean maennlich) {
        return gesamtwertung[(maennlich ? 1 : 0)];
    }

    public void setGesamtwertung(boolean maennlich, boolean g) {
        gesamtwertung[(maennlich ? 1 : 0)] = g;
    }

    public void setDisciplineChoiceAllowed(boolean b) {
        diszchoice = b;
    }

    public boolean isDisciplineChoiceAllowed() {
        return diszchoice;
    }

    public int getMinimalChosenDisciplines() {
        if (!diszchoice) {
            return getDiszAnzahl();
        }
        return mindisz;
    }

    public int getUsedDisciplines() {
        if (!diszchoice) {
            return getDiszAnzahl();
        }
        return useddisz;
    }

    public int getMaximalChosenDisciplines() {
        if (!diszchoice) {
            return getDiszAnzahl();
        }
        return maxdisz;
    }

    public void setChosenDisciplines(int min, int used, int max) {
        if (min < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (max < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (used < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (min > disziplinen[0].length) {
            throw new IndexOutOfBoundsException();
        }
        if (max > disziplinen[0].length) {
            throw new IndexOutOfBoundsException();
        }
        if (used > disziplinen[0].length) {
            throw new IndexOutOfBoundsException();
        }
        if (used < min) {
            throw new IllegalArgumentException();
        }
        if (max < used) {
            throw new IllegalArgumentException();
        }
        maxdisz = max;
        useddisz = used;
        mindisz = min;
    }

    public boolean hasEinzelwertung() {
        return einzelwertung;
    }

    public void setEinzelwertung(boolean einzelwertung) {
        this.einzelwertung = einzelwertung;
    }

    public boolean isEinzelwertungHlw() {
        if (!hasHLW()) {
            return false;
        }
        return einzelwertunghlw;
    }

    public void setEinzelwertungHlw(boolean einzelwertunghlw) {
        this.einzelwertunghlw = einzelwertunghlw;
    }

    public boolean isStrafeIstDisqualifikation() {
        return strafeIstDisqualifikation;
    }

    public void setStrafeIstDisqualifikation(boolean strafeIstDisqualifikation) {
        this.strafeIstDisqualifikation = strafeIstDisqualifikation;
    }

    public int getLaufsortierung() {
        return laufsortierung;
    }

    public void setLaufsortierung(int laufsortierung) {
        this.laufsortierung = laufsortierung;
    }

    public boolean getLaufrotation() {
        return laufrotation;
    }

    public int getMinimumAlter() {
        return alter[0];
    }

    public int getMaximumAlter() {
        return alter[1];
    }

    public int getMinimumAlterInSumme() {
        if (alterMannschaft == null) {
            alterMannschaft = new int[] { 0, 0 };
        }
        return alterMannschaft[0];
    }

    public int getMaximumAlterInSumme() {
        if (alterMannschaft == null) {
            alterMannschaft = new int[] { 0, 0 };
        }
        return alterMannschaft[1];
    }

    public void setMinimumAlter(int alt) {
        if (alt < 0) {
            alt = 0;
        }
        alter[0] = alt;
    }

    public void setMaximumAlter(int alt) {
        if (alt < 0) {
            alt = 0;
        }
        alter[1] = alt;
    }

    public void setMinimumAlterInSumme(int alt) {
        if (alterMannschaft == null) {
            alterMannschaft = new int[] { 0, 0 };
        }
        if (alt < 0) {
            alt = 0;
        }
        alterMannschaft[0] = alt;
    }

    public void setMaximumAlterInSumme(int alt) {
        if (alterMannschaft == null) {
            alterMannschaft = new int[] { 0, 0 };
        }
        if (alt < 0) {
            alt = 0;
        }
        alterMannschaft[1] = alt;
    }

    public boolean fitsYearOfBirth(int year, int base) {
        if (year == 0) {
            return true;
        }
        int age = base - year;
        return fitsAge(age);
    }

    public boolean fitsAge(int age) {
        if (alter[0] > 0) {
            if (age < alter[0]) {
                return false;
            }
        }
        if (alter[1] > 0) {
            if (age > alter[1]) {
                return false;
            }
        }
        return true;
    }

    public void setLaufrotation(boolean laufrotation) {
        this.laufrotation = laufrotation;
    }

    public int getMinMembers() {
        if (minMembers <= 0) {
            minMembers = 4;
        }
        return minMembers;
    }

    public void setMemberCounts(int min, int max) {
        if ((min <= 0) || (max <= 0)) {
            throw new IllegalArgumentException("Min and max must be higher than zero: " + min + ", " + max + "!");
        }
        if (min > max) {
            throw new IllegalArgumentException("Min must not be higher than max: " + min + ">" + max + "!");
        }
        this.minMembers = min;
        this.maxMembers = max;
    }

    public int getMaxMembers() {
        if (maxMembers <= 0) {
            maxMembers = 5;
        }
        return maxMembers;
    }

    public String getChecksum() {
        String[] text = new String[getDiszAnzahl()];

        int length = 50;
        for (int x = 0; x < text.length; x++) {
            Disziplin d = getDisziplin(x, true);
            length = Math.max(length, d.getName().length());
        }
        for (int x = 0; x < text.length; x++) {
            StringBuilder sb = new StringBuilder();

            {
                Disziplin d = getDisziplin(x, true);
                sb.append(d.getName());
                StringTools.fill(sb, length - d.getName().length());
            }
            for (int y = 0; y < 2; y++) {
                Disziplin d = getDisziplin(x, y == 1);
                sb.append(StringTools.zeitString(d.getRec()));
                sb.append("x");
            }

            text[x] = sb.toString();
        }

        Arrays.sort(text);
        StringBuilder sb = new StringBuilder();
        for (String aText : text) {
            sb.append(aText);
            sb.append("|");
        }
        return StringTools.crc(sb.toString());
    }

    public Startgruppe getInterneStartgruppe() {
        if (interneStartgruppe == null) {
            interneStartgruppe = new InterneStartgruppe();
        }
        return interneStartgruppe;
    }

    public String getStartgruppe() {
        return startgruppe;
    }

    public void setStartgruppe(String startgruppe) {
        if ((startgruppe != null) && (startgruppe.length() == 0)) {
            startgruppe = null;
        }
        this.startgruppe = startgruppe;
    }

    public String getWertungsgruppe() {
        return wertungsgruppe;
    }

    public void setWertungsgruppe(String wertungsgruppe) {
        this.wertungsgruppe = wertungsgruppe;
    }

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
        migrator1(dom.getRootElement());
    }

    public static void migrator1(Element node) {
        node.addElement("protokollMitEinzelwertung").setText("true");
        node.addElement("protokollMitMehrkampfwertung").setText("true");
    }

    @SuppressWarnings("unused")
    private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
        migrator2(dom.getRootElement());
    }

    public static void migrator2(Element node) {
        node.remove(node.element("mehrkampfwertung"));
        node.remove(node.element("protokollMitEinzelwertung"));
        node.remove(node.element("protokollMitMehrkampfwertung"));
    }

    public boolean isValid() {
        Hashtable<String, String> disz = new Hashtable<>();
        for (int x = 0; x < disziplinen[0].length; x++) {
            if (!disziplinen[0][x].getName().equals(disziplinen[1][x].getName())) {
                return false;
            }
            if (disz.containsKey(disziplinen[0][x].getName())) {
                return false;
            }
            disz.put(disziplinen[0][x].getName(), disziplinen[0][x].getName());
        }
        return true;
    }

    public boolean hasMehrkampfwertung() {
        return !keineMehrkampfwertung;
    }

    public void setMehrkampfwertung(boolean mehrkampfwertung) {
        this.keineMehrkampfwertung = !mehrkampfwertung;
    }

    protected class InterneStartgruppe extends Startgruppe {

        public InterneStartgruppe() {
            super((Altersklasse.this.getName() == null || Altersklasse.this.getName().length() == 0 ? "-"
                    : Altersklasse.this.getName()));
        }

        @Override
        public String getName() {
            return Altersklasse.this.getName();
        }

        @Override
        public int getLaufsortierung() {
            return Altersklasse.this.getLaufsortierung();
        }

        @Override
        public boolean hasLaufrotation() {
            return Altersklasse.this.getLaufrotation();
        }
    }

    public boolean isFinal(int disziplin, boolean maennlich, int round) {
        Disziplin dis = getDisziplin(disziplin, maennlich);
        int[] runden = dis.getRunden();
        if (runden == null) {
            return false;
        }
        return runden.length == round;
    }
}