package de.df.jauswertung.io.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.*;
import de.df.jauswertung.io.portal.RegistrationExportModel.Discipline;
import de.df.jauswertung.io.portal.RegistrationExportModel.Participant;
import de.df.jauswertung.io.portal.RegistrationExportModel.Registration;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.valueobjects.Teammember;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.formula.functions.T;
import org.lisasp.competition.base.api.type.Gender;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalImporter implements IImporter {

    private final ObjectMapper mapper = new ObjectMapper();

    public PortalImporter() {
        mapper.findAndRegisterModules();
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream input, AWettkampf<T> wk, Feedback fb, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        LinkedList<T> data = new LinkedList<>();
        RegistrationExportModel model = mapper.readValue(input, RegistrationExportModel.class);
        if (wk instanceof MannschaftWettkampf mwk) {
            return (LinkedList<T>) registrationTeam(model, mwk, fb);
        }
        if (wk instanceof EinzelWettkampf ewk) {
            return (LinkedList<T>) registrationIndividual(model, ewk, fb);
        }
        return data;
    }

    private LinkedList<Mannschaft> registrationTeam(RegistrationExportModel model, MannschaftWettkampf wk, Feedback fb) {
        fb.showFeedback("Importiere Mannschaften");
        LinkedList<Mannschaft> teams = new LinkedList<>();
        for (Registration registration : model.getRegistrations()) {
            for (RegistrationExportModel.Team team : registration.getTeams()) {
                if (team.getAgeGroup() == null || team.getAgeGroup().isBlank()) {
                    continue;
                }
                int akNummer = findAkNummer(wk, team.getAgeGroup());
                if (akNummer < 0) {
                    fb.showFeedback("Altersklasse " + team.getAgeGroup() + " für " + team.getName() + " (" + team.getGender() + ") nicht gefunden");
                    continue;
                }
                Boolean isMale = findGender(wk, team.getGender());
                if (isMale == null) {
                    fb.showFeedback("Geschlecht " + team.getGender() + " für " + team.getName() + " (" + team.getAgeGroup() + ") nicht gefunden");
                    continue;
                }
                Mannschaft m = wk.createMannschaft(team.getName(), isMale, fixGliederung(registration.getOrganization()), akNummer, team.getComment());
                if (team.getSubOrganization() != null && !team.getSubOrganization().isBlank()) {
                    m.setQualifikationsebene(fixGliederung(m.getGliederung()));
                    m.setGliederung(team.getSubOrganization());
                }
                importRegistrationEntry(team, m);
                if (team.getMemberIds().size() > m.getMaxMembers()) {
                    fb.showFeedback(
                            "Mannschaft '%s' (%s %s) hat %d Mitglieder, aber nur %d sind erlaubt. Es werden nur die ersten %d importiert. Für einen vollständigen Import muss das Regelwerk entsprechend angepasst werden.".formatted(
                                    team.getName(),
                                    team.getAgeGroup(),
                                    I18n.get(team.getGender().toString().toLowerCase(Locale.ROOT)),
                                    team.getMemberIds().size(),
                                    m.getMaxMembers(),
                                    m.getMaxMembers()));
                }
                for (int x = 0; x < Math.min(team.getMemberIds().size(), m.getMaxMembers()); x++) {
                    importMannschaftsmitglied(m.getMannschaftsmitglied(x), registration, team.getMemberIds().get(x));
                }
                if (notAlreadyImported(wk, team)) {
                    teams.add(m);
                }
            }
        }
        fb.showFeedback("Import abgeschlossen");
        return teams;
    }

    private Boolean findGender(AWettkampf<?> wk, Gender gender) {
        Regelwerk rw = wk.getRegelwerk();
        String trueValue = I18n.geschlechtToShortString(rw, true);
        String falseValue = I18n.geschlechtToShortString(rw, false);
        return switch (gender) {
            case Male -> {
                if (trueValue.equalsIgnoreCase("m")) {
                    yield Boolean.TRUE;
                } else if (falseValue.equalsIgnoreCase("m")) {
                    yield Boolean.FALSE;
                } else {
                    yield null;
                }
            }
            case Female -> {
                if (trueValue.equalsIgnoreCase("w") || trueValue.equalsIgnoreCase("f")) {
                    yield Boolean.TRUE;
                } else if (falseValue.equalsIgnoreCase("w") || falseValue.equalsIgnoreCase("f")) {
                    yield Boolean.FALSE;
                } else {
                    yield null;
                }
            }
            case Mixed -> {
                if (trueValue.equalsIgnoreCase("x")) {
                    yield Boolean.TRUE;
                } else if (falseValue.equalsIgnoreCase("x")) {
                    yield Boolean.FALSE;
                } else {
                    yield null;
                }
            }
            case Unknown -> null;
        };
    }

    private Geschlecht findGeschlecht(AWettkampf<?> wk, Gender gender) {
        Boolean isMale = findGender(wk, gender);
        if (isMale == null) {
            return Geschlecht.unbekannt;
        }
        return isMale ? Geschlecht.maennlich : Geschlecht.weiblich;
    }

    private void importRegistrationEntry(Participant source, ASchwimmer destination) {
        destination.setImportId(source.getId());
        destination.setMeldepunkte(0, source.getPoints() == null ? 0 : source.getPoints());
        for (int x = 0; x < destination.getAK().getDiszAnzahl(); x++) {
            destination.setMeldePlatz(source.getPlace() == null ? 0 : source.getPlace());
            destination.setMeldezeit(x, 0);
            destination.setDisciplineChoice(x, false);
        }
        for (Discipline discipline : source.getDisciplines().stream().filter(Discipline::isSelected).toList()) {
            for (int x = 0; x < destination.getAK().getDiszAnzahl(); x++) {
                Disziplin d = destination.getAK().getDisziplin(x, destination.isMaennlich());
                int timeInHundreds = discipline.getTimeInMilliseconds() == null ? 0 : discipline.getTimeInMilliseconds() / 10;
                if (d.getName().equalsIgnoreCase(discipline.getName())) {
                    destination.setDisciplineChoice(x, true);
                    destination.setMeldezeit(x, timeInHundreds);
                }
            }
        }
    }

    private void importMannschaftsmitglied(Mannschaftsmitglied mitglied, RegistrationExportModel.Registration registration, String id) {
        Optional<RegistrationExportModel.Athlete> maybeAthlete = registration.getAthletes().stream().filter(a -> a.getId().equals(id)).findFirst();
        if (maybeAthlete.isEmpty()) {
            mitglied.setVorname("");
            mitglied.setNachname("");
            mitglied.setGeschlecht(Geschlecht.weiblich);
            mitglied.setJahrgang(0);
            mitglied.setImportId("");
        } else {
            RegistrationExportModel.Athlete athlete = maybeAthlete.get();
            mitglied.setVorname(athlete.getFirstName());
            mitglied.setNachname(athlete.getLastName());
            mitglied.setGeschlecht(athlete.getGender() == Gender.Male ? Geschlecht.maennlich : Geschlecht.weiblich);
            mitglied.setJahrgang(athlete.getYearOfBirth() == null ? 0 : athlete.getYearOfBirth());
            mitglied.setImportId(id);
        }
    }

    private <T extends ASchwimmer> int findAkNummer(AWettkampf<T> wk, String name) {
        for (int i = 0; i < wk.getRegelwerk().size(); i++) {
            if (matchesAkName(wk.getRegelwerk().getAk(i).getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    private boolean matchesAkName(String akName, String name) {
        return akName.equalsIgnoreCase(name);
    }

    private LinkedList<Teilnehmer> registrationIndividual(RegistrationExportModel model, EinzelWettkampf wk, Feedback fb) {
        fb.showFeedback("Importiere Teilnehmer");
        LinkedList<Teilnehmer> teilnehmerListe = new LinkedList<>();
        for (Registration registration : model.getRegistrations()) {
            for (RegistrationExportModel.Athlete athlete : registration.getAthletes()) {
                if (athlete.getAgeGroup() == null || athlete.getAgeGroup().isBlank()) {
                    continue;
                }
                int akNummer = findAkNummer(wk, athlete.getAgeGroup());
                if (akNummer < 0) {
                    fb.showFeedback("Altersklasse " + athlete.getAgeGroup() + " für " + athlete.getFirstName() + " " + athlete.getLastName() + " " + athlete.getGender() + " nicht gefunden");
                    continue;
                }
                Boolean isMale = findGender(wk, athlete.getGender());
                if (isMale == null) {
                    fb.showFeedback("Geschlecht " + athlete.getGender() + " für " + athlete.getFirstName() + " " + athlete.getLastName() + " (" + athlete.getAgeGroup() + ") nicht gefunden");
                    continue;
                }
                Teilnehmer t = wk.createTeilnehmer(athlete.getAthleteId(),
                                                   athlete.getLastName(),
                                                   athlete.getFirstName(),
                                                   athlete.getYearOfBirth() == null ? 0 : athlete.getYearOfBirth(),
                                                   athlete.getGender() == Gender.Male,
                                                   fixGliederung(registration.getOrganization()),
                                                   akNummer,
                                                   athlete.getComment());
                if (athlete.getSubOrganization() != null && !athlete.getSubOrganization().isBlank()) {
                    t.setQualifikationsebene(fixGliederung(t.getGliederung()));
                    t.setGliederung(athlete.getSubOrganization());
                }
                importRegistrationEntry(athlete, t);
                if (notAlreadyImported(wk, athlete)) {
                    teilnehmerListe.add(t);
                }
            }
        }
        fb.showFeedback("Import abgeschlossen");
        return teilnehmerListe;
    }

    private static <T extends ASchwimmer> boolean notAlreadyImported(AWettkampf<T> wk, RegistrationExportModel.Participant participant) {
        String importId = participant.getId();
        if (importId == null || importId.isBlank()) {
            return true;
        }
        return wk.getSchwimmer().stream().noneMatch(s -> hasImportId(s, importId));
    }

    private static boolean hasImportId(ASchwimmer schwimmer, String importId) {
        if (importId == null || importId.isBlank()) {
            return false;
        }
        return schwimmer.getImportId() != null && schwimmer.getImportId().equals(importId);
    }

    private static final List<String> ORGANIZATION_LEVEL = Stream.of("Bundesverband",
                                                                     "DLRG",
                                                                     "BV",
                                                                     "Landesverband",
                                                                     "LV",
                                                                     "Bezirk",
                                                                     "BZ",
                                                                     "Bez",
                                                                     "Ortsgruppe",
                                                                     "Ortsverband",
                                                                     "Kreisgruppe",
                                                                     "Kreisverband",
                                                                     "Gruppe",
                                                                     "Kreis",
                                                                     "Ortsverein",
                                                                     "Stützpunkt",
                                                                     "Stadtgruppe",
                                                                     "DLRG",
                                                                     "Bundeswehr Gruppe",
                                                                     "Stadtverband",
                                                                     "og",
                                                                     "sp")
            .map(s -> s.toLowerCase(Locale.GERMANY) + " ")
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    private String fixGliederung(String gliederung) {
        if (gliederung == null || gliederung.isBlank()) {
            return "";
        }
        for (String level : ORGANIZATION_LEVEL) {
            if (gliederung.toLowerCase().startsWith(level)) {
                return gliederung.substring(level.length()).trim();
            }
        }
        return gliederung;
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registrationUpdate(InputStream name, AWettkampf<T> wk, Feedback fb, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> Hashtable<String, Teammember> teammembers(InputStream input, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {

        if (wk instanceof MannschaftWettkampf mwk) {

            Map<String, Integer> uuid2sn = new HashMap<>();
            for (T s : wk.getSchwimmer()) {
                if (s.getImportId() != null && !s.getImportId().isBlank()) {
                    uuid2sn.put(s.getImportId(), s.getStartnummer());
                }
            }

            Hashtable<String, Teammember> teammembers = new Hashtable<>();
            RegistrationExportModel model = mapper.readValue(input, RegistrationExportModel.class);
            return teammembers(model, mwk, uuid2sn, fb);
        }
        return new Hashtable<>();
    }

    private Hashtable<String, Teammember> teammembers(RegistrationExportModel model, MannschaftWettkampf mwk, Map<String, Integer> uuid2sn, Feedback fb) {
        Hashtable<String, Teammember> teammembers = new Hashtable<>();
        for (Registration registration : model.getRegistrations()) {
            Map<String, RegistrationExportModel.Athlete> athletesById = registration.getAthletes().stream().collect(Collectors.toMap(RegistrationExportModel.Athlete::getId,
                                                                                                                                     a -> a));

            for (RegistrationExportModel.Team team : registration.getTeams()) {
                Integer sn = uuid2sn.get(team.getId());
                if (sn == null) {
                    if (findGender(mwk, team.getGender()) != null) {
                        fb.showFeedback("Keine Mannschaft mit ImportId '%s' (%s %s %s) gefunden.".formatted(team.getId(),
                                                                                                            team.getName(),
                                                                                                            team.getAgeGroup(),
                                                                                                            team.getGender().toString().toLowerCase(
                                                                                                                    Locale.ROOT)));
                    }
                    continue;
                }

                for (int x = 0; x < team.getMemberIds().size(); x++) {
                    String memberId = team.getMemberIds().get(x);
                    RegistrationExportModel.Athlete athlete = athletesById.get(memberId);
                    if (athlete != null) {
                        teammembers.put(sn + StringTools.asText(x).toLowerCase(Locale.ROOT), toTeammember(x, athlete, mwk));
                    }
                }
            }
        }
        return teammembers;
    }

    private Teammember toTeammember(int x, RegistrationExportModel.Athlete athlete, MannschaftWettkampf mwk) {
        int yearOfBirth = athlete.getYearOfBirth() == null ? 0 : athlete.getYearOfBirth();
        return new Teammember(x, athlete.getFirstName(), athlete.getLastName(), findGeschlecht(mwk, athlete.getGender()), yearOfBirth);
    }

    @Override
    public <T extends ASchwimmer> Hashtable<ZWStartnummer, Double> zusatzwertungResults(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heats(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> zusatzwertung(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> results(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heattimes(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> KampfrichterVerwaltung referees(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> List<TeamWithStarters> starters(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        return switch (type) {
            case REGISTRATION, TEAM_MEMBERS, STARTERS -> true;
            default -> false;
        };
    }

    @Override
    public String getName() {
        return "Wettkampfportal";
    }

    @Override
    public String[] getSuffixes() {
        return new String[]{".json"};
    }

}
