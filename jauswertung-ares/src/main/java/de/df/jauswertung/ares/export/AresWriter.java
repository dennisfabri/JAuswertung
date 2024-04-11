package de.df.jauswertung.ares.export;

import static de.df.jauswertung.daten.laufliste.HeatsNumberingScheme.fromString;
import static java.util.Arrays.stream;

import java.io.IOException;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.*;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.FormelILSFinals;
import de.df.jauswertung.util.ergebnis.FormelManager;

public class AresWriter {

    @SuppressWarnings("unchecked")
    public <T extends ASchwimmer> void write(String[] filenames, String dir) throws IOException {
        if (filenames == null || filenames.length == 0) {
            throw new IOException(I18n.get("NoFilesSelected"));
        }
        AWettkampf<T>[] wks = new AWettkampf[filenames.length];
        for (int x = 0; x < filenames.length; x++) {
            String filename = filenames[x];
            wks[x] = InputManager.ladeWettkampf(filename);
            if (wks[x] == null) {
                throw new IOException(I18n.get("CouldNotOpenFile", filename));
            }
            wks[x] = mapCompetition(wks[x]);
        }
        write(wks, dir);
    }

    private <T extends ASchwimmer> AWettkampf<T> mapCompetition(AWettkampf<T> wk) {
        boolean hasHundrets = fromString(wk.getStringProperty(PropertyConstants.HEATS_NUMBERING_SCHEME,
                HeatsNumberingScheme.Standard.getValue())) == HeatsNumberingScheme.Hundreds;
        if (!hasHundrets) {
            return wk;
        }

        AWettkampf<T> newWk = Utils.copy(wk);
        newWk.setProperty(PropertyConstants.HEATS_NUMBERING_SCHEME, HeatsNumberingScheme.Standard.getValue());

        newWk.getLaufliste().clear();
        newWk.getSchwimmer().forEach(schwimmer -> schwimmer.setAKNummer(0, true));
        newWk.getRegelwerk().setSize(1);
        newWk.getRegelwerk().setFormelID(FormelILSFinals.ID);
        stream(newWk.getRegelwerk().getAks()).forEach(ak -> {
            ak.setLaufsortierung(Reihenfolge.ILSPool.getValue());
            ak.setWertungsgruppe(null);
            ak.setStartgruppe(null);
        });
        newWk.getRegelwerk().setStartgruppen(new Startgruppe[0]);
        newWk.getRegelwerk().setWertungsgruppen(new Wertungsgruppe[0]);

        for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
            int roundId = lauf.getLaufnummer() / 100;
            int heatId = lauf.getLaufnummer() % 100;
            T schwimmer = lauf.getSchwimmer();
            int disciplineNumber = lauf.getDisznummer(3);

            OWLaufliste<T> laufliste = newWk.getLauflisteOW();

            if (laufliste.getDisziplin(0, schwimmer.isMaennlich(), lauf.getDisznummer(3), 0) == null) {
                laufliste.addDisziplin(0, schwimmer.isMaennlich(), disciplineNumber, 0);
                newWk.getRegelwerk().getAk(0).getDisziplin(lauf.getDisziplin(), schwimmer.isMaennlich())
                        .setRunden(new int[] { 8 }, new int[] { roundId, roundId + 100 });
            }

            OWDisziplin<T> disziplin = laufliste.getDisziplin(0, schwimmer.isMaennlich(),
                    lauf.getDisznummer(3), 0);
            newWk.getSchwimmer().stream().filter(
                    s -> s.isDisciplineChosen(disciplineNumber) && s.isMaennlich() == schwimmer.isMaennlich())
                    .forEach(disziplin::addSchwimmer);
            Lauf<T> owLauf = new Lauf<>(lauf.getBahnen(), heatId, 0, lauf.getBenutzbareBahnen());
            for (int x = 0; x < lauf.getBahnen(); x++) {
                T s = lauf.getSchwimmer(x);
                if (s != null) {
                    owLauf.setSchwimmer(SearchUtils.getSchwimmer(newWk, s.getStartnummer()), 0, x);
                }
            }
            disziplin.laeufe.add(new OWLauf<>(newWk, disziplin.Id, owLauf));
        }

        return newWk;
    }

    private <T extends ASchwimmer> void write(AWettkampf<T>[] wks, String dir) throws IOException {
        boolean hasFinals = FormelManager.isHeatBased(wks[0].getRegelwerk().getFormelID());
        for (int x = 1; x < wks.length; x++) {
            if (hasFinals != FormelManager.isHeatBased(wks[x].getRegelwerk().getFormelID())) {
                throw new IOException(I18n.get("Competition finals has mismatching formulas"));
            }
        }
        if (hasFinals) {
            AresWriterFinals.writeAres(wks, dir);
        } else {
            AresWriterDefault.writeAres(wks, dir);
        }
    }

}
