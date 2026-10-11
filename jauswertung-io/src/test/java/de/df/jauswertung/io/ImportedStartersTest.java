package de.df.jauswertung.io;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.io.model.StartersImportDto;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.data.HeatsUtils;

class ImportedStartersTest {

    private static final int AK = 0;
    private static final boolean MALE = false;
    private static final int DISCIPLINE = 0;

    @Test
    void roundOneUsesStartersChangedAfterTheImport() {
        MannschaftWettkampf wk = (MannschaftWettkampf) InputManager
                .ladeWettkampf("src/test/resources/compatibility/2026/Finale-Mannschaft.wk");
        OWSelection vorlauf = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, DISCIPLINE, 0);
        OWSelection finale = new OWSelection(wk.getRegelwerk().getAk(AK), AK, MALE, DISCIPLINE, 1);
        Mannschaft team = wk.getLauflisteOW().getDisziplin(finale).getLaeufe().getFirst().getAllSchwimmer().getFirst();
        assertNotNull(team.getEingabe(vorlauf.getId(), false));
        int members = team.getStarter(DISCIPLINE).length;
        String discipline = wk.getRegelwerk().getAk(AK).getDisziplin(DISCIPLINE, MALE).getName();

        int[] imported = starters(members, 1);
        Object result = ImportManager.finishImport(ImportExportTypes.STARTERS, wk,
                new StartersImportDto(List.of(new TeamWithStarters(team.getStartnummer(), discipline, 0, imported))),
                null);
        assertEquals(true, result);
        assertArrayEquals(imported, team.getStarter(DISCIPLINE));

        // Changed for round 0 in the UI, which edits the starters of the discipline
        int[] changed = starters(members, 2);
        team.setStarter(DISCIPLINE, changed);

        createHeats(wk, finale);

        assertArrayEquals(changed, team.getStarter(OWDisziplin.getId(AK, MALE, DISCIPLINE, 1)));
    }

    private static int[] starters(int members, int first) {
        int[] starters = new int[members];
        for (int x = 0; x < members; x++) {
            starters[x] = (first + x - 1) % members + 1;
        }
        return starters;
    }

    private static void createHeats(MannschaftWettkampf wk, OWSelection selection) {
        MannschaftWettkampf wkx = (MannschaftWettkampf) ResultUtils.createCompetitionFor(wk, selection);
        wkx.setProperty(HEATS_LANES, 8);
        wkx.setProperty(HEATS_SORTING_ORDER, Reihenfolge.Regelwerk.getValue());
        wkx.setProperty(HEATS_EMPTY_LIST, false);
        wkx.getLaufliste().erzeugen();
        HeatsUtils.save(wk, selection, wkx);
    }
}
