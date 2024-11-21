package de.df.jauswertung.misc.recupdater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.misc.times.Time;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetition;
import de.df.jauswertung.timesextractor.model.JAuswertungCompetitorType;
import de.df.jauswertung.timesextractor.model.JAuswertungEntry;
import de.df.jauswertung.timesextractor.model.JAuswertungEvent;
import de.df.jauswertung.timesextractor.TimesExtractor;
import de.df.jauswertung.timesextractor.model.JAuswertungValueTypes;

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

        JAuswertungCompetition competition = new TimesExtractor().getZeiten(wk);

        List<Time> times = new ArrayList<>();
        for (JAuswertungEvent event : competition.getEvents()) {
            if (event.getValueType() == JAuswertungValueTypes.TimeInMillis) {
                times.addAll(toTimes(event));
            }
        }

        records.update(times);
    }

    private static String[] merge(String[] first, String[] second) {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(first));
        result.addAll(Arrays.asList(second));
        return result.toArray(String[]::new);
    }

    private static String[] males = { "m", "male", "männlich" };
    private static String[] females = { "f", "female", "w", "weiblich" };

    private static String[] sexes = merge(males, females);

    private List<Time> toTimes(JAuswertungEvent event) {
        boolean isTeam = event.getCompetitorType().equals(JAuswertungCompetitorType.Team);
        
        List<Time> result = new ArrayList<>();
        if (isKnownSex(event.getSex())) {
            boolean male = isMale(event.getSex());
            for (JAuswertungEntry entry : event.getTimes()) {
                result.add(new Time(competition, isTeam, entry.getName(), "", entry.getOrganization(),
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
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.competition, this.filename);
    }
}
