package de.df.jauswertung.timesextractor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JAuswertungEvent {
    private final String agegroup;
    private final JAuswertungCompetitorType competitorType;
    private final String sex;
    private final String discipline;
    private final int round;
    private final boolean isFinal;
    private final JAuswertungValueTypes valueType;
    private final List<JAuswertungEntry> times = new ArrayList<>();

    public boolean merge(JAuswertungEvent event) {
        if (!isSame(event)) {
            return false;
        }
        assertMergable(event);

        Map<String, JAuswertungEntry> result = new HashMap<>();
        times.forEach(entry -> result.put(entry.getStartnumber(), entry));
        event.times.forEach(entry -> result.put(entry.getStartnumber(), entry));
        times.clear();
        times.addAll(result.values());

        return true;
    }

    private boolean isSame(JAuswertungEvent event) {
        if (!event.agegroup.equals(agegroup)) {
            return false;
        }
        if (!event.sex.equals(sex)) {
            return false;
        }
        if (!event.discipline.equals(discipline)) {
            return false;
        }
        if (event.round != round) {
            return false;
        }
        return true;
    }

    private void assertMergable(JAuswertungEvent event) {
        if (event.isFinal != isFinal) {
            throw new IllegalStateException();
        }
        if (event.valueType != valueType) {
            throw new IllegalStateException();
        }
    }

    public void addTime(JAuswertungEntry entry) {
        times.stream().filter(e -> e.getStartnumber().equals(entry.getStartnumber())).findFirst()
                .ifPresent(times::remove);
        times.add(entry);
    }

    public boolean getIsFinal() {
        return isFinal;
    }
}
