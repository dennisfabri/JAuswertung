package de.df.jauswertung.timesextractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.format.StartnumberFormatManager;

public class TimesExtractor {

    @SuppressWarnings("unchecked")
    private <T extends ASchwimmer> AWettkampf<T>[] createAllCompetitions(AWettkampf<T> wk) {
        if (wk.isHeatBased()) {
            if (wk.getLauflisteOW() == null) {
                return new AWettkampf[0];
            }
            Stream<OWSelection> selections = Arrays.stream(wk.getLauflisteOW().getDisziplinen())
                    .map(wk::toOWSelection);
            return selections.map(s -> ResultUtils.createCompetitionFor(wk, s)).toArray(AWettkampf[]::new);
        }
        return new AWettkampf[] { wk };
    }

    public <T extends ASchwimmer> Competition getZeiten(AWettkampf<T> wk) {
        if (wk == null) {
            return new Competition();
        }

        wk = Utils.copy(wk);
        AWettkampf<T>[] competitions = createAllCompetitions(wk);

        Competition competition = new Competition(wk.getStringProperty(PropertyConstants.NAME), wk.getStringProperty(PropertyConstants.SHORTNAME));
        for (AWettkampf<T> wkx : competitions) {
            for (ASchwimmer s : wkx.getSchwimmer()) {
                extractTimes(s, competition);
            }
        }
        return competition;
    }

    private Collection<? extends Entry> extractTimes(ASchwimmer s, Competition competition) {
        List<Entry> times = new ArrayList<>();
        ValueTypes type = s.getWettkampf().getDataType() == DataType.RANK ? ValueTypes.Rank : ValueTypes.TimeInMillis;

        Altersklasse ak = s.getAK();
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            if (s.isDisciplineChosen(x)) {
                int value = s.getZeit(x);
                if (type == ValueTypes.TimeInMillis) {
                    value *= 10;
                }
                Penalty[] penalties = getPenalties(s, x);
                int round = s.getWettkampf().getIntegerProperty("round", 0);
                boolean isFinal = s.getWettkampf().getBooleanProperty("isFinal", true);

                Start start = determineStart(s);
                CompetitorType competitorType = s instanceof Mannschaft ? CompetitorType.Team
                        : CompetitorType.Individual;

                competition.addTime(ak.getName(), competitorType, I18n.geschlechtToShortString(s),
                        ak.getDisziplin(x, s.isMaennlich()).getName(), round, isFinal, type,
                        new Entry(StartnumberFormatManager.format(s), s.getName(), s.getGliederungMitQGliederung(),
                                value, penalties,
                                getSwimmer(s), start));
            }
        }
        return times;
    }

    private <T extends ASchwimmer> Start determineStart(T schwimmer) {
        AWettkampf<T> wk = schwimmer.getWettkampf();
        LinkedList<Lauf<T>> heats = wk.getLaufliste().getLaufliste();
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();
        if (heats == null) {
            // Do Nothing
        } else {
            for (Lauf<T> l : heats) {
                for (int x = 0; x < l.getBahnen(); x++) {
                    T s = l.getSchwimmer(x);
                    if (s == schwimmer) {
                        return new Start(l.getName(scheme), x + 1);
                    }
                }
            }
        }
        return null;
    }

    private Penalty[] getPenalties(ASchwimmer s, int x) {
        List<Strafe> penalties = new ArrayList<>();
        penalties.addAll(s.getStrafen(x));
        penalties.addAll(s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));
        return penalties.stream().map(this::toPenalty).toArray(Penalty[]::new);
    }

    private Penalty toPenalty(Strafe str) {
        PenaltyType type = switch (str.getArt()) {
        case DISQUALIFIKATION -> PenaltyType.Disqualified;
        case NICHT_ANGETRETEN -> PenaltyType.DidNotStart;
        case AUSSCHLUSS -> PenaltyType.Disqualified;
        case NICHTS -> PenaltyType.None;
        case STRAFPUNKTE -> PenaltyType.Points;
        };
        return new Penalty(str.getShortname(), type, str.getStrafpunkte());
    }

    private Swimmer[] getSwimmer(ASchwimmer s) {
        if (s instanceof Teilnehmer t) {
            return new Swimmer[] { new Swimmer(StartnumberFormatManager.format(t), t.getVorname(), t.getNachname(),
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
                String gender;
                switch (mitglied.getGeschlecht()) {
                case maennlich:
                    gender = "m";
                    break;
                case weiblich:
                    gender = "f";
                    break;
                default:
                    gender = "-";
                    break;

                }
                swimmer.add(new Swimmer("", mitglied.getVorname(),
                        mitglied.getNachname(),
                        gender, mitglied.getJahrgang()));
            }
            return swimmer.toArray(new Swimmer[swimmer.size()]);
        }
        return new Swimmer[0];
    }
}
