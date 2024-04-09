package de.df.jauswertung.ares.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AresCompetition {
    private final Map<String, Discipline> disciplineMap = new HashMap<>();
    private final List<Discipline> disciplines = new ArrayList<>();
    private final List<Laenge> lengths = new ArrayList<>();
    private final Map<String, Integer> lengthIds = new HashMap<>();

    public void addDiscipline(Discipline discipline) {
        if (!disciplineMap.containsKey(discipline.name())) {
            disciplineMap.put(discipline.name(), discipline);
            disciplines.add(discipline);
        }
    }

    public Discipline[] getDisciplines() {
        return disciplines.toArray(Discipline[]::new);
    }

    public void calculateLengths() {
        disciplineMap.values().stream().map(discipline -> new Laenge(discipline.length(), discipline.getDistance()))
                     .distinct().sorted().forEach(this::addLength);
    }

    private void addLength(Laenge l) {
        lengthIds.put(l.distance(), lengths.size());
        lengths.add(l);
    }

    public Integer getLengthId(String distance) {
        return lengthIds.get(distance);
    }

    public Integer getDisciplineId(String discipline) {
        int x = 0;
        for (Discipline d : disciplines) {
            if (d.name().equals(discipline)) {
                return x;
            }
            x++;
        }
        for (Discipline discipline1 : disciplines) {
            System.out.println(discipline1.name());
        }
        throw new IllegalArgumentException("Disziplin \"" + discipline + "\" nicht gefunden.");
    }

    public int getLengthIdByDiscipline(String disziplin) {
        Discipline d = disciplineMap.get(disziplin);
        if (d == null) {
            throw new IllegalArgumentException("Disziplin \"" + disziplin + "\" nicht gefunden.");
        }
        return getLengthId(d.getDistance());
    }

    public Laenge[] lengths() {
        return lengths.toArray(Laenge[]::new);
    }
}
