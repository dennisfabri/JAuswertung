package de.df.jauswertung.io.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.lisasp.competition.base.api.type.Gender;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.IImporter;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.TableEntryException;
import de.df.jauswertung.io.TableException;
import de.df.jauswertung.io.TableFormatException;
import de.df.jauswertung.io.portal.RegistrationExportModel.Discipline;
import de.df.jauswertung.io.portal.RegistrationExportModel.Participant;
import de.df.jauswertung.io.portal.RegistrationExportModel.Registration;
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
                int akNummer = findAkNummer(wk, team.getAgeGroup());
                if (akNummer < 0) {
                    fb.showFeedback(
                            "Altersklasse " + team.getAgeGroup() + " für " + team.getName() + " (" + team.getGender()
                                    + ") nicht gefunden");
                    return;
                }
                Boolean isMale = findGender(wk, team.getGender());
                if (isMale == null) {
                    fb.showFeedback(
                            "Geschlecht " + team.getGender() + " für " + team.getName() + " (" + team.getAgeGroup()
                                    + ") nicht gefunden");
                    return;                    
                }
                Mannschaft m = wk.createMannschaft(team.getName(), isMale,
                        registration.getOrganization(),
                        akNummer, team.getComment());
                importRegistrationEntry(team, m);
                for (int x = 0; x < team.getMemberIds().size(); x++) {
                    importMannschaftsmitglied(m.getMannschaftsmitglied(x), registration, team.getMemberIds().get(x));
                }
                teams.add(m);
            });
        }
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

    private void importRegistrationEntry(Participant team, ASchwimmer as) {
        as.setImportId(team.getId());
        as.setMeldepunkte(0, team.getPoints() == null ? 0 : team.getPoints());
        for (int x = 0; x < as.getAK().getDiszAnzahl(); x++) {
            as.setMeldezeit(x, 0);
            as.setDisciplineChoice(x, false);
        }
        for (Discipline discipline : team.getDisciplines().stream().filter(d -> d.isSelected())
                .toList()) {
            for (int x = 0; x < as.getAK().getDiszAnzahl(); x++) {
                Disziplin d = as.getAK().getDisziplin(x, as.isMaennlich());
                int timeInHundreds = discipline.getTimeInMilliseconds() == null ? 0
                        : discipline.getTimeInMilliseconds() / 10;
                if (d.getName().equalsIgnoreCase(discipline.getName())) {
                    as.setDisciplineChoice(x, true);
                    as.setMeldezeit(x, timeInHundreds);
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

    private LinkedList<Teilnehmer> registrationIndividual(RegistrationExportModel model,
            EinzelWettkampf wk, Feedback fb, LinkedList<Teilnehmer> data, String filename) {
        LinkedList<Teilnehmer> teilnehmerListe = new LinkedList<>();
        for (Registration registration : model.getRegistrations()) {
            for (RegistrationExportModel.Athlete athlete : registration.getAthletes()) {
                int akNummer = findAkNummer(wk, athlete.getAgeGroup());
                if (akNummer < 0) {
                    fb.showFeedback("Altersklasse " + athlete.getAgeGroup() + " für " + athlete.getFirstName() + " "
                            + athlete.getLastName() + " " + athlete.getGender()
                            + " nicht gefunden");
                    continue;
                }
                Boolean isMale = findGender(wk, athlete.getGender());
                if (isMale == null) {
                    fb.showFeedback(
                            "Geschlecht " + athlete.getGender() + " für " + athlete.getFirstName() + " "
                                    + athlete.getLastName() + " (" + athlete.getAgeGroup()
                                    + ") nicht gefunden");
                    continue;                    
                }
                Teilnehmer t = wk.createTeilnehmer(athlete.getAthleteId(), athlete.getLastName(),
                        athlete.getFirstName(),
                        athlete.getYearOfBirth() == null ? 0 : athlete.getYearOfBirth(),
                        athlete.getGender() == Gender.Male,
                        registration.getOrganization(),
                        akNummer, athlete.getComment());
                importRegistrationEntry(athlete, t);
                teilnehmerListe.add(t);
            }
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
