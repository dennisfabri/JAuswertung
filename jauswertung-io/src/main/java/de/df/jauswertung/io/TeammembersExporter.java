package de.df.jauswertung.io;

import java.io.OutputStream;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitgliedermeldung;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.Feedback;

public class TeammembersExporter extends EmptyExporter {

    @Override
    public boolean isSupported(ImportExportTypes type) {
        return type == ImportExportTypes.TEAM_MEMBERS;
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
            mwk.setZielrichterentscheide(new LinkedList<>());
            Mannschaftsmitgliedermeldung mm = new Mannschaftsmitgliedermeldung(mwk);
            try {
                return OutputManager.speichereObject(name, mm);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}