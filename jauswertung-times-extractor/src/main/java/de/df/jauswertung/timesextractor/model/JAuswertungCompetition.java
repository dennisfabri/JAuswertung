package de.df.jauswertung.timesextractor.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JAuswertungCompetition {
    @Getter
    private final String name;
    @Getter
    private final String acronym;
    @Getter
    private final String lengthOfCourse;
    @Getter
    private final String date;

    private final List<JAuswertungEvent> events = new ArrayList<>();

    public JAuswertungCompetition() {
        name = "";
        acronym = "";
        lengthOfCourse = "";
        date = "";
    }

    public void addEvent(JAuswertungEvent newEvent) {
        if (events.stream().noneMatch(event -> event.merge(newEvent))) {
            events.add(newEvent);
        }
    }

    public void addTime(String agegroup, JAuswertungCompetitorType competitorType, String sex, String discipline,
            int round, boolean isFinal,
            JAuswertungValueTypes type, JAuswertungEntry entry) {
        JAuswertungEvent event = new JAuswertungEvent(agegroup, competitorType, sex, discipline, round, isFinal, type);
        event.addTime(entry);

        addEvent(event);
    }

    public JAuswertungEvent[] getEvents() {
        return events.toArray(JAuswertungEvent[]::new);
    }
}
