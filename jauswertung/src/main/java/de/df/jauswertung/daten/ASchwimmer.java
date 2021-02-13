package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.util.ArrayUtils;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Mueller @version 0.1 @since 9. Februar 2001, 12:10
 */
public abstract class ASchwimmer implements Comparable<ASchwimmer>, Serializable {

    /*
     * Definiert den Index fuer die Zusatzwertung.
     */
    public static final int            DISCIPLINE_NUMBER_ZW   = -1;
    /*
     * Definiert den Index fuer Strafen, die direkt den
     * Schwimmer und keine Disziplin betreffen.
     */
    public static final int            DISCIPLINE_NUMBER_SELF = -2;

    /*
     * Kleinste gemessene Zeitunterschiede sind 0.01 also ist 0.01/2 ein sinnvolles Epsilon.
     */
    public static final double         TIME_EPSILON           = 0.005;

    @XStreamAsAttribute
    private String                     bemerkung;
    @XStreamAsAttribute
    private String                     gliederung;
    @XStreamAsAttribute
    private boolean                    maennlich;
    @XStreamAsAttribute
    private int                        aknummer;
    @XStreamAsAttribute
    private String                     qualifikationsebene;
    @XStreamAsAttribute
    private Qualifikation              quali                  = Qualifikation.OFFEN;

    private int[][]                    starter;
    private int[]                      zeiten;
    private int[]                      meldezeiten;
    private boolean[]                  disciplineChoice;
    private LinkedList<Strafe>         allgemeineStrafen;
    private LinkedList<Strafe>[]       strafen;

    private Hashtable<String, Eingabe> eingaben               = new Hashtable<String, Eingabe>();

    @XStreamAsAttribute
    private boolean                    ausserkonkurrenz;
    @XStreamAsAttribute
    private int                        startnummer;
    private HLWStates[]                hlwState;
    private double[]                   punktehlw;

    @XStreamAsAttribute
    private boolean                    dopingkontrolle        = false;

    private double[]                   meldepunkte;
    private boolean[]                  meldungMitProtokoll;

    private Startunterlagen            startunterlagen;

    @SuppressWarnings("rawtypes")
    private AWettkampf                 wk;

    /**
     * Erzeugt einen neuen Schwimmer.
     * 
     * @param sWK
     *            Wettkampf @param sGeschlecht Geschlecht @param sGliederung Name der Gliederung @param ak Nummer der
     *            Altersklasse @param sBemerkung Bemerkung @param maxhlw Durchzufuehrende HLW (nur fuer die HLW-Liste)
     */
    @SuppressWarnings("rawtypes")
    public ASchwimmer(AWettkampf sWK, boolean sGeschlecht, String sGliederung, int ak, String sBemerkung) {
        wk = sWK;

        ausserkonkurrenz = false;
        startnummer = 0;
        hlwState = null;
        punktehlw = null;
        initHLW();
        meldepunkte = new double[1];
        meldepunkte[0] = 0;
        meldungMitProtokoll = new boolean[1];
        meldungMitProtokoll[0] = false;
        disciplineChoice = new boolean[0];
        meldezeiten = new int[0];

        allgemeineStrafen = new LinkedList<Strafe>();

        startunterlagen = Startunterlagen.NICHT_PRUEFEN;

        setBemerkung(sBemerkung);
        setMaennlich(sGeschlecht);
        setGliederung(sGliederung);
        setAKNummer(ak, false);
    }

    /**
     * Liefert die Altersklasse des Schwimmers.
     * 
     * @return Altersklasse
     */
    public Altersklasse getAK() {
        return wk.getRegelwerk().getAk(aknummer);
    }

    /**
     * Liefert die Nummer der Altersklasse des Schwimmers.
     * 
     * @return Nummer der Altersklasse
     */
    public int getAKNummer() {
        return aknummer;
    }

    /**
     * Generiert einen Text mit dem Inhalt des Schwimmers.
     * 
     * @return Inhalt des Schwimmers
     */
    @Override
    public String toString() {
        String s = "";
        if (ausserkonkurrenz) {
            s = " (Au\u00DFer Konkurrenz)";
        }
        return "S#" + startnummer + " - " + getAK().toString() + " " + (maennlich ? "männlich" : "weiblich") + " - " + gliederung + " - " + getName() + s;
    }

