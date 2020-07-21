package de.df.jauswertung.gui.plugins.upload.dto;

import java.util.Collections;
import java.util.List;

public class Individual {
    private final String firstName;
    private final String lastName;
    private final SexIndividual sex;
    private final int yearOfBirth;
    private final String agegroup;
    private final List<Result> results;

    public Individual(String firstName, String lastName, SexIndividual sex, int yearOfBirth, String agegroup,
            List<Result> results) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.yearOfBirth = yearOfBirth;
        this.agegroup = agegroup;
        this.results = Collections.synchronizedList(results);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public SexIndividual getSex() {
        return sex;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public List<Result> getResults() {
        return results;
    }
}
