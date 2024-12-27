package de.df.jauswertung.io.portal;

import java.util.ArrayList;
import java.util.List;

import org.lisasp.competition.base.api.type.Gender;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
class RegistrationExportModel {
    private String acronym;
    private String name;
    private String organizer;
    private Nationality nationality;
    private List<Registration> registrations = new ArrayList<>();

    @Data
    static class Registration {
        private String organization;
        private Nationality nationality;
        @JsonProperty("isCompleted")
        private boolean completed;
        private List<Athlete> athletes = new ArrayList<>();
        private List<Team> teams = new ArrayList<>();
    }

    @Data
    abstract static class Participant {
        private String id;
        private Gender gender;
        @JsonInclude(Include.ALWAYS)
        private String ageGroup;
        private Double points;
        private Integer place;
        private String comment;
        private List<Discipline> disciplines = new ArrayList<>();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class Athlete extends Participant {
        private String athleteId;
        private String firstName;
        private String lastName;
        private Integer yearOfBirth;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class Team extends Participant {
        private String id;
        private String name;
        private List<String> memberIds = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    static class Discipline {
        private String name;
        @JsonProperty("isSelected")
        private boolean selected;
        private Integer timeInMilliseconds;

        public Discipline(String name, Integer timeInMilliseconds, boolean selected) {
            this.name = name;
            this.timeInMilliseconds = timeInMilliseconds;
            this.selected = selected;
        }
    }
}