    @Override
    public int compareTo(ASchwimmer o) {
        return getStartnummer() - o.getStartnummer();
    }

    /**
     * Vergleicht diesen Schwimmer mit einem Anderen. Es wird ledigtlich die Startnummer unterschieden
     * 
     * @param t
     *            Anderer Schwimmer @return true wenn beide Schwimmer die gleiche Startnummer haben.
     */
    public boolean equals(ASchwimmer t) {
        if (t == null) {
            return false;
        }
        return t.getStartnummer() == getStartnummer();
    }

    @SuppressWarnings("unchecked")
    public <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
        return wk;
    }

    <T extends ASchwimmer> void setWettkampf(AWettkampf<T> wk) {
        this.wk = wk;
    }

    @Override
    public boolean equals(Object s) {
        if (s instanceof ASchwimmer) {
            return equals((ASchwimmer) s);
        }
        return false;
    }

    /**
     * Liefert den Namen des Schwimmers.
     * 
     * @return Name des Schwimmers
     */
    public abstract String getName();

    /**
     * Liefert die Gliederung des Schwimmers.
     * 
     * @return Name der Gliederung
     */
    public String getGliederung() {
        return gliederung;
    }

    public String getGliederungMitQGliederung() {
        StringBuffer sb = new StringBuffer();
        sb.append(gliederung);
        if (getQualifikationsebene().length() > 0) {
            sb.append(" (");
            sb.append(qualifikationsebene);
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Setzt den Namen der Gliederung
     * 
     * @param g
     *            Name der Gliederung
     */
    public void setGliederung(String g) {
        if (g == null) {
            throw new NullPointerException("Organisation must not be null!");
        }
        gliederung = wk.getGliederung(g.trim());
    }

    /**
     * Liefert die Startnummer des Schwimmers.
     * 
     * @return Startnummer
     */
    public int getStartnummer() {
        return startnummer;
    }

    /**
     * Setzt die Startnummer.
     * 
     * @param sn
     *            Startnummer (>0)
     */
    public void setStartnummer(int sn) {
        if (sn < 0) {
            throw new IllegalArgumentException("Startnumber must not be negative!");
        }
        startnummer = sn;
    }

    /**
     * Identifiziert den Schwimmer als "Ausser Konkurrenz".
     * 
     * @return true, wenn der Schwimmer ausser Konkurrenz schwimmt
     */
    public boolean isAusserKonkurrenz() {
        return ausserkonkurrenz;
    }

    /**
     * Setzt den Schwimmer als (nicht) "Ausser Konkurrenz".
     * 
     * @param sAk
     *            true, wenn der Schwimmer "Ausser Konkurrenz" teilnimmt. false sonst.
     */
    public void setAusserKonkurrenz(boolean sAk) {
        ausserkonkurrenz = sAk;
    }

    /**
     * Liefert die Meldepunkte.
     * 
     * @return Meldepunkte
     */
    public double getMeldepunkte(int x) {
        if (x >= meldepunkte.length) {
            return 0;
        }
        return meldepunkte[x];
    }

    /**
     * Setzt die Meldepunkte.
     * 
     * @param punkte
     *            Meldepunkte
     */
    public void setMeldepunkte(int x, double punkte) {
        if (punkte < -0.005) {
            throw new IllegalArgumentException("Points must not be negative (" + getName() + ": " + punkte + ")!");
        }
        if (x >= meldepunkte.length) {
            if (punkte <= 0.005) {
                return;
            }
            double[] m = new double[x + 1];
            System.arraycopy(meldepunkte, 0, m, 0, meldepunkte.length);
            for (int i = meldepunkte.length; i < m.length; i++) {
                m[i] = 0;
            }
            meldepunkte = m;
        }
        meldepunkte[x] = ((double) Math.round(punkte * 100)) / 100;
    }

    /**
     * Liefert die Bemerkung.
     * 
     * @return Bemerkung
     */
    public String getBemerkung() {
        return bemerkung;
    }

    /**
     * Setzt die Bemerkung.
     * 
     * @param b
     *            Bemerkung
     */
    public void setBemerkung(String b) {
        if (b == null) {
            throw new NullPointerException("Comment must not be null!");
        }
        bemerkung = b;
    }

    /**
     * Liefert die HLW-Punkte.
     * 
     * @return HLW-Punkte
     */
    public double getHLWPunkte(int index) {
        initHLW();
        if (!getAK().hasHLW()) {
            return 0;
        }
        if (hlwState[index] != HLWStates.ENTERED) {
            return 0.0;
        }
        return punktehlw[index];
    }

    public double getHLWPunkte() {
        double sum = 0;
        for (int x = 0; x < getMaximaleHLW(); x++) {
            sum += getHLWPunkte(x);
        }
        return sum;
    }

    /**
     * Setzt die HLW-Punkte.
     * 
     * @param sHlw
     *            HLW-Punkte
     */
    public void setHLWPunkte(int index, double sHlw) {
        initHLW();
        if (sHlw < -0.005) {
            throw new IllegalArgumentException("ZW-points must not be negative (" + getName() + " " + index + ": " + sHlw + ")!");
        }
        punktehlw[index] = Math.max(0, sHlw);
        hlwState[index] = HLWStates.ENTERED;
    }

    /**
     * Liefert die Zeit einer Disziplin.
     * 
     * @param disz
     *            Nummer der Disziplin @return Zeit
     */
    public int getZeit(int disz) {
        if (!isDisciplineChosen(disz)) {
            return 0;
        }
        return zeiten[disz];
    }

    public int getZeit(String id) {
        Eingabe e = getEingabe(id, false);
        if (e == null) {
            return 0;
        }
        return e.getZeit();

    }

    public int[] getStarter(int disz) {
        if (!isDisciplineChosen(disz)) {
            return null;
        }
        initStarter();
        return starter[disz];
    }

    public int[] getStarter(String id) {
        Eingabe e = getEingabe(id, false);
        if (e == null) {
            return null;
        }
        return e.getStarter();
    }

    public void setStarter(String id, int[] starter) {
        Eingabe e = getEingabe(id, true);
        if (e == null) {
            return;
        }
        e.setStarter(starter);
    }

    /**
     * Gibt an, ob der Schwimmer an einer Disziplin teilnimmt.
     * 
     * @param disz
     *            Nummber Disziplin @return true, wenn der Schwimmer teilnimmt. false sonst.
     */
    public boolean isDisciplineChosen(int disz) {
        if (disz < 0) {
            return false;
        }
        if (getAK().getDiszAnzahl() <= disz) {
            return false;
        }
        if (!getAK().isDisciplineChoiceAllowed()) {
            return true;
        }
        return disciplineChoice[disz];
    }

    public boolean[] getDisciplineChoice() {
        boolean[] c = new boolean[getAK().getDiszAnzahl()];
        if (!getAK().isDisciplineChoiceAllowed()) {
            for (int x = 0; x < c.length; x++) {
                c[x] = true;
            }
        } else {
            System.arraycopy(disciplineChoice, 0, c, 0, c.length);
        }
        return c;
    }

    /**
     * Liefert eine Liste der ausgewaehlten Disziplinen.
     * 
     * @param sep
     *            Trennzeichen @return Liste der ausgewaehlten Disziplinen
     */
    public String getDisciplineChoiceAsString(char sep) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int x = 0; x < disciplineChoice.length; x++) {
            if (disciplineChoice[x]) {
                if (!first) {
                    sb.append(sep);
                } else {
                    first = false;
                }
                sb.append(getAK().getDisziplin(x, isMaennlich()).getName());
            }
        }
        return sb.toString();
    }

    /**
     * Liefert die Anzahl der ausgewaehlten Disziplinen. Koennen die Disziplinen nicht gewaehlt werden, wird die Anzahl
     * der Disziplinen in der Altersklasse zurueckgeliefert.
     * 
     * @return Anzahl der gewaehlten Disziplinen
     */
    public int getDisciplineChoiceCount() {
        Altersklasse ak = getAK();
        if (!ak.isDisciplineChoiceAllowed()) {
            return ak.getDiszAnzahl();
        }
        int count = 0;
        for (boolean aDisciplineChoice : disciplineChoice) {
            if (aDisciplineChoice) {
                count++;
            }
        }
        return count;
    }

    public boolean isDisciplineChoiceValid() {
        int count = getDisciplineChoiceCount();
        Altersklasse ak = getAK();
        if (!ak.isDisciplineChoiceAllowed()) {
            return true;
        }
        if (ak.getMaximalChosenDisciplines() < count) {
            return false;
        }
        if (ak.getMinimalChosenDisciplines() > count) {
            return false;
        }
        return true;
    }

    /**
     * Setzt die Auswahl fuer eine Disziplin.
     * 
     * @param i
     *            Nummer der Diszplin @param c true, wenn die Disziplin gewaehlt ist. false sonst.
     */
    public void setDisciplineChoice(int i, boolean c) {
        disciplineChoice[i] = c;
        if (!c) {
            setZeit(i, 0);
            setStrafen(i, new LinkedList<Strafe>());
        }
        wk.check();
    }

    /**
     * Setzt die Auswahl der Disziplinen.
     * 
     * @param c
     *            Feld mit der Auswahl der Disziplinen
     */
    public void setDisciplineChoice(boolean[] c) {
        if (c == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        if (disciplineChoice.length != c.length) {
            throw new IllegalArgumentException("Choicearray must have correct size");
        }
        for (int x = 0; x < c.length; x++) {
            disciplineChoice[x] = c[x];
            if (!c[x]) {
                setZeit(x, 0);
                setStrafen(x, new LinkedList<Strafe>());
            }
        }
        wk.check();
    }

    /**
     * Setzt eine Meldezeit.
     * 
     * @param i
     *            Nummer der Disziplin @param c Zeit in Sekunden
     */
    public void setMeldezeit(int index, int zeit) {
        meldezeiten[index] = zeit;
    }

    /**
     * Setzt alle Meldezeiten.
     * 
     * @param c
     *            Feld der Meldezeiten. Die Laenge des Felds muss der Anzahl der Disziplinen in der Altersklasse oder der
     *            Anzahl der ausgewaehlten Disziplinen entsprechen.
     */
    public void setMeldezeiten(int[] c) {
        if (c == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        if (c.length == getDisciplineChoiceCount()) {
            int y = 0;
            for (int aC : c) {
                while (!isDisciplineChosen(y)) {
                    y++;
                }
                setMeldezeit(y, aC);
                y++;
            }
            return;
        }

        if (meldezeiten.length != c.length) {
            throw new IllegalArgumentException("Timearray must have correct size, which is either the " + "number of disciplines in its Agegroup or the "
                    + "number of chosen disciplines (" + c.length + " != " + meldezeiten.length + " and " + getAK().getDiszAnzahl() + ").");
        }
        System.arraycopy(c, 0, meldezeiten, 0, c.length);
    }

    /**
     * Liefert die Meldezeit einer Disziplin.
     * 
     * @param x
     *            Nummer der Disziplin @return Meldezeit in Sekunden
     */
    public int getMeldezeit(int x) {
        if (meldezeiten == null) {
            meldezeiten = new int[zeiten.length];
        }
        return meldezeiten[x];
    }

    /**
     * @param disz
     * @param zeit
     */
    public synchronized void setZeit(int disz, int zeit) {
        if (zeit < 0) {
            throw new IllegalArgumentException("Time must not be negative");
        }

        zeiten[disz] = zeit;
    }

    public synchronized void setStarter(int disz, int[] st) {
        initStarter();
        starter[disz] = st;
    }

    public void setStarter(int[][] c) {
        if (c == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        initStarter();
        if (c.length == getDisciplineChoiceCount()) {
            int y = 0;
            for (int[] aC : c) {
                while (!isDisciplineChosen(y)) {
                    y++;
                }
                setStarter(y, aC);
                y++;
            }
            return;
        }

        if (starter.length != c.length) {
            throw new IllegalArgumentException(
                    "Timearray must have correct size, which is either the " + "number of disciplines in its Agegroup or the number of chosen disciplines ("
                            + c.length + " != " + starter.length + " and " + getAK().getDiszAnzahl() + ").");
        }
        System.arraycopy(c, 0, starter, 0, c.length);
    }

    public LinkedList<Strafe> getStrafen(String id) {
        Eingabe e = getEingabe(id, false);
        if (e == null) {
            return new LinkedList<Strafe>();
        }
        return e.getStrafen();
    }

    public void setStrafen(String id, LinkedList<Strafe> strafen) {
        if (strafen == null) {
            strafen = new LinkedList<Strafe>();
        }
        Eingabe e = getEingabe(id, true);
        e.setStrafen(strafen);
    }

    /**
     * Liefert die Strafe zu einer Disziplin.
     * 
     * @param disz
     *            Nummer der Disziplin @return Strafe
     */
    public LinkedList<Strafe> getStrafen(int disz) {
        if (disz == DISCIPLINE_NUMBER_SELF) {
            return getAllgemeineStrafen();
        }
        if (strafen[disz] == null) {
            return new LinkedList<Strafe>();
        }
        if (!isDisciplineChosen(disz)) {
            return new LinkedList<Strafe>();
        }
        return new LinkedList<Strafe>(strafen[disz]);
    }

    /**
     * Setzt eine Strafe für eine Disziplin
     * 
     * @param disz
     *            Nummer der Disziplin @param s Strafe
     */
    public synchronized boolean setStrafen(int disz, LinkedList<Strafe> s) {
        if (s == null) {
            // throw new NullPointerException("s must not be null.");
            s = new LinkedList<Strafe>();
        }
        LinkedList<Strafe> liste = new LinkedList<Strafe>(s);
        boolean found = false;
        ListIterator<Strafe> li = liste.listIterator();
        while (li.hasNext()) {
            if (li.next().getArt() == Strafarten.NICHT_ANGETRETEN) {
                if (found) {
                    li.remove();
                } else {
                    found = true;
                }
            }
        }

        if (disz == DISCIPLINE_NUMBER_SELF) {
            allgemeineStrafen = liste;
            return true;
        }
        strafen[disz] = liste;
        return true;
    }

    /**
     * Fragt das Geschlecht des Schwimmers ab.
     * 
     * @return true, wenn der Schwimmer maennlich. false sonst.
     */
    public boolean isMaennlich() {
        return maennlich;
    }

    /**
     * Setzt das Geschlecht eines Schwimmers.
     * 
     * @param male
     *            true, wenn der Schwimmer maennlich. false sonst.
     */
    public void setMaennlich(boolean male) {
        maennlich = male;
    }

    /**
     * Fragt ab, ob die HLW-Punkte bereits eingegeben wurden.
     * 
     * @return true, wenn die HLW-Punkte eingegeben wurden. false sonst.
     */
    public boolean hasHLWSet(int index) {
        initHLW();
        if (getAK().hasHLW()) {
            return hlwState[index] != HLWStates.NOT_ENTERED;
        }
        return true;
    }

    public boolean hasHLWSet() {
        for (int x = 0; x < getMaximaleHLW(); x++) {
            if (!hasHLWSet(x)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        clearHlw();

        for (int x = 0; x < zeiten.length; x++) {
            zeiten[x] = 0;
            strafen[x] = new LinkedList<Strafe>();
            allgemeineStrafen = new LinkedList<Strafe>();
            getEingabe("", false);
            eingaben.clear();
        }
    }

    public void clearHlw() {
        if (!getAK().hasHLW()) {
            return;
        }
        for (int x = 0; x < getMaximaleHLW(); x++) {
            setHLWState(x, HLWStates.NOT_ENTERED);
        }
    }

    /**
     * Liefert den Zustand der HLW-Punkte.
     * 
     * @return Zustand der HLW-Punkte. @see de.df.jauswertung.daten.HLWStates
     */
    public HLWStates getHLWState(int index) {
        initHLW();
        if (!getAK().hasHLW()) {
            return HLWStates.NOT_ENTERED;
        }
        return hlwState[index];
    }

    private void initStarter() {
        if (starter == null) {
            starter = new int[getAK().getDiszAnzahl()][getMaxMembers()];
        } else if (starter.length != getAK().getDiszAnzahl()) {
            int[][] copy = new int[getAK().getDiszAnzahl()][getMaxMembers()];
            for (int x = 0; x < Math.min(copy.length, starter.length); x++) {
                copy[x] = starter[x];
            }
            starter = copy;
        }
    }

    private void initHLW() {
        if ((hlwState == null) || (hlwState.length == 0)) {
            hlwState = new HLWStates[getMaximaleHLW()];
            punktehlw = new double[getMaximaleHLW()];
            for (int x = 0; x < hlwState.length; x++) {
                hlwState[x] = HLWStates.NOT_ENTERED;
                punktehlw[x] = 0.0;
            }
        }
    }

    public HLWStates getHLWState() {
        if (!wk.getRegelwerk().getAk(aknummer).hasHLW()) {
            return HLWStates.NOT_ENTERED;
        }
        boolean entered = false;
        boolean na = false;
        boolean disq = false;
        for (int x = 0; x < getMaximaleHLW(); x++) {
            switch (getHLWState(x)) {
            case ENTERED:
                entered = true;
                break;
            case NICHT_ANGETRETEN:
                na = true;
                break;
            case DISQALIFIKATION:
                disq = true;
                break;
            case NOT_ENTERED:
                break;
            }
        }
        if (entered) {
            return HLWStates.ENTERED;
        }
        if (disq) {
            return HLWStates.DISQALIFIKATION;
        }
        if (na) {
            return HLWStates.NICHT_ANGETRETEN;
        }
        return HLWStates.NOT_ENTERED;
    }

    /**
     * setzt den Zustand der Eingabe der HLW-Punkte.
     * 
     * @param x
     *            Zustand
     */
    public void setHLWState(int index, HLWStates x) {
        initHLW();
        hlwState[index] = x;
        switch (x) {
        case NOT_ENTERED:
        case NICHT_ANGETRETEN:
            punktehlw[index] = 0.0;
            break;
        default:
        }
    }

    /**
     * Aktualisiert die Altersklasse des Schwimmers in dem Fall, wenn die Altersklasse geaendert wurde.
     * 
     * @param check
     *            true, wenn zusaetzliche Ueberpruefungen der Lauf- und HLW-Liste durchgefuehrt werden sollen. false
     *            sonst.
     */
    public void updateAK(int nummer, boolean check) {
        setAKNummer(nummer, check);
    }

    /**
     * Setzt die Nummer der Altersklasse.
     * 
     * @param index
     *            Nummer der Altersklasse @param check Gibt an, ob zusaetzliche Ueberpruefungen der Lauf- und HLW-
     *            Liste durchgefuehrt werden sollen.
     */
    @SuppressWarnings("unchecked")
    public void setAKNummer(int index, boolean check) {
        if ((index < 0) || (index >= wk.getRegelwerk().size())) {
            throw new IndexOutOfBoundsException();
        }

        aknummer = index;

        int laenge = -1;
        if (zeiten != null) {
            laenge = zeiten.length;
        }

        int anzahl = getAK().getDiszAnzahl();
        if (laenge != anzahl) {
            boolean choiceDefault = !getAK().isDisciplineChoiceAllowed();
            int[] zeiten2 = new int[anzahl];
            LinkedList<Strafe>[] strafen2 = new LinkedList[anzahl];
            boolean[] choice2 = new boolean[anzahl];
            int[] melde2 = new int[anzahl];
            for (int x = 0; x < anzahl; x++) {
                if (laenge > x) {
                    zeiten2[x] = zeiten[x];
                    strafen2[x] = strafen[x];
                    choice2[x] = disciplineChoice[x];
                    melde2[x] = meldezeiten[x];
                } else {
                    zeiten2[x] = 0;
                    strafen2[x] = new LinkedList<Strafe>();
                    choice2[x] = choiceDefault;
                    melde2[x] = 0;
                }
            }
            zeiten = zeiten2;
            strafen = strafen2;
            disciplineChoice = choice2;
            meldezeiten = melde2;
        }

        if (check) {
            wk.getLaufliste().check(SearchUtils.getSchwimmer(wk, startnummer));
            wk.getHLWListe().check(SearchUtils.getSchwimmer(wk, startnummer));
        }
    }

    /**
     * Gibt an, wie oft der Schwimmer an der HLW teilnehmen muss.
     * 
     * @return Anzahl der HLW-Teilnahmen.
     */
    public abstract int getMaximaleHLW();

    public abstract int getMinMembers();

    public abstract int getMaxMembers();

    @Override
    public int hashCode() {
        return getStartnummer();
    }

    public Startunterlagen getStartunterlagen() {
        return startunterlagen;
    }

    public void setStartunterlagen(Startunterlagen su) {
        startunterlagen = su;
    }

    public boolean hasInput(int dis) {
        if (getZeit(dis) > 0) {
            return true;
        }
        LinkedList<Strafe> ls = getStrafen(dis);
        ListIterator<Strafe> li = ls.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next();
            switch (s.getArt()) {
            case AUSSCHLUSS:
            case DISQUALIFIKATION:
            case NICHT_ANGETRETEN:
                return true;
            default:
                break;
            }
        }
        return false;
    }

    public boolean hasInput(String dis) {
        if (getZeit(dis) > 0) {
            return true;
        }
        LinkedList<Strafe> ls = getStrafen(dis);
        ListIterator<Strafe> li = ls.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next();
            switch (s.getArt()) {
            case AUSSCHLUSS:
            case DISQUALIFIKATION:
            case NICHT_ANGETRETEN:
                return true;
            default:
                break;
            }
        }
        return false;
    }

    public LinkedList<Strafe> getAllgemeineStrafen() {
        if (allgemeineStrafen == null) {
            allgemeineStrafen = new LinkedList<Strafe>();
        }
        return new LinkedList<Strafe>(allgemeineStrafen);
    }

    public void setAllgemeineStrafen(LinkedList<Strafe> s) {
        if (s == null) {
            s = new LinkedList<Strafe>();
        }
        allgemeineStrafen = s;
    }

    public Strafe getAkkumulierteStrafe(int disz) {
        LinkedList<Strafe> ls = null;
        if (disz == DISCIPLINE_NUMBER_SELF) {
            ls = getAllgemeineStrafen();
        } else {
            ls = strafen[disz];
        }
        return akkumuliereStrafen(ls);
    }

    public Strafe getAkkumulierteStrafe(String id) {
        Eingabe e = getEingabe(id, false);
        if (e == null) {
            return Strafe.NICHTS;
        }
        return akkumuliereStrafen(e.getStrafen());
    }

    public static Strafe akkumuliereStrafen(LinkedList<Strafe> ls) {
        if (ls.isEmpty()) {
            return Strafe.NICHTS;
        }

        boolean hascode = false;
        boolean hasnocode = false;

        Strafarten art = Strafarten.NICHTS;
        int punkte = 0;
        String code = "";
        ListIterator<Strafe> li = ls.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next();

            switch (s.getArt()) {
            case AUSSCHLUSS:
                art = Strafarten.AUSSCHLUSS;
                break;
            case DISQUALIFIKATION:
                if (art != Strafarten.AUSSCHLUSS) {
                    art = Strafarten.DISQUALIFIKATION;
                }
                break;
            case NICHT_ANGETRETEN:
                if (art == Strafarten.NICHTS) {
                    art = Strafarten.NICHT_ANGETRETEN;
                }
                break;
            case NICHTS:
                break;
            case STRAFPUNKTE:
                if ((art == Strafarten.NICHTS) || (art == Strafarten.NICHT_ANGETRETEN)) {
                    art = Strafarten.STRAFPUNKTE;
                }
                break;
            }
            punkte += s.getStrafpunkte();

            if (s.getShortname().length() == 0) {
                hasnocode = true;
            } else {
                hascode = true;
            }

            if ((code.length() == 0) || (s.getShortname().length() == 0)) {
                code = code + s.getShortname();
            } else {
                code = code + "," + s.getShortname();
            }
        }

        if (hasnocode && hascode) {
            code += ",X";
        }

        return new Strafe("", code, art, punkte);
    }

    public boolean addStrafe(int disz, Strafe s) {
        LinkedList<Strafe> liste = null;
        if (disz == DISCIPLINE_NUMBER_SELF) {
            liste = allgemeineStrafen;
        } else {
            liste = strafen[disz];
        }
        if (s.getArt() == Strafarten.NICHT_ANGETRETEN) {
            ListIterator<Strafe> li = liste.listIterator();
            while (li.hasNext()) {
                if (li.next().getArt() == Strafarten.NICHT_ANGETRETEN) {
                    return false;
                }
            }
        }
        liste.addLast(s);
        return true;
    }

    public void removeStrafe(int disz, int index) {
        if (disz == DISCIPLINE_NUMBER_SELF) {
            allgemeineStrafen.remove(index);
        } else {
            strafen[disz].remove(index);
        }
    }

    public void removeStrafe(String id, int index) {
        if (id.equals("")) {
            removeStrafe(DISCIPLINE_NUMBER_SELF, index);
        } else {
            Eingabe e = getEingabe(id, false);
            if (e != null) {
                e.removeStrafe(index);
            }
        }
    }

    public String getZeiten() {
        StringBuffer sb = new StringBuffer();
        if (getZeit(0) > 0) {
            sb.append(StringTools.zeitString(getZeit(0)));
        }
        for (int x = 1; x < getAK().getDiszAnzahl(); x++) {
            sb.append(";");
            if (getZeit(x) > 0) {
                sb.append(StringTools.zeitString(getZeit(x)));
            }
        }
        return sb.toString();
    }

    public boolean isAusgeschlossen() {
        if (getAkkumulierteStrafe(DISCIPLINE_NUMBER_SELF).getArt().equals(Strafarten.AUSSCHLUSS)) {
            return true;
        }
        for (int x = 0; x < strafen.length; x++) {
            if (getAkkumulierteStrafe(x).getArt().equals(Strafarten.AUSSCHLUSS)) {
                return true;
            }
        }
        return false;
    }

    public void setQualifikationsebene(String qualifikationsebene) {
        if (qualifikationsebene == null) {
            throw new NullPointerException();
        }
        this.qualifikationsebene = wk.getQualifikationsebene(qualifikationsebene.trim());
    }

    public String getQualifikationsebene() {
        return (qualifikationsebene == null ? "" : qualifikationsebene);
    }

    public void setQualifikation(Qualifikation quali) {
        if (quali == null) {
            throw new NullPointerException();
        }
        this.quali = quali;
    }

    public Qualifikation getQualifikation() {
        if (quali == null) {
            return Qualifikation.OFFEN;
        }
        return quali;
    }

    public void setDopingkontrolle(boolean dopingkontrolle) {
        this.dopingkontrolle = dopingkontrolle;
    }

    public boolean hasDopingkontrolle() {
        return dopingkontrolle;
    }

    public boolean hasMeldungMitProtokoll(int x) {
        if (x >= meldungMitProtokoll.length) {
            return false;
        }
        return meldungMitProtokoll[x];
    }

    public void setMeldungMitProtokoll(int x, boolean meldungMitProtokoll) {
        if (x >= this.meldungMitProtokoll.length) {
            if (meldungMitProtokoll) {
                boolean[] m = new boolean[x + 1];
                System.arraycopy(this.meldungMitProtokoll, 0, m, 0, this.meldungMitProtokoll.length);
                for (int i = this.meldungMitProtokoll.length; i < m.length; i++) {
                    m[i] = false;
                }
                this.meldungMitProtokoll = m;
            } else {
                return;
            }
        }
        this.meldungMitProtokoll[x] = meldungMitProtokoll;
    }

    public int getMeldepunkteSize() {
        if (meldepunkte == null) {
            meldepunkte = new double[1];
        }
        if (meldungMitProtokoll == null) {
            meldungMitProtokoll = new boolean[1];
        }
        return Math.max(meldepunkte.length, meldungMitProtokoll.length);
    }

    void removeEingabe(String id) {
        if (eingaben == null) {
            eingaben = new Hashtable<String, Eingabe>();
        }
        eingaben.remove(id);
    }

    public Eingabe getEingabe(String id) {
        return getEingabe(id, false);
    }

    public Eingabe getEingabe(String id, boolean create) {
        if (eingaben == null) {
            eingaben = new Hashtable<String, Eingabe>();
        }
        Eingabe e = eingaben.getOrDefault(id, null);
        if (e == null && create) {
            e = new Eingabe();
            eingaben.put(id, e);
        }
        return e;
    }

    public void setZeit(String id, int timeAsInt) {
        Eingabe e = getEingabe(id, true);
        if (e == null) {
            e = new Eingabe();
            eingaben.put(id, e);
        }
        e.setZeit(timeAsInt);
    }

    public String[] getDisciplinesOW() {
        getEingabe("", false);
        return ArrayUtils.toArray(eingaben.keys());
    }

    public boolean isFinished(String id) {
        if (getZeit(id) > 0) {
            return true;
        }
        Strafe s = getAkkumulierteStrafe(id);
        switch (s.getArt()) {
        case AUSSCHLUSS:
            return true;
        case DISQUALIFIKATION:
            return true;
        case NICHT_ANGETRETEN:
            return true;
        default:
            return false;
        }
    }

    public void addStrafe(String id, Strafe strafe) {
        if (id.equals("")) {
            addStrafe(DISCIPLINE_NUMBER_SELF, strafe);
        } else {
            Eingabe e = getEingabe(id, true);
            e.addStrafe(strafe);
        }
    }

    public Regelwerk getRegelwerk() {
        return getWettkampf().getRegelwerk();
    }
}