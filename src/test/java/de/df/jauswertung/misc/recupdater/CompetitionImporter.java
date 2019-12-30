package de.df.jauswertung.misc.recupdater;

import java.util.Collections;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.ZeitenVergleicher;
import de.df.jutils.util.StringTools;

public class CompetitionImporter implements IImporter {

    private final String filename;
    private final String competition;

    public CompetitionImporter(String filename, String competition) {
        this.filename = "../../required/archive/"+filename;
        this.competition = competition;
    }

    @Override
    public void execute(Records records) {
        System.out.println("Lade Wettkampf " + filename);
        AWettkampf wk = InputManager.ladeWettkampf(filename);

        LinkedList<Record> times = new LinkedList<Record>();
        for (int ak = 0; ak < wk.getRegelwerk().size(); ak++) {
            times.addAll(getData(wk, ak));
        }

        records.update(times);
    }

    LinkedList<Record> getData(AWettkampf<ASchwimmer> wk, int index) {
        boolean team = ((AWettkampf) wk) instanceof MannschaftWettkampf;

        int akmin = 0;
        int akmax = wk.getRegelwerk().size();
        if ((index >= 0) && (index < wk.getRegelwerk().size())) {
            akmin = index;
            akmax = index + 1;
        }
        LinkedList<Record> result = new LinkedList<Record>();
        for (int x = akmin; x < akmax; x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            for (int y = 0; y < 2; y++) {
                LinkedList<ASchwimmer> swimmer = SearchUtils.getSchwimmer(wk, ak, y == 1);
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    Collections.sort(swimmer, new ZeitenVergleicher(z));
                    for (ASchwimmer s : swimmer) {
                        Disziplin d = ak.getDisziplin(z, y == 1);
                        if ((true || s.getZeit(z) < d.getRec()) && (s.getZeit(z) > 0) && !s.getAkkumulierteStrafe(z).isStrafe()) {
                            Object[] data = new Object[8];
                            data[0] = s.getName();
                            Strafe str = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                            if (str.isStrafe()) {
                                data[0] = data[0] + " (" + I18n.getPenaltyShort(str) + ")";
                            }
                            data[1] = s.getGliederung();
                            data[2] = s.getQualifikationsebene();
                            data[3] = I18n.getAgeGroupAsString(s);
                            data[4] = StringTools.zeitString(s.getZeit(z));
                            data[5] = PenaltyUtils.getPenaltyMediumText(s.getAkkumulierteStrafe(z), ak);
                            data[6] = d.getName();
                            data[7] = StringTools.zeitString(d.getRec());
                            result.addLast(new Record(competition, s.getAK().getName(), s.isMaennlich(), d.getName(), s.getZeit(z), s.getName(), team));
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

}
