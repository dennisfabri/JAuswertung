package de.df.jauswertung.util.times;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.DataType;

public class TimesExtractor {

    @SuppressWarnings("unchecked")
    private <T extends ASchwimmer> AWettkampf<T>[] createAllCompetitions(AWettkampf<T> wk) {
        if (wk.getDataType() == DataType.RANK) {
            return new AWettkampf[0];
        }
        if (wk.isHeatBased()) {
            // Todo: Implement
            return new AWettkampf[0];
        }
        return new AWettkampf[] { wk };
    }

    public <T extends ASchwimmer> Time[] getZeiten(AWettkampf<T> wk) {
        if (wk == null) {
            return new Time[0];
        }

        wk = Utils.copy(wk);
        AWettkampf<T>[] competitions = createAllCompetitions(wk);

        LinkedList<T> sw = wk.getSchwimmer();
        for (T s : sw) {
            if (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
                wk.removeSchwimmer(s);
            }
        }

        List<Time> times = new ArrayList<>();

        for (AWettkampf<T> wkx : competitions) {
            for (ASchwimmer s : wkx.getSchwimmer()) {
                times.addAll(extractTimes(s));
            }
        }

        return times.stream().filter(t -> t.getTimeInMillis() > 0).toArray(Time[]::new);
    }

    private Collection<? extends Time> extractTimes(ASchwimmer s) {
        List<Time> times = new ArrayList<>();
        if (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
            return times;
        }

        Altersklasse ak = s.getAK();
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            if (s.isDisciplineChosen(x)) {
                times.add(new Time(ak.getDisziplin(x, s.isMaennlich()).getName(), ak.getName(),
                        I18n.geschlechtToShortString(s), s.getZeit(x) * 10, s.getAkkumulierteStrafe(x).toString(),
                        getSwimmer(s)));
            }
        }
        return times;
    }

    private Swimmer[] getSwimmer(ASchwimmer s) {
        if (s instanceof Teilnehmer t) {
            return new Swimmer[] { new Swimmer(t.getVorname(), t.getNachname(), t.getGliederungMitQGliederung(),
                    I18n.geschlechtToShortString(t), t.getJahrgang()) };
        }
        if (s instanceof Mannschaft m && m.hasMannschaftsmitglieder()) {
            int amount = m.getMannschaftsmitgliederAnzahl();
            List<Swimmer> swimmer = new ArrayList<>();
            for (int x = 0; x < amount; x++) {
                Mannschaftsmitglied mitglied = m.getMannschaftsmitglied(x);
                if (mitglied.isEmpty()) {
                    continue;
                }
                String sex;
                switch (mitglied.getGeschlecht()) {
                case maennlich:
                    sex = "m";
                    break;
                case weiblich:
                    sex = "w";
                    break;
                default:
                    sex = "x";
                    break;

                }
                swimmer.add(new Swimmer(mitglied.getVorname(), mitglied.getNachname(), m.getGliederungMitQGliederung(),
                        sex, mitglied.getJahrgang()));
            }
            return swimmer.toArray(new Swimmer[swimmer.size()]);
        }
        return new Swimmer[0];
    }
}
