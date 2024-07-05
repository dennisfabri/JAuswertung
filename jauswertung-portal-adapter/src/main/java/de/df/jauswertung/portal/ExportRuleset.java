package de.df.jauswertung.portal;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import org.lisasp.competition.base.api.type.Gender;
import org.lisasp.competition.registration.domain.ruleset.Ruleset;
import org.lisasp.competition.registration.domain.ruleset.agegroup.*;
import org.lisasp.competition.registration.domain.ruleset.agegroup.discipline.Discipline;
import org.lisasp.competition.registration.domain.ruleset.agegroup.discipline.RatingType;
import org.lisasp.competition.registration.domain.ruleset.templates.RulesetTemplate;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.InputManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExportRuleset {
    RulesetTemplate toRuleSetTemplate(String filename) {
        Regelwerk rwk = InputManager.ladeAKs(filename);

        Ruleset ruleset = new Ruleset();

        for (int i = 0; i < rwk.getAks().length; i++) {
            Altersklasse ak = rwk.getAks()[i];
            ruleset.getAgeGroups()
                    .add(new AgeGroup(ak.getName(),
                            typeFromName(filename),
                            getAgeLimits(ak),
                            getDisciplineSelectionConfiguration(ak),
                            getTeamMemberConfiguration(ak),
                            ak.getMaxMembers(),
                            getDisciplines(rwk, ak.getDisziplinen(), typeFromName(filename)),
                            getAdmissionConditionType(ak)));
        }

        RulesetTemplate template = new RulesetTemplate(rwk.getBeschreibung(), ruleset);

        return template;
    }

    private AdmissionConditionType getAdmissionConditionType(Altersklasse ak) {
        return AdmissionConditionType.ALL;
    }

    private DisciplineSelectionConfiguration getDisciplineSelectionConfiguration(Altersklasse ak) {
        return new DisciplineSelectionConfiguration(ak.getMinimalChosenDisciplines(), ak.getMaximalChosenDisciplines());
    }

    private Collection<Discipline> getDisciplines(Regelwerk rwk, Disziplin[][] disziplinen, AgeGroupType type) {
        List<Discipline> disciplines = new ArrayList<>();
        int index = 0;
        for (Disziplin[] disziplin : disziplinen) {
            disciplines.add(new Discipline(toGender(rwk, 0), index, disziplin[0].getName(), RatingType.TIME,
                    type.equals(AgeGroupType.SINGLE) ? null : 4));
            index++;
        }
        return disciplines;
    }

    private Gender toGender(Regelwerk rwk, int genderIndex) {
        String key = genderIndex == 0 ? "femaleShort" : "maleShort";
        String value = rwk.getTranslation(key, "m");
        return switch (value) {
        case "m" -> Gender.Male;
        case "w", "f" -> Gender.Female;
        case "x" -> Gender.Mixed;
        default -> Gender.Male;
        };
    }

    private TeamMemberConfiguration getTeamMemberConfiguration(Altersklasse ak) {
        return new TeamMemberConfiguration(false, ak.getMinMembers(), ak.getMaxMembers());
    }

    private AgeLimitConfiguration getAgeLimits(Altersklasse ak) {
        return AgeLimitConfiguration.singleConfiguration(ak.getMinimumAlter(), ak.getMaximumAlter());
    }

    private static AgeGroupType typeFromName(String filename) {
        if (filename.endsWith(".rwe")) {
            return AgeGroupType.SINGLE;
        }
        if (filename.endsWith(".rwm")) {
            return AgeGroupType.TEAM;
        }
        throw new RuntimeException("Invalid extension for file: " + filename);
    }
}
