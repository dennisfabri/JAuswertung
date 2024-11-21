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
import de.df.jauswertung.timesextractor.model.*;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.util.StringTools;
import org.apache.commons.lang3.StringUtils;

import static de.df.jauswertung.daten.PropertyConstants.IS_FINAL;
import static de.df.jauswertung.daten.PropertyConstants.ROUND;

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

    public <T extends ASchwimmer> JAuswertungCompetition getZeiten(AWettkampf<T> wk) {
        if (wk == null) {
            return new JAuswertungCompetition();
        }

        wk = Utils.copy(wk);
        AWettkampf<T>[] competitions = createAllCompetitions(wk);

        JAuswertungCompetition competition = new JAuswertungCompetition(wk.getStringProperty(PropertyConstants.NAME),
                wk.getStringProperty(PropertyConstants.SHORTNAME),
                wk.getStringProperty(PropertyConstants.LENGTH_OF_POOL),
                wk.getStringProperty(PropertyConstants.DATE));
        for (AWettkampf<T> wkx : competitions) {
            for (ASchwimmer s : wkx.getSchwimmer()) {
                extractTimes(s, competition);
            }
        }
        return competition;
    }

    private Collection<? extends JAuswertungEntry> extractTimes(ASchwimmer s, JAuswertungCompetition competition) {
        List<JAuswertungEntry> times = new ArrayList<>();
        JAuswertungValueTypes type = s.getWettkampf().getDataType() == DataType.RANK ? JAuswertungValueTypes.Rank
                : JAuswertungValueTypes.TimeInMillis;

        Altersklasse ak = s.getAK();
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            if (s.isDisciplineChosen(x)) {
                int value = s.getZeit(x);
                if (type == JAuswertungValueTypes.TimeInMillis) {
                    value *= 10;
                }
                JAuswertungPenalty[] penalties = getPenalties(s, x);
                int round = s.getWettkampf().getIntegerProperty(ROUND, 0);
                boolean isFinal = s.getWettkampf().getBooleanProperty(IS_FINAL, true);

                JAuswertungStart start = determineStart(s);
                JAuswertungCompetitorType competitorType = s instanceof Mannschaft ? JAuswertungCompetitorType.Team
                        : JAuswertungCompetitorType.Individual;

                String competitorId = "";
                if (s instanceof Teilnehmer t) {
                    competitorId = t.getCompetitorId();
                }

                competition.addTime(ak.getName(), competitorType, I18n.geschlechtToShortString(s),
                        ak.getDisziplin(x, s.isMaennlich()).getName(), round, isFinal, type,
                        new JAuswertungEntry(competitorId, StartnumberFormatManager.format(s), s.getName(),
                                s.getGliederungMitQGliederung(), s.getNationality(),
                                value, penalties,
                                getSwimmer(s), start));
            }
        }
        return times;
    }

    private <T extends ASchwimmer> JAuswertungStart determineStart(T schwimmer) {
        AWettkampf<T> wk = schwimmer.getWettkampf();
        LinkedList<Lauf<T>> heats = wk.getLaufliste().getLaufliste();
        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();
        int roundId = wk.getIntegerProperty(PropertyConstants.ROUND_ID, -1);
        if (heats == null) {
            // Do Nothing
        } else {
            for (Lauf<T> l : heats) {
                for (int x = 0; x < l.getBahnen(); x++) {
                    T s = l.getSchwimmer(x);
                    if (s == schwimmer) {
                        return new JAuswertungStart(getHeatName(roundId, l, scheme), x + 1);
                    }
                }
            }
        }
        return null;
    }

    private static <T extends ASchwimmer> String getHeatName(int roundId, Lauf<T> lauf, HeatsNumberingScheme scheme) {
        if (roundId > 0) {
            return String.format("%03d-%02d%s", roundId, lauf.getLaufnummer(),
                    StringTools.characterString(lauf.getLaufbuchstabe()));
        }
        return lauf.getName(scheme);
    }

    private JAuswertungPenalty[] getPenalties(ASchwimmer s, int x) {
        List<Strafe> penalties = new ArrayList<>();
        penalties.addAll(s.getStrafen(x));
        penalties.addAll(s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));
        return penalties.stream().map(this::toPenalty).toArray(JAuswertungPenalty[]::new);
    }

    private JAuswertungPenalty toPenalty(Strafe str) {
        JAuswertungPenaltyType type = switch (str.getArt()) {
        case DISQUALIFIKATION -> JAuswertungPenaltyType.Disqualified;
        case NICHT_ANGETRETEN -> JAuswertungPenaltyType.DidNotStart;
        case AUSSCHLUSS -> JAuswertungPenaltyType.Disqualified;
        case NICHTS -> JAuswertungPenaltyType.None;
        case STRAFPUNKTE -> JAuswertungPenaltyType.Points;
        };
        return new JAuswertungPenalty(str.getShortname(), type, str.getStrafpunkte());
    }

    private JAuswertungSwimmer[] getSwimmer(ASchwimmer s) {
        if (s instanceof Teilnehmer t) {
            return new JAuswertungSwimmer[] {
                    new JAuswertungSwimmer(t.getCompetitorId(), StartnumberFormatManager.format(t), t.getVorname(),
                            t.getNachname(), I18n.geschlechtToShortString(t), t.getJahrgang()) };
        }
        if (s instanceof Mannschaft m && m.hasMannschaftsmitglieder()) {
            int amount = m.getMannschaftsmitgliederAnzahl();
            List<JAuswertungSwimmer> swimmer = new ArrayList<>();
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
                swimmer.add(new JAuswertungSwimmer("", "", mitglied.getVorname(),
                        mitglied.getNachname(), gender, mitglied.getJahrgang()));
            }
            return swimmer.toArray(JAuswertungSwimmer[]::new);
        }
        return new JAuswertungSwimmer[0];
    }
}
