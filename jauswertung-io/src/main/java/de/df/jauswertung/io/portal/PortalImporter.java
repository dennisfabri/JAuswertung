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
import de.df.jutils.util.Feedback;
import org.apache.commons.lang3.NotImplementedException;
import org.lisasp.competition.base.api.type.Gender;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class PortalImporter implements IImporter {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream input, AWettkampf<T> wk, Feedback fb, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        LinkedList<T> data = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
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
                for (int x = 0; x < team.getMemberIds().size(); x++) {
                    importMannschaftsmitglied(m.getMannschaftsmitglied(x), registration, team.getMemberIds().get(x));
                }
                teams.add(m);
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
                teilnehmerListe.add(t);
            }
        }
        fb.showFeedback("Import abgeschlossen");
        return teilnehmerListe;
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
    public <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
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
            case REGISTRATION -> true;
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
