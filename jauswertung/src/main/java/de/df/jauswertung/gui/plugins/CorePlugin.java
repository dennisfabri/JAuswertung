/*
 * Created on 14.05.2004
 */
package de.df.jauswertung.gui.plugins;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Mannschaftsmitgliedermeldung;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.plugin.io.FileLock;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 14.05.2004
 */
public class CorePlugin extends AFeature {

    public enum Filemode {
        Competition, Teammembers
    }

    @SuppressWarnings("rawtypes")
    private AWettkampf wk;
    private String     name;
    private Filemode   mode = Filemode.Competition;

    public CorePlugin() {
        if (Utils.getPreferences().getBoolean("SingleCompetition", false)) {
            wk = new EinzelWettkampf(AltersklassenUtils.getDefaultAKs(true), InputManager.ladeStrafen(null, true));
        } else {
            wk = new MannschaftWettkampf(AltersklassenUtils.getDefaultAKs(false), InputManager.ladeStrafen(null, false));
        }
    }

    public void setMode(Filemode mode) {
        if (mode != this.mode) {
            this.mode = mode;
            switch (mode) {
            case Teammembers:
                wk = new MannschaftWettkampf(AltersklassenUtils.getDefaultAKs(false), InputManager.ladeStrafen(null, false));
                break;
            case Competition:
                if (Utils.getPreferences().getBoolean("SingleCompetition", false)) {
                    wk = new EinzelWettkampf(AltersklassenUtils.getDefaultAKs(true), InputManager.ladeStrafen(null, true));
                } else {
                    wk = new MannschaftWettkampf(AltersklassenUtils.getDefaultAKs(false), InputManager.ladeStrafen(null, false));
                }
                break;
            }
        }
    }

    public Date getLastChangedDate() {
        return wk.getLastChangedDate();
    }

    @SuppressWarnings("rawtypes")
    public void setWettkampf(AWettkampf w, boolean neu) {
        setWettkampf(w, neu, "NewCompetition");
    }

