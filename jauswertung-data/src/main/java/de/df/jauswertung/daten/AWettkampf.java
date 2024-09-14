/*
 * Wettkampf.java Created on 24. Juli 2001, 10:48
 */
package de.df.jauswertung.daten;

import static de.df.jauswertung.daten.PropertyConstants.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.event.PropertyChangeListener;
import de.df.jauswertung.daten.event.PropertyChangeManager;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelManager;

/**
 * Diese Klasse verwaltet den Wettkampf.
 * 
 * @author Dennis Fabri @version 1.0 @param <T> (Einzel-) Teilnehmer oder
 *         Mannschaft
 */
public abstract class AWettkampf<T extends ASchwimmer> implements Serializable {

    /**
     * Creates new Wettkampf
     * 
     * @param altersklassen
     */
    public AWettkampf(Regelwerk altersklassen, Strafen s) {
        if (altersklassen == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        if (s == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        properties = new Hashtable<>();
        pcManager = new PropertyChangeManager(this);

        strafen = s;

        setProperty(ART_DES_WETTKAMPFS, Wettkampfart.SonstigerWettkampf);
        setProperty(ELEKTRONISCHE_ZEITNAHME, false);
        setProperty(HEATS_LANES, 6);
        setProperty(HEATS_MIXED, true);
        setProperty(HEATS_SORTING_ORDER, Reihenfolge.Regelwerk.getValue());
        setProperty(HEATS_ROTATE, false);
        setProperty(HEATS_NOT_COMPETING_MIXED, true);
        setProperty(HEATS_EMPTY_LIST, false);
        setProperty(HEATS_FIRST_HEAT, 1);

        setProperty(YEAR_OF_COMPETITION, Calendar.getInstance().get(Calendar.YEAR));

        int[] times = new int[altersklassen.size()];
        Arrays.fill(times, 8 * 60);
        setProperty(LAST_CHANGE, new Date());

        filter = new Filter[] { new Filter(I18n.get("Filter.NoFilter"), null) };
        filterindex = 0;

        startnummern = new Startnummern();
        laufliste = new Laufliste<>(this);
        lauflisteow = new OWLaufliste<>(this);
        einsprueche = new LinkedList<>();
        schwimmer = new LinkedList<>();
        hlwliste = new HLWListe<>(this);
        aks = altersklassen;
        kampfrichter = null;
        zielrichterentscheide = new LinkedList<>();
        timelimits = new TimelimitsContainer();

        changedNow();
    }

    private final Regelwerk aks;
    private final Laufliste<T> laufliste;
    private final OWLaufliste<T> lauflisteow;
    private final HLWListe<T> hlwliste;
    private final Strafen strafen;
    private final LinkedList<T> schwimmer;
    @SuppressWarnings("unused")
    private final LinkedList<Einspruch> einsprueche;
    private final Hashtable<String, Object> properties;
    private KampfrichterVerwaltung kampfrichter;
    private LinkedList<Zielrichterentscheid<T>> zielrichterentscheide;

    @Deprecated
    @SuppressWarnings("unused")
    private Ergebnisfreigabe exportfreigabe;
    @Deprecated
    @SuppressWarnings("unused")
    private Ergebnisfreigabe webfreigabe;

    private PropertyChangeManager pcManager;

    private TimelimitsContainer timelimits;

    @SuppressWarnings("unused")
    private Startnummern startnummern;

    private Filter[] filter;
    @XStreamAsAttribute
    private int filterindex;

    private boolean[] disciplinesComplete = new boolean[0];
    private boolean[][][] disciplinesAgeGroupComplete = new boolean[0][0][0];
    private boolean[][] hlwAkComplete = new boolean[0][0];

    public byte[] getLogo() {
        return (byte[]) properties.get(PropertyConstants.LOGO);
    }

    public abstract boolean isEinzel();

    public boolean isMultiline() {
        return !isEinzel() && getBooleanProperty(PropertyConstants.RESULT_MULTILINE, false);
    }

    public void setLogo(byte[] i) {
        if (i == null) {
            setProperty(PropertyConstants.LOGO, null);
            return;
        }
        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // try {
        // ImageIO.write(i, "png", bos);
        // } catch (IOException e) {
        // e.printStackTrace();
        // return;
        // }
        // setProperty(PropertyConstants.LOGO, bos.toByteArray());
        setProperty(PropertyConstants.LOGO, i);
        changedNow(false);
    }

    /**
     * Listener auf Aenderungen der Einstellungen hinzufuegen.
     * 
     * @param pcl Listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcManager.add(pcl);
        changedNow(false);
    }

    /**
     * Kampfrichterverwaltung festlegen.
     * 
     * @param k Kampfrichterverwaltung
     */
    public final void setKampfrichterverwaltung(KampfrichterVerwaltung k) {
        kampfrichter = k;
        changedNow(false);
    }

    /**
     * Kampfrichterverwaltung auslesen.
     * 
     * @return Kampfrichterverwaltung
     */
    public final KampfrichterVerwaltung getKampfrichterverwaltung() {
        return kampfrichter;
    }

    private boolean updatesDisabled = false;
    private boolean changedSinceDisable = false;

    public void disableUpdates() {
        updatesDisabled = true;
    }

    public void enableUpdates() {
        updatesDisabled = false;
        changedNow(changedSinceDisable);
        changedSinceDisable = false;
    }

    /**
     * Liefert die Anzahl der Schwimmer.
     * 
     * @return Anzahl Schwimmer
     */
    public final int getSchwimmeranzahl() {
        if (schwimmer == null) {
            return 0;
        }
        return schwimmer.size();
    }

    /**
     * Liefert den Zeitpunkt der letzten Aenderung.
     * 
     * @return Zeitpunkt der letzten Aenderung
     */
    public final Date getLastChangedDate() {
        return (Date) getProperty(LAST_CHANGE);
    }

    private boolean changing = false;

    private void changedNow(boolean update) {
        if (changing) {
            return;
        }
        try {
            changing = true;
            if (updatesDisabled) {
                changedSinceDisable |= update;
                return;
            }
            startnummern = null;
            setProperty(VERSION, I18n.getVersion());
            setProperty(LAST_CHANGE, new Date());
            if (update) {
                updateInputStatus();
            }
        } finally {
            changing = false;
        }
    }

    /**
     * Wettkampf jetzt als geaendert markieren.
     */
    public final void changedNow() {
        changedNow(true);
    }

    /**
     * Ueberprueft, ob bis zu einer bestimmten Disziplin in einer bestimmten
     * Altersklasse alle Zeiten eingetragen wurden.
     * 
     * @param disz Nummer der Disziplin @param ak Altersklasse @param male
     *             Geschlecht @return true, wenn alle Zeiten eingegeben wurden.
     *             false sonst.
     */
    public final boolean isToDisciplineComplete(int disz, int ak, boolean male) {
        if (disciplinesAgeGroupComplete.length > ak) {
            for (int x = 0; x < disz + 1; x++) {
                if (disciplinesAgeGroupComplete[ak].length > x) {
                    if (!disciplinesAgeGroupComplete[ak][x][male ? 1 : 0]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public final boolean isDisciplineComplete(int disz, int ak, boolean male) {
        return disciplinesAgeGroupComplete[ak][disz][male ? 1 : 0];
    }

    /**
     * Ueberprueft, ob bis zu einer bestimmten Disziplin alle Zeiten eingetragen
     * wurden.
     * 
     * @param disz Nummer der Disziplin @return true, wenn alle Zeiten eingegeben
     *             wurden. false sonst.
     */
    public final boolean isToDisciplineComplete(int disz) {
        for (int x = 0; x < disz + 1; x++) {
            if (!disciplinesComplete[x]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ueberprueft, ob bis zu einer bestimmten Disziplin alle Zeiten eingetragen
     * wurden.
     */
    public final int getToDisciplineComplete() {
        for (int x = 0; x < disciplinesComplete.length; x++) {
            if (!disciplinesComplete[x]) {
                return x - 1;
            }
        }
        return disciplinesComplete.length - 1;
    }

    public final boolean isOneDisciplineComplete(int ak, boolean male) {
        for (int x = 0; x < disciplinesAgeGroupComplete[ak].length; x++) {
            if (disciplinesAgeGroupComplete[ak][x][male ? 1 : 0]) {
                return true;
            }
        }
        return false;
    }

    public final boolean isOneDisciplineComplete(int ak) {
        return isOneDisciplineComplete(ak, true) || isOneDisciplineComplete(ak, false);
    }

    public final boolean isOneDisciplineInOneAgeGroupComplete() {
        for (int x = 0; x < aks.size(); x++) {
            if (isOneDisciplineComplete(x)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ueberprueft, ob bis zu einer bestimmten Disziplin alle Zeiten eingetragen
     * wurden.
     */
    public final int getToDisciplineComplete(int ak, boolean male) {
        for (int x = 0; x < disciplinesAgeGroupComplete[ak].length; x++) {
            if (!disciplinesAgeGroupComplete[ak][x][male ? 1 : 0]) {
                return x;
            }
        }
        return disciplinesAgeGroupComplete[ak].length;
    }

    /**
     * Ueberprueft, ob bis zu einer bestimmten Disziplin alle Zeiten eingetragen
     * wurden.
     */
    public final boolean isDisciplinesComplete() {
        for (boolean aDisciplinesComplete : disciplinesComplete) {
            if (!aDisciplinesComplete) {
                return false;
            }
        }
        return true;
    }

    public final boolean isAgegroupComplete(int ak) {
        for (int y = 0; y < disciplinesAgeGroupComplete[ak].length; y++) {
            for (int z = 0; z < disciplinesAgeGroupComplete[ak][y].length; z++) {
                if (!disciplinesAgeGroupComplete[ak][y][z]) {
                    return false;
                }
            }
        }
        return true;
    }

    public final boolean isAgegroupComplete(int ak, boolean male) {
        for (int y = 0; y < disciplinesAgeGroupComplete[ak].length; y++) {
            if (!disciplinesAgeGroupComplete[ak][y][male ? 1 : 0]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ueberprueft, ob zur HLW in einer Altersklasse alle Punkte eingetragen wurden.
     * 
     * @param ak Altersklasse @param male Geschlecht @return true, wenn alle Punkte
     *           eingegeben wurden. false sonst.
     */
    public final boolean isHlwComplete(int ak, boolean male) {
        return hlwAkComplete[ak][male ? 1 : 0];
    }

    public final boolean isHlwComplete() {
        for (boolean[] aHlwAkComplete : hlwAkComplete) {
            for (int y = 0; y < 2; y++) {
                if (!aHlwAkComplete[y]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ueberprueft, ob alle Zeiten und HLW-Punkte eingetragen wurden.
     * 
     * @return true, wenn alle Zeiten eingegeben wurden. false sonst.
     */
    public final boolean isCompetitionComplete() {
        for (boolean aDisciplinesComplete : disciplinesComplete) {
            if (!aDisciplinesComplete) {
                return false;
            }
        }
        for (boolean[] aHlwAkComplete : hlwAkComplete) {
            if (!aHlwAkComplete[0]) {
                return false;
            }
            if (!aHlwAkComplete[1]) {
                return false;
            }
        }
        return true;
    }

    // 1. Agegroup
    // 2. Sex
    // 3. discipline
    private int[] lastcomplete = null;
    private boolean[][] empty = new boolean[0][0];

    private void updateInputStatus() {
        int amount = aks.size();
        int length = aks.getMaxDisciplineCount();

        if (lastcomplete == null) {
            lastcomplete = new int[] { -1, -1, -1 };
        }
        if (lastcomplete[0] >= aks.size()) {
            lastcomplete[0] = -1;
        }
        if (lastcomplete[0] >= 0) {
            if (lastcomplete[2] >= aks.getAk(lastcomplete[0]).getDiszAnzahl()) {
                lastcomplete[0] = -1;
            }
        }

        empty = new boolean[amount][2];
        for (int x = 0; x < amount; x++) {
            for (int y = 0; y < 2; y++) {
                empty[x][y] = !SearchUtils.hasSchwimmer(this, aks.getAk(x), y == 1);
            }
        }

        boolean[][][] backup = disciplinesAgeGroupComplete;
        if (backup == null) {
            backup = new boolean[amount][aks.getMaxDisciplineCount()][2];
        }
        for (int x = 0; x < Math.min(Math.min(backup.length, disciplinesAgeGroupComplete.length), empty.length); x++) {
            for (int y = 0; y < Math.min(backup[x].length, disciplinesAgeGroupComplete[x].length); y++) {
                for (int z = 0; z < Math.min(backup[x][y].length, empty[x].length); z++) {
                    try {
                        backup[x][y][z] = backup[x][y][z] && !empty[x][z];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Check if all arrays fit
        if (length != disciplinesComplete.length) {
            disciplinesComplete = new boolean[length];
        }
        if (hlwAkComplete.length != amount) {
            hlwAkComplete = new boolean[amount][2];
        }
        disciplinesAgeGroupComplete = new boolean[amount][0][0];
        for (int x = 0; x < amount; x++) {
            disciplinesAgeGroupComplete[x] = new boolean[aks.getAk(x).getDiszAnzahl()][2];
        }
        for (int x = 0; x < amount; x++) {
            if (disciplinesAgeGroupComplete[x].length != aks.getAk(x).getDiszAnzahl()) {
                disciplinesAgeGroupComplete[x] = new boolean[aks.getAk(x).getDiszAnzahl()][2];
            }
        }

        // Initialize fields
        for (int x = 0; x < disciplinesComplete.length; x++) {
            disciplinesComplete[x] = true;
        }
        for (int x = 0; x < disciplinesAgeGroupComplete.length; x++) {
            hlwAkComplete[x][0] = true;
            hlwAkComplete[x][1] = true;
            for (int y = 0; y < disciplinesAgeGroupComplete[x].length; y++) {
                disciplinesAgeGroupComplete[x][y][0] = true;
                disciplinesAgeGroupComplete[x][y][1] = true;
            }
        }

        // Update data
        @SuppressWarnings("unchecked")
        T[] swimmers = (T[]) schwimmer.toArray(new ASchwimmer[schwimmer.size()]);
        for (T s : swimmers) {
            for (int y = 0; y < s.getAK().getDiszAnzahl(); y++) {
                if (!SchwimmerUtils.hasCompleteTime(s, y)) {
                    disciplinesComplete[y] = false;
                    disciplinesAgeGroupComplete[s.getAKNummer()][y][s.isMaennlich() ? 1 : 0] = false;
                }
            }
            if (s.getAK().hasHLW() && !s.hasHLWSet()) {
                hlwAkComplete[s.getAKNummer()][s.isMaennlich() ? 1 : 0] = false;
            }
        }

        if (lastcomplete[0] >= 0) {
            if ((!disciplinesAgeGroupComplete[lastcomplete[0]][lastcomplete[2]][lastcomplete[1]])
                    || (empty[lastcomplete[0]][lastcomplete[1]])) {
                lastcomplete[0] = -1;
            }
        }

        for (int x = 0; x < Math.min(backup.length, disciplinesAgeGroupComplete.length); x++) {
            for (int y = 0; y < Math.min(backup[x].length, disciplinesAgeGroupComplete[x].length); y++) {
                for (int z = 0; z < Math.min(backup[x][y].length, disciplinesAgeGroupComplete[x][y].length); z++) {
                    if ((!backup[x][y][z]) && disciplinesAgeGroupComplete[x][y][z] && !empty[x][z]) {
                        lastcomplete[0] = x;
                        lastcomplete[1] = z;
                        lastcomplete[2] = y;
                    }
                }
            }
        }

        if (lastcomplete[0] < 0) {
            for (int x = 0; x < Math.min(backup.length, disciplinesAgeGroupComplete.length); x++) {
                for (int y = 0; y < Math.min(backup[x].length, disciplinesAgeGroupComplete[x].length); y++) {
                    for (int z = 0; z < Math.min(backup[x][y].length, disciplinesAgeGroupComplete[x][y].length); z++) {
                        if (disciplinesAgeGroupComplete[x][y][z] && !empty[x][z]) {
                            lastcomplete[0] = x;
                            lastcomplete[1] = z;
                            lastcomplete[2] = y;
                        }
                    }
                }
            }
        }
    }

    public int[] getLastComplete() {
        if (lastcomplete == null) {
            updateInputStatus();
        }
        int[] result = new int[lastcomplete.length];
        System.arraycopy(lastcomplete, 0, result, 0, result.length);
        return result;
    }

    /**
     * F\u00fcgt einen neuen Schwimmer hinzu
     * 
     * @param t Enth\u00e4lt den hinzuzuf\u00fcgenden Schwimmer
     */
    public final synchronized boolean addSchwimmer(T t) {
        // if (t.getWettkampf() != this) {
        // return false;
        // }
        t.setWettkampf(this);
        if (contains(t)) {
            return false;
        }
        if ((t.getStartnummer() <= 0) || !isStartnummerFree(t.getStartnummer())) {
            t.setStartnummer(viewNextStartnummer());
        }
        schwimmer.addLast(t);
        t.setGliederung(getGliederung(t.getGliederung()));

        Collections.sort(schwimmer);

        changedNow(true);

        return true;
    }

    private boolean contains(T t) {
        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            if (t == li.next()) {
                return true;
            }
        }
        return false;
    }

    public final synchronized boolean switchStartnummer(T s, int sn) {
        if (sn <= 0) {
            return false;
        }
        if (s == null) {
            return false;
        }
        if (SearchUtils.getSchwimmer(this, s) == null) {
            return false;
        }
        if (!SearchUtils.getSchwimmer(this, s).toString().equals(s.toString())) {
            return false;
        }
        s = SearchUtils.getSchwimmer(this, s);
        if (s.getStartnummer() == sn) {
            return true;
        }
        if (SearchUtils.getSchwimmer(this, sn) != null) {
            return false;
        }
        if (isStartnummerUsed(sn)) {
            return false;
        }

        // int xsn = s.getStartnummer();
        s.setStartnummer(sn);
        // startnummern.recycle(xsn);

        changedNow(false);

        return true;
    }

    public final synchronized void removeSchwimmer(Collection<T> t) {
        if (t == null) {
            return;
        }
        for (T aT : t) {
            removeSchwimmerI(aT);
        }
        changedNow(true);
    }

    public final synchronized void removeSchwimmer(Predicate<T> condition) {
        removeSchwimmer(getSchwimmer().stream().filter(condition).toList());
    }

    public final synchronized boolean removeSchwimmer(T t) {
        boolean b = removeSchwimmerI(t);
        if (b) {
            changedNow(true);
        }
        return b;
    }

    private synchronized boolean removeSchwimmerI(T t) {
        boolean geloescht = false;
        ListIterator<T> li = schwimmer.listIterator();
        while ((!geloescht) && (li.hasNext())) {
            T jetzt = li.next();
            if (t.equals(jetzt)) {
                li.remove();
                geloescht = true;
                // Startnummer zum Wiederverwenden speichern...
                // startnummern.recycle(jetzt.getStartnummer());
            }
        }
        if (geloescht) {
            if (laufliste != null) {
                laufliste.remove(t);
            }
            if (lauflisteow != null) {
                lauflisteow.remove(t);
            }
            if (hlwliste != null) {
                hlwliste.remove(t);
            }
        }
        return geloescht;
    }

    /**
     * Liefert die Altersklassen des Wettkampfes.
     * 
     * @return Liefert die Altersklassen des Wettkampfes.
     */
    public final Regelwerk getRegelwerk() {
        return aks;
    }

    public final synchronized LinkedList<String> getGliederungen() {
        Hashtable<String, String> temp = new Hashtable<>();
        LinkedList<String> result = new LinkedList<>();

        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            String jetzt = li.next().getGliederung();
            if (temp.get(jetzt) == null) {
                result.add(jetzt);
                temp.put(jetzt, jetzt);
            }
        }
        Collections.sort(result);
        return result;
    }

    public final synchronized LinkedList<String> getQualigliederungen() {
        Hashtable<String, String> temp = new Hashtable<>();
        LinkedList<String> result = new LinkedList<>();

        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            String jetzt = li.next().getQualifikationsebene();
            if (jetzt.length() > 0) {
                if (temp.get(jetzt) == null) {
                    result.add(jetzt);
                    temp.put(jetzt, jetzt);
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    public final synchronized LinkedList<String> getGliederungenMitQGliederung() {
        Hashtable<String, String> temp = new Hashtable<>();
        LinkedList<String> result = new LinkedList<>();

        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            T t = li.next();
            String jetzt = t.getGliederung();
            if (t.getQualifikationsebene().length() > 0) {
                jetzt += " (" + t.getQualifikationsebene() + ")";
            }
            if (temp.get(jetzt) == null) {
                result.add(jetzt);
                temp.put(jetzt, jetzt);
            }
        }
        Collections.sort(result);
        return result;
    }

    public final synchronized int getAnzahlGliederungen() {
        Hashtable<String, String> temp = new Hashtable<>();

        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            String jetzt = li.next().getGliederung();
            if (temp.get(jetzt) == null) {
                temp.put(jetzt, jetzt);
            }
        }
        return temp.size();
    }

    /**
     * Liefert die gesuchte Gliederung.
     */
    public final String getGliederung(String vergleicher) {
        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            String jetzt = li.next().getGliederung();
            if (vergleicher.equals(jetzt)) {
                return jetzt;
            }
        }
        return vergleicher;
    }

    /**
     * Liefert die gesuchte Gliederung.
     * 
     */
    final String getQualifikationsebene(String vergleicher) {
        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            String jetzt = li.next().getGliederung();
            if (vergleicher.equals(jetzt)) {
                return jetzt;
            }
        }
        return vergleicher;
    }

    public final Laufliste<T> getLaufliste() {
        return laufliste;
    }

    public final OWLaufliste<T> getLauflisteOW() {
        return lauflisteow;
    }

    public final HLWListe<T> getHLWListe() {
        return hlwliste;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public final void setProperty(String name, Object value) {
        // It wouldn't be wrong if these two if-clauses
        // were missing. But the listeners would be notified
        // though nothing has been changed.
        if ((getProperty(name) == null) && (value == null)) {
            return;
        }
        if (value instanceof String text) {
            value = text.trim();
        }
        if ((value != null) && (value.equals(getProperty(name)))) {
            return;
        }
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
        pcManager.firePropertyChangeEvent(name);
        changedNow(false);
    }

    public final String getStringProperty(String name) {
        return getStringProperty(name, "");
    }

    public final String getStringProperty(String name, String fallback) {
        Object o = getProperty(name);
        if (o == null) {
            return fallback;
        }
        return o.toString();
    }

    public final boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    public final boolean getBooleanProperty(String name, boolean fallback) {
        Object o = getProperty(name);
        if (o == null) {
            return fallback;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return fallback;
    }

    public final int getIntegerProperty(String name) {
        return getIntegerProperty(name, 0);
    }

    public final int getIntegerProperty(String name, int fallback) {
        Object o = getProperty(name);
        if ((o == null) || (!(o instanceof Number))) {
            return fallback;
        }
        return ((Number) o).intValue();
    }

    public final double getDoubleProperty(String name) {
        return getDoubleProperty(name, 0);
    }

    private double getDoubleProperty(String name, double fallback) {
        Object o = getProperty(name);
        if ((o == null) || (!(o instanceof Number))) {
            return fallback;
        }
        return ((Number) o).doubleValue();
    }

    public final Strafen getStrafen() {
        return strafen;
    }

    /**
     * Liefert alle Schwimmer des Wettkampfes
     * 
     * @return Liefert alle Schwimmer des Wettkampfes
     */
    public final synchronized LinkedList<T> getSchwimmer() {
        return new LinkedList<>(schwimmer);
    }

    public final synchronized boolean hasSchwimmer() {
        return !schwimmer.isEmpty();
    }

    public final synchronized boolean hasHLW() {
        ListIterator<T> iterator = schwimmer.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getAK().hasHLW()) {
                return true;
            }
        }
        return false;
    }

    private int[] getUsedStartnummern() {
        int[] sns = new int[schwimmer.size()];
        ListIterator<T> li = schwimmer.listIterator();
        int x = 0;
        while (li.hasNext()) {
            sns[x] = li.next().getStartnummer();
            x++;
        }
        Arrays.sort(sns);
        return sns;
    }

    private boolean isStartnummerUsed(int sn) {
        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            T t = li.next();
            if (t.getStartnummer() == sn) {
                return true;
            }
        }
        return false;
    }

    public final boolean isStartnummerFree(int sn) {
        return !isStartnummerUsed(sn);
    }

    public final int viewNextStartnummer() {
        int[] sns = getUsedStartnummern();
        int sn = 1;
        while (true) {
            for (int x = 0; x < sns.length; x++) {
                if (sns[x] == sn) {
                    sn++;
                    continue;
                }
            }
            break;
        }
        return sn; // startnummern.viewNext();
    }

    public final boolean hasLaufliste() {
        if (isHeatBased()) {
            if (getLauflisteOW().isEmpty()) {
                return false;
            }
            for (OWDisziplin<T> d : getLauflisteOW().getDisziplinen()) {
                if (!d.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
        return (getLaufliste() != null) && (getLaufliste().getLaufliste() != null)
                && (!(getLaufliste().getLaufliste().isEmpty()));
    }

    public final void check() {
        if (getHLWListe() != null) {
            getHLWListe().check();
        }
        if (getLaufliste() != null) {
            getLaufliste().check(getLauflisteOW() != null && !getLauflisteOW().isEmpty());
        }
        checkZielrichterentscheide();
    }

    public Filter[] getFilter() {
        return Arrays.copyOf(filter, filter.length);
    }

    public void setFilter(Filter[] filter) {
        if ((filter == null) || (filter.length == 0)) {
            filter = new Filter[] { new Filter(I18n.get("Filter.NoFilter"), null) };
            filterindex = 0;
        } else {
            if (!filter[0].getName().equals(I18n.get("Filter.NoFilter"))) {
                filter[0] = new Filter(I18n.get("Filter.NoFilter"), null);
            }
        }
        this.filter = filter;
        changedNow(false);
    }

    public void setCurrentFilterIndex(int x) {
        if ((x < 0) || (x >= filter.length)) {
            throw new IndexOutOfBoundsException();
        }
        filterindex = x;
        changedNow(false);
    }

    public int getCurrentFilterIndex() {
        if (filterindex >= filter.length) {
            filterindex = filter.length - 1;
        }
        return filterindex;
    }

    public Filter getCurrentFilter() {
        int index = getCurrentFilterIndex();
        if (index == 0) {
            if (!filter[0].getName().equals(I18n.get("Filter.NoFilter"))) {
                filter[0] = new Filter(I18n.get("Filter.NoFilter"), null);
            }
        }
        return filter[getCurrentFilterIndex()];
    }

    private void checkZielrichterentscheide() {
        if (zielrichterentscheide == null) {
            zielrichterentscheide = new LinkedList<>();
            return;
        }

        ListIterator<Zielrichterentscheid<T>> li = zielrichterentscheide.listIterator();
        while (li.hasNext()) {
            Zielrichterentscheid<T> ze = li.next();
            ze.check();
            if (ze.getSchwimmer().isEmpty()) {
                li.remove();
            }
        }
    }

    public LinkedList<Zielrichterentscheid<T>> getZielrichterentscheide() {
        checkZielrichterentscheide();
        return new LinkedList<>(zielrichterentscheide);
    }

    public void setZielrichterentscheide(LinkedList<Zielrichterentscheid<T>> ze) {
        if (ze == null) {
            throw new NullPointerException("List must not be null");
        }
        zielrichterentscheide = new LinkedList<>(ze);
    }

    public boolean isFilterActive() {
        Filter f = getCurrentFilter();
        if (f.getGliederungen() == null) {
            return false;
        }
        String[] names = f.getGliederungen();
        LinkedList<String> glds = getGliederungen();
        ListIterator<String> li = glds.listIterator();
        while (li.hasNext()) {
            String s = li.next();
            for (String name : names) {
                if (s.equals(name)) {
                    li.remove();
                    break;
                }
            }
        }
        LinkedList<T> swimmer = SearchUtils.getSchwimmer(this, glds.toArray(new String[glds.size()]), true);
        return !swimmer.isEmpty();
    }

    // TODO: Update implementation SG
    public void reorderDisciplines(int[][] verteilung) {
        Startgruppe[] sgs = getRegelwerk().getEffektiveStartgruppen();

        int[] sgindizes = new int[aks.size()];

        // Regelwerk anpassen
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            Startgruppe sg = aks.getStartgruppe(ak);
            sgindizes[x] = 0;
            for (int i = 0; i < sgs.length; i++) {
                if (sgs[i].equals(sg)) {
                    sgindizes[x] = i;
                    break;
                }
            }
            for (int y = 0; y < 2; y++) {
                Disziplin[] disziplinen = ak.getDisziplinen(y == 1);
                for (int z = 0; z < disziplinen.length; z++) {
                    ak.setDisziplin(disziplinen[verteilung[sgindizes[x]][z]], z, y == 1);
                }
            }
        }

        // Schwimmer anpassen
        ListIterator<T> li = schwimmer.listIterator();
        while (li.hasNext()) {
            T t = li.next();
            int anzahl = t.getAK().getDiszAnzahl();
            int[] zeiten = new int[anzahl];
            int[][] starter = new int[anzahl][0];
            int[] meldezeiten = new int[anzahl];
            boolean[] choice = new boolean[anzahl];
            @SuppressWarnings("unchecked")
            LinkedList<Strafe>[] penalties = new LinkedList[anzahl];
            for (int x = 0; x < zeiten.length; x++) {
                choice[x] = t.isDisciplineChosen(x);
                zeiten[x] = t.getZeit(x);
                starter[x] = t.getStarter(x);
                meldezeiten[x] = t.getMeldezeit(x);
                penalties[x] = t.getStrafen(x);
            }
            for (int x = 0; x < zeiten.length; x++) {
                int index = verteilung[sgindizes[t.getAKNummer()]][x];
                t.setDisciplineChoice(x, choice[index]);
                t.setZeit(x, zeiten[index]);
                t.setStarter(x, starter[index]);
                t.setStrafen(x, penalties[index]);
                t.setMeldezeit(x, meldezeiten[index]);
            }
        }

        getLaufliste().reorderAgegroupsByStartgroups(verteilung);

        changedNow();
    }

    private void setIntArray2(String id, int[][] limits) {
        int[][] newlimits = new int[aks.size()][2];
        if (limits == null) {
            limits = new int[0][2];
        }
        for (int x = 0; x < newlimits.length; x++) {
            for (int y = 0; y < 2; y++) {
                if (x < limits.length) {
                    newlimits[x][y] = limits[x][y];
                } else {
                    newlimits[x][y] = 0;
                }
            }
        }
        setProperty(id, newlimits);
    }

    private int[][] getIntArray2(String id) {
        Object o = getProperty(id);
        int[][] limits = null;
        if (o != null) {
            if (o instanceof int[][]) {
                limits = (int[][]) o;
            } else {
                if (o instanceof int[]) {
                    int[] tmp = (int[]) o;
                    limits = new int[tmp.length][2];
                    for (int x = 0; x < tmp.length; x++) {
                        for (int y = 0; y < 2; y++) {
                            limits[x][y] = tmp[x];
                        }
                    }
                }
            }
        }
        int[][] newlimits = new int[aks.size()][2];
        if (limits == null) {
            limits = new int[0][2];
        }
        for (int x = 0; x < newlimits.length; x++) {
            for (int y = 0; y < 2; y++) {
                if (x < limits.length) {
                    newlimits[x][y] = limits[x][y];
                } else {
                    newlimits[x][y] = 0;
                }
            }
        }
        return newlimits;
    }

    public int[][] getZulassungslimits() {
        return getIntArray2("ZULASSUNGSLIMITS");
    }

    public void setZulassungslimits(int[][] limits) {
        setIntArray2("ZULASSUNGSLIMITS", limits);
    }

    public int[][] getDirektqualifizierte() {
        return getIntArray2("DIREKTQUALIFIZIERTE");
    }

    public int[][] getNachruecker() {
        return getIntArray2("NACHRUECKER");
    }

    public void setDirektqualifizierte(int[][] limits) {
        setIntArray2("DIREKTQUALIFIZIERTE", limits);
    }

    public void setNachruecker(int[][] limits) {
        setIntArray2("NACHRUECKER", limits);
    }

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
        migrator1(dom.getRootElement());
    }

    public static void migrator1(Element node) {
        Regelwerk.migrator1(node.element("aks"));
    }

    @SuppressWarnings("unused")
    private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
        migrator2(dom.getRootElement());
    }

    public static void migrator2(Element node) {
        Regelwerk.migrator2(node.element("aks"));
    }

    @SuppressWarnings("unused")
    private void migrate3(VersionedDocument dom, Stack<Integer> versions) {
        migrator3(dom.getRootElement());
    }

    public static void migrator3(Element node) {
        Laufliste.migrator3(node.element("laufliste"));
    }

    public boolean HasOpenQualifications() {
        for (T t : schwimmer) {
            if (t.getQualifikation() == Qualifikation.OFFEN) {
                return true;
            }
        }
        return false;
    }

    public void removeDiscipline(String id) {
        getLauflisteOW().removeDiscipline(id);
        for (T t : schwimmer) {
            t.removeEingabe(id);
        }
    }

    public boolean isFinal(OWSelection t) {
        Altersklasse ak = getRegelwerk().getAk(t.akNummer);
        Disziplin d = ak.getDisziplin(t.discipline, t.male);
        return d.getRunden().length <= t.round;
    }

    public boolean isFinal(OWDisziplin<T> t) {
        return getRegelwerk().getAk(t.akNummer).isFinal(t.disziplin, t.maennlich, t.round);
    }

    public OWSelection toOWSelection(OWDisziplin<T> disziplin) {
        return new OWSelection(getRegelwerk().getAk(disziplin.akNummer), disziplin.akNummer, disziplin.maennlich,
                disziplin.disziplin, disziplin.round);
    }

    public boolean isDLRGBased() {
        return FormelManager.isDLRGBased(getRegelwerk().getFormelID());
    }

    public boolean isHeatBased() {
        return FormelManager.isHeatBased(getRegelwerk().getFormelID());
    }

    public boolean isOpenWater() {
        return FormelManager.isOpenwater(getRegelwerk().getFormelID());
    }

    public DataType getDataType() {
        return FormelManager.getDataType(getRegelwerk().getFormelID());
    }

    @SuppressWarnings("rawtypes")
    public void copyPropertiesTo(AWettkampf target) {

        for (String key : properties.keySet()) {
            try {
                target.setProperty(key, Utils.copy(getProperty(key)));
            } catch (Exception ex) {
                target.setProperty(key, getProperty(key));
            }
        }
    }

    public TimelimitsContainer getTimelimits() {
        if (timelimits == null) {
            timelimits = new TimelimitsContainer();
        }
        return timelimits;
    }

    public int getMaximaleAnzahlBahnen() {
        int max = getIntegerProperty(PropertyConstants.HEATS_LANES);
        if (getLauflisteOW() != null) {
            max = getLauflisteOW().getMaximaleAnzahlBahnen();
        }
        return max;
    }

    public HeatsNumberingScheme getHeatsNumberingScheme() {
        return HeatsNumberingScheme.fromString(
                getStringProperty(PropertyConstants.HEATS_NUMBERING_SCHEME, HeatsNumberingScheme.Standard.getValue()));
    }
}