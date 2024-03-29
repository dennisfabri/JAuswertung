package de.df.jauswertung.timesextractor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Competition {
    @Getter
    private final String name;
    @Getter
    private final String acronym;

    private final List<Event> events = new ArrayList<>();

    public Competition() {
        name = "";
        acronym = "";
    }

    public void addEvent(Event newEvent) {
        if (events.stream().noneMatch(event -> event.merge(newEvent))) {
            events.add(newEvent);
        }
    }

    public void addTime(String agegroup, CompetitorType competitorType, String sex, String discipline, int round, boolean isFinal,
            ValueTypes type, Entry entry) {
        Event event = new Event(agegroup, competitorType, sex, discipline, round, isFinal, type);
        event.addTime(entry);

        addEvent(event);
    }

    public Event[] getEvents() {
        return events.toArray(Event[]::new);
    }
}
