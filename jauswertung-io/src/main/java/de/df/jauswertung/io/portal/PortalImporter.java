package de.df.jauswertung.io.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.df.jauswertung.daten.*;
import org.apache.commons.lang3.NotImplementedException;
import org.lisasp.competition.base.api.type.Gender;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.io.*;
import de.df.jauswertung.io.portal.RegistrationExportModel.Discipline;
import de.df.jauswertung.io.portal.RegistrationExportModel.Participant;
import de.df.jauswertung.io.portal.RegistrationExportModel.Registration;
import de.df.jauswertung.io.portal.RegistrationExportModel.Team;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.Feedback;

public class PortalImporter implements IImporter {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream input, AWettkampf<T> wk, Feedback fb,
            LinkedList<T> data, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        RegistrationExportModel model = mapper.readValue(input, RegistrationExportModel.class);
        if (wk instanceof MannschaftWettkampf mwk) {
            return (LinkedList<T>) registrationTeam(model, mwk, fb, (LinkedList<Mannschaft>) data, filename);
        }
        if (wk instanceof EinzelWettkampf ewk) {
            return (LinkedList<T>) registrationIndividual(model, ewk, fb, (LinkedList<Teilnehmer>) data, filename);
        }
        return data != null ? data : new LinkedList<>();
    }

    private LinkedList<Mannschaft> registrationTeam(RegistrationExportModel model, MannschaftWettkampf wk, Feedback fb,
            LinkedList<Mannschaft> data, String filename) {
        LinkedList<Mannschaft> teams = new LinkedList<>();
        for (Registration registration : model.getRegistrations()) {
            registration.getTeams().forEach(team -> {
                Mannschaft m = wk.createMannschaft(team.getName(), team.getGender() == Gender.Male,
                        registration.getOrganization(),
                        findAK(wk, team.getAgeGroup()), team.getComment());
                importRegistrationEntry(team, m);
                for (int x = 0; x < team.getMemberIds().size(); x++) {
                    importMannschaftsmitglied(m.getMannschaftsmitglied(x), registration, team.getMemberIds().get(x));
                }
                teams.add(m);
            });
        }
        return teams;
    }

    private void importRegistrationEntry(Participant team, ASchwimmer as) {
        as.setImportId(team.getId());
        as.setMeldepunkte(0, team.getPoints());
        for (int x = 0; x < as.getAK().getDiszAnzahl(); x++) {
            as.setMeldezeit(x, 0);
            as.setDisciplineChoice(x, false);
        }
        for (Discipline discipline : team.getDisciplines().stream().filter(d -> d.getTimeInMilliseconds() != null)
                .toList()) {
            for (int x = 0; x < as.getAK().getDiszAnzahl(); x++) {
                Disziplin d = as.getAK().getDisziplin(x, as.isMaennlich());
                if (d.getName().equalsIgnoreCase(discipline.getName())) {
                    as.setDisciplineChoice(x, true);
                    as.setMeldezeit(x, discipline.getTimeInMilliseconds() / 10);
                }
            }
        }
    }

    private void importMannschaftsmitglied(Mannschaftsmitglied mitglied,
            RegistrationExportModel.Registration registration, String id) {
        Optional<RegistrationExportModel.Athlete> maybeAthlete = registration.getAthletes().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
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
            mitglied.setJahrgang(athlete.getYearOfBirth());
            mitglied.setImportId(id);
        }
    }

    private <T extends ASchwimmer> int findAK(AWettkampf<T> wk, String name) {
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

    private LinkedList<Teilnehmer> registrationIndividual(RegistrationExportModel model,
            EinzelWettkampf wk, Feedback fb, LinkedList<Teilnehmer> data, String filename) {
        LinkedList<Teilnehmer> teilnehmerListe = new LinkedList<>();
        for (Registration registration : model.getRegistrations()) {
            registration.getAthletes().forEach(athlete -> {
                Teilnehmer t = wk.createTeilnehmer(athlete.getAthleteId(), athlete.getLastName(),
                        athlete.getFirstName(),
                        athlete.getYearOfBirth() == null ? 0 : athlete.getYearOfBirth(),
                        athlete.getGender() == Gender.Male,
                        registration.getOrganization(),
                        findAK(wk, athlete.getAgeGroup()), athlete.getComment());
                importRegistrationEntry(athlete, t);
                teilnehmerListe.add(t);
            });
        }
        return teilnehmerListe;
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registrationUpdate(InputStream name, AWettkampf<T> wk, Feedback fb,
            LinkedList<T> data, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream name, AWettkampf<T> wk,
            Feedback fb) throws TableFormatException, TableEntryException, TableException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends ASchwimmer> Hashtable<ZWStartnummer, Double> zusatzwertungResults(InputStream name,
            AWettkampf<T> wk, Feedback fb)
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
        if (!Utils.isInDevelopmentMode()) {
            return false;
        }
        switch (type) {
        case REGISTRATION:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String getName() {
        return "Meldeportal";
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { ".json" };
    }

}
