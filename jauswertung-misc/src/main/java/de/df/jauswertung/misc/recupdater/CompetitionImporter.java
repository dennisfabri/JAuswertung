package de.df.jauswertung.misc.recupdater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.misc.times.Time;
import de.df.jauswertung.timesextractor.Competition;
import de.df.jauswertung.timesextractor.Entry;
import de.df.jauswertung.timesextractor.Event;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jauswertung.timesextractor.ValueTypes;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.ZeitenVergleicher;
import de.df.jutils.util.StringTools;

public class CompetitionImporter implements IImporter {

    private final String filename;
    private final String competition;

    public CompetitionImporter(String filename, String competition) {
        this.filename = "src/test/resources/competitions/" + filename;
        this.competition = competition;
    }

    @Override
    public void execute(Records records) {
        System.out.println("Lade Wettkampf " + filename);
        AWettkampf wk = InputManager.ladeWettkampf(filename);

        Competition competition = new TimesExtractor().getZeiten(wk);

        List<Time> times = new ArrayList<>();
        for (Event event : competition.getEvents()) {
            if (event.getValueType() == ValueTypes.TimeInMillis) {
                times.addAll(toTimes(event));
            }
        }

        records.update(times);
    }

    private static String[] merge(String[] first, String[] second) {
        List<String> result = new ArrayList<>();
        Arrays.stream(first).forEach(result::add);
        Arrays.stream(second).forEach(result::add);
        return result.toArray(String[]::new);
    }

    private static String[] males = { "m", "male", "männlich" };
    private static String[] females = { "f", "female", "w", "weiblich" };

    private static String[] sexes = merge(males, females);

    private List<Time> toTimes(Event event) {

        List<Time> result = new ArrayList<>();
        if (isKnownSex(event.getSex())) {
            boolean male = isMale(event.getSex());
            for (Entry entry : event.getTimes()) {
                result.add(new Time(competition, event.isTeam(), entry.getName(), "", entry.getOrganization(),
                        event.getAgegroup(), male, event.getDiscipline(), entry.getValue() / 10, entry.getPenalties()));
            }
        }
        return result;
    }

    private boolean isMale(String sex) {
        return Arrays.stream(males).anyMatch(s -> s.equals(sex.toLowerCase(Locale.ROOT)));
    }

    private boolean isKnownSex(String sex) {
        return Arrays.stream(sexes).anyMatch(s -> s.equals(sex.toLowerCase(Locale.ROOT)));
    }
}
