package de.df.jauswertung.io.portal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.lisasp.competition.base.api.type.Gender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class RegistrationExportModel {
    private String acronym;
    private String name;
    private String organizer;
    private Nationality nationality;
    private List<Registration> registrations = new ArrayList<>();

    @Data
    public static class Registration {
        private String organization;
        private Nationality nationality;
        @JsonProperty("isCompleted")
        private boolean completed;
        private List<Athlete> athletes = new ArrayList<>();
        private List<Team> teams = new ArrayList<>();
        private Optional<Form> form;
    }

    @Data
    public abstract static class Participant {
        private String id;
        private Gender gender;
        @JsonInclude(Include.ALWAYS)
        private String ageGroup;
        private Double points;
        private Integer place;
        private String comment;
        private List<Discipline> disciplines = new ArrayList<>();
        @JsonInclude(Include.NON_NULL)
        private String subOrganization;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Athlete extends Participant {
        private String athleteId;
        private String firstName;
        private String lastName;
        private Integer yearOfBirth;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Team extends Participant {
        private String id;
        private String name;
        private List<String> memberIds = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class Discipline {
        private String name;
        @JsonProperty("isSelected")
        private boolean selected;
        private Integer timeInMilliseconds;
        private String comment;
        @JsonInclude(Include.NON_EMPTY)
        private Map<Integer, String> relayPositions;

        public Discipline(String name, Integer timeInMilliseconds, String comment, boolean selected) {
            this.name = name;
            this.timeInMilliseconds = timeInMilliseconds;
            this.comment = comment;
            this.selected = selected;
        }
    }

    @Value
    public static class Form {

        @NotNull
        private final String id;
        @NotNull
        private final List<FormCategory> categories = new ArrayList<>();

        public Form(String id) {
            this.id = id;
        }

        @JsonCreator
        public Form(@JsonProperty("id") String id, @JsonProperty("categories") List<FormCategory> categories) {
            this(id);
            this.categories.addAll(categories);
        }

        public Form addCategory(FormCategory category) {
            categories.add(category);
            return this;
        }
    }

    @Value
    public static class FormCategory {
        @NotNull
        private final String id;
        @NotNull
        private final String name;
        @Min(0)
        private final int position;
        @NotNull
        private final List<FormEntry> entries = new ArrayList<>();

        public FormCategory(String id, String name, int position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }

        @JsonCreator
        public FormCategory(@JsonProperty("id") String id,
                            @JsonProperty("name") String name,
                            @JsonProperty("position") int position,
                            @JsonProperty("entries") List<FormEntry> entries) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.entries.addAll(entries);
        }

        public FormCategory addEntry(FormEntry entry) {
            entries.add(entry);
            return this;
        }
    }

    @Value
    public static class FormEntry {
        @NotNull
        private final String id;
        @NotNull
        private final String name;
        @NotNull
        private final FormEntryType type;
        private final String value;
        @Min(0)
        private final int position;

        @JsonCreator
        public FormEntry(@JsonProperty("id") String id,
                         @JsonProperty("name") String name,
                         @JsonProperty("type") FormEntryType type,
                         @JsonProperty("value") String value,
                         @JsonProperty("position") int position) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.value = value;
            this.position = position;
        }
    }
}