    @SuppressWarnings("rawtypes")
    public void setWettkampf(AWettkampf w, boolean neu, String reason) {
        if (w == null) {
            throw new NullPointerException();
        }
        if (wk != w) {
            wk = w;
            if (neu) {
                unlock();
                setFilename(null);
                getController().sendDataUpdateEvent(reason, UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_FILENAME_CHANGED
                        | UpdateEventConstants.REASON_FILTER_SELECTION | UpdateEventConstants.REASON_FILTERS_CHANGED, this);
            } else {
                getController().sendDataUpdateEvent(reason,
                        UpdateEventConstants.REASON_LOAD_WK | UpdateEventConstants.REASON_FILTER_SELECTION | UpdateEventConstants.REASON_FILTERS_CHANGED, this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
        return wk;
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends ASchwimmer> AWettkampf<T> getFilteredWettkampf() {
        return CompetitionUtils.getFilteredInstance(wk);
    }

    public EinzelWettkampf getEinzelWettkampf() {
        if (wk instanceof EinzelWettkampf) {
            return (EinzelWettkampf) wk;
        }
        return null;
    }

    public MannschaftWettkampf getMannschaftWettkampf() {
        if (wk instanceof EinzelWettkampf) {
            return null;
        }
        return (MannschaftWettkampf) wk;
    }

    public Teilnehmer addTeilnehmer(String nachname, String vorname, int jahrgang, String gliederung, String qualiebene, boolean maennlich, int ak,
            String bemerkung, int sn, double punkte, boolean[] selection, int[] meldezeiten, boolean ausserkonkurrenz) {
        EinzelWettkampf ewk = getEinzelWettkampf();
        String g = ewk.getGliederung(gliederung);
        Teilnehmer tn = ewk.createTeilnehmer(nachname, vorname, jahrgang, maennlich, g, ak, bemerkung);
        tn.setMeldepunkte(0, punkte);
        if (selection != null) {
            tn.setDisciplineChoice(selection);
        }
        if (meldezeiten != null) {
            tn.setMeldezeiten(meldezeiten);
        }
        tn.setStartnummer(sn);
        tn.setAusserKonkurrenz(ausserkonkurrenz);
        tn.setQualifikationsebene(qualiebene);
        ewk.addSchwimmer(tn);
        getController().sendDataUpdateEvent("NewPerson", UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED, tn, null, this);
        return tn;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#getStrafen()
     */
    public Strafen getStrafen() {
        return wk.getStrafen();
    }

    public Mannschaft addMannschaft(String mannschaftsname, String gliederung, String qualiebene, boolean maennlich, int ak, String bemerkung, int sn,
            double punkte, boolean[] selection, int[] meldezeiten, boolean ausserkonkurrenz, Mannschaftsmitglied[] members) {
        Mannschaft m = addMannschaftI(mannschaftsname, gliederung, qualiebene, maennlich, ak, bemerkung, sn, punkte, selection, meldezeiten, ausserkonkurrenz,
                members);
        getController().sendDataUpdateEvent(
                new UpdateEvent("NewTeam", UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED, m, null, this));
        return m;
    }

    public void addMannschaften(String mannschaftsname, int amount, String gliederung, String qualiebene, boolean maennlich, int ak, String bemerkung, int sn,
            double punkte, boolean[] selection, int[] meldezeiten, boolean ausserkonkurrenz) {
        for (int x = 1; x <= amount; x++) {
            addMannschaftI(mannschaftsname + " " + x, gliederung, qualiebene, maennlich, ak, bemerkung, sn, punkte, selection, meldezeiten, ausserkonkurrenz,
                    null);
        }
        getController().sendDataUpdateEvent("NewTeam", UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED, this);
    }

    public Mannschaft addMannschaftI(String mannschaftsname, String gliederung, String qualiebene, boolean maennlich, int ak, String bemerkung, int sn,
            double punkte, boolean[] selection, int[] meldezeiten, boolean ausserkonkurrenz, Mannschaftsmitglied[] members) {
        MannschaftWettkampf mwk = getMannschaftWettkampf();
        String gl = mwk.getGliederung(gliederung);
        Mannschaft mannschaft = mwk.createMannschaft(mannschaftsname, maennlich, gl, ak, bemerkung);
        mannschaft.setMeldepunkte(0, punkte);
        if (selection != null) {
            mannschaft.setDisciplineChoice(selection);
        }
        if (meldezeiten != null) {
            mannschaft.setMeldezeiten(meldezeiten);
        }
        mannschaft.setQualifikationsebene(qualiebene);
        mannschaft.setStartnummer(sn);
        mannschaft.setAusserKonkurrenz(ausserkonkurrenz);
        if (members != null) {
            for (int i = 0; i < Math.min(mannschaft.getMaxMembers(), members.length); i++) {
                members[i].copyTo(mannschaft.getMannschaftsmitglied(i));
            }
            for (int i = members.length; i < mannschaft.getMaxMembers(); i++) {
                mannschaft.getMannschaftsmitglied(i).clear();
            }
        }

        mwk.addSchwimmer(mannschaft);
        return mannschaft;
    }

    public void add(LinkedList<? extends ASchwimmer> schwimmer) {
        boolean found = false;
        ListIterator<? extends ASchwimmer> li = schwimmer.listIterator();
        while (li.hasNext()) {
            @SuppressWarnings("unchecked")
            boolean b = wk.addSchwimmer(li.next());
            found = found || b;
        }
        if (found) {
            getController().sendDataUpdateEvent("Import", UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED,
                    schwimmer.toArray(), null, this);
        }
    }

    public boolean remove(ASchwimmer[] s) {
        if (s.length == 1) {
            return remove(s[0]);
        }
        StringBuilder text = new StringBuilder();
        if (s.length <= 10) {
            text.append("\n");
            for (ASchwimmer value : s) {
                text.append("\n");
                text.append(value);
            }
            text.append("\n");
        }
        if (DialogUtils.askAndWarn(getController().getWindow(), I18n.get("ConfirmationRequired"), I18n.get("ReallyDeleteMultiple", s.length),
                text.toString())) {
            boolean result = false;
            for (ASchwimmer value : s) {
                if (removeI(value)) {
                    result = true;
                }
            }
            if (result) {
                if (s[0] instanceof Mannschaft) {
                    getController().sendDataUpdateEvent(new UpdateEvent("RemoveTeam", UpdateEventConstants.REASON_SWIMMER_DELETED, s, null, this));
                } else {
                    getController().sendDataUpdateEvent(new UpdateEvent("RemovePerson", UpdateEventConstants.REASON_SWIMMER_DELETED, s, null, this));
                }
            }
            return result;
        }
        return false;
    }

    public boolean remove(ASchwimmer s) {
        if (s instanceof Teilnehmer) {
            return remove((Teilnehmer) s);
        }
        if (s instanceof Mannschaft) {
            return remove((Mannschaft) s);
        }
        return false;
    }

    private boolean removeI(ASchwimmer s) {
        if (s instanceof Teilnehmer) {
            return removeI((Teilnehmer) s);
        }
        if (s instanceof Mannschaft) {
            return removeI((Mannschaft) s);
        }
        return false;
    }

    public boolean remove(Teilnehmer t) {
        if (DialogUtils.askAndWarn(getController().getWindow(), I18n.get("ConfirmationRequired"), I18n.get("ReallyDelete", t.getName()),
                I18n.get("ReallyDelete.Note", t.getName()))) {
            boolean result = removeI(t);
            if (result) {
                getController().sendDataUpdateEvent("Remove", UpdateEventConstants.REASON_SWIMMER_DELETED, t, null, this);
            }
            return result;
        }
        return false;
    }

    /**
     * @param t
     * @return
     */
    private boolean removeI(Teilnehmer t) {
        return getEinzelWettkampf().removeSchwimmer(t);
    }

    public boolean remove(Mannschaft m) {
        if (DialogUtils.askAndWarn(getController().getWindow(), I18n.get("ConfirmationRequired"), I18n.get("ReallyDelete", m.getName()),
                I18n.get("ReallyDelete.Note", m.getName()))) {
            boolean result = removeI(m);
            if (result) {
                UpdateEvent due = new UpdateEvent("Remove", UpdateEventConstants.REASON_SWIMMER_DELETED, m, null, this);
                getController().sendDataUpdateEvent(due);
            }
            return result;
        }
        return false;
    }

    /**
     * @param m
     * @return
     */
    private boolean removeI(Mannschaft m) {
        return getMannschaftWettkampf().removeSchwimmer(m);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#load(java.lang.String)
     */
    @SuppressWarnings("rawtypes")
    public boolean load(String filename) {
        if (filename == null) {
            return false;
        }
        if (filename.equals(getFilename())) {
            return true;
        }

        FileLock f = getLock(filename);
        if (f == null) {
            return false;
        }
        AWettkampf w = null;
        switch (mode) {
        case Competition:
            w = InputManager.ladeWettkampf(filename);
            break;
        case Teammembers:
            Mannschaftsmitgliedermeldung mm = (Mannschaftsmitgliedermeldung) InputManager.ladeObject(filename);
            if (mm != null) {
                w = mm.getWettkampf();
            }
            break;
        }
        if (w == null) {
            return false;
        }
        setLock(f);
        wk = w;
        setFilename(filename);
        getController().sendDataUpdateEvent("Load", UpdateEventConstants.REASON_LOAD_WK | UpdateEventConstants.REASON_FILENAME_CHANGED
                | UpdateEventConstants.REASON_FILTER_SELECTION | UpdateEventConstants.REASON_FILTERS_CHANGED, this);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#save()
     */
    public boolean save() {
        boolean result = false;
        switch (mode) {
        case Competition:
            result = OutputManager.speichereWettkampf(name, wk);
            break;
        case Teammembers:
            Mannschaftsmitgliedermeldung mmm = new Mannschaftsmitgliedermeldung((MannschaftWettkampf) wk);
            result = OutputManager.speichereObject(name, mmm);
            break;
        }
        if (result) {
            getController().setChanged(false);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#saveAs(java.lang.String)
     */
    public boolean saveCopyAs(String filename) {
        FileLock f = getLock(filename);
        if (f == null) {
            return false;
        }
        boolean result = false;
        switch (mode) {
        case Competition:
            result = OutputManager.speichereWettkampf(filename, wk);
            break;
        case Teammembers:
            Mannschaftsmitgliedermeldung mmm = new Mannschaftsmitgliedermeldung((MannschaftWettkampf) wk);
            result = OutputManager.speichereObject(filename, mmm);
            break;
        }
        f.unlock();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#saveAs(java.lang.String)
     */
    public boolean saveAs(String filename) {
        if ((filename != null) && (filename.equals(name))) {
            return save();
        }
        FileLock f = getLock(filename);
        if (f == null) {
            return false;
        }
        boolean result = false;
        switch (mode) {
        case Competition:
            result = OutputManager.speichereWettkampf(filename, wk);
            break;
        case Teammembers:
            Mannschaftsmitgliedermeldung mmm = new Mannschaftsmitgliedermeldung((MannschaftWettkampf) wk);
            result = OutputManager.speichereObject(filename, mmm);
            break;
        }
        if (result) {
            setLock(f);
            setFilename(filename);
            getController().sendDataUpdateEvent("SaveAs", UpdateEventConstants.REASON_FILENAME_CHANGED, this);
            getController().setChanged(false);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#getFilename()
     */
    public String getFilename() {
        return name;
    }

    private FileLock lock = null;

    private synchronized void unlock() {
        if (lock != null) {
            lock.unlock();
            lock = null;
        }
    }

    private synchronized void setLock(FileLock lock) {
        unlock();
        this.lock = lock;
    }

    private static synchronized FileLock getLock(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            filename = StringTools.md5(filename);
            FileLock lock = new FileLock(filename);
            if (lock.lock()) {
                return lock;
            }
        }
        return null;
    }

    private void setFilename(String filename) {
        if (filename != null) {
            switch (mode) {
            case Competition:
                if (!filename.toLowerCase().endsWith(".wk")) {
                    throw new IllegalArgumentException("File has to end with .wk but name is " + filename + "!");
                }
                break;
            case Teammembers:
                if (!filename.toLowerCase().endsWith(".wkmm")) {
                    throw new IllegalArgumentException("File has to end with .wkmm but name is " + filename + "!");
                }
                break;
            }
        }
        name = filename;

        String title = (name == null ? "" : name);
        if (title.indexOf(File.separator) >= 0) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }
        getController().setTitle(title);
    }

    private void checkAks() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                checkAKsInternal();
            }
        };
        new Thread(run).start();
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        checkAks();
        wk.changedNow();
        if ((due.getChangeReason() & UpdateEventConstants.REASON_NEW_WK) > 0) {
            setFilename(null);
            getController().setChanged(false);
        } else {
            getController().setChanged(true);
        }
        if ((due.getChangeReason() & UpdateEventConstants.REASON_FILENAME_CHANGED) > 0) {
            getController().setChanged(false);
        }
    }

    void checkAKsInternal() {
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl() - 1; y++) {
                for (int z = y + 1; z < ak.getDiszAnzahl(); z++) {
                    for (int a = 0; a < 2; a++) {
                        if (ak.getDisziplin(y, a == 1) == ak.getDisziplin(z, a == 1)) {
                            throw new IllegalStateException("Disciplines must not include same objects.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void shutDown() {
        unlock();
    }
}