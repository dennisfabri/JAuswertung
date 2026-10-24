package de.df.jauswertung.gui.veranstaltung;

import java.nio.file.Path;

import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.FileCore;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.plugin.UpdateEvent;

/**
 * Holds the event that is edited in the Veranstaltungswertung and its file.
 */
public class VeranstaltungCorePlugin extends AFeature implements FileCore {

    private Veranstaltung vs = new Veranstaltung();
    private String filename;

    /**
     * The event that is edited. Panels change it on the EDT and announce the change with {@link #changed}.
     */
    public Veranstaltung getVeranstaltung() {
        return vs;
    }

    public void changed(String title, long reason, IFeature source) {
        getController().sendDataUpdateEvent(title, reason, source);
    }

    void neu() {
        if (getController().acceptWarning()) {
            vs = new Veranstaltung();
            setFilename(null);
            getController().sendDataUpdateEvent("New", UpdateEventConstants.REASON_NEW_WK, this);
        }
    }

    @Override
    public boolean load(String name) {
        Veranstaltung v = Veranstaltungsdatei.lade(name);
        if (v == null) {
            return false;
        }
        vs = v;
        setFilename(name);
        getController().sendDataUpdateEvent("Load",
                UpdateEventConstants.REASON_LOAD_WK | UpdateEventConstants.REASON_FILENAME_CHANGED, this);
        return true;
    }

    @Override
    public boolean save() {
        boolean result = Veranstaltungsdatei.speichere(filename, vs);
        if (result) {
            getController().setChanged(false);
        }
        return result;
    }

    @Override
    public boolean saveAs(String name) {
        if (!Veranstaltungsdatei.speichere(name, vs)) {
            return false;
        }
        setFilename(name);
        getController().sendDataUpdateEvent("SaveAs", UpdateEventConstants.REASON_FILENAME_CHANGED, this);
        return true;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    private void setFilename(String name) {
        filename = name;
        getController().setTitle(name == null ? "" : Path.of(name).getFileName().toString());
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        getController().setChanged(!due.isReason(UpdateEventConstants.REASON_NEW_WK)
                && !due.isReason(UpdateEventConstants.REASON_FILENAME_CHANGED));
    }
}
