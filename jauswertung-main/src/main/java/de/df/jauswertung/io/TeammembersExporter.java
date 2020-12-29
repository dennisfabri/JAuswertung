package de.df.jauswertung.io;

import java.io.OutputStream;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitgliedermeldung;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.Feedback;

public class TeammembersExporter implements IExporter {

    @Override
    public boolean isSupported(int type) {
        return type == ExportManager.TEAMMEMBERS;
    }

    @Override
    public String getName() {
        return "Meldedatei";
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "wkmm" };
    }

    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk instanceof MannschaftWettkampf) {
            MannschaftWettkampf mwk = Utils.copy((MannschaftWettkampf) wk);
            // Clean up
            mwk.setLogo(null);

            for (Mannschaft t : mwk.getSchwimmer()) {
                t.setBemerkung("");
                for (int x = 0; x < t.getAK().getDiszAnzahl(); x++) {
                    t.setZeit(x, 0);
                    t.setStrafen(x, null);
                    t.setStartunterlagen(Startunterlagen.NICHT_PRUEFEN);
                    t.setDopingkontrolle(false);
                    t.clearHlw();
                    t.setAllgemeineStrafen(null);
                }
            }
            mwk.setKampfrichterverwaltung(null);
            mwk.getLaufliste().resetAll();
            mwk.getHLWListe().resetAll();
            mwk.setZielrichterentscheide(new LinkedList<Zielrichterentscheid<Mannschaft>>());
            Mannschaftsmitgliedermeldung mm = new Mannschaftsmitgliedermeldung(mwk);
            try {
                return OutputManager.speichereObject(name, mm);
                // IOUtils.toXML(mm, name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }
}