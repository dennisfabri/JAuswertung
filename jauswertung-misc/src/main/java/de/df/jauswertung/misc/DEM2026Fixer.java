package de.df.jauswertung.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.io.portal.RegistrationExportModel;
import org.lisasp.competition.base.api.type.Gender;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DEM2026Fixer {

    private final List<TeamDetail> teams = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        DEM2026Fixer fixer = new DEM2026Fixer();
        fixer.importPortal(Path.of("DEM2026", "fix", "DEM2026.json"));
        fixer.fix(Path.of("DEM2026", "fix", "dem206_Mannschaft_Final_Neu.wk"), Path.of("DEM2026", "fix", "dem206_Mannschaft_Final_Neu_2.wk"));
    }

    private void fix(Path filename, Path outputFilename) {
        MannschaftWettkampf mwk = (MannschaftWettkampf) InputManager.ladeWettkampf(filename.toString());
        if (mwk == null) {
            throw new IllegalArgumentException("Keine Mannschaftswettkampfdatei: " + filename);
        }
        mwk.getSchwimmer().stream().sorted(Comparator.comparing(ASchwimmer::getGliederungMitQGliederung)).forEach(mannschaft -> {
            teams.stream().filter(t -> matching(mannschaft, t)).findFirst().ifPresentOrElse(t -> updateMannschaft(mannschaft, t), () -> report(mannschaft));
        });
        OutputManager.speichereWettkampf(outputFilename.toString(), mwk);
    }

    private static void report(Mannschaft mannschaft) {
        System.out.println("Keine Übereinstimmung für " + mannschaft.getName() + " (" + mannschaft.getAK().getName() + ", " + (mannschaft.isMaennlich() ? "männlich" : "weiblich") + ")");
    }

    private static void updateMannschaft(Mannschaft mannschaft, TeamDetail t) {
        //System.out.println("Aktualiere " + mannschaft.getName() + " (" + mannschaft.getAK().getName() + ", " + (mannschaft.isMaennlich() ? "männlich" : "weiblich") + ") mit UUID " + t.uuid);
        mannschaft.setImportId(t.uuid());
    }

    private boolean matching(Mannschaft mannschaft, TeamDetail t) {
        if (!mannschaft.getName().equalsIgnoreCase(t.name())) {
            return false;
        }
        if (mannschaft.isMaennlich() != (t.gender() == Gender.Male)) {
            return false;
        }
        if (!mannschaft.getAK().getName().equalsIgnoreCase(t.agegroup())) {
            return false;
        }
        return true;
    }

    private void importPortal(Path portalJson) throws IOException {
        teams.clear();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        RegistrationExportModel model = mapper.readValue(portalJson.toFile(), RegistrationExportModel.class);
        for (RegistrationExportModel.Registration registration : model.getRegistrations()) {
            for (RegistrationExportModel.Team team : registration.getTeams()) {
                TeamDetail detail = new TeamDetail(registration.getOrganization(), team.getName(), team.getAgeGroup(), team.getGender(), team.getId());
                teams.add(detail);
            }
        }
    }

    private record TeamDetail(String organization, String name, String agegroup, Gender gender, String uuid) {

    }
}
